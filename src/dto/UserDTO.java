package dto;

public class UserDTO {
    private int id;
    private String username;
    private String email;
    private String phoneNumber;
    private String role;
    private Long telegramChatId;

    public UserDTO() {}

    public UserDTO(int id, String username, String email, String phoneNumber, String role, Long telegramChatId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.telegramChatId = telegramChatId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getTelegramChatId() { return telegramChatId; }
    public void setTelegramChatId(Long telegramChatId) { this.telegramChatId = telegramChatId; }

    @Override
    public String toString() {
        return String.format("User: %s (%s) - %s [Telegram ID: %s]", username, role, email, telegramChatId != null ? telegramChatId : "N/A");
    }
}
