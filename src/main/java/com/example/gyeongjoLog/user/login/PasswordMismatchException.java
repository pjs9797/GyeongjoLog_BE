package com.example.gyeongjoLog.user.login;

import javax.naming.AuthenticationException;

public class PasswordMismatchException extends AuthenticationException {
    public PasswordMismatchException(String msg) {
        super(msg);
    }
}
