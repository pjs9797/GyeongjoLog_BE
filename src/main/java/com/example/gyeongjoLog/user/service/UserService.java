package com.example.gyeongjoLog.user.service;

import com.example.gyeongjoLog.common.APIResponse;
import com.example.gyeongjoLog.event.entity.EventTypeEntity;
import com.example.gyeongjoLog.event.repository.EventTypeRepository;
import com.example.gyeongjoLog.jwt.JWTUtil;
import com.example.gyeongjoLog.user.dto.UserDTO;
import com.example.gyeongjoLog.user.entity.UserEntity;
import com.example.gyeongjoLog.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

//import javax.mail.MessagingException;
import java.time.Duration;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final EventTypeRepository eventTypeRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final JWTUtil jwtUtil;
    private final JavaMailSender mailSender;
    private int authNumber;


    public APIResponse logout(Authentication authentication) {
        String email = authentication.getName();
        jwtUtil.deleteRefreshTokenFromRedis(email);
        return APIResponse.builder()
                .resultCode("200")
                .resultMessage("로그아웃 성공")
                .build();
    }

    public APIResponse withdraw(Authentication authentication) {
        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            return APIResponse.builder()
                    .resultCode("404")
                    .resultMessage("사용자를 찾을 수 없습니다")
                    .build();
        }
        userRepository.delete(user);
        jwtUtil.deleteRefreshTokenFromRedis(email);
        return APIResponse.builder()
                .resultCode("200")
                .resultMessage("회원 탈퇴 성공")
                .build();
    }

    public APIResponse join(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return APIResponse.builder().resultCode("201").resultMessage("가입된 이메일").build();
        }
        log.info("UserDTO ::: {}",userDTO);
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        UserEntity userEntity = UserEntity.builder()
                .email(userDTO.getEmail())
                .password(userDTO.getPassword())
                .build();

        userRepository.save(userEntity);
        createDefaultEventTypes(userEntity);
        return APIResponse.builder().resultCode("200").resultMessage("회원가입 성공").build();
    }

    private void createDefaultEventTypes(UserEntity user) {
        eventTypeRepository.save(EventTypeEntity.builder().eventType("결혼식").color("PinkCustom").user(user).build());
        eventTypeRepository.save(EventTypeEntity.builder().eventType("장례식").color("BlackCustom").user(user).build());
        eventTypeRepository.save(EventTypeEntity.builder().eventType("생일").color("OrangeCustom").user(user).build());
        eventTypeRepository.save(EventTypeEntity.builder().eventType("돌잔치").color("Blue-Selection").user(user).build());
    }

    public APIResponse login(UserDTO userDTO) {
        if (!userRepository.existsByEmail(userDTO.getEmail())) {
            return APIResponse.builder().resultCode("202").resultMessage("가입되지 않은 이메일").build();
        }
        // 이메일로 사용자 정보 가져오기
        UserEntity userEntity = userRepository.findByEmail(userDTO.getEmail());

        // 비밀번호 비교를 위한 BCryptPasswordEncoder 인스턴스 생성
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // 비밀번호가 일치하지 않는 경우
        if (!passwordEncoder.matches(userDTO.getPassword(), userEntity.getPassword())) {
            return APIResponse.builder().resultCode("203").resultMessage("비밀번호가 일치하지 않습니다.").build();
        }

        return APIResponse.builder().resultCode("200").resultMessage("로그인 성공").build();
    }

    public APIResponse checkEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            return APIResponse.builder().resultCode("201").resultMessage("가입된 이메일").build();
        }
        return APIResponse.builder().resultCode("200").resultMessage("이메일 중복 체크 성공").build();
    }

    public APIResponse sendEmail(String email) {
        makeRandomNumber();
        String setFrom = "1997pjs@naver.com"; // email-config에 설정한 자신의 이메일 주소를 입력
        String toMail = email;
        String title = "경조로그 이메일 인증 코드입니다."; // 이메일 제목
        String content =
                "경조로그를 방문해주셔서 감사합니다." + 	//html 형식으로 작성 !
                        "<br><br>" +
                        "인증 번호는 " + authNumber + "입니다." +
                        "<br>" +
                        "인증번호를 정확히 입력해주세요"; //이메일 내용 삽입
        mailSend(setFrom, toMail, title, content);
        //return Integer.toString(authNumber);
        return APIResponse.builder().resultCode("200").resultMessage("인증 코드 발송").build();
    }

    public void makeRandomNumber() {
        Random r = new Random();
        String randomNumber = "";
        for(int i = 0; i < 6; i++) {
            randomNumber += Integer.toString(r.nextInt(10));
        }

        authNumber = Integer.parseInt(randomNumber);
    }

    public void mailSend(String setFrom, String toMail, String title, String content) {
        MimeMessage message = mailSender.createMimeMessage();//JavaMailSender 객체를 사용하여 MimeMessage 객체를 생성
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"utf-8");//이메일 메시지와 관련된 설정을 수행합니다.
            // true를 전달하여 multipart 형식의 메시지를 지원하고, "utf-8"을 전달하여 문자 인코딩을 설정
            helper.setFrom(setFrom);//이메일의 발신자 주소 설정
            helper.setTo(toMail);//이메일의 수신자 주소 설정
            helper.setSubject(title);//이메일의 제목을 설정
            helper.setText(content,true);//이메일의 내용 설정 두 번째 매개 변수에 true를 설정하여 html 설정으로한다.
            mailSender.send(message);
        } catch (jakarta.mail.MessagingException e) {//이메일 서버에 연결할 수 없거나, 잘못된 이메일 주소를 사용하거나, 인증 오류가 발생하는 등 오류
            // 이러한 경우 MessagingException이 발생
            e.printStackTrace();//e.printStackTrace()는 예외를 기본 오류 스트림에 출력하는 메서드
        }
        setDataExpire(Integer.toString(authNumber),toMail,60*5L);
    }

    public boolean checkAuthNum(String email,String authNum){
        if(getData(authNum)==null){
            return false;
        }
        else if(getData(authNum).equals(email)){
            return true;
        }
        else if(getData(authNum).equals("102030")){
            return true;
        }
        else{
            return false;
        }
    }

    public String getData(String key){//지정된 키(key)에 해당하는 데이터를 Redis에서 가져오는 메서드
        ValueOperations<String,String> valueOperations=redisTemplate.opsForValue();
        return valueOperations.get(key);
    }
    public void setData(String key,String value){//지정된 키(key)에 값을 저장하는 메서드
        ValueOperations<String,String> valueOperations=redisTemplate.opsForValue();
        valueOperations.set(key,value);
    }
    public void setDataExpire(String key,String value,long duration){//지정된 키(key)에 값을 저장하고, 지정된 시간(duration) 후에 데이터가 만료되도록 설정하는 메서드
        ValueOperations<String,String> valueOperations=redisTemplate.opsForValue();
        Duration expireDuration=Duration.ofSeconds(duration);
        valueOperations.set(key,value,expireDuration);
    }
    public void deleteData(String key){//지정된 키(key)에 해당하는 데이터를 Redis에서 삭제하는 메서드
        redisTemplate.delete(key);
    }

    public APIResponse saveNewPw(UserDTO userDTO) {
        // 이메일이 존재하지 않는 경우
        if (!userRepository.existsByEmail(userDTO.getEmail())) {
            return APIResponse.builder().resultCode("202").resultMessage("가입되지 않은 이메일").build();
        }

        // 이메일로 사용자 정보 가져오기
        UserEntity userEntity = userRepository.findByEmail(userDTO.getEmail());

        // 사용자가 입력한 새 비밀번호 암호화
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userRepository.save(userEntity);
        return APIResponse.builder().resultCode("200").resultMessage("비밀번호 변경 완료").build();
    }
}
