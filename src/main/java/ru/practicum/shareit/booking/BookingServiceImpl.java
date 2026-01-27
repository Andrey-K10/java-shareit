package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto addBooking(Long userId, BookingRequestDto bookingRequestDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Item not found"));

        if (!item.getAvailable()) {
            throw new IllegalStateException("Item is not available");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Owner cannot book own item");
        }

        if (bookingRequestDto.getStart() == null || bookingRequestDto.getEnd() == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }

        if (bookingRequestDto.getStart().isAfter(bookingRequestDto.getEnd()) ||
                bookingRequestDto.getStart().isEqual(bookingRequestDto.getEnd())) {
            throw new IllegalArgumentException("Invalid booking dates");
        }

        if (bookingRequestDto.getStart().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }

        if (bookingRequestDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("End date cannot be in the past");
        }

        Booking booking = new Booking();
        booking.setStart(bookingRequestDto.getStart());
        booking.setEnd(bookingRequestDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return toBookingDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto updateBookingStatus(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only owner can update booking status");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalStateException("Booking is not in WAITING status");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        return toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
            case "ALL":
                return bookingRepository.findByBookerId(userId, sort).stream()
                        .map(this::toBookingDto)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findByBookerIdAndEndIsBefore(userId, now, sort).stream()
                        .map(this::toBookingDto)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findByBookerIdAndStartIsAfter(userId, now, sort).stream()
                        .map(this::toBookingDto)
                        .collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, now, now, sort).stream()
                        .map(this::toBookingDto)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, sort).stream()
                        .map(this::toBookingDto)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, sort).stream()
                        .map(this::toBookingDto)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
            case "ALL":
                return bookingRepository.findByItemOwnerId(userId, sort).stream()
                        .map(this::toBookingDto)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findByItemOwnerIdAndEndIsBefore(userId, now, sort).stream()
                        .map(this::toBookingDto)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findByItemOwnerIdAndStartIsAfter(userId, now, sort).stream()
                        .map(this::toBookingDto)
                        .collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(userId, now, now, sort).stream()
                        .map(this::toBookingDto)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, sort).stream()
                        .map(this::toBookingDto)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, sort).stream()
                        .map(this::toBookingDto)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    private BookingDto toBookingDto(Booking booking) {
        return BookingMapper.toBookingDto(booking);
    }
}