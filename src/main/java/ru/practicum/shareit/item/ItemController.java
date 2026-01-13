package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Positive;

import java.util.List;

@RestController
@RequestMapping("/items")
@Validated
public class ItemController {

    private final ItemService itemService;
    private final UserService userService;

    public ItemController(ItemService itemService, UserService userService) {
        this.itemService = itemService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ItemDto> addItem(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @Valid @RequestBody ItemDto itemDto) {

        User owner = userService.getUser(userId);
        if (owner == null) {
            return ResponseEntity.status(404).build();
        }

        ItemDto result = itemService.addItem(userId, itemDto);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @PathVariable @Positive Long itemId,
            @RequestBody ItemDto itemDto) {

        try {
            ItemDto result = itemService.updateItem(userId, itemId, itemDto);

            if (result == null) {
                return ResponseEntity.status(404).build();
            }

            return ResponseEntity.ok(result);

        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItem(@PathVariable @Positive Long itemId) {
        ItemDto result = itemService.getItem(itemId);
        if (result == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getItems(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {

        User owner = userService.getUser(userId);
        if (owner == null) {
            return ResponseEntity.status(404).build();
        }

        List<ItemDto> items = itemService.getItemsByOwner(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public List<ItemDto> search(
            @RequestParam("text") String text) {

        return itemService.searchItems(text);
    }
}