package ru.practicum.shareit.booking;

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
import static org.mockito.ArgumentMatchers.*;
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

    @Test
    void addBooking_whenUserNotFound_throwNotFoundException() {
        Long userId = 1L;
        BookingRequestDto requestDto = new BookingRequestDto();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.addBooking(userId, requestDto));
        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRepository, bookingRepository, bookingMapper);
    }

    @Test
    void addBooking_whenItemNotFound_throwNotFoundException() {
        Long userId = 1L;
        Long itemId = 2L;
        User booker = new User();
        booker.setId(userId);
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(itemId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.addBooking(userId, requestDto));
        verify(itemRepository).findById(itemId);
    }

    @Test
    void addBooking_whenItemNotAvailable_throwIllegalStateException() {
        Long userId = 1L;
        Long itemId = 2L;
        User booker = new User();
        booker.setId(userId);
        User owner = new User();
        owner.setId(3L);
        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(false);
        item.setOwner(owner);
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(itemId);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(IllegalStateException.class, () -> bookingService.addBooking(userId, requestDto));
    }

    @Test
    void addBooking_whenOwnerBooksOwnItem_throwIllegalArgumentException() {
        Long userId = 1L;
        Long itemId = 2L;
        User owner = new User();
        owner.setId(userId);
        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(true);
        item.setOwner(owner);
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(itemId);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> bookingService.addBooking(userId, requestDto));
    }

    @Test
    void addBooking_whenStartDateNull_throwIllegalArgumentException() {
        Long userId = 1L;
        Long itemId = 2L;
        User booker = new User();
        booker.setId(userId);
        User owner = new User();
        owner.setId(3L);
        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(true);
        item.setOwner(owner);
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(itemId);
        requestDto.setStart(null);
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> bookingService.addBooking(userId, requestDto));
    }

    @Test
    void addBooking_whenEndDateNull_throwIllegalArgumentException() {
        Long userId = 1L;
        Long itemId = 2L;
        User booker = new User();
        booker.setId(userId);
        User owner = new User();
        owner.setId(3L);
        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(true);
        item.setOwner(owner);
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(itemId);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> bookingService.addBooking(userId, requestDto));
    }

    @Test
    void addBooking_whenStartAfterEnd_throwIllegalArgumentException() {
        Long userId = 1L;
        Long itemId = 2L;
        User booker = new User();
        booker.setId(userId);
        User owner = new User();
        owner.setId(3L);
        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(true);
        item.setOwner(owner);
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(itemId);
        requestDto.setStart(LocalDateTime.now().plusHours(2));
        requestDto.setEnd(LocalDateTime.now().plusHours(1));

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> bookingService.addBooking(userId, requestDto));
    }

    @Test
    void addBooking_whenStartEqualsEnd_throwIllegalArgumentException() {
        LocalDateTime now = LocalDateTime.now().plusHours(1);
        Long userId = 1L;
        Long itemId = 2L;
        User booker = new User();
        booker.setId(userId);
        User owner = new User();
        owner.setId(3L);
        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(true);
        item.setOwner(owner);
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(itemId);
        requestDto.setStart(now);
        requestDto.setEnd(now);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> bookingService.addBooking(userId, requestDto));
    }

    @Test
    void addBooking_whenStartInPast_throwIllegalArgumentException() {
        Long userId = 1L;
        Long itemId = 2L;
        User booker = new User();
        booker.setId(userId);
        User owner = new User();
        owner.setId(3L);
        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(true);
        item.setOwner(owner);
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(itemId);
        requestDto.setStart(LocalDateTime.now().minusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusHours(2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> bookingService.addBooking(userId, requestDto));
    }

    @Test
    void addBooking_whenEndInPast_throwIllegalArgumentException() {
        Long userId = 1L;
        Long itemId = 2L;
        User booker = new User();
        booker.setId(userId);
        User owner = new User();
        owner.setId(3L);
        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(true);
        item.setOwner(owner);
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(itemId);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(LocalDateTime.now().minusHours(1));

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> bookingService.addBooking(userId, requestDto));
    }

    @Test
    void addBooking_whenValid_shouldSaveBooking() {
        Long userId = 1L;
        Long itemId = 2L;
        User booker = new User();
        booker.setId(userId);
        User owner = new User();
        owner.setId(3L);
        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(true);
        item.setOwner(owner);
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(itemId);
        requestDto.setStart(LocalDateTime.now().plusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusHours(2));
        Booking savedBooking = new Booking();
        savedBooking.setId(10L);
        savedBooking.setStart(requestDto.getStart());
        savedBooking.setEnd(requestDto.getEnd());
        savedBooking.setItem(item);
        savedBooking.setBooker(booker);
        savedBooking.setStatus(BookingStatus.WAITING);
        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(10L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(bookingMapper.toBookingDto(savedBooking)).thenReturn(expectedDto);

        BookingDto result = bookingService.addBooking(userId, requestDto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void updateBookingStatus_whenBookingNotFound_throwIllegalArgumentException() {
        Long userId = 1L;
        Long bookingId = 2L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> bookingService.updateBookingStatus(userId, bookingId, true));
    }

    @Test
    void updateBookingStatus_whenUserNotOwner_throwIllegalArgumentException() {
        Long userId = 1L;
        Long ownerId = 2L;
        Long bookingId = 3L;
        User owner = new User();
        owner.setId(ownerId);
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(IllegalArgumentException.class, () -> bookingService.updateBookingStatus(userId, bookingId, true));
    }

    @Test
    void updateBookingStatus_whenBookingNotWaiting_throwIllegalStateException() {
        Long userId = 1L;
        Long bookingId = 2L;
        User owner = new User();
        owner.setId(userId);
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class, () -> bookingService.updateBookingStatus(userId, bookingId, true));
    }

    @Test
    void updateBookingStatus_whenApproved_shouldUpdateStatusToApproved() {
        Long userId = 1L;
        Long bookingId = 2L;
        User owner = new User();
        owner.setId(userId);
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        Booking updatedBooking = new Booking();
        updatedBooking.setStatus(BookingStatus.APPROVED);
        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(updatedBooking);
        when(bookingMapper.toBookingDto(updatedBooking)).thenReturn(expectedDto);

        BookingDto result = bookingService.updateBookingStatus(userId, bookingId, true);

        assertNotNull(result);
        assertEquals(bookingId, result.getId());
        verify(bookingRepository).save(booking);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    @Test
    void updateBookingStatus_whenRejected_shouldUpdateStatusToRejected() {
        Long userId = 1L;
        Long bookingId = 2L;
        User owner = new User();
        owner.setId(userId);
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        Booking updatedBooking = new Booking();
        updatedBooking.setStatus(BookingStatus.REJECTED);
        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(updatedBooking);
        when(bookingMapper.toBookingDto(updatedBooking)).thenReturn(expectedDto);

        BookingDto result = bookingService.updateBookingStatus(userId, bookingId, false);

        assertNotNull(result);
        assertEquals(bookingId, result.getId());
        verify(bookingRepository).save(booking);
        assertEquals(BookingStatus.REJECTED, booking.getStatus());
    }

    @Test
    void getBooking_whenBookingNotFound_throwIllegalArgumentException() {
        Long userId = 1L;
        Long bookingId = 2L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> bookingService.getBooking(userId, bookingId));
    }

    @Test
    void getBooking_whenUserIsNotBookerAndNotOwner_throwIllegalArgumentException() {
        Long userId = 1L;
        Long bookerId = 2L;
        Long ownerId = 3L;
        Long bookingId = 4L;
        User booker = new User();
        booker.setId(bookerId);
        User owner = new User();
        owner.setId(ownerId);
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(IllegalArgumentException.class, () -> bookingService.getBooking(userId, bookingId));
    }

    @Test
    void getBooking_whenUserIsBooker_shouldReturnBooking() {
        Long userId = 1L;
        Long bookingId = 2L;
        User booker = new User();
        booker.setId(userId);
        User owner = new User();
        owner.setId(3L);
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setBooker(booker);
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
    void getBooking_whenUserIsOwner_shouldReturnBooking() {
        Long userId = 1L;
        Long bookingId = 2L;
        User booker = new User();
        booker.setId(3L);
        User owner = new User();
        owner.setId(userId);
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setBooker(booker);
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
    void getUserBookings_whenUserNotFound_throwNotFoundException() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.getUserBookings(userId, "ALL"));
    }

    @Test
    void getUserBookings_whenStatePast_shouldReturnPastBookings() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndEndIsBefore(eq(userId), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toBookingDto(any())).thenReturn(new BookingDto());

        List<BookingDto> result = bookingService.getUserBookings(userId, "PAST");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getUserBookings_whenStateFuture_shouldReturnFutureBookings() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStartIsAfter(eq(userId), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toBookingDto(any())).thenReturn(new BookingDto());

        List<BookingDto> result = bookingService.getUserBookings(userId, "FUTURE");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getUserBookings_whenStateCurrent_shouldReturnCurrentBookings() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toBookingDto(any())).thenReturn(new BookingDto());

        List<BookingDto> result = bookingService.getUserBookings(userId, "CURRENT");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getUserBookings_whenStateWaiting_shouldReturnWaitingBookings() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStatus(eq(userId), eq(BookingStatus.WAITING), any(Sort.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toBookingDto(any())).thenReturn(new BookingDto());

        List<BookingDto> result = bookingService.getUserBookings(userId, "WAITING");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getUserBookings_whenStateRejected_shouldReturnRejectedBookings() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStatus(eq(userId), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toBookingDto(any())).thenReturn(new BookingDto());

        List<BookingDto> result = bookingService.getUserBookings(userId, "REJECTED");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getUserBookings_whenInvalidState_throwIllegalArgumentException() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> bookingService.getUserBookings(userId, "INVALID"));
    }

    @Test
    void getOwnerBookings_whenUserNotFound_throwNotFoundException() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.getOwnerBookings(userId, "ALL"));
    }

    @Test
    void getOwnerBookings_whenStateAll_shouldReturnAllBookings() {
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
    void getOwnerBookings_whenStatePast_shouldReturnPastBookings() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdAndEndIsBefore(eq(userId), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toBookingDto(any())).thenReturn(new BookingDto());

        List<BookingDto> result = bookingService.getOwnerBookings(userId, "PAST");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_whenStateFuture_shouldReturnFutureBookings() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdAndStartIsAfter(eq(userId), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toBookingDto(any())).thenReturn(new BookingDto());

        List<BookingDto> result = bookingService.getOwnerBookings(userId, "FUTURE");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_whenStateCurrent_shouldReturnCurrentBookings() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toBookingDto(any())).thenReturn(new BookingDto());

        List<BookingDto> result = bookingService.getOwnerBookings(userId, "CURRENT");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_whenStateWaiting_shouldReturnWaitingBookings() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdAndStatus(eq(userId), eq(BookingStatus.WAITING), any(Sort.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toBookingDto(any())).thenReturn(new BookingDto());

        List<BookingDto> result = bookingService.getOwnerBookings(userId, "WAITING");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_whenStateRejected_shouldReturnRejectedBookings() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdAndStatus(eq(userId), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toBookingDto(any())).thenReturn(new BookingDto());

        List<BookingDto> result = bookingService.getOwnerBookings(userId, "REJECTED");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_whenInvalidState_throwIllegalArgumentException() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> bookingService.getOwnerBookings(userId, "INVALID"));
    }

}