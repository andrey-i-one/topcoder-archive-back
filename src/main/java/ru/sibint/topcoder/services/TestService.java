package ru.sibint.topcoder.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sibint.topcoder.generated.dto.TestDto;
import ru.sibint.topcoder.model.Problem;
import ru.sibint.topcoder.model.Test;
import ru.sibint.topcoder.repos.ProblemRepository;
import ru.sibint.topcoder.repos.TestRepository;
import ru.sibint.topcoder.utils.ExamplesParser;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TestService {

    private final TestRepository testRepository;
    private final ProblemRepository problemRepository;
    private final ExamplesParser examplesParser;

    public void fillTests() {
        List<Problem> problems = problemRepository.findAll();
        int count = 0;
        int failedCount = 0;
        for(Problem problem: problems) {
            if(count % 100 == 0) {
                log.info("{}% completed", String.valueOf(count / (problems.size() + 0.0) * 100.0));
            }
            count++;
            int testsCountInDb = problem.getTests() == null ? 0 : problem.getTests().size();
            try {
                List<TestDto> tests = examplesParser.parseExamples("<root>" + problem.getExamples() + "</root>");
                if(tests.size() == testsCountInDb) {
                    continue;
                }
                List<Test> testsToSave = new ArrayList<>();
                for(int i = 0; i < tests.size(); i++) {
                    Test test = Test.builder()
                            .number(i + 1)
                            .expectedOutput(tests.get(i).getExpectedOutput())
                            .input(tests.get(i).getInput())
                            .problem(problem)
                            .build();
                    testsToSave.add(test);
                }
                testRepository.saveAll(testsToSave);
            } catch (Exception e) {
                failedCount++;
            }
        }
        log.info("Failed count: {}", failedCount);
    }

}
