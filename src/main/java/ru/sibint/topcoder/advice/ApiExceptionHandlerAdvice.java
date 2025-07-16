package ru.sibint.topcoder.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.sibint.topcoder.exceptions.UnprocessableEntityException;
import ru.sibint.topcoder.generated.dto.Message;


@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ApiExceptionHandlerAdvice {

    private final ObjectMapper objectMapper;

    @ExceptionHandler(value = {UnprocessableEntityException.class})
    protected ResponseEntity<Message> handleExceptionNotFoundException(UnprocessableEntityException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Message.builder().message("operation prohibited").details(exception.getMessage()).build());
    }

}