package com.gb.app.VCS.exception;

import com.gb.app.VCS.models.FileInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@RestController
@ControllerAdvice
public class FileExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(FileStorageException.class)
    public final ResponseEntity<Object> handleFileStorageException(FileStorageException ex, WebRequest request) {
        CustomErrorMessage error = new CustomErrorMessage();
        error.setTimeStamp(Instant.now().toString().replaceAll("Z", "+00.00"));
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        error.setMessage(ex.getMsg());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public final ResponseEntity<Object> handleFileNotFoundException(FileNotFoundException ex, WebRequest request) {
        CustomErrorMessage error = new CustomErrorMessage();
        error.setTimeStamp(Instant.now().toString().replaceAll("Z", "+00.00"));
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
        error.setMessage(ex.getMsg());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
