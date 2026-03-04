package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.item.dto.CommentGatewayDto;
import ru.practicum.shareit.item.dto.ItemGatewayDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@ContextConfiguration(classes = ShareItGateway.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemClient itemClient;

    @Test
    void addItem_whenValid_shouldReturnOk() throws Exception {
        ItemGatewayDto dto = new ItemGatewayDto();
        dto.setName("Item name");
        dto.setDescription("Item description");
        dto.setAvailable(true);

        when(itemClient.addItem(anyLong(), any(ItemGatewayDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void addItem_whenNameBlank_shouldReturnBadRequest() throws Exception {
        ItemGatewayDto dto = new ItemGatewayDto();
        dto.setName("");
        dto.setDescription("Item description");
        dto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_whenNameNull_shouldReturnBadRequest() throws Exception {
        ItemGatewayDto dto = new ItemGatewayDto();
        dto.setName(null);
        dto.setDescription("Item description");
        dto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_whenDescriptionBlank_shouldReturnBadRequest() throws Exception {
        ItemGatewayDto dto = new ItemGatewayDto();
        dto.setName("Item name");
        dto.setDescription("");
        dto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_whenDescriptionNull_shouldReturnBadRequest() throws Exception {
        ItemGatewayDto dto = new ItemGatewayDto();
        dto.setName("Item name");
        dto.setDescription(null);
        dto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_whenAvailableNull_shouldReturnBadRequest() throws Exception {
        ItemGatewayDto dto = new ItemGatewayDto();
        dto.setName("Item name");
        dto.setDescription("Item description");
        dto.setAvailable(null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void updateItem_whenValid_shouldReturnOk() throws Exception {
        ItemGatewayDto dto = new ItemGatewayDto();
        dto.setName("Updated name");
        dto.setDescription("Updated description");
        dto.setAvailable(false);

        when(itemClient.updateItem(anyLong(), anyLong(), any(ItemGatewayDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }


    @Test
    void getItem_whenValid_shouldReturnOk() throws Exception {
        when(itemClient.getItem(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk());
    }


    @Test
    void getItems_whenValid_shouldReturnOk() throws Exception {
        when(itemClient.getItems(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }


    @Test
    void search_whenValidText_shouldReturnOk() throws Exception {
        when(itemClient.searchItems(anyString()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", "search text"))
                .andExpect(status().isOk());
    }

    @Test
    void search_whenEmptyText_shouldReturnOk() throws Exception {
        when(itemClient.searchItems(anyString()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk());
    }

    @Test
    void search_whenBlankText_shouldReturnOk() throws Exception {
        when(itemClient.searchItems(anyString()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", "   "))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_whenValid_shouldReturnOk() throws Exception {
        CommentGatewayDto dto = new CommentGatewayDto();
        dto.setText("Great item!");

        when(itemClient.addComment(anyLong(), anyLong(), any(CommentGatewayDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_whenTextBlank_shouldReturnBadRequest() throws Exception {
        CommentGatewayDto dto = new CommentGatewayDto();
        dto.setText("");

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_whenTextNull_shouldReturnBadRequest() throws Exception {
        CommentGatewayDto dto = new CommentGatewayDto();
        dto.setText(null);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

}