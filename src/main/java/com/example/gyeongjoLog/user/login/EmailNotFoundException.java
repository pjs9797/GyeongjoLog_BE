package com.example.gyeongjoLog.user.login;

import javax.naming.AuthenticationException;

public class EmailNotFoundException extends AuthenticationException {
    public EmailNotFoundException(String msg) {
        super(msg);
    }
}
