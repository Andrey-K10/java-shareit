package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingItemRequestDto {

    @NotNull(message = "start date cannot be null")
    private LocalDateTime start;

    @NotNull(message = "end date cannot be null")
    private LocalDateTime end;

    @Positive(message = "itemId must be positive")
    private Long itemId;
}