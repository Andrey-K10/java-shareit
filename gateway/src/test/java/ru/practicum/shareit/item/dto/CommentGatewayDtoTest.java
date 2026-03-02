package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentGatewayDtoTest {

    @Autowired
    private JacksonTester<CommentGatewayDto> json;

    @Test
    void testSerialize() throws Exception {
        CommentGatewayDto dto = new CommentGatewayDto();
        dto.setText("Great item!");

        JsonContent<CommentGatewayDto> result = json.write(dto);

        assertThat(result).hasJsonPathStringValue("$.text");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"text\":\"Great item!\"}";

        CommentGatewayDto dto = json.parseObject(content);

        assertThat(dto.getText()).isEqualTo("Great item!");
    }
}