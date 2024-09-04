package com.example.gyeongjoLog.user.controller;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.user.dto.UserDTO;
import com.example.gyeongjoLog.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/aa")
    public String aa() {
        return "aaa";
    }
    @PostMapping("/join")
    public ResponseEntity<APIResponse> join(@RequestBody UserDTO userDTO) {
        return new ResponseEntity<>(userService.join(userDTO), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse> login(@RequestBody UserDTO userDTO) {
        return new ResponseEntity<>(userService.login(userDTO), HttpStatus.OK);
    }

    @GetMapping("/emailCheck")
    public ResponseEntity<APIResponse> checkEmail(@RequestParam String email) {
        return new ResponseEntity<>(userService.checkEmail(email), HttpStatus.OK);
    }
}
