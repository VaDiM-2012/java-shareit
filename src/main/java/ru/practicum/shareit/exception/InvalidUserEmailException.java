package ru.practicum.shareit.exception;

public class InvalidUserEmailException extends ValidationException {
    public InvalidUserEmailException(String message) {
        super(message);
    }
}