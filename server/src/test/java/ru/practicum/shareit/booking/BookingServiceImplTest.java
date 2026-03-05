package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
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

        requestDto.setStart(LocalDateTime.now());
        requestDto.setEnd(LocalDateTime.now());

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.addBooking(2L, requestDto));
    }
}