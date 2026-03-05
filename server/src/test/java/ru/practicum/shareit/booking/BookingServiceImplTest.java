package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingRequestDto requestDto;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {

        owner = new User();
        owner.setId(1L);

        booker = new User();
        booker.setId(2L);

        item = new Item();
        item.setId(1L);
        item.setOwner(owner);
        item.setAvailable(true);

        booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(booker);

        requestDto = new BookingRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().minusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(1));

        bookingDto = new BookingDto();
        bookingDto.setId(1L);
    }

    @Test
    void addBooking_shouldCreateBooking() {

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toBookingDto(any())).thenReturn(bookingDto);

        BookingDto result = bookingService.addBooking(2L, requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(bookingRepository).save(any());
    }

    @Test
    void addBooking_shouldThrow_whenUserNotFound() {

        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(2L, requestDto));
    }

    @Test
    void addBooking_shouldThrow_whenItemNotFound() {

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(2L, requestDto));
    }

    @Test
    void addBooking_shouldThrow_whenItemNotAvailable() {

        item.setAvailable(false);

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(IllegalStateException.class,
                () -> bookingService.addBooking(2L, requestDto));
    }

    @Test
    void addBooking_shouldThrow_whenOwnerBooksOwnItem() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.addBooking(1L, requestDto));
    }

    @Test
    void addBooking_shouldThrow_whenDatesInvalid() {

        requestDto.setStart(LocalDateTime.now().plusDays(2));
        requestDto.setEnd(LocalDateTime.now().plusDays(1));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.addBooking(2L, requestDto));
    }

    @Test
    void getUserBookings_shouldThrow_whenStateUnknown() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getUserBookings(1L, "UNKNOWN"));
    }

    @Test
    void getOwnerBookings_shouldThrow_whenStateUnknown() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getOwnerBookings(1L, "UNKNOWN"));
    }

    @Test
    void updateBookingStatus_shouldThrow_whenBookingNotFound() {
        Long userId = 1L;
        Long bookingId = 999L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.updateBookingStatus(userId, bookingId, true));
    }

    @Test
    void updateBookingStatus_shouldThrow_whenUserNotOwner() {
        Long userId = 3L; // не владелец
        Long bookingId = 1L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.updateBookingStatus(userId, bookingId, true));
    }

    @Test
    void updateBookingStatus_shouldThrow_whenBookingNotWaiting() {
        Long userId = 1L;
        Long bookingId = 1L;
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class,
                () -> bookingService.updateBookingStatus(userId, bookingId, true));
    }

    @Test
    void getBooking_shouldReturnBookingForBooker() {
        Long userId = 2L; // booker
        Long bookingId = 1L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.getBooking(userId, bookingId);

        assertNotNull(result);
        verify(bookingMapper).toBookingDto(booking);
    }

    @Test
    void getBooking_shouldReturnBookingForOwner() {
        Long userId = 1L; // owner
        Long bookingId = 1L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.getBooking(userId, bookingId);

        assertNotNull(result);
        verify(bookingMapper).toBookingDto(booking);
    }

    @Test
    void getBooking_shouldThrow_whenBookingNotFound() {
        Long userId = 1L;
        Long bookingId = 999L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getBooking(userId, bookingId));
    }

    @Test
    void getBooking_shouldThrow_whenUserNotAuthorized() {
        Long userId = 3L; // чужой пользователь
        Long bookingId = 1L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getBooking(userId, bookingId));
    }

    @Test
    void getUserBookings_shouldReturnAllBookings() {
        Long userId = 2L;
        String state = "ALL";
        List<Booking> bookings = List.of(booking);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByBookerId(eq(userId), any(Sort.class))).thenReturn(bookings);
        when(bookingMapper.toBookingDto(any())).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findByBookerId(eq(userId), any(Sort.class));
    }

    @Test
    void getUserBookings_shouldReturnPastBookings() {
        Long userId = 2L;
        String state = "PAST";
        List<Booking> bookings = List.of(booking);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndEndIsBefore(eq(userId), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(bookings);
        when(bookingMapper.toBookingDto(any())).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getUserBookings_shouldThrow_whenUserNotFound() {
        Long userId = 999L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> bookingService.getUserBookings(userId, "ALL"));
    }

    @Test
    void getOwnerBookings_shouldReturnAllBookings() {
        Long userId = 1L;
        String state = "ALL";
        List<Booking> bookings = List.of(booking);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByItemOwnerId(eq(userId), any(Sort.class))).thenReturn(bookings);
        when(bookingMapper.toBookingDto(any())).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getOwnerBookings(userId, state);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void addBooking_shouldThrow_whenStartDateNull() {
        requestDto.setStart(null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.addBooking(2L, requestDto));
    }

    @Test
    void addBooking_shouldThrow_whenEndDateNull() {
        requestDto.setEnd(null);

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.addBooking(2L, requestDto));
    }

    @Test
    void addBooking_shouldThrow_whenEndEqualsStart() {
        LocalDateTime now = LocalDateTime.now();
        requestDto.setStart(now);
        requestDto.setEnd(now);

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.addBooking(2L, requestDto));
    }

}