package io.burpabet.betting.web.api;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.burpabet.betting.service.NoSuchBetException;
import io.burpabet.betting.service.NoSuchRaceException;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    @Nullable
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        logger.error("Internal server error", ex);
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    @ExceptionHandler({Exception.class})
    public ProblemDetail handleAny(Throwable ex) {
        if (ex instanceof UndeclaredThrowableException) {
            ex = ((UndeclaredThrowableException) ex).getUndeclaredThrowable();
        }

        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            if (responseStatus.code().is5xxServerError()) {
                logger.error("Exception processing request", ex);
            }
            return ProblemDetail.forStatusAndDetail(responseStatus.value(), Objects.toString(ex));
        } else {
            logger.error("Exception processing request", ex);
            return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, Objects.toString(ex));
        }
    }

    @ExceptionHandler(NoSuchRaceException.class)
    public ProblemDetail handleNoSuchRaceException(NoSuchRaceException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                ex.getMessage());
        problemDetail.setTitle("Race Not Found");
        return problemDetail;
    }

    @ExceptionHandler(NoSuchBetException.class)
    public ProblemDetail handleNoSuchBetException(NoSuchBetException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                ex.getMessage());
        problemDetail.setTitle("Bet Not Found");
        return problemDetail;
    }

}
