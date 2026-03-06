package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestMapperTest {

    private final ItemRequestMapper mapper = Mappers.getMapper(ItemRequestMapper.class);

    @Test
    void toItemRequestDto_shouldMapAllFields() {
        User requestor = new User();
        requestor.setId(1L);
        requestor.setName("User");
        requestor.setEmail("user@example.com");

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need a drill");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.of(2024, 1, 1, 10, 0));

        ItemRequestDto dto = mapper.toItemRequestDto(request);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Need a drill", dto.getDescription());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), dto.getCreated());
        assertNull(dto.getItems()); // items are mapped separately
    }

    @Test
    void toItemRequest_shouldMapAllFields() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Need a drill");
        dto.setCreated(LocalDateTime.of(2024, 1, 1, 10, 0));

        ItemRequest request = mapper.toItemRequest(dto);

        assertNotNull(request);
        assertNull(request.getId()); // id is ignored
        assertEquals("Need a drill", request.getDescription());
        assertNull(request.getCreated()); // created is ignored
        assertNull(request.getRequestor()); // requestor is ignored
    }
}