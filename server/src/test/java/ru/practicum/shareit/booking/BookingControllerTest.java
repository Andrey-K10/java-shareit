package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @Test
    void addBooking_whenValid_shouldReturnOk() throws Exception {
        Long userId = 1L;
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        BookingDto responseDto = new BookingDto();
        responseDto.setId(1L);

        when(bookingService.addBooking(eq(userId), any(BookingRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void addBooking_whenItemNotAvailable_shouldReturnBadRequest() throws Exception {
        Long userId = 1L;
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        when(bookingService.addBooking(eq(userId), any(BookingRequestDto.class)))
                .thenThrow(new IllegalStateException("Item is not available"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBooking_whenInvalidDates_shouldReturnBadRequest() throws Exception {
        Long userId = 1L;
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().minusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        when(bookingService.addBooking(eq(userId), any(BookingRequestDto.class)))
                .thenThrow(new IllegalArgumentException("Invalid booking dates"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBookingStatus_whenValid_shouldReturnOk() throws Exception {
        Long userId = 1L;
        Long bookingId = 2L;

        BookingDto responseDto = new BookingDto();
        responseDto.setId(bookingId);
        responseDto.setStatus(BookingStatus.APPROVED);

        when(bookingService.updateBookingStatus(userId, bookingId, true))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));
    }

    @Test
    void getBooking_whenValid_shouldReturnOk() throws Exception {
        Long userId = 1L;
        Long bookingId = 2L;

        BookingDto responseDto = new BookingDto();
        responseDto.setId(bookingId);

        when(bookingService.getBooking(userId, bookingId)).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));
    }

    @Test
    void getBooking_whenNotFound_shouldReturnBadRequest() throws Exception {
        Long userId = 1L;
        Long bookingId = 999L;

        when(bookingService.getBooking(userId, bookingId))
                .thenThrow(new IllegalArgumentException("Booking not found"));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserBookings_whenValid_shouldReturnOk() throws Exception {
        Long userId = 1L;

        when(bookingService.getUserBookings(userId, "ALL"))
                .thenReturn(List.of(new BookingDto(), new BookingDto()));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getOwnerBookings_whenValid_shouldReturnOk() throws Exception {
        Long userId = 1L;

        when(bookingService.getOwnerBookings(userId, "ALL"))
                .thenReturn(List.of(new BookingDto(), new BookingDto()));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getOwnerBookings_whenInvalidUserId_shouldThrowException() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", "invalid"))
                .andExpect(status().isBadRequest());
    }
}