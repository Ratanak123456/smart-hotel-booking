package model.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TelegramService {
    // BOT_TOKEN is required from @BotFather in Telegram
    private static final String BOT_TOKEN = "8731239997:AAElO0bjxNTomgdN42gvthqpjC8iPG9lpF4";
    private static final String API_URL = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";

    /**
     * Sends a message to a specific Telegram chat ID.
     * @param chatId The recipient's chat ID
     * @param message The message text
     * @return true if successful, false otherwise
     */
    public boolean sendMessage(long chatId, String message) {
        if (BOT_TOKEN.equals("YOUR_BOT_TOKEN_HERE")) {
            System.err.println("[TelegramService] Error: Bot token not configured.");
            return false;
        }

        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String jsonPayload = String.format("{\"chat_id\": %d, \"text\": \"%s\", \"parse_mode\": \"HTML\"}", 
                                               chatId, message.replace("\"", "\\\""));

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                return true;
            } else {
                System.err.println("[TelegramService] Failed to send message. HTTP Code: " + responseCode);
                return false;
            }
        } catch (Exception e) {
            System.err.println("[TelegramService] Error sending message: " + e.getMessage());
            return false;
        }
    }

    /**
     * Utility method to notify a user about a booking status change.
     */
    public void notifyBookingStatus(long chatId, String bookingRef, String status, String roomNumber) {
        String emoji = status.equalsIgnoreCase("ACTIVE") ? "✅" : (status.equalsIgnoreCase("CANCELLED") ? "❌" : "⏳");
        String statusText = status.equalsIgnoreCase("ACTIVE") ? "APPROVED" : status;
        
        String message = String.format(
            "<b>Booking Update %s</b>\n\n" +
            "Reference: #%s\n" +
            "Room: %s\n" +
            "Status: <b>%s</b>\n\n" +
            "Thank you for choosing our hotel!",
            emoji, bookingRef, roomNumber, statusText
        );
        sendMessage(chatId, message);
    }

    /**
     * Sends an invoice details notification.
     */
    public void notifyInvoice(long chatId, model.entities.Invoice invoice) {
        String message = String.format(
            "<b>🧾 INVOICE GENERATED</b>\n\n" +
            "Invoice No: <code>%s</code>\n" +
            "Guest: %s\n" +
            "Room: %s (%s)\n" +
            "Check-in: %s\n" +
            "Check-out: %s\n" +
            "Nights: %d\n\n" +
            "<b>Total Amount: $%s</b>\n" +
            "Status: <b>%s</b>\n\n" +
            "Enjoy your stay! 🏨",
            invoice.getInvoiceNumber(),
            invoice.getGuestName(),
            invoice.getRoomNumber(),
            invoice.getRoomTypeName(),
            invoice.getCheckInDate(),
            invoice.getCheckOutDate(),
            invoice.getNights(),
            invoice.getTotalAmount(),
            invoice.getInvoiceStatus()
        );
        sendMessage(chatId, message);
    }
}
