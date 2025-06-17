package com.yourcompany.onlineexam.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Intake {
    @DocumentId
    private String id;
    private String code; // intake_code
    private String name;
}