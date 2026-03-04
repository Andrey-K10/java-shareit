package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServicePrivateTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void getRequestById_withItemsAndWithoutItems_shouldMapCorrectly() {
        Long requestId = 1L;

        User user = new User();
        user.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now());

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(requestId);

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRequestMapper.toItemRequestDto(request)).thenReturn(dto);

        // Сценарий: есть items
        Item item = new Item();
        item.setId(100L);
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setRequest(request);

        when(itemRepository.findByRequestId(requestId)).thenReturn(List.of(item));

        ItemRequestDto resultWithItems = itemRequestService.getRequestById(requestId);

        assertNotNull(resultWithItems.getItems());
        assertEquals(1, resultWithItems.getItems().size());
        assertEquals(100L, resultWithItems.getItems().get(0).getId());

        // Сценарий: нет items
        when(itemRepository.findByRequestId(requestId)).thenReturn(List.of());

        ItemRequestDto resultWithoutItems = itemRequestService.getRequestById(requestId);
        assertNotNull(resultWithoutItems.getItems());
        assertTrue(resultWithoutItems.getItems().isEmpty());
    }
}