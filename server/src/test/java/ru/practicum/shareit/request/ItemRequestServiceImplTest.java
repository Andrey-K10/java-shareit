package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestMapper itemRequestMapper;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void addRequest_whenUserNotFound_throwNotFoundException() {
        Long userId = 1L;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.addRequest(userId, requestDto));
        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRequestRepository, itemRequestMapper);
    }

    @Test
    void getUserRequests_whenUserNotFound_throwNotFoundException() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemRequestService.getUserRequests(userId));
        verify(userRepository).existsById(userId);
        verifyNoInteractions(itemRequestRepository);
    }

    @Test
    void getAllRequests_whenUserNotFound_throwNotFoundException() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemRequestService.getAllRequests(userId));
        verify(userRepository).existsById(userId);
        verifyNoInteractions(itemRequestRepository);
    }

    @Test
    void getRequestById_whenRequestNotFound_throwNotFoundException() {
        Long requestId = 999L;

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(requestId));
        verify(itemRequestRepository).findById(requestId);
        verifyNoInteractions(itemRepository, itemRequestMapper);
    }

    @Test
    void addRequest_whenValid_shouldSaveRequest() {
        Long userId = 1L;
        User requestor = new User();
        requestor.setId(userId);
        requestor.setName("User");
        requestor.setEmail("user@example.com");

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill");

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("Need a drill");

        ItemRequest savedRequest = new ItemRequest();
        savedRequest.setId(10L);
        savedRequest.setDescription("Need a drill");
        savedRequest.setRequestor(requestor);
        savedRequest.setCreated(LocalDateTime.now());

        ItemRequestDto expectedDto = new ItemRequestDto();
        expectedDto.setId(10L);
        expectedDto.setDescription("Need a drill");

        when(userRepository.findById(userId)).thenReturn(Optional.of(requestor));
        when(itemRequestMapper.toItemRequest(requestDto)).thenReturn(itemRequest);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(savedRequest);
        when(itemRequestMapper.toItemRequestDto(savedRequest)).thenReturn(expectedDto);

        ItemRequestDto result = itemRequestService.addRequest(userId, requestDto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Need a drill", result.getDescription());
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    void getUserRequests_whenUserNotFound_throwNoSuchElementException() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> itemRequestService.getUserRequests(userId));
        verify(userRepository).existsById(userId);
        verifyNoInteractions(itemRequestRepository);
    }

    @Test
    void getUserRequests_whenUserExists_shouldReturnRequests() {
        Long userId = 1L;
        User requestor = new User();
        requestor.setId(userId);

        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        request1.setDescription("Request 1");
        request1.setRequestor(requestor);
        request1.setCreated(LocalDateTime.now());

        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);
        request2.setDescription("Request 2");
        request2.setRequestor(requestor);
        request2.setCreated(LocalDateTime.now());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId))
                .thenReturn(List.of(request1, request2));

        ItemRequestDto dto1 = new ItemRequestDto();
        dto1.setId(1L);
        dto1.setDescription("Request 1");

        ItemRequestDto dto2 = new ItemRequestDto();
        dto2.setId(2L);
        dto2.setDescription("Request 2");

        when(itemRequestMapper.toItemRequestDto(request1)).thenReturn(dto1);
        when(itemRequestMapper.toItemRequestDto(request2)).thenReturn(dto2);

        List<ItemRequestDto> result = itemRequestService.getUserRequests(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void getUserRequests_whenNoRequests_shouldReturnEmptyList() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId))
                .thenReturn(List.of());

        List<ItemRequestDto> result = itemRequestService.getUserRequests(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllRequests_whenUserNotFound_throwNoSuchElementException() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> itemRequestService.getAllRequests(userId));
    }

    @Test
    void getAllRequests_whenUserExists_shouldReturnOtherUsersRequests() {
        Long userId = 1L;
        User requestor1 = new User();
        requestor1.setId(2L);
        User requestor2 = new User();
        requestor2.setId(3L);

        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        request1.setDescription("Request 1");
        request1.setRequestor(requestor1);
        request1.setCreated(LocalDateTime.now());

        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);
        request2.setDescription("Request 2");
        request2.setRequestor(requestor2);
        request2.setCreated(LocalDateTime.now());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId))
                .thenReturn(List.of(request1, request2));

        ItemRequestDto dto1 = new ItemRequestDto();
        dto1.setId(1L);
        dto1.setDescription("Request 1");

        ItemRequestDto dto2 = new ItemRequestDto();
        dto2.setId(2L);
        dto2.setDescription("Request 2");

        when(itemRequestMapper.toItemRequestDto(request1)).thenReturn(dto1);
        when(itemRequestMapper.toItemRequestDto(request2)).thenReturn(dto2);

        List<ItemRequestDto> result = itemRequestService.getAllRequests(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAllRequests_whenNoOtherRequests_shouldReturnEmptyList() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId))
                .thenReturn(List.of());

        List<ItemRequestDto> result = itemRequestService.getAllRequests(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRequestById_whenRequestNotFound_throwNoSuchElementException() {
        Long requestId = 999L;

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> itemRequestService.getRequestById(requestId));
    }

    @Test
    void getRequestById_whenRequestExists_shouldReturnRequestWithItems() {
        Long requestId = 1L;
        Long userId = 2L;

        User requestor = new User();
        requestor.setId(userId);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setDescription("Need a drill");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        Item item1 = new Item();
        item1.setId(10L);
        item1.setName("Drill");
        item1.setDescription("Powerful drill");
        item1.setAvailable(true);
        item1.setRequest(request);

        Item item2 = new Item();
        item2.setId(11L);
        item2.setName("Hammer");
        item2.setDescription("Big hammer");
        item2.setAvailable(true);
        item2.setRequest(request);

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(requestId)).thenReturn(List.of(item1, item2));

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(requestId);
        dto.setDescription("Need a drill");

        when(itemRequestMapper.toItemRequestDto(request)).thenReturn(dto);

        ItemRequestDto result = itemRequestService.getRequestById(requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertNotNull(result.getItems());
        assertEquals(2, result.getItems().size());
    }

    @Test
    void getRequestById_whenRequestExistsWithoutItems_shouldReturnRequestWithEmptyItems() {
        Long requestId = 1L;
        Long userId = 2L;

        User requestor = new User();
        requestor.setId(userId);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setDescription("Need a drill");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(requestId)).thenReturn(List.of());

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(requestId);
        dto.setDescription("Need a drill");

        when(itemRequestMapper.toItemRequestDto(request)).thenReturn(dto);

        ItemRequestDto result = itemRequestService.getRequestById(requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty());
    }
}