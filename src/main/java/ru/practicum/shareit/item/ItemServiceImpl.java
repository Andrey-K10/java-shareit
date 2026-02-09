package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Only owner can update item");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDtoWithBookings getItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        ItemDtoWithBookings dto = new ItemDtoWithBookings();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());

        List<Comment> comments = commentRepository.findByItemId(itemId);
        dto.setComments(comments.stream()
                .map(ItemMapper::toCommentDto)
                .collect(Collectors.toList()));

        return dto;
    }

    @Override
    public List<ItemDtoWithBookings> getItemsByOwner(Long userId) {
        List<Item> items = itemRepository.findByOwnerId(userId);
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();

        Map<Long, Booking> lastBookingsMap = getLastBookingsForItems(itemIds, now);
        Map<Long, Booking> nextBookingsMap = getNextBookingsForItems(itemIds, now);
        Map<Long, List<Comment>> commentsMap = getCommentsForItems(itemIds);

        return items.stream().map(item -> {
            ItemDtoWithBookings dto = new ItemDtoWithBookings();
            dto.setId(item.getId());
            dto.setName(item.getName());
            dto.setDescription(item.getDescription());
            dto.setAvailable(item.getAvailable());

            Booking lastBooking = lastBookingsMap.get(item.getId());
            if (lastBooking != null) {
                ItemDtoWithBookings.BookingInfo bookingInfo = new ItemDtoWithBookings.BookingInfo();
                bookingInfo.setId(lastBooking.getId());
                bookingInfo.setBookerId(lastBooking.getBooker().getId());
                bookingInfo.setStart(lastBooking.getStart());
                bookingInfo.setEnd(lastBooking.getEnd());
                dto.setLastBooking(bookingInfo);
            }

            Booking nextBooking = nextBookingsMap.get(item.getId());
            if (nextBooking != null) {
                ItemDtoWithBookings.BookingInfo bookingInfo = new ItemDtoWithBookings.BookingInfo();
                bookingInfo.setId(nextBooking.getId());
                bookingInfo.setBookerId(nextBooking.getBooker().getId());
                bookingInfo.setStart(nextBooking.getStart());
                bookingInfo.setEnd(nextBooking.getEnd());
                dto.setNextBooking(bookingInfo);
            }

            List<Comment> comments = commentsMap.get(item.getId());
            if (comments != null) {
                dto.setComments(comments.stream()
                        .map(ItemMapper::toCommentDto)
                        .collect(Collectors.toList()));
            } else {
                dto.setComments(Collections.emptyList());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        List<Item> items = itemRepository.searchAvailableItems(text);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        LocalDateTime now = LocalDateTime.now();

        boolean hasBooked = bookingRepository.hasUserBookedItem(userId, itemId, now);
        if (!hasBooked) {
            throw new IllegalArgumentException("User has not booked this item");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);

        Comment savedComment = commentRepository.save(comment);
        return ItemMapper.toCommentDto(savedComment);
    }

    private Map<Long, Booking> getLastBookingsForItems(List<Long> itemIds, LocalDateTime now) {
        if (itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Booking> lastBookings = new ArrayList<>();
        for (Long itemId : itemIds) {
            List<Booking> bookings = bookingRepository.findLastBookingForItem(
                    itemId, now, PageRequest.of(0, 1));
            if (!bookings.isEmpty()) {
                lastBookings.add(bookings.get(0));
            }
        }

        return lastBookings.stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> booking
                ));
    }

    private Map<Long, Booking> getNextBookingsForItems(List<Long> itemIds, LocalDateTime now) {
        if (itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Booking> nextBookings = new ArrayList<>();
        for (Long itemId : itemIds) {
            List<Booking> bookings = bookingRepository.findNextBookingForItem(
                    itemId, now, PageRequest.of(0, 1));
            if (!bookings.isEmpty()) {
                nextBookings.add(bookings.get(0));
            }
        }

        return nextBookings.stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> booking
                ));
    }

    private Map<Long, List<Comment>> getCommentsForItems(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Comment> allComments = commentRepository.findAll();
        return allComments.stream()
                .filter(comment -> itemIds.contains(comment.getItem().getId()))
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId()
                ));
    }
}