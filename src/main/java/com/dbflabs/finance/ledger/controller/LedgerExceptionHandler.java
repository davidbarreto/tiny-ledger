package com.dbflabs.finance.ledger.controller;

import com.dbflabs.finance.ledger.exception.AccountNotFoundException;
import com.dbflabs.finance.ledger.exception.InsufficientFundsException;
import com.dbflabs.finance.ledger.exception.InvalidAmountException;
import com.dbflabs.finance.ledger.model.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class LedgerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler({
            InvalidAmountException.class,
            InsufficientFundsException.class
    })
    ResponseEntity<Object> handleBadRequest(RuntimeException ex, WebRequest request) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<Object> handleInternalServerError(RuntimeException ex, WebRequest request) {
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
    }

    private ResponseEntity<Object> buildResponseEntity(HttpStatus statusCode, RuntimeException ex, WebRequest request) {
        return super.handleExceptionInternal(
                ex, new ErrorResponse(ex.getMessage()), new HttpHeaders(), statusCode, request);
    }
}
