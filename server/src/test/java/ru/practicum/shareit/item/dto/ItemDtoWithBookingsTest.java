package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoWithBookingsTest {

    @Autowired
    private JacksonTester<ItemDtoWithBookings> json;

    @Test
    void testSerialize() throws Exception {
        ItemDtoWithBookings dto = new ItemDtoWithBookings();
        dto.setId(1L);
        dto.setName("Item name");
        dto.setDescription("Item description");
        dto.setAvailable(true);

        ItemDtoWithBookings.BookingInfo lastBooking = new ItemDtoWithBookings.BookingInfo();
        lastBooking.setId(1L);
        lastBooking.setBookerId(2L);
        lastBooking.setStart(LocalDateTime.of(2024, 1, 1, 10, 0));
        lastBooking.setEnd(LocalDateTime.of(2024, 1, 1, 12, 0));
        dto.setLastBooking(lastBooking);

        ItemDtoWithBookings.BookingInfo nextBooking = new ItemDtoWithBookings.BookingInfo();
        nextBooking.setId(2L);
        nextBooking.setBookerId(3L);
        nextBooking.setStart(LocalDateTime.of(2024, 1, 2, 10, 0));
        nextBooking.setEnd(LocalDateTime.of(2024, 1, 2, 12, 0));
        dto.setNextBooking(nextBooking);

        CommentDto comment = new CommentDto();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setAuthorName("User");
        comment.setCreated(LocalDateTime.of(2024, 1, 1, 15, 0));
        dto.setComments(List.of(comment));

        JsonContent<ItemDtoWithBookings> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathBooleanValue("$.available");
        assertThat(result).hasJsonPathMapValue("$.lastBooking");
        assertThat(result).hasJsonPathMapValue("$.nextBooking");
        assertThat(result).hasJsonPathArrayValue("$.comments");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"name\":\"Item name\",\"description\":\"Item description\",\"available\":true}";

        ItemDtoWithBookings dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Item name");
        assertThat(dto.getDescription()).isEqualTo("Item description");
        assertThat(dto.getAvailable()).isTrue();
    }
}