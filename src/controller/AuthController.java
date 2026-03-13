package controller;

import model.entities.User;
import model.service.UserService;
import view.UiUtils;
import view.UserPanel;

import java.sql.SQLException;
import java.util.Scanner;

public class AuthController {
    private UserService userService;
    private Scanner scanner;

    public AuthController() {
        this.userService = new UserService();
        this.scanner = new Scanner(System.in);
    }

    public void showLoginMenu() throws SQLException {
        while (true) {
            UiUtils.clearScreen();
            UiUtils.printBanner();
            UiUtils.printHeader("HOTEL RESERVATION SYSTEM");
            UiUtils.printMenu("Main Menu",
                    "1. Login",
                    "2. Register",
                    "3. Exit");

            System.out.print("Enter choice : ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleRegister();
                    break;
                case "3":
                    UiUtils.printMessage("Thank you for using our system. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    UiUtils.printError("Invalid option. Please try again.");
            }
        }
    }

    private void handleRegister() {
        while (true) {
            UiUtils.printHeader("USER REGISTRATION");
            System.out.println("(Leave any field empty to cancel)");

            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) return;

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) return;

            System.out.print("Phone Number: ");
            String phoneNumber = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            if (password.isEmpty()) return;

            try {
                boolean success = userService.register(username, email, phoneNumber, password);
                if (success) {
                    UiUtils.printSuccess("Registration successful! You can now login.");
                    return;
                } else {
                    UiUtils.printError("Registration failed. Please try again.");
                }
            } catch (Exception e) {
                UiUtils.printError("Registration error: " + e.getMessage());
                System.out.println("Please try again with valid information.");
            }
        }
    }

    private void handleLogin() throws SQLException {
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;

        while (attempts < MAX_ATTEMPTS) {
            UiUtils.printHeader("USER LOGIN (Attempt " + (attempts + 1) + " of " + MAX_ATTEMPTS + ")");
            System.out.println("(Leave any field empty to cancel)");

            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) return;

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            if (password.isEmpty()) return;

            try {
                User user = userService.login(username, password);
                if (user != null && (user.getRole().equals("USER") || user.getRole().equals("ADMIN"))) {
                    UiUtils.showLoadingAnimation("Authenticating user...");
                    UiUtils.printSuccess("Login successful! Welcome, " + user.getUsername() + " (" + user.getRole() + ")");

                    if (user.getRole().equals("ADMIN")) {
                        // Enter admin panel
                        view.AdminPanel adminPanel = new view.AdminPanel(user);
                        adminPanel.start();
                    } else {
                        // Enter user panel with booking features
                        UserPanel userPanel = new UserPanel(user);
                        userPanel.start();
                    }
                    return; // Return to main menu after logout
                } else {
                    attempts++;
                    if (attempts < MAX_ATTEMPTS) {
                        UiUtils.printError("Invalid username or password. You have " + (MAX_ATTEMPTS - attempts) + " attempts left.");
                    } else {
                        UiUtils.printError("Too many failed attempts. Returning to main menu.");
                    }
                }
            } catch (SQLException e) {
                UiUtils.printError("Database error: " + e.getMessage());
                return;
            } catch (Exception e) {
                UiUtils.printError("Login error: " + e.getMessage());
                return;
            }
        }
    }
}