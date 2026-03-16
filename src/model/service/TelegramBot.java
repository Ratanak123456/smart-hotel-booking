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
        
        // Safety check: Ensure no ADMIN can bypass via an existing session
        if (session.currentUser != null && "ADMIN".equalsIgnoreCase(session.currentUser.getRole())) {
            session.currentUser = null;
            session.reset();
            reply(chatId, "⚠️ <b>Access Revoked!</b>\nAdmin accounts are not allowed to use the Telegram bot. Please use the management console.");
            showMainMenu(chatId, session);
            return;
        }

        // 1. Global Commands
        if (text.equals("/start")) {
            session.reset();
            showMainMenu(chatId, session);
            return;
        }
        
        if (text.equals("/cancel")) {
            session.reset();
            reply(chatId, "⏹ <b>Action cancelled.</b> Returning to home.");
            showMainMenu(chatId, session);
            return;
        }

        if (text.equals("/logout")) {
            session.currentUser = null;
            session.reset();
            reply(chatId, "👋 <b>Logged out successfully.</b>");
            showMainMenu(chatId, session);
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
            case BOOKING_DATE_START:
            case BOOKING_DATE_END:
            case BOOKING_SELECT_ROOM:
                handleBookingFlow(chatId, session, text);
                break;
        }
    }

    private void showMainMenu(long chatId, UserSession session) {
        if (session.currentUser == null) {
            reply(chatId, "<b>🏨 Welcome to Smart Hotel!</b>\n\n" +
                          "Your personal assistant for booking luxury stays.\n\n" +
                          "<b>Commands:</b>\n" +
                          "🔑 /login - Access your account\n" +
                          "📝 /register - Create an account");
        } else {
            reply(chatId, "<b>───────── Main Menu ─────────</b>\n" +
                          "❯ /rooms - View Rooms\n" +
                          "❯ /book - Make a Booking\n" +
                          "❯ /bookings - My Bookings\n" +
                          "❯ /invoices - My Invoices\n" +
                          "❯ /logout - Log Out\n" +
                          "<b>─────────────────────────────</b>");
        }
    }

    private void handleNoneState(long chatId, UserSession session, String text) {
        if (text.equals("/login")) {
            session.state = State.LOGIN_USERNAME;
            reply(chatId, "👤 <b>LOGIN</b>\nPlease enter your <b>Username</b>:");
        } else if (text.equals("/register")) {
            session.state = State.REGISTER_USERNAME;
            reply(chatId, "📝 <b>REGISTRATION</b>\nLet's get started. What's your <b>Username</b>?");
        } else if (text.equals("/book")) {
            if (session.currentUser == null) {
                reply(chatId, "⚠️ <b>Wait!</b> You need to /login first to make a booking.");
            } else {
                session.state = State.BOOKING_DATE_START;
                reply(chatId, "📅 <b>NEW BOOKING</b>\nWhen would you like to arrive?\n\n<i>Format: YYYY-MM-DD (e.g., 2026-03-05)</i>");
            }
        } else if (text.equals("/rooms")) {
            if (session.currentUser == null) {
                reply(chatId, "⚠️ <b>Wait!</b> You need to /login first to view our rooms.");
            } else {
                showRooms(chatId);
            }
        } else if (text.equals("/bookings")) {
            if (session.currentUser == null) {
                reply(chatId, "⚠️ Please /login first.");
            } else {
                showUserBookings(chatId, session);
            }
        } else if (text.equals("/invoices")) {
            if (session.currentUser == null) {
                reply(chatId, "⚠️ Please /login first.");
            } else {
                showUserInvoices(chatId, session);
            }
        } else {
            reply(chatId, "🤔 <b>I didn't quite get that.</b>\nTry /start to see the menu!");
        }
    }

    private void showUserBookings(long chatId, UserSession session) {
        try {
            List<Booking> bookings = bookingService.getUserBookings(session.currentUser.getId());
            if (bookings.isEmpty()) {
                reply(chatId, "📭 <b>You have no bookings yet.</b>\nTry /book to start one!");
                return;
            }

            StringBuilder msg = new StringBuilder("📅 <b>YOUR BOOKINGS</b>\n\n");
            for (Booking b : bookings) {
                String statusEmoji = b.getStatus() == Booking.BookingStatus.PENDING ? "⏳" : 
                                   b.getStatus() == Booking.BookingStatus.ACTIVE ? "✅" : "❌";
                msg.append(String.format("<b>#%d</b> | Room %s\n" +
                                       "📅 %s to %s\n" +
                                       "Status: %s <b>%s</b>\n" +
                                       "Total: <b>$%s</b>\n\n",
                           b.getId(), b.getRoomNumber(), 
                           b.getCheckInDate(), b.getCheckOutDate(),
                           statusEmoji, b.getStatus(), b.getTotalPrice()));
            }
            reply(chatId, msg.toString());
        } catch (SQLException e) {
            reply(chatId, "⚠️ <b>Error:</b> Could not fetch bookings.");
        }
    }

    private void showUserInvoices(long chatId, UserSession session) {
        try {
            List<model.entities.Invoice> invoices = bookingService.getUserInvoices(session.currentUser.getId());
            if (invoices.isEmpty()) {
                reply(chatId, "🧾 <b>No invoices found.</b>\nInvoices are generated when your booking is approved.");
                return;
            }

            StringBuilder msg = new StringBuilder("🧾 <b>YOUR INVOICES</b>\n\n");
            for (model.entities.Invoice inv : invoices) {
                msg.append(String.format("<b>%s</b>\n" +
                                       "Room %s | %s nights\n" +
                                       "Total: <b>$%s</b>\n" +
                                       "Status: ✅ <b>PAID</b>\n\n",
                           inv.getInvoiceNumber(), inv.getRoomNumber(), 
                           inv.getNights(), inv.getTotalAmount()));
            }
            reply(chatId, msg.toString());
        } catch (SQLException e) {
            reply(chatId, "⚠️ <b>Error:</b> Could not fetch invoices.");
        }
    }

    private void handleLoginFlow(long chatId, UserSession session, String text) {
        if (session.state == State.LOGIN_USERNAME) {
            session.tempData.put("username", text);
            session.state = State.LOGIN_PASSWORD;
            reply(chatId, "🔐 <b>Password:</b>\nEnter your password for " + text + ":");
        } else if (session.state == State.LOGIN_PASSWORD) {
            try {
                User user = userService.login(session.tempData.get("username"), text);
                if (user != null) {
                    // Restrict admin login in telegram
                    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                        session.state = State.NONE;
                        reply(chatId, "❌ <b>Login Restricted!</b>\nAdmin accounts are not allowed to use the Telegram bot. Please login via the management console.");
                        showMainMenu(chatId, session);
                        return;
                    }
                    session.currentUser = user;
                    // Auto-link Telegram ID if not set
                    if (user.getTelegramChatId() == null || user.getTelegramChatId() != chatId) {
                        userService.updateTelegramChatId(user.getId(), chatId);
                        user.setTelegramChatId(chatId);
                    }
                    session.state = State.NONE;
                    reply(chatId, "✅ <b>Login Successful!</b>\nWelcome back, <b>" + user.getUsername() + "</b>!\n\nReady to /book a room?");
                    showMainMenu(chatId, session);
                } else {
                    session.state = State.NONE;
                    reply(chatId, "❌ <b>Login Failed.</b>\nInvalid credentials. Try /login again.");
                    showMainMenu(chatId, session);
                }
            } catch (Exception e) {
                reply(chatId, "⚠️ <b>Error:</b> " + e.getMessage());
                session.state = State.NONE;
            }
        }
    }

    private void handleRegisterFlow(long chatId, UserSession session, String text) {
        switch (session.state) {
            case REGISTER_USERNAME:
                session.tempData.put("username", text);
                session.state = State.REGISTER_EMAIL;
                reply(chatId, "📧 <b>Email Address:</b>\n(Note: Must end with @gmail.com)");
                break;
            case REGISTER_EMAIL:
                session.tempData.put("email", text);
                session.state = State.REGISTER_PHONE;
                reply(chatId, "📞 <b>Phone Number:</b>\nEnter your contact number:");
                break;
            case REGISTER_PHONE:
                session.tempData.put("phone", text);
                session.state = State.REGISTER_PASSWORD;
                reply(chatId, "🔒 <b>Create Password:</b>\nMust have 8+ chars, 1 Upper, 1 Lower, 1 Digit, 1 Special.");
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
                        reply(chatId, "✨ <b>Account Created!</b>\nYou're all set. Now please /login.");
                        showMainMenu(chatId, session);
                    } else {
                        reply(chatId, "❌ <b>Registration failed.</b>\nPlease try /register again.");
                        session.state = State.NONE;
                        showMainMenu(chatId, session);
                    }
                } catch (Exception e) {
                    reply(chatId, "⚠️ <b>Error:</b> " + e.getMessage() + "\nTry /register again.");
                    session.state = State.NONE;
                }
                break;
        }
    }

    private void handleBookingFlow(long chatId, UserSession session, String text) {
        try {
            if (session.state == State.BOOKING_DATE_START) {
                LocalDate date = LocalDate.parse(text);
                if (date.isBefore(LocalDate.now())) throw new IllegalArgumentException("Date cannot be in the past!");
                session.tempData.put("checkin", text);
                session.state = State.BOOKING_DATE_END;
                reply(chatId, "📅 <b>Departure:</b>\nEnter Check-out Date (YYYY-MM-DD):");
                
            } else if (session.state == State.BOOKING_DATE_END) {
                LocalDate date = LocalDate.parse(text);
                LocalDate checkin = LocalDate.parse(session.tempData.get("checkin"));
                if (!date.isAfter(checkin)) throw new IllegalArgumentException("Check-out must be after check-in!");
                
                session.tempData.put("checkout", text);
                
                // Show available rooms
                List<Room> rooms = roomService.getAvailableRoomsByDate(checkin, date, 1);
                if (rooms.isEmpty()) {
                    reply(chatId, "😔 <b>Sorry!</b> No rooms available for those dates.\nTry different dates with /book.");
                    session.state = State.NONE;
                    return;
                }
                
                StringBuilder msg = new StringBuilder("🛌 <b>SELECT YOUR ROOM</b>\n\n");
                for (Room r : rooms) {
                    msg.append(String.format("<b>#%s</b> - %s\n💰 <b>$%s</b> /night\n\n", 
                               r.getRoomNumber(), r.getRoomTypeName(), r.getPricePerNight()));
                }
                msg.append("👉 <b>Reply with the Room Number</b> to confirm:");
                reply(chatId, msg.toString());
                session.state = State.BOOKING_SELECT_ROOM;
                
            } else if (session.state == State.BOOKING_SELECT_ROOM) {
                String roomNum = text;
                Room room = roomService.getRoomByNumber(roomNum);
                if (room == null) {
                    reply(chatId, "❌ <b>Invalid Room Number.</b>\nPlease choose from the list above or /cancel.");
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
                
                reply(chatId, "🎉 <b>BOOKING REQUEST SENT!</b>\n\n" +
                              "<b>Reference:</b> #" + booking.getId() + "\n" +
                              "<b>Status:</b> PENDING ⏳\n\n" +
                              "We will notify you once the admin approves your stay!");
                session.state = State.NONE;
            }
        } catch (DateTimeParseException e) {
            reply(chatId, "❌ <b>Invalid Format.</b>\nPlease use YYYY-MM-DD.");
        } catch (Exception e) {
            reply(chatId, "⚠️ <b>Error:</b> " + e.getMessage());
            session.state = State.NONE;
        }
    }

    private void showRooms(long chatId) {
        try {
            List<Room> rooms = roomService.getAllRooms(100, 0);
            StringBuilder msg = new StringBuilder("🏨 <b>OUR ROOMS</b>\n\n");
            for (Room r : rooms) {
                String statusEmoji = r.getStatus() == Room.RoomStatus.AVAILABLE ? "✅" : "❌";
                msg.append(String.format("%s <b>Room %s</b> - %s\nPrice: <b>$%s</b>\n\n", 
                           statusEmoji, r.getRoomNumber(), r.getRoomTypeName(), r.getPricePerNight()));
            }
            msg.append("Ready to stay? /book now!");
            reply(chatId, msg.toString());
        } catch (SQLException e) {
            reply(chatId, "⚠️ <b>Error:</b> Could not fetch room list.");
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
