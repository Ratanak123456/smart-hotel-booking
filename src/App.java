import controller.AuthController;
import model.service.TelegramBot;

public class App {
    public static void main(String[] args) {
        // Start Telegram Bot in background
        Thread botThread = new Thread(new TelegramBot());
        botThread.setDaemon(true); // Ensure it closes when the app closes
        botThread.start();

        try {
            AuthController authController = new AuthController();
            authController.showLoginMenu();
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}