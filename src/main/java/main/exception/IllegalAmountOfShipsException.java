package main.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "non-positive amount of ships")
public class IllegalAmountOfShipsException extends RuntimeException {
}
