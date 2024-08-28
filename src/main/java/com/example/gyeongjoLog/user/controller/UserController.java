package com.example.gyeongjoLog.user.controller;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.user.dto.UserDTO;
import com.example.gyeongjoLog.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/join")
    public APIResponse join(@RequestBody UserDTO user) {
        return userService.join(user);
    }

    @GetMapping("/emailCheck")
    public APIResponse checkEmail(@RequestParam String email) {
        return userService.checkEmail(email);
    }
}
