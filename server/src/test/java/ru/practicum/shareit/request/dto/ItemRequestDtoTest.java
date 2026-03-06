package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Need a drill");
        dto.setCreated(LocalDateTime.of(2024, 1, 1, 10, 0));

        ItemDto item1 = new ItemDto();
        item1.setId(10L);
        item1.setName("Drill");
        item1.setDescription("Powerful drill");
        item1.setAvailable(true);

        ItemDto item2 = new ItemDto();
        item2.setId(11L);
        item2.setName("Hammer");
        item2.setDescription("Big hammer");
        item2.setAvailable(true);

        dto.setItems(List.of(item1, item2));

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathStringValue("$.created");
        assertThat(result).hasJsonPathArrayValue("$.items");
        assertThat(result).hasJsonPathNumberValue("$.items[0].id");
        assertThat(result).hasJsonPathNumberValue("$.items[1].id");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"description\":\"Need a drill\",\"created\":\"2024-01-01T10:00:00\"}";

        ItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need a drill");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(dto.getItems()).isNull();
    }
}