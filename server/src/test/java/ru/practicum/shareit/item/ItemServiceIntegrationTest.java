package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private BookingRepository bookingRepository;

    @MockitoBean
    private CommentRepository commentRepository;

    @Test
    void addItem_shouldSaveItem() {
        Long userId = 1L;
        Long itemId = 10L;

        User owner = new User();
        owner.setId(userId);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item name");
        itemDto.setDescription("Item description");
        itemDto.setAvailable(true);

        Item savedItem = new Item();
        savedItem.setId(itemId);
        savedItem.setName(itemDto.getName());
        savedItem.setDescription(itemDto.getDescription());
        savedItem.setAvailable(itemDto.getAvailable());
        savedItem.setOwner(owner);

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        ItemDto result = itemService.addItem(userId, itemDto);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Item name", result.getName());
        assertEquals("Item description", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void updateItem_shouldUpdateItem() {
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
    void getItem_shouldReturnItemWithComments() {
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
    void getItem_whenItemNotFound_throwNotFoundException() {
        Long itemId = 999L;

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItem(itemId));
    }

    @Test
    void getItemsByOwner_shouldReturnItems() {
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
        when(bookingRepository.findLastBookingForItem(anyLong(), any(LocalDateTime.class), any()))
                .thenReturn(List.of());
        when(bookingRepository.findNextBookingForItem(anyLong(), any(LocalDateTime.class), any()))
                .thenReturn(List.of());
        when(commentRepository.findAll()).thenReturn(List.of());

        List<ItemDtoWithBookings> result = itemService.getItemsByOwner(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void searchItems_shouldReturnItems() {
        String text = "search";
        Item item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description with search text");
        item.setAvailable(true);

        when(itemRepository.searchAvailableItems(text)).thenReturn(List.of(item));

        List<ItemDto> result = itemService.searchItems(text);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void searchItems_whenTextBlank_shouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("");
        assertNotNull(result);
        assertTrue(result.isEmpty());

        result = itemService.searchItems("   ");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void addComment_shouldSaveComment() {
        Long userId = 1L;
        Long itemId = 2L;
        Long commentId = 10L;

        User author = new User();
        author.setId(userId);
        author.setName("Author");

        Item item = new Item();
        item.setId(itemId);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        Comment savedComment = new Comment();
        savedComment.setId(commentId);
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
        assertEquals(commentId, result.getId());
        assertEquals("Great item!", result.getText());
        assertEquals("Author", result.getAuthorName());
    }

    @Test
    void addComment_whenUserHasNotBookedItem_throwIllegalArgumentException() {
        Long userId = 1L;
        Long itemId = 2L;

        User author = new User();
        author.setId(userId);

        Item item = new Item();
        item.setId(itemId);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.hasUserBookedItem(eq(userId), eq(itemId), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> itemService.addComment(userId, itemId, commentDto));
    }
}