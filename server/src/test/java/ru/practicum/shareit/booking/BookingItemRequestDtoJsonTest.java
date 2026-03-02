package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<BookingRequestDto> json;

    @Test
    void testSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 12, 0);

        BookingRequestDto dto = new BookingRequestDto();
        dto.setStart(start);
        dto.setEnd(end);
        dto.setItemId(1L);

        JsonContent<BookingRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).hasJsonPathStringValue("$.end");
        assertThat(result).hasJsonPathNumberValue("$.itemId");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"start\":\"2024-01-01T10:00:00\",\"end\":\"2024-01-01T12:00:00\",\"itemId\":1}";

        BookingRequestDto dto = json.parseObject(content);

        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0));
        assertThat(dto.getItemId()).isEqualTo(1L);
    }
}