package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserGatewayDtoTest {

    @Autowired
    private JacksonTester<UserGatewayDto> json;

    @Test
    void testSerialize() throws Exception {
        UserGatewayDto dto = new UserGatewayDto();
        dto.setName("John Doe");
        dto.setEmail("john@example.com");

        JsonContent<UserGatewayDto> result = json.write(dto);

        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).hasJsonPathStringValue("$.email");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";

        UserGatewayDto dto = json.parseObject(content);

        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testDeserialize_whenEmailOnly() throws Exception {
        String content = "{\"email\":\"john@example.com\"}";

        UserGatewayDto dto = json.parseObject(content);

        assertThat(dto.getName()).isNull();
        assertThat(dto.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testDeserialize_whenNameOnly() throws Exception {
        String content = "{\"name\":\"John Doe\"}";

        UserGatewayDto dto = json.parseObject(content);

        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getEmail()).isNull();
    }
}