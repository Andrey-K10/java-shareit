package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> addItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemDto itemDto) {
        ItemDto result = itemService.addItem(userId, itemDto);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto) {
        ItemDto result = itemService.updateItem(userId, itemId, itemDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDtoWithBookings> getItem(@PathVariable Long itemId) {
        ItemDtoWithBookings result = itemService.getItem(itemId);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<ItemDtoWithBookings>> getItems(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemDtoWithBookings> items = itemService.getItemsByOwner(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam("text") String text) {
        return itemService.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentDto commentDto) {
        CommentDto result = itemService.addComment(userId, itemId, commentDto);
        return ResponseEntity.ok(result);
    }
}