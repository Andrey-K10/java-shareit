package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentGatewayDto {
    @NotBlank(message = "Comment text cannot be blank")
    private String text;
}