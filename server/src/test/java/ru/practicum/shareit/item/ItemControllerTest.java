package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemService itemService;

    @Test
    void addItem_whenValid_shouldReturnOk() throws Exception {
        Long userId = 1L;
        ItemDto requestDto = new ItemDto();
        requestDto.setName("Item name");
        requestDto.setDescription("Item description");
        requestDto.setAvailable(true);

        ItemDto responseDto = new ItemDto();
        responseDto.setId(1L);
        responseDto.setName("Item name");
        responseDto.setDescription("Item description");
        responseDto.setAvailable(true);

        when(itemService.addItem(eq(userId), any(ItemDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Item name"))
                .andExpect(jsonPath("$.description").value("Item description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void addItem_whenUserNotFound_shouldReturnBadRequest() throws Exception {
        Long userId = 999L;
        ItemDto requestDto = new ItemDto();
        requestDto.setName("Item name");
        requestDto.setDescription("Item description");
        requestDto.setAvailable(true);

        when(itemService.addItem(eq(userId), any(ItemDto.class)))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_whenValid_shouldReturnOk() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;
        ItemDto requestDto = new ItemDto();
        requestDto.setName("Updated name");
        requestDto.setDescription("Updated description");
        requestDto.setAvailable(false);

        ItemDto responseDto = new ItemDto();
        responseDto.setId(itemId);
        responseDto.setName("Updated name");
        responseDto.setDescription("Updated description");
        responseDto.setAvailable(false);

        when(itemService.updateItem(eq(userId), eq(itemId), any(ItemDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Updated name"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void updateItem_whenItemNotFound_shouldReturnBadRequest() throws Exception {
        Long userId = 1L;
        Long itemId = 999L;
        ItemDto requestDto = new ItemDto();
        requestDto.setName("Updated name");

        when(itemService.updateItem(eq(userId), eq(itemId), any(ItemDto.class)))
                .thenThrow(new IllegalArgumentException("Item not found"));

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_whenUserNotOwner_shouldReturnBadRequest() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;
        ItemDto requestDto = new ItemDto();
        requestDto.setName("Updated name");

        when(itemService.updateItem(eq(userId), eq(itemId), any(ItemDto.class)))
                .thenThrow(new IllegalArgumentException("Only owner can update item"));

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItem_whenValid_shouldReturnOk() throws Exception {
        Long itemId = 1L;
        ItemDtoWithBookings responseDto = new ItemDtoWithBookings();
        responseDto.setId(itemId);
        responseDto.setName("Item");
        responseDto.setDescription("Description");
        responseDto.setAvailable(true);

        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Great item!");
        commentDto.setAuthorName("User");
        responseDto.setComments(List.of(commentDto));

        when(itemService.getItem(itemId)).thenReturn(responseDto);

        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Item"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.comments[0].id").value(1L))
                .andExpect(jsonPath("$.comments[0].text").value("Great item!"));
    }

    @Test
    void getItem_whenNotFound_shouldReturnNotFound() throws Exception {
        Long itemId = 999L;

        when(itemService.getItem(itemId)).thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItems_whenValid_shouldReturnOk() throws Exception {
        Long userId = 1L;

        ItemDtoWithBookings item1 = new ItemDtoWithBookings();
        item1.setId(1L);
        item1.setName("Item 1");

        ItemDtoWithBookings item2 = new ItemDtoWithBookings();
        item2.setId(2L);
        item2.setName("Item 2");

        when(itemService.getItemsByOwner(userId)).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getItems_whenNoItems_shouldReturnEmptyList() throws Exception {
        Long userId = 1L;

        when(itemService.getItemsByOwner(userId)).thenReturn(List.of());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void search_whenValidText_shouldReturnOk() throws Exception {
        String text = "search";

        ItemDto item1 = new ItemDto();
        item1.setId(1L);
        item1.setName("Item 1");

        ItemDto item2 = new ItemDto();
        item2.setId(2L);
        item2.setName("Item 2");

        when(itemService.searchItems(text)).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items/search")
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void search_whenEmptyText_shouldReturnEmptyList() throws Exception {
        when(itemService.searchItems("")).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addComment_whenValid_shouldReturnOk() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        CommentDto requestDto = new CommentDto();
        requestDto.setText("Great item!");

        CommentDto responseDto = new CommentDto();
        responseDto.setId(1L);
        responseDto.setText("Great item!");
        responseDto.setAuthorName("User");
        responseDto.setCreated(java.time.LocalDateTime.now());

        when(itemService.addComment(eq(userId), eq(itemId), any(CommentDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("User"));
    }

    @Test
    void addComment_whenUserNotFound_shouldReturnNotFound() throws Exception {
        Long userId = 999L;
        Long itemId = 2L;

        CommentDto requestDto = new CommentDto();
        requestDto.setText("Great item!");

        when(itemService.addComment(eq(userId), eq(itemId), any(CommentDto.class)))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addComment_whenItemNotFound_shouldReturnNotFound() throws Exception {
        Long userId = 1L;
        Long itemId = 999L;

        CommentDto requestDto = new CommentDto();
        requestDto.setText("Great item!");

        when(itemService.addComment(eq(userId), eq(itemId), any(CommentDto.class)))
                .thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addComment_whenUserHasNotBookedItem_shouldReturnBadRequest() throws Exception {
        Long userId = 1L;
        Long itemId = 2L;

        CommentDto requestDto = new CommentDto();
        requestDto.setText("Great item!");

        when(itemService.addComment(eq(userId), eq(itemId), any(CommentDto.class)))
                .thenThrow(new IllegalArgumentException("User has not booked this item"));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }
}