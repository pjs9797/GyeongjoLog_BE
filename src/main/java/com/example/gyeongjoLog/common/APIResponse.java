package com.example.gyeongjoLog.common;

import lombok.*;

@Getter
@Builder
//@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class APIResponse {
    private String resultCode;
    private String resultMessage;
    private Object data;
}
