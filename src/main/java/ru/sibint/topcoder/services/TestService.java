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
        for(Problem problem: problems) {
            if(count % 100 == 0) {
                log.info(String.valueOf(count / (problems.size() + 0.0) * 100.0));
            }
            count++;
            try {
                List<TestDto> tests = examplesParser.parseExamples("<root>" + problem.getExamples() + "</root>");
                for(int i = 0; i < tests.size(); i++) {
                    Test test = Test.builder()
                            .number(i + 1)
                            .expectedOutput(tests.get(i).getExpectedOutput())
                            .input(tests.get(i).getInput())
                            .problem(problem)
                            .build();
                    testRepository.save(test);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
