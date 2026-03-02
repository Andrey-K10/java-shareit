package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void addItem_whenUserNotFound_throwIllegalArgumentException() {
        Long userId = 1L;
        ItemDto itemDto = new ItemDto();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> itemService.addItem(userId, itemDto));
        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void addItem_whenValid_shouldSaveItem() {
        Long userId = 1L;
        User owner = new User();
        owner.setId(userId);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item name");
        itemDto.setDescription("Item description");
        itemDto.setAvailable(true);

        Item item = new Item();
        item.setId(10L);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.addItem(userId, itemDto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Item name", result.getName());
        assertEquals("Item description", result.getDescription());
        assertTrue(result.getAvailable());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void addItem_whenWithRequestId_shouldSetRequest() {
        Long userId = 1L;
        Long requestId = 2L;
        User owner = new User();
        owner.setId(userId);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item name");
        itemDto.setDescription("Item description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(requestId);

        Item item = new Item();
        item.setId(10L);
        item.setRequest(request);

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.addItem(userId, itemDto);

        assertNotNull(result);
        verify(itemRequestRepository).findById(requestId);
    }

    @Test
    void addItem_whenRequestNotFound_throwIllegalArgumentException() {
        Long userId = 1L;
        Long requestId = 2L;
        User owner = new User();
        owner.setId(userId);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item name");
        itemDto.setDescription("Item description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(requestId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> itemService.addItem(userId, itemDto));
    }

    @Test
    void updateItem_whenItemNotFound_throwIllegalArgumentException() {
        Long userId = 1L;
        Long itemId = 2L;
        ItemDto itemDto = new ItemDto();
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> itemService.updateItem(userId, itemId, itemDto));
    }

    @Test
    void updateItem_whenUserNotOwner_throwIllegalArgumentException() {
        Long userId = 1L;
        Long ownerId = 2L;
        Long itemId = 3L;
        User owner = new User();
        owner.setId(ownerId);
        Item item = new Item();
        item.setOwner(owner);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> itemService.updateItem(userId, itemId, new ItemDto()));
    }

    @Test
    void updateItem_whenValid_shouldUpdateAllFields() {
        Long userId = 1L;
        Long itemId = 2L;
        User owner = new User();
        owner.setId(userId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setName("Old name");
        existingItem.setDescription("Old description");
        existingItem.setAvailable(false);
        existingItem.setOwner(owner);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New name");
        updateDto.setDescription("New description");
        updateDto.setAvailable(true);

        Item updatedItem = new Item();
        updatedItem.setId(itemId);
        updatedItem.setName("New name");
        updatedItem.setDescription("New description");
        updatedItem.setAvailable(true);
        updatedItem.setOwner(owner);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        ItemDto result = itemService.updateItem(userId, itemId, updateDto);

        assertNotNull(result);
        assertEquals("New name", result.getName());
        assertEquals("New description", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void updateItem_whenPartialUpdate_shouldUpdateOnlyProvidedFields() {
        Long userId = 1L;
        Long itemId = 2L;
        User owner = new User();
        owner.setId(userId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setName("Old name");
        existingItem.setDescription("Old description");
        existingItem.setAvailable(false);
        existingItem.setOwner(owner);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New name");
        // description and available are null

        Item updatedItem = new Item();
        updatedItem.setId(itemId);
        updatedItem.setName("New name");
        updatedItem.setDescription("Old description");
        updatedItem.setAvailable(false);
        updatedItem.setOwner(owner);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        ItemDto result = itemService.updateItem(userId, itemId, updateDto);

        assertNotNull(result);
        assertEquals("New name", result.getName());
        assertEquals("Old description", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void getItem_whenItemNotFound_throwNotFoundException() {
        Long itemId = 1L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItem(itemId));
    }

    @Test
    void getItem_whenValid_shouldReturnItemWithComments() {
        Long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);

        User author = new User();
        author.setId(2L);
        author.setName("Author");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of(comment));

        ItemDtoWithBookings result = itemService.getItem(itemId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Item", result.getName());
        assertEquals("Description", result.getDescription());
        assertTrue(result.getAvailable());
        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());
        assertEquals("Great item!", result.getComments().get(0).getText());
        assertEquals("Author", result.getComments().get(0).getAuthorName());
    }

    @Test
    void getItemsByOwner_whenNoItems_shouldReturnEmptyList() {
        Long userId = 1L;
        when(itemRepository.findByOwnerId(userId)).thenReturn(List.of());

        List<ItemDtoWithBookings> result = itemService.getItemsByOwner(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getItemsByOwner_whenItemsExist_shouldReturnItemsWithBookingsAndComments() {
        Long userId = 1L;
        Long itemId1 = 1L;
        Long itemId2 = 2L;

        Item item1 = new Item();
        item1.setId(itemId1);
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        item1.setAvailable(true);

        Item item2 = new Item();
        item2.setId(itemId2);
        item2.setName("Item 2");
        item2.setDescription("Description 2");
        item2.setAvailable(true);

        when(itemRepository.findByOwnerId(userId)).thenReturn(List.of(item1, item2));

        User booker = new User();
        booker.setId(3L);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Comment");
        comment.setAuthor(booker);
        comment.setItem(item1);  // <-- Этого не хватало!
        comment.setCreated(LocalDateTime.now());

        when(commentRepository.findAll()).thenReturn(List.of(comment));

        List<ItemDtoWithBookings> result = itemService.getItemsByOwner(userId);

        assertNotNull(result);
        assertEquals(2, result.size());

        ItemDtoWithBookings dto1 = result.get(0);
        assertEquals(itemId1, dto1.getId());
        assertNotNull(dto1.getLastBooking());
        assertNotNull(dto1.getNextBooking());

        ItemDtoWithBookings dto2 = result.get(1);
        assertEquals(itemId2, dto2.getId());
        assertNull(dto2.getLastBooking());
        assertNull(dto2.getNextBooking());
    }

    @Test
    void searchItems_whenTextBlank_shouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("   ");
        assertNotNull(result);
        assertTrue(result.isEmpty());

        result = itemService.searchItems(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        result = itemService.searchItems("");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_whenTextValid_shouldReturnItems() {
        String text = "search";
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Item 1");
        item1.setDescription("Description with search text");
        item1.setAvailable(true);

        when(itemRepository.searchAvailableItems(text)).thenReturn(List.of(item1));

        List<ItemDto> result = itemService.searchItems(text);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void addComment_whenUserNotFound_throwNotFoundException() {
        Long userId = 1L;
        Long itemId = 2L;
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addComment(userId, itemId, commentDto));
    }

    @Test
    void addComment_whenItemNotFound_throwNotFoundException() {
        Long userId = 1L;
        Long itemId = 2L;
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        User author = new User();
        author.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addComment(userId, itemId, commentDto));
    }

    @Test
    void addComment_whenUserHasNotBookedItem_throwIllegalArgumentException() {
        Long userId = 1L;
        Long itemId = 2L;
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        User author = new User();
        author.setId(userId);

        Item item = new Item();
        item.setId(itemId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.hasUserBookedItem(eq(userId), eq(itemId), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> itemService.addComment(userId, itemId, commentDto));
    }

    @Test
    void addComment_whenValid_shouldSaveComment() {
        Long userId = 1L;
        Long itemId = 2L;
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        User author = new User();
        author.setId(userId);
        author.setName("Author");

        Item item = new Item();
        item.setId(itemId);

        Comment savedComment = new Comment();
        savedComment.setId(10L);
        savedComment.setText("Great item!");
        savedComment.setAuthor(author);
        savedComment.setItem(item);
        savedComment.setCreated(LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.hasUserBookedItem(eq(userId), eq(itemId), any(LocalDateTime.class)))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentDto result = itemService.addComment(userId, itemId, commentDto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Great item!", result.getText());
        assertEquals("Author", result.getAuthorName());
        verify(commentRepository).save(any(Comment.class));
    }
}