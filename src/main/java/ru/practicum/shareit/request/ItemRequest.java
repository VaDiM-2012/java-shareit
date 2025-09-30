package ru.practicum.shareit.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Заглушка модели ItemRequest, необходимая для компиляции Item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    private Long id;
}