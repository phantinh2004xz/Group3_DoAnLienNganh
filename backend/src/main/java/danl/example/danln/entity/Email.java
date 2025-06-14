package danl.example.danln.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Email {
    private String fromAddress;
//    private String toAddress;
    private String subject;
    private String body;
}
