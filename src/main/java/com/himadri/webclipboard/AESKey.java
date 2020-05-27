package com.himadri.webclipboard;

import lombok.Data;

@Data
public class AESKey {
    private final byte[] key;
    private final byte[] iv;
}
