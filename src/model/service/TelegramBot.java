package model.service;

import model.dao.UserDao;
import model.dao.RoomDao;
import model.dao.BookingDao;
import model.entities.User;
import model.entities.Room;
import model.entities.Booking;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelegramBot implements Runnable {
    // REPLACE WITH YOUR TOKEN
    private static final String BOT_TOKEN = "8731239997:AAElO0bjxNTomgdN42gvthqpjC8iPG9lpF4";
    private static final String BASE_URL = "https://api.telegram.org/bot" + BOT_TOKEN;
    
    private boolean running = true;
    private long lastUpdateId = 0;
    
    // Services
    private UserService userService = new UserService();
    private RoomService roomService = new RoomService();
    private BookingService bookingService = new BookingService();
    private TelegramService senderService = new TelegramService(); // For sending replies

    // Session Management
    private Map<Long, UserSession> sessions = new HashMap<>();

    @Override
    public void run() {
        System.out.println("[TelegramBot] Bot started. Listening for messages...");
        while (running) {
            try {
                getUpdates();
                Thread.sleep(1000); // Poll every 1 second
            } catch (Exception e) {
                System.err.println("[TelegramBot] Error in polling loop: " + e.getMessage());
                e.printStackTrace();
                try { Thread.sleep(5000); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void getUpdates() {
        try {
            String urlString = BASE_URL + "/getUpdates?offset=" + (lastUpdateId + 1) + "&timeout=0";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) response.append(line);
                br.close();
                
                parseAndProcessUpdates(response.toString());
            }
        } catch (Exception e) {
            // Ignore connection timeouts, they happen
        }
    }

    // A simple manual JSON parser logic for Telegram Updates
    private void parseAndProcessUpdates(String json) {
        // Regex to find "update_id" and "text"
        // This is a simplified parser. It assumes standard structure.
        
        // Pattern to match each update object roughly
        Pattern updatePattern = Pattern.compile("\\{\"update_id\":(\\d+),.*?\"chat\":\\{\"id\":(\\d+).*?\"text\":\"(.*?)\"");
        Matcher matcher = updatePattern.matcher(json);

        while (matcher.find()) {
            try {
                long updateId = Long.parseLong(matcher.group(1));
                long chatId = Long.parseLong(matcher.group(2));
                String text = matcher.group(3);
                
                // Handle escaped unicode text if necessary, simplified here
                text = text.replace("\\n", "\n").replace("\\\"", "\"");

                if (updateId > lastUpdateId) {
                    lastUpdateId = updateId;
                    handleMessage(chatId, text.trim());
                }
            } catch (Exception e) {
                System.err.println("Error parsing update: " + e.getMessage());
            }
        }
    }

    private void handleMessage(long chatId, String text) {
        UserSession session = sessions.computeIfAbsent(chatId, k -> new UserSession());
        
        // 1. Global Commands
        if (text.equals("/start")) {
            session.reset();
            reply(chatId, "Welcome to Smart Hotel Bot! 🏨\n\nCommands:\n/login - Log in to your account\n/register - Create a new account\n/cancel - Cancel current action");
            return;
        }
        
        if (text.equals("/cancel")) {
            session.reset();
            reply(chatId, "Action cancelled.");
            return;
        }

        // 2. State Machine Handling
        switch (session.state) {
            case NONE:
                handleNoneState(chatId, session, text);
                break;
            case LOGIN_USERNAME:
            case LOGIN_PASSWORD:
                handleLoginFlow(chatId, session, text);
                break;
            case REGISTER_USERNAME:
            case REGISTER_EMAIL:
            case REGISTER_PHONE:
            case REGISTER_PASSWORD:
                handleRegisterFlow(chatId, session, text);
                break;
            case BOOKING_SELECT_ROOM:
            case BOOKING_DATE_START:
            case BOOKING_DATE_END:
                handleBookingFlow(chatId, session, text);
                break;
        }
    }

    private void handleNoneState(long chatId, UserSession session, String text) {
        if (text.equals("/login")) {
            session.state = State.LOGIN_USERNAME;
            reply(chatId, "Please enter your Username:");
        } else if (text.equals("/register")) {
            session.state = State.REGISTER_USERNAME;
            reply(chatId, "Let's create an account.\nPlease enter a Username:");
        } else if (text.equals("/book")) {
            if (session.currentUser == null) {
                reply(chatId, "You must /login first.");
            } else {
                session.state = State.BOOKING_DATE_START;
                reply(chatId, "📅 Format: YYYY-MM-DD\nPlease enter Check-in Date:");
            }
        } else if (text.equals("/rooms")) {
            showRooms(chatId);
        } else {
            reply(chatId, "I didn't understand that. Try /start, /login, or /register.");
        }
    }

    private void handleLoginFlow(long chatId, UserSession session, String text) {
        if (session.state == State.LOGIN_USERNAME) {
            session.tempData.put("username", text);
            session.state = State.LOGIN_PASSWORD;
            reply(chatId, "Enter Password:");
        } else if (session.state == State.LOGIN_PASSWORD) {
            try {
                User user = userService.login(session.tempData.get("username"), text);
                if (user != null) {
                    session.currentUser = user;
                    // Auto-link Telegram ID if not set
                    if (user.getTelegramChatId() == null || user.getTelegramChatId() != chatId) {
                        userService.updateTelegramChatId(user.getId(), chatId);
                        user.setTelegramChatId(chatId);
                    }
                    session.state = State.NONE;
                    reply(chatId, "✅ Login Successful! Welcome " + user.getUsername() + ".\n\nYou can now use /book to make a reservation or /rooms to see rooms.");
                } else {
                    session.state = State.NONE;
                    reply(chatId, "❌ Invalid credentials. Try /login again.");
                }
            } catch (Exception e) {
                reply(chatId, "Error: " + e.getMessage());
                session.state = State.NONE;
            }
        }
    }

    private void handleRegisterFlow(long chatId, UserSession session, String text) {
        switch (session.state) {
            case REGISTER_USERNAME:
                session.tempData.put("username", text);
                session.state = State.REGISTER_EMAIL;
                reply(chatId, "Enter Email (must be @gmail.com):");
                break;
            case REGISTER_EMAIL:
                session.tempData.put("email", text);
                session.state = State.REGISTER_PHONE;
                reply(chatId, "Enter Phone Number:");
                break;
            case REGISTER_PHONE:
                session.tempData.put("phone", text);
                session.state = State.REGISTER_PASSWORD;
                reply(chatId, "Enter Password (min 8 chars, 1 Upper, 1 Lower, 1 Digit, 1 Special):");
                break;
            case REGISTER_PASSWORD:
                try {
                    boolean success = userService.register(
                        session.tempData.get("username"),
                        session.tempData.get("email"),
                        session.tempData.get("phone"),
                        text,
                        chatId
                    );
                    if (success) {
                        session.state = State.NONE;
                        reply(chatId, "✅ Registration successful! Please /login.");
                    } else {
                        reply(chatId, "Registration failed. Try /register again.");
                        session.state = State.NONE;
                    }
                } catch (Exception e) {
                    reply(chatId, "Error: " + e.getMessage() + "\nTry /register again.");
                    session.state = State.NONE;
                }
                break;
        }
    }

    private void handleBookingFlow(long chatId, UserSession session, String text) {
        try {
            if (session.state == State.BOOKING_DATE_START) {
                LocalDate date = LocalDate.parse(text);
                if (date.isBefore(LocalDate.now())) throw new IllegalArgumentException("Date cannot be in past");
                session.tempData.put("checkin", text);
                session.state = State.BOOKING_DATE_END;
                reply(chatId, "Enter Check-out Date (YYYY-MM-DD):");
                
            } else if (session.state == State.BOOKING_DATE_END) {
                LocalDate date = LocalDate.parse(text);
                LocalDate checkin = LocalDate.parse(session.tempData.get("checkin"));
                if (!date.isAfter(checkin)) throw new IllegalArgumentException("Check-out must be after check-in");
                
                session.tempData.put("checkout", text);
                
                // Show available rooms
                List<Room> rooms = roomService.getAvailableRoomsByDate(checkin, date, 1);
                if (rooms.isEmpty()) {
                    reply(chatId, "No rooms available for these dates. Try different dates with /book.");
                    session.state = State.NONE;
                    return;
                }
                
                StringBuilder msg = new StringBuilder("🏠 Available Rooms:\n\n");
                for (Room r : rooms) {
                    msg.append(String.format("ID: %s | %s | Type: %s | $%s\n", r.getRoomNumber(), r.getRoomNumber(), r.getRoomTypeName(), r.getPricePerNight()));
                }
                msg.append("\nType the Room Number ID to confirm booking:");
                reply(chatId, msg.toString());
                session.state = State.BOOKING_SELECT_ROOM;
                
            } else if (session.state == State.BOOKING_SELECT_ROOM) {
                String roomNum = text;
                Room room = roomService.getRoomByNumber(roomNum);
                if (room == null) {
                    reply(chatId, "Invalid Room Number. Try again or /cancel.");
                    return;
                }
                
                Booking booking = new Booking();
                booking.setUserId(session.currentUser.getId());
                booking.setRoomId(room.getId());
                booking.setCheckInDate(LocalDate.parse(session.tempData.get("checkin")));
                booking.setCheckOutDate(LocalDate.parse(session.tempData.get("checkout")));
                booking.setUsername(session.currentUser.getUsername());
                booking.setUserEmail(session.currentUser.getEmail());
                booking.setUserPhone(session.currentUser.getPhoneNumber());
                booking.setTelegramChatId(chatId);
                
                bookingService.createBooking(booking);
                
                reply(chatId, "🎉 Booking Request Sent! Reference #" + booking.getId() + "\nStatus: PENDING\nWe will notify you when approved.");
                session.state = State.NONE;
            }
        } catch (DateTimeParseException e) {
            reply(chatId, "Invalid Date format. Use YYYY-MM-DD.");
        } catch (Exception e) {
            reply(chatId, "Error: " + e.getMessage());
            e.printStackTrace();
            session.state = State.NONE;
        }
    }

    private void showRooms(long chatId) {
        try {
            List<Room> rooms = roomService.getAllRooms(100, 0);
            StringBuilder msg = new StringBuilder("🏨 Hotel Rooms:\n");
            for (Room r : rooms) {
                msg.append(r.getRoomNumber()).append(" - ").append(r.getRoomTypeName())
                   .append(" ($").append(r.getPricePerNight()).append(")\n");
            }
            reply(chatId, msg.toString());
        } catch (SQLException e) {
            reply(chatId, "Error fetching rooms.");
        }
    }

    private void reply(long chatId, String message) {
        senderService.sendMessage(chatId, message);
    }

    // Inner classes for State Management
    private enum State {
        NONE,
        LOGIN_USERNAME, LOGIN_PASSWORD,
        REGISTER_USERNAME, REGISTER_EMAIL, REGISTER_PHONE, REGISTER_PASSWORD,
        BOOKING_DATE_START, BOOKING_DATE_END, BOOKING_SELECT_ROOM
    }

    private class UserSession {
        User currentUser;
        State state = State.NONE;
        Map<String, String> tempData = new HashMap<>();
        
        void reset() {
            state = State.NONE;
            tempData.clear();
        }
    }
}
