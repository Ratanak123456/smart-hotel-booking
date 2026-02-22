package model.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class User {
    private int id;
    private String username;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private String role; // 'USER' or 'ADMIN'
    private Timestamp deletedAt;
}