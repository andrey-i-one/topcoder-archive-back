package ru.sibint.topcoder.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.sibint.topcoder.generated.api.ApiUtil;
import ru.sibint.topcoder.generated.api.ProblemsApiDelegate;
import ru.sibint.topcoder.generated.dto.ProblemDetailsDto;
import ru.sibint.topcoder.generated.dto.ProblemsPageDto;
import ru.sibint.topcoder.services.ProblemsService;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProblemsFacade implements ProblemsApiDelegate {

    private final ProblemsService problemsService;

    @Override
    public ResponseEntity<ProblemsPageDto> retrieveProblems(Integer page,
                                                            Integer perPage,
                                                            String sortField,
                                                            String sortOrder,
                                                            String tags,
                                                            String div1Level,
                                                            String div2Level) throws Exception {
        return ResponseEntity.ok(problemsService.retrieveProblems(page, perPage, sortField, sortOrder, tags, div1Level, div2Level));
    }

    public ResponseEntity<ProblemDetailsDto> retrieveProblemById(UUID id) throws Exception {
        return ResponseEntity.ok(problemsService.retrieveProblemById(id));
    }

}
