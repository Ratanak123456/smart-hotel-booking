import controller.AuthController;

public class App {
    public static void main(String[] args) {
        try {
            AuthController authController = new AuthController();
            authController.showLoginMenu();
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}