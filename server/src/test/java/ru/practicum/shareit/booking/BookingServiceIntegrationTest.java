package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private BookingRepository bookingRepository;

    @MockitoBean
    private BookingMapper bookingMapper;

    @Test
    void addBooking_shouldSaveBooking() {
        Long userId = 1L;
        Long itemId = 2L;
        Long bookingId = 10L;

        User booker = new User();
        booker.setId(userId);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        User owner = new User();
        owner.setId(3L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        Item item = new Item();
        item.setId(itemId);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);

        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(itemId);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        Booking savedBooking = new Booking();
        savedBooking.setId(bookingId);
        savedBooking.setStart(requestDto.getStart());
        savedBooking.setEnd(requestDto.getEnd());
        savedBooking.setItem(item);
        savedBooking.setBooker(booker);
        savedBooking.setStatus(BookingStatus.WAITING);

        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(bookingId);
        expectedDto.setStart(requestDto.getStart());
        expectedDto.setEnd(requestDto.getEnd());
        expectedDto.setStatus(BookingStatus.WAITING);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(bookingMapper.toBookingDto(savedBooking)).thenReturn(expectedDto);

        BookingDto result = bookingService.addBooking(userId, requestDto);

        assertNotNull(result);
        assertEquals(bookingId, result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @Test
    void updateBookingStatus_shouldUpdateStatus() {
        Long userId = 1L;
        Long bookingId = 2L;

        User owner = new User();
        owner.setId(userId);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        Booking updatedBooking = new Booking();
        updatedBooking.setId(bookingId);
        updatedBooking.setStatus(BookingStatus.APPROVED);

        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(bookingId);
        expectedDto.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(updatedBooking);
        when(bookingMapper.toBookingDto(updatedBooking)).thenReturn(expectedDto);

        BookingDto result = bookingService.updateBookingStatus(userId, bookingId, true);

        assertNotNull(result);
        assertEquals(bookingId, result.getId());
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void getBooking_shouldReturnBooking() {
        Long userId = 1L;
        Long bookingId = 2L;

        User user = new User();
        user.setId(userId);

        Item item = new Item();
        item.setOwner(user);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setBooker(user);
        booking.setItem(item);

        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(expectedDto);

        BookingDto result = bookingService.getBooking(userId, bookingId);

        assertNotNull(result);
        assertEquals(bookingId, result.getId());
    }

    @Test
    void getUserBookings_shouldReturnBookings() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByBookerId(eq(userId), any(Sort.class)))
                .thenReturn(List.of(new Booking(), new Booking()));
        when(bookingMapper.toBookingDto(any())).thenReturn(new BookingDto());

        List<BookingDto> result = bookingService.getUserBookings(userId, "ALL");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getUserBookings_whenUserNotFound_throwNotFoundException() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.getUserBookings(userId, "ALL"));
    }

    @Test
    void getOwnerBookings_shouldReturnBookings() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByItemOwnerId(eq(userId), any(Sort.class)))
                .thenReturn(List.of(new Booking(), new Booking()));
        when(bookingMapper.toBookingDto(any())).thenReturn(new BookingDto());

        List<BookingDto> result = bookingService.getOwnerBookings(userId, "ALL");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getOwnerBookings_whenUserNotFound_throwNotFoundException() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.getOwnerBookings(userId, "ALL"));
    }
}