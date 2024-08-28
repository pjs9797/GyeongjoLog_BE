package com.example.gyeongjoLog.common;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@RequiredArgsConstructor
public class APIResponse {
    private String resultCode;
    private String resultMessage;
    private Object data;

    public APIResponse(String resultCode, String resultMessage, Object data) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.data = data;
    }
    public static APIResponse createWithoutData(String resultCode, String resultMessage) {
        return new APIResponse(resultCode, resultMessage, null);
    }

    // Static factory method for data
    public static APIResponse createWithData(String resultCode, String resultMessage, Object data) {
        return new APIResponse(resultCode, resultMessage, data);
    }
}
