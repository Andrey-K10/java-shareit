package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
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
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @MockitoBean
    private ItemRequestRepository itemRequestRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private ItemRequestMapper itemRequestMapper;

    @Test
    void addRequest_shouldSaveRequest() {
        Long userId = 1L;
        Long requestId = 10L;

        User requestor = new User();
        requestor.setId(userId);
        requestor.setName("User");
        requestor.setEmail("user@example.com");

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill");

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("Need a drill");

        ItemRequest savedRequest = new ItemRequest();
        savedRequest.setId(requestId);
        savedRequest.setDescription("Need a drill");
        savedRequest.setRequestor(requestor);
        savedRequest.setCreated(LocalDateTime.now());

        ItemRequestDto expectedDto = new ItemRequestDto();
        expectedDto.setId(requestId);
        expectedDto.setDescription("Need a drill");

        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(itemRequestMapper.toItemRequest(requestDto)).thenReturn(itemRequest);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(savedRequest);
        when(itemRequestMapper.toItemRequestDto(savedRequest)).thenReturn(expectedDto);

        ItemRequestDto result = itemRequestService.addRequest(userId, requestDto);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("Need a drill", result.getDescription());
    }

    @Test
    void getUserRequests_shouldReturnRequests() {
        Long userId = 1L;

        User requestor = new User();
        requestor.setId(userId);

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Request");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequestDto expectedDto = new ItemRequestDto();
        expectedDto.setId(1L);
        expectedDto.setDescription("Request");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId))
                .thenReturn(List.of(request));
        when(itemRequestMapper.toItemRequestDto(request)).thenReturn(expectedDto);
        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of());

        List<ItemRequestDto> result = itemRequestService.getUserRequests(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequests() {
        Long userId = 1L;

        User requestor = new User();
        requestor.setId(2L);

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Request");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequestDto expectedDto = new ItemRequestDto();
        expectedDto.setId(1L);
        expectedDto.setDescription("Request");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId))
                .thenReturn(List.of(request));
        when(itemRequestMapper.toItemRequestDto(request)).thenReturn(expectedDto);
        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of());

        List<ItemRequestDto> result = itemRequestService.getAllRequests(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        Long requestId = 1L;
        Long userId = 2L;

        User requestor = new User();
        requestor.setId(userId);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setDescription("Need a drill");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        Item item = new Item();
        item.setId(10L);
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);
        item.setRequest(request);

        ItemRequestDto expectedDto = new ItemRequestDto();
        expectedDto.setId(requestId);
        expectedDto.setDescription("Need a drill");

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(requestId)).thenReturn(List.of(item));
        when(itemRequestMapper.toItemRequestDto(request)).thenReturn(expectedDto);

        ItemRequestDto result = itemRequestService.getRequestById(requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
    }
}