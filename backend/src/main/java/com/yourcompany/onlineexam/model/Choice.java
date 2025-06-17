package com.yourcompany.onlineexam.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Choice {
    private String text; // choice_text
    private Boolean isCorrect; // corrected
}