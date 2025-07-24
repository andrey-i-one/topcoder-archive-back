package ru.sibint.topcoder.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sibint.topcoder.enums.Language;
import ru.sibint.topcoder.enums.Verdict;
import ru.sibint.topcoder.exceptions.UnprocessableEntityException;
import ru.sibint.topcoder.generated.dto.SubmissionRequestDto;
import ru.sibint.topcoder.generated.dto.SubmissionResponseDto;
import ru.sibint.topcoder.generated.dto.TestDto;
import ru.sibint.topcoder.generated.dto.TestResultDto;
import ru.sibint.topcoder.model.Problem;
import ru.sibint.topcoder.model.Submission;
import ru.sibint.topcoder.repos.ProblemRepository;
import ru.sibint.topcoder.repos.SubmissionRepository;
import ru.sibint.topcoder.utils.IOUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubmissionService {

    private final String JAVA_CLASS_REGEX = "[^{}]*public\\s+(final)?\\s*class\\s+(\\w+).*";
    private final String CPP_FILE_NAME = "main";
    private final String COMPILE_SCRIPT_NAME = "compile.sh";
    private final String RUN_SCRIPT_NAME = "run_test.sh";
    private final String COMPILE_DATA_FILE = "compiledata.txt";
    private final String INPUT_FILE_NAME = "input.txt";
    private final String OUTPUT_FILE_NAME = "output.txt";
    private final String METADATA_FILE_NAME = "metadata.txt";
    private final String LINE_BREAK = "\n";
    private final String COMMAND = "sh";
    private final String SYSTEM_TIME_LINE_START = "System time (seconds): ";
    private final String MEMORY_LINE_START = "Maximum resident set size (kbytes): ";

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;

    @Value("${tempDir}")
    String tempDir;

    @Value("${compileTimeout}")
    Long compileTimeout;

    @Value("${memoryLimit}")
    Long memoryLimit;

    public SubmissionResponseDto createSubmission(SubmissionRequestDto submissionRequestDto) throws Exception {
        Language language = Language.fromValue(submissionRequestDto.getLanguage());
        if(language == null) {
            throw new UnprocessableEntityException("No language is not supported");
        }
        Problem problem = problemRepository.findById(submissionRequestDto.getTaskId()).orElseThrow(() -> new UnprocessableEntityException("No problem for given id"));
        Submission submission = Submission.builder()
                .sources(submissionRequestDto.getSources())
                .author(submissionRequestDto.getAuthor())
                .submitTime(LocalDateTime.now())
                .problem(problem)
                .build();
        submissionRepository.save(submission);
        String workingDir = tempDir + submission.getId().toString() + "/";
        String className = getClassName(language, submissionRequestDto.getSources());

        String compilationResult = compile(workingDir, submissionRequestDto.getSources(), className + language.getExtension(), language);
        if(!compilationResult.isEmpty() && language != Language.C_SHARP || language == Language.C_SHARP && compilationResult.contains("error")) {
            FileUtils.deleteDirectory(new File(workingDir));
            return SubmissionResponseDto.builder()
                    .id(submission.getId())
                    .comment(compilationResult)
                    .overallVerdict(Verdict.COMPILATION_ERROR.value())
                    .testsResults(List.of(TestResultDto.builder()
                                    .number(1)
                                    .verdict(Verdict.COMPILATION_ERROR.value())
                                    .output(compilationResult)
                            .build()))
                    .build();
        }
        List<TestResultDto> testResults = new ArrayList<>();
        Verdict overallStatus = Verdict.ACCEPTED;
        for(int i = 0; i < submissionRequestDto.getTests().size(); i++) {
            TestDto test = submissionRequestDto.getTests().get(i);
            TestResultDto testResult = runTest(i + 1, workingDir, className + language.getRunExtension(), language, test);
            testResults.add(testResult);
            if(testResult.getMemory() != null) {
                long currentMemoryConsumption = Integer.parseInt(testResult.getMemory());
                if(currentMemoryConsumption * 1024L > memoryLimit) {
                    testResult.setVerdict(Verdict.MEMORY_LIMIT_EXCEEDED.value());
                }
            }
            if(!Verdict.ACCEPTED.value().equals(testResult.getVerdict())) {
                overallStatus = Verdict.fromValue(testResult.getVerdict());
                break;
            }
        }
        FileUtils.deleteDirectory(new File(workingDir));
        return SubmissionResponseDto.builder()
                .id(submission.getId())
                .overallVerdict(overallStatus == null ? null :overallStatus.value())
                .testsResults(testResults)
                .build();
    }

    private String compile(String dir, String sources, String sourceFileName, Language language) throws Exception {
        Files.createDirectories(Path.of(dir));
        File workingDir = new File(dir);
        IOUtils.saveToFile(dir + COMPILE_SCRIPT_NAME, IOUtils.readInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("compile_" + language.getValue() + ".sh")));
        IOUtils.saveToFile(dir + sourceFileName, sources);
        ProcessBuilder compileProcessBuilder = new ProcessBuilder(COMMAND, COMPILE_SCRIPT_NAME, sourceFileName);
        compileProcessBuilder.directory(workingDir);
        compileProcessBuilder.redirectErrorStream(true);
        Process compileProcess = compileProcessBuilder.start();
        try {
            compileProcess.waitFor(compileTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return Verdict.COMPILATION_ERROR.value();
        }
        return IOUtils.readOutputFromFile(dir + COMPILE_DATA_FILE);
    }

    private TestResultDto runTest(int id, String dir, String compiledName, Language language, TestDto test) throws Exception {
        File workingDir = new File(dir);
        IOUtils.saveToFile(dir + RUN_SCRIPT_NAME, IOUtils.readInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("run_test_" + language.getValue() + ".sh")));
        IOUtils.saveToFile(dir + INPUT_FILE_NAME, test.getInput());

        ProcessBuilder runProcessBuilder = new ProcessBuilder(COMMAND, RUN_SCRIPT_NAME, compiledName);
        runProcessBuilder.directory(workingDir);
        Process runProcess = runProcessBuilder.start();
        Verdict verdict = Verdict.ACCEPTED;
        boolean finished;
        try {
            finished = runProcess.waitFor(compileTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            finished = false;
        }
        if(!finished) {
            runProcess.destroyForcibly();
            return TestResultDto.builder()
                    .number(id)
                    .verdict(Verdict.TIME_LIMIT_EXCEEDED.value())
                    .output(null)
                    .expectedOutput(test.getExpectedOutput())
                    .time(String.valueOf(compileTimeout / 1000.0))
                    .memory(null)
                    .build();
        }
        String actualOutput = IOUtils.readOutputFromFile(dir + OUTPUT_FILE_NAME);
        if(!IOUtils.isEqualOutput(actualOutput, test.getExpectedOutput())) {
            verdict = Verdict.WRONG_ANSWER;
        }
        String output = IOUtils.readOutputFromFile(dir + METADATA_FILE_NAME);
        String[] outputLines = output.split(LINE_BREAK);
        String time = null;
        String memory = null;
        for(String outputLine: outputLines) {
            if(outputLine.trim().startsWith(SYSTEM_TIME_LINE_START)) {
                time = outputLine.trim().substring(SYSTEM_TIME_LINE_START.length());
            }
            if(outputLine.trim().startsWith(MEMORY_LINE_START)) {
                memory = outputLine.trim().substring(MEMORY_LINE_START.length());
            }
        }
        return TestResultDto.builder()
                .number(id)
                .verdict(verdict.value())
                .output(actualOutput)
                .expectedOutput(test.getExpectedOutput())
                .time(time)
                .memory(memory)
                .build();
    }

    private String getClassName(Language language, String sources) {
        if(language == Language.CPP) return CPP_FILE_NAME;
        Pattern pattern = Pattern.compile(JAVA_CLASS_REGEX);
        Matcher matcher = pattern.matcher(sources);
        if(matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

}
