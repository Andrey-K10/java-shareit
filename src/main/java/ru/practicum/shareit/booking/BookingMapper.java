package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());

        if (booking.getItem() != null) {
            dto.setItem(ItemMapper.toItemDto(booking.getItem()));
        }

        if (booking.getBooker() != null) {
            dto.setBooker(UserMapper.toUserDto(booking.getBooker()));
        }

        return dto;
    }
}