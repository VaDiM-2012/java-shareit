package ru.practicum.shareit.request;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между сущностью {@link ItemRequest} и ее DTO.
 */
public class ItemRequestMapper {

    private ItemRequestMapper() {
        // Утилитарный класс
    }

    /**
     * Преобразует сущность {@link ItemRequest} в DTO для ответа {@link ItemRequestResponseDto}.
     *
     * @param request Сущность запроса.
     * @param items Карта вещей, связанных с запросами (для оптимизации).
     * @return DTO запроса.
     */
    public static ItemRequestResponseDto toDto(ItemRequest request, Map<Long, List<ItemDto>> items) {
        Long requestId = request.getId();
        return new ItemRequestResponseDto(
                requestId,
                request.getDescription(),
                request.getCreated(),
                items.getOrDefault(requestId, Collections.emptyList())
        );
    }

    /**
     * Преобразует коллекцию сущностей {@link ItemRequest} в коллекцию DTO {@link ItemRequestResponseDto}.
     *
     * @param requests Список сущностей запросов.
     * @param items Карта вещей, связанных с запросами.
     * @return Список DTO запросов.
     */
    public static List<ItemRequestResponseDto> toDto(List<ItemRequest> requests, Map<Long, List<ItemDto>> items) {
        return requests.stream()
                .map(request -> toDto(request, items))
                .collect(Collectors.toList());
    }

    /**
     * Преобразует DTO создания {@link ItemRequestCreateDto} в сущность {@link ItemRequest}.
     * Requestor и Created устанавливаются в Service.
     *
     * @param dto DTO создания запроса.
     * @return Сущность запроса.
     */
    public static ItemRequest toEntity(ItemRequestCreateDto dto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(dto.description());
        return itemRequest;
    }
}