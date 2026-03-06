package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemRequestGatewayDto {
    @NotBlank(message = "Description cannot be blank")
    private String description;
}