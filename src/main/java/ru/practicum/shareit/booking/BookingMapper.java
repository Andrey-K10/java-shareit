package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

@Mapper(componentModel = "spring", uses = {ItemMapper.class, UserMapper.class})
public interface BookingMapper {

    @Mapping(source = "item", target = "item")
    @Mapping(source = "booker", target = "booker")
    BookingDto toBookingDto(Booking booking);
}