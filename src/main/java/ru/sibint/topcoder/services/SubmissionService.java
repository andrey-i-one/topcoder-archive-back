package ru.sibint.topcoder.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sibint.topcoder.exceptions.UnprocessableEntityException;
import ru.sibint.topcoder.generated.dto.SubmissionRequestDto;
import ru.sibint.topcoder.generated.dto.SubmissionResponseDto;
import ru.sibint.topcoder.generated.dto.TestDto;
import ru.sibint.topcoder.generated.dto.TestResultDto;
import ru.sibint.topcoder.model.Problem;
import ru.sibint.topcoder.model.Submission;
import ru.sibint.topcoder.repos.ProblemRepository;
import ru.sibint.topcoder.repos.SubmissionRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubmissionService {

    private final String JAVA_CLASS_REGEX = "[^{}]*public\\s+(final)?\\s*class\\s+(\\w+).*";

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;

    @Value("${tempDir}")
    String tempDir;

    @Value("${compileTimeout}")
    Long compileTimeout;

    public SubmissionResponseDto createSubmission(SubmissionRequestDto submissionRequestDto) throws Exception {
        Problem problem = problemRepository.findById(submissionRequestDto.getTaskId()).orElseThrow(() -> new UnprocessableEntityException("No problem for given id"));
        Submission submission = Submission.builder()
                .sources(submissionRequestDto.getSources())
                .author(submissionRequestDto.getAuthor())
                .submitTime(LocalDateTime.now())
                .problem(problem)
                .build();
        submissionRepository.save(submission);
        Pattern pattern = Pattern.compile(JAVA_CLASS_REGEX);
        Matcher matcher = pattern.matcher(submissionRequestDto.getSources());
        String className = null;
        if(matcher.find()) {
            className = matcher.group(2);
        }
        String result = compile(tempDir + submission.getId().toString(), submissionRequestDto.getSources(), className);
        if(!result.isEmpty()) {
            return SubmissionResponseDto.builder()
                    .id(submission.getId())
                    .comment(result)
                    .overallVerdict("Complication error")
                    .build();
        }
        List<TestResultDto> testResults = new ArrayList<>();
        String overallStatus = "Accepted";
        for(TestDto test: submissionRequestDto.getTests()) {
            TestResultDto testResult = runTest(tempDir + submission.getId().toString(), className, test);
            testResults.add(testResult);
            if(!"Accepted".equals(testResult.getVerdict())) {
                break;
            }
        }
        return SubmissionResponseDto.builder()
                .id(submission.getId())
                .overallVerdict(overallStatus)
                .testsResults(testResults)
                .build();
    }

    private String compile(String dir, String sources, String className) throws Exception {
        Files.createDirectories(Path.of(dir));
        File workingDir = new File(dir);
        saveToFile(dir + "/" + className + ".java", sources);
        ProcessBuilder compileProcessBuilder = new ProcessBuilder("javac", className + ".java");
        compileProcessBuilder.directory(workingDir);
        compileProcessBuilder.redirectErrorStream(true);
        Process compileProcess = compileProcessBuilder.start();
        try {
            compileProcess.waitFor(compileTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return "Compilation failed";
        }
        return readInputStream(compileProcess.getErrorStream());
    }

    private TestResultDto runTest(String dir, String className, TestDto test) throws Exception {
        File workingDir = new File(dir);
        saveToFile(dir + "/run_test.sh", readInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("run_test.sh")));
        saveToFile(dir + "/input.txt", test.getInput());

        ProcessBuilder runProcessBuilder = new ProcessBuilder("sh", "./run_test.sh", className);
        runProcessBuilder.directory(workingDir);
        Process runProcess = runProcessBuilder.start();
        String verdict = "Accepted";
        try {
            runProcess.waitFor(compileTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            verdict = "Time limit exceeded";
        }
        String actualOutput = readOutputFromFile(dir + "/output.txt");
        if(!isEqualOutput(actualOutput, test.getExpectedOutput())) {
            verdict = "Wrong answer";
        }
        String output = readOutputFromFile(dir + "/metadata.txt");
        System.out.println(output);
        String[] outputLines = output.split("\n");
        String time = null;
        String memory = null;
        for(String outputLine: outputLines) {
            if(outputLine.trim().startsWith("System time (seconds): ")) {
                time = outputLine.trim().substring("System time (seconds): ".length());
            }
            if(outputLine.trim().startsWith("Maximum resident set size (kbytes): ")) {
                memory = outputLine.trim().substring("Maximum resident set size (kbytes): ".length());
            }
        }
        return TestResultDto.builder()
                .verdict(verdict)
                .output(actualOutput)
                .expectedOutput(test.getExpectedOutput())
                .time(time)
                .memory(memory)
                .build();
    }

    private void saveToFile(String fileName, String content) throws Exception {
        PrintWriter printWriter = new PrintWriter(fileName);
        printWriter.print(content);
        printWriter.flush();
        printWriter.close();
    }

    private String readInputStream(InputStream is) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuilder lines = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            lines.append(line).append("\n");
        }
        return lines.toString().trim();
    }

    private String readOutputFromFile(String fileName) throws Exception {
        Scanner scanner = new Scanner(new File(fileName));
        StringBuilder stringBuilder = new StringBuilder();
        while(scanner.hasNextLine()) {
            stringBuilder.append(scanner.nextLine());
            if(scanner.hasNextLine()) {
                stringBuilder.append("\n");
            }
        }
        scanner.close();
        return stringBuilder.toString();
    }

    private boolean isEqualOutput(String actual, String expected) {
        return actual.trim().equals(expected.trim());
    }
}
