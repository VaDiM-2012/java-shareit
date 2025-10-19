package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

/**
 * Глобальный обработчик исключений для приложения ShareIt.
 * Перехватывает бизнес-исключения (404, 409, 403), ошибки валидации Spring (400) и непредвиденные ошибки (500).
 * Для каждого исключения возвращает структурированный JSON-ответ с описанием ошибки и сообщением.
 */
@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    /**
     * Создает объект ответа с ошибкой для возврата клиенту.
     *
     * @param error Тип ошибки.
     * @param e     Перехваченное исключение.
     * @return Объект {@link ErrorResponse} с описанием ошибки и сообщением исключения.
     */
    private ErrorResponse createErrorResponse(String error, Throwable e) {
        return new ErrorResponse(error, e.getMessage());
    }

    // --- Код ответа 400 Bad Request ---

    /**
     * Обрабатывает исключения валидации DTO, помеченных аннотацией @Valid.
     * Перехватывает {@link MethodArgumentNotValidException}, возникающее при нарушении
     * аннотаций валидации (например, @NotBlank, @NotNull) в объектах DTO.
     *
     * @param e Перехваченное исключение {@link MethodArgumentNotValidException}.
     * @return Объект {@link ErrorResponse} с кодом ошибки 400 и сообщением о нарушении валидации.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.error("Ошибка валидации DTO: {}", e.getMessage(), e);
        String defaultMessage = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return new ErrorResponse("Ошибка валидации DTO", defaultMessage);
    }

    /**
     * Обрабатывает исключения, связанные с отсутствием обязательного заголовка запроса
     * (например, X-Sharer-User-Id).
     * Перехватывает {@link MissingRequestHeaderException}.
     *
     * @param e Перехваченное исключение {@link MissingRequestHeaderException}.
     * @return Объект {@link ErrorResponse} с кодом ошибки 400 и сообщением об отсутствующем заголовке.
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestHeaderException(final MissingRequestHeaderException e) {
        log.error("Отсутствует заголовок: {}", e.getMessage(), e);
        return new ErrorResponse("Отсутствует обязательный заголовок", "Заголовок " + e.getHeaderName() + " обязателен.");
    }

    /**
     * Обрабатывает исключения, связанные с недоступностью вещи для бронирования.
     * Перехватывает {@link ItemNotAvailableException}.
     *
     * @param e Перехваченное исключение {@link ItemNotAvailableException}.
     * @return Объект {@link ErrorResponse} с кодом ошибки 400 и сообщением о недоступности вещи.
     */
    @ExceptionHandler(ItemNotAvailableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleItemNotAvailableException(final ItemNotAvailableException e) {
        log.error("Вещь недоступна: {}", e.getMessage(), e);
        return new ErrorResponse("Вещь недоступна", e.getMessage());
    }

    /**
     * Обрабатывает исключения, связанные с невозможностью оставить комментарий к вещи
     * из-за отсутствия бронирования.
     * Перехватывает {@link BookingNotFoundException}.
     *
     * @param e Перехваченное исключение {@link BookingNotFoundException}.
     * @return Объект {@link ErrorResponse} с кодом ошибки 400 и сообщением о невозможности комментирования.
     */
    @ExceptionHandler(BookingNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBookingNotFoundException(final BookingNotFoundException e) {
        log.error("Ошибка бронирования: {}", e.getMessage(), e);
        return new ErrorResponse("Ошибка комментирования", e.getMessage());
    }

    // --- Код ответа 403 Forbidden ---

    /**
     * Обрабатывает исключения, связанные с нарушением прав доступа владельца вещи.
     * Перехватывает {@link OwnerMismatchException}.
     *
     * @param e Перехваченное исключение {@link OwnerMismatchException}.
     * @return Объект {@link ErrorResponse} с кодом ошибки 403 и сообщением о нарушении доступа.
     */
    @ExceptionHandler(OwnerMismatchException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleOwnerMismatchException(final OwnerMismatchException e) {
        log.error("Нарушение доступа: {}", e.getMessage(), e);
        return new ErrorResponse("Нарушение доступа", e.getMessage());
    }

    /**
     * Обрабатывает исключения, связанные с некорректным идентификатором пользователя.
     * Перехватывает {@link InvalidUserIdException}.
     *
     * @param e Перехваченное исключение {@link InvalidUserIdException}.
     * @return Объект {@link ErrorResponse} с кодом ошибки 403 и сообщением о неверном идентификаторе.
     */
    @ExceptionHandler(InvalidUserIdException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleInvalidUserIdException(final InvalidUserIdException e) {
        log.error("Неверный ID пользователя: {}", e.getMessage(), e);
        return new ErrorResponse("Неверный ID пользователя", e.getMessage());
    }

    // --- Код ответа 404 Not Found ---

    /**
     * Обрабатывает исключения, связанные с отсутствием объекта в базе данных.
     * Перехватывает {@link NotFoundException}.
     *
     * @param e Перехваченное исключение {@link NotFoundException}.
     * @return Объект {@link ErrorResponse} с кодом ошибки 404 и сообщением о ненайденном объекте.
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        log.error("Объект не найден: {}", e.getMessage(), e);
        return createErrorResponse("Объект не найден", e);
    }

    // --- Код ответа 409 Conflict ---

    /**
     * Обрабатывает исключения, связанные с нарушением бизнес-правил.
     * Перехватывает {@link ValidationException}, например, при конфликте уникальности или нарушении прав доступа.
     *
     * @param e Перехваченное исключение {@link ValidationException}.
     * @return Объект {@link ErrorResponse} с кодом ошибки 409 и сообщением о нарушении бизнес-правил.
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleValidationException(final ValidationException e) {
        log.error("Нарушение бизнес-правил: {}", e.getMessage(), e);
        return createErrorResponse("Нарушение бизнес-правил", e);
    }

    /**
     * Обрабатывает исключения, связанные с дублированием email пользователя.
     * Перехватывает {@link InvalidUserEmailException}.
     *
     * @param e Перехваченное исключение {@link InvalidUserEmailException}.
     * @return Объект {@link ErrorResponse} с кодом ошибки 409 и сообщением о конфликте уникальности.
     */
    @ExceptionHandler(InvalidUserEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInvalidUserEmailException(final InvalidUserEmailException e) {
        log.error("Конфликт уникальности email: {}", e.getMessage(), e);
        return createErrorResponse("Конфликт уникальности", e);
    }

    // --- Код ответа 500 Internal Server Error ---

    /**
     * Обрабатывает все необработанные исключения, не перехваченные другими обработчиками.
     * Перехватывает {@link Throwable} для предотвращения необработанных ошибок сервера.
     *
     * @param e Перехваченное исключение {@link Throwable}.
     * @return Объект {@link ErrorResponse} с кодом ошибки 500 и сообщением о непредвиденной ошибке.
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        log.error("Непредвиденная ошибка сервера: {}", e.getMessage(), e);
        return createErrorResponse("Непредвиденная ошибка сервера", e);
    }
}