package ru.sibint.topcoder.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.sibint.topcoder.generated.api.ApiUtil;
import ru.sibint.topcoder.generated.api.ProblemsApiDelegate;
import ru.sibint.topcoder.generated.api.SubmissionApi;
import ru.sibint.topcoder.generated.api.SubmissionApiDelegate;
import ru.sibint.topcoder.generated.dto.ProblemDetailsDto;
import ru.sibint.topcoder.generated.dto.ProblemsPageDto;
import ru.sibint.topcoder.generated.dto.SubmissionRequestDto;
import ru.sibint.topcoder.generated.dto.SubmissionResponseDto;
import ru.sibint.topcoder.services.ProblemsService;
import ru.sibint.topcoder.services.SubmissionService;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmissionFacade implements SubmissionApiDelegate {

    private final SubmissionService submissionService;

    @Override
    public ResponseEntity<SubmissionResponseDto> createSubmission(SubmissionRequestDto submissionRequestDto, String authorization) throws Exception {
        return ResponseEntity.ok(submissionService.createSubmission(submissionRequestDto));

    }

    @Override
    public ResponseEntity<SubmissionResponseDto> retrieveSubmission(Integer id) throws Exception {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
