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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
        File sourcesFile = new File(dir + "/" + className + ".java");
        PrintWriter printWriter = new PrintWriter(sourcesFile);
        printWriter.print(sources);
        printWriter.flush();
        printWriter.close();
        ProcessBuilder compileProcessBuilder = new ProcessBuilder("javac", className + ".java");
        compileProcessBuilder.directory(workingDir);
        compileProcessBuilder.redirectErrorStream(true);
        Process compileProcess = compileProcessBuilder.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
        StringBuilder lines = new StringBuilder();
        String line = "";
        while ((line = in.readLine()) != null) {
            lines.append(line).append("\n");
        }
        try {
            compileProcess.waitFor(compileTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return "Compilation failed";
        }
        return lines.toString().trim();
    }

    private TestResultDto runTest(String dir, String className, TestDto test) {
        return TestResultDto.builder()
                .build();
    }

}
