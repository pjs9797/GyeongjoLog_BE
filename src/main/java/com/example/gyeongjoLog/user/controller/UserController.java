package com.example.gyeongjoLog.user.controller;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.user.dto.EmailCheckDTO;
import com.example.gyeongjoLog.user.dto.EmailDto;
import com.example.gyeongjoLog.user.dto.UserDTO;
import com.example.gyeongjoLog.user.service.UserService;
import jakarta.validation.Valid;
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

    @PostMapping("/join")
    public ResponseEntity<APIResponse> join(@RequestBody UserDTO userDTO) {
        return new ResponseEntity<>(userService.join(userDTO), HttpStatus.OK);
    }

//    @PostMapping("/login")
//    public ResponseEntity<APIResponse> login(@RequestBody UserDTO userDTO) {
//        return new ResponseEntity<>(userService.login(userDTO), HttpStatus.OK);
//    }

    @PostMapping("/checkDuplicateEmail")
    public ResponseEntity<APIResponse> checkEmail(@RequestBody EmailDto emailDto) {
        return new ResponseEntity<>(userService.checkEmail(emailDto.getEmail()), HttpStatus.OK);
    }

    @PostMapping ("/sendAuthCode")
    public ResponseEntity<APIResponse> mailSend(@RequestBody @Valid EmailDto emailDto){
        return new ResponseEntity<>(userService.sendEmail(emailDto.getEmail()),HttpStatus.OK);
    }

    @PostMapping("/checkAuthCode")
    public ResponseEntity<APIResponse> AuthCheck(@RequestBody @Valid EmailCheckDTO emailCheckDto){
        Boolean Checked=userService.checkAuthNum(emailCheckDto.getEmail(),emailCheckDto.getAuthNum());
        if(Checked){
            return new ResponseEntity<>(APIResponse.builder().resultCode("200").resultMessage("인증 성공").build(),HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(APIResponse.builder().resultCode("401").resultMessage("인증 실패").build(),HttpStatus.OK);
        }
    }

    @PostMapping("/saveNewPw")
    public ResponseEntity<APIResponse> saveNewPw(@RequestBody UserDTO userDTO) {
        return new ResponseEntity<>(userService.saveNewPw(userDTO), HttpStatus.OK);
    }
}
