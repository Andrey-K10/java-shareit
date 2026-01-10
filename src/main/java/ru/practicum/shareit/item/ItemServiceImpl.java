package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.*;

/**
 * TODO Sprint add-controllers.
 */
@Service
public class ItemServiceImpl implements ItemService {

    private final Map<Long, Item> items = new HashMap<>();

    private Long itemIdSequence = 1L;

    private final UserService userService;

    public ItemServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {

        User owner = userService.getUser(userId);

        if (owner == null) {
            return null;
        }

        if (itemDto.getName() == null
                || itemDto.getName().trim().isEmpty()
                || itemDto.getDescription() == null
                || itemDto.getAvailable() == null) {

            return null;
        }

        Item item = new Item();
        item.setId(itemIdSequence++);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);

        items.put(item.getId(), item);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {

        Item item = items.get(itemId);

        if (item == null) {
            return null;
        }

        if (item.getOwner() != null
                && !item.getOwner().getId().equals(userId)) {

            throw new SecurityException();
        }

        if (itemDto.getName() != null) {

            if (itemDto.getName().trim().isEmpty()) {
                return null;
            }

            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItem(Long itemId) {

        Item item = items.get(itemId);

        if (item == null) {
            return null;
        }

        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {

        List<ItemDto> result = new ArrayList<>();

        for (Item item : items.values()) {
            if (item.getOwner() != null
                    && item.getOwner().getId().equals(userId)) {

                result.add(ItemMapper.toItemDto(item));
            }
        }

        return result;
    }

    @Override
    public List<ItemDto> searchItems(String text) {

        List<ItemDto> result = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return result;
        }

        for (Item item : items.values()) {

            if (!Boolean.TRUE.equals(item.getAvailable())) {
                continue;
            }

            boolean nameMatch = item.getName() != null
                    && item.getName().toLowerCase().contains(text.toLowerCase());

            boolean descMatch = item.getDescription() != null
                    && item.getDescription().toLowerCase().contains(text.toLowerCase());

            if (nameMatch || descMatch) {
                result.add(ItemMapper.toItemDto(item));
            }
        }

        return result;
    }
}
