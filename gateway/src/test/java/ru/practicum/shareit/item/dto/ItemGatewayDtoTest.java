package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemGatewayDtoTest {

    @Autowired
    private JacksonTester<ItemGatewayDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemGatewayDto dto = new ItemGatewayDto();
        dto.setName("Item name");
        dto.setDescription("Item description");
        dto.setAvailable(true);
        dto.setRequestId(1L);

        JsonContent<ItemGatewayDto> result = json.write(dto);

        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathBooleanValue("$.available");
        assertThat(result).hasJsonPathNumberValue("$.requestId");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"name\":\"Item name\",\"description\":\"Item description\",\"available\":true,\"requestId\":1}";

        ItemGatewayDto dto = json.parseObject(content);

        assertThat(dto.getName()).isEqualTo("Item name");
        assertThat(dto.getDescription()).isEqualTo("Item description");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(1L);
    }

    @Test
    void testDeserialize_whenRequestIdNull() throws Exception {
        String content = "{\"name\":\"Item name\",\"description\":\"Item description\",\"available\":true}";

        ItemGatewayDto dto = json.parseObject(content);

        assertThat(dto.getName()).isEqualTo("Item name");
        assertThat(dto.getDescription()).isEqualTo("Item description");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isNull();
    }
}