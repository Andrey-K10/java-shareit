package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    @Test
    void toItemDto_shouldMapAllFields() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);

        ItemDto dto = ItemMapper.toItemDto(item);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Item", dto.getName());
        assertEquals("Description", dto.getDescription());
        assertTrue(dto.getAvailable());
    }

    @Test
    void toItem_shouldMapAllFields() {
        ItemDto dto = new ItemDto();
        dto.setName("Item");
        dto.setDescription("Description");
        dto.setAvailable(true);

        Item item = ItemMapper.toItem(dto);

        assertNotNull(item);
        assertEquals("Item", item.getName());
        assertEquals("Description", item.getDescription());
        assertTrue(item.getAvailable());
        assertNull(item.getId());
        assertNull(item.getOwner());
    }

    @Test
    void toCommentDto_shouldMapAllFields() {
        User author = new User();
        author.setId(1L);
        author.setName("Author");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        CommentDto dto = ItemMapper.toCommentDto(comment);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Great item!", dto.getText());
        assertEquals("Author", dto.getAuthorName());
        assertNotNull(dto.getCreated());
    }

}