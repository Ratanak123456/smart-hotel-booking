package mapper;

import dto.BookingDTO;
import model.entities.Booking;

public class BookingMapper {
    public static BookingDTO toDTO(Booking booking) {
        if (booking == null) return null;
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUserId());
        dto.setRoomId(booking.getRoomId());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus());
        dto.setRoomNumber(booking.getRoomNumber());
        dto.setRoomTypeName(booking.getRoomTypeName());
        dto.setUsername(booking.getUsername());
        return dto;
    }

    public static Booking toEntity(BookingDTO dto) {
        if (dto == null) return null;
        Booking booking = new Booking();
        booking.setId(dto.getId());
        booking.setUserId(dto.getUserId());
        booking.setRoomId(dto.getRoomId());
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setTotalPrice(dto.getTotalPrice());
        booking.setStatus(dto.getStatus());
        booking.setRoomNumber(dto.getRoomNumber());
        booking.setRoomTypeName(dto.getRoomTypeName());
        booking.setUsername(dto.getUsername());
        return booking;
    }
}
