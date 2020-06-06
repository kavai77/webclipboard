package com.himadri.webclipboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clipboard {
    private String user;
    private byte[] encrypted;
    private long date;
}
