package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestGatewayDtoTest {

    @Autowired
    private JacksonTester<ItemRequestGatewayDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemRequestGatewayDto dto = new ItemRequestGatewayDto();
        dto.setDescription("Need a drill");

        JsonContent<ItemRequestGatewayDto> result = json.write(dto);

        assertThat(result).hasJsonPathStringValue("$.description");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"description\":\"Need a drill\"}";

        ItemRequestGatewayDto dto = json.parseObject(content);

        assertThat(dto.getDescription()).isEqualTo("Need a drill");
    }
}