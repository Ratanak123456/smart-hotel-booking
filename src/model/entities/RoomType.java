package model.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomType {
    private int id;
    private String name; // 'Regular', 'Family', 'Suite', 'Deluxe'
}
