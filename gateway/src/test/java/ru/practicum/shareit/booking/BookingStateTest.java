package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BookingStateTest {

    @Test
    void from_whenValidState_shouldReturnOptionalWithState() {
        Optional<BookingState> result = BookingState.from("ALL");
        assertTrue(result.isPresent());
        assertEquals(BookingState.ALL, result.get());
    }

    @Test
    void from_whenValidStateLowerCase_shouldReturnOptionalWithState() {
        Optional<BookingState> result = BookingState.from("current");
        assertTrue(result.isPresent());
        assertEquals(BookingState.CURRENT, result.get());
    }

    @Test
    void from_whenInvalidState_shouldReturnEmptyOptional() {
        Optional<BookingState> result = BookingState.from("INVALID");
        assertFalse(result.isPresent());
    }

    @Test
    void from_whenNull_shouldReturnEmptyOptional() {
        Optional<BookingState> result = BookingState.from(null);
        assertFalse(result.isPresent());
    }

    @Test
    void from_whenEmptyString_shouldReturnEmptyOptional() {
        Optional<BookingState> result = BookingState.from("");
        assertFalse(result.isPresent());
    }
}