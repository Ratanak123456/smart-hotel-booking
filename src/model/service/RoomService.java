package model.service;

import model.dao.RoomDao;
import model.entities.Room;
import java.sql.SQLException;
import java.util.List;

public class RoomService {
    private RoomDao roomDao = new RoomDao();
    private static final int PAGE_SIZE = 5;

    public List<Room> getAllRoomsPaginated(int pageNumber) throws SQLException {
        int offset = (pageNumber - 1) * PAGE_SIZE;
        return roomDao.getAllRoomsPaginated(PAGE_SIZE, offset);
    }

    public List<Room> getRoomsByType(String roomTypeName, int pageNumber) throws SQLException {
        int offset = (pageNumber - 1) * PAGE_SIZE;
        return roomDao.getRoomsByType(roomTypeName, PAGE_SIZE, offset);
    }

    public List<Room> getRoomsByStatus(String status, int pageNumber) throws SQLException {
        int offset = (pageNumber - 1) * PAGE_SIZE;
        return roomDao.getRoomsByStatus(status, PAGE_SIZE, offset);
    }

    public List<Room> getRoomsByTypeAndStatus(String roomTypeName, String status, int pageNumber) throws SQLException {
        int offset = (pageNumber - 1) * PAGE_SIZE;
        return roomDao.getRoomsByTypeAndStatus(roomTypeName, status, PAGE_SIZE, offset);
    }

    public Room getRoomById(int roomId) throws SQLException {
        return roomDao.getRoomById(roomId);
    }

    public Room getRoomByNumber(String roomNumber) throws SQLException {
        return roomDao.getRoomByNumber(roomNumber);
    }

    public long getTotalRoomsCount() throws SQLException {
        return roomDao.getTotalRoomsCount();
    }

    public long getRoomCountByType(String roomTypeName) throws SQLException {
        return roomDao.getRoomCountByType(roomTypeName);
    }

    public long getRoomCountByStatus(String status) throws SQLException {
        return roomDao.getRoomCountByStatus(status);
    }

    public List<Room> getAvailableRoomsByDate(java.time.LocalDate checkIn, java.time.LocalDate checkOut, int pageNumber) throws SQLException {
        int offset = (pageNumber - 1) * PAGE_SIZE;
        return roomDao.getAvailableRoomsByDate(checkIn, checkOut, PAGE_SIZE, offset);
    }

    public long getAvailableRoomCountByDate(java.time.LocalDate checkIn, java.time.LocalDate checkOut) throws SQLException {
        return roomDao.getAvailableRoomCountByDate(checkIn, checkOut);
    }

    public List<Room> getAvailableRooms() throws SQLException {
        return roomDao.getRoomsByStatus("AVAILABLE", PAGE_SIZE, 0);
    }

    public int getTotalPages(long totalCount) {
        return (int) Math.ceil((double) totalCount / PAGE_SIZE);
    }

    public static int getPageSize() {
        return PAGE_SIZE;
    }
}