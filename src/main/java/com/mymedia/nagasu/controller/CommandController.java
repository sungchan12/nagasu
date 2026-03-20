package com.mymedia.nagasu.controller;

import com.mymedia.nagasu.service.PrivateSessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/command")
public class CommandController {

    private final String privateCommand;
    private final PrivateSessionService sessionService;

    public CommandController(
            @Value("${private.command}") String privateCommand,
            PrivateSessionService sessionService
    ) {
        this.privateCommand = privateCommand;
        this.sessionService = sessionService;
    }

    @PostMapping
    public Map<String, Object> executeCommand(
            @RequestBody Map<String, String> body,
            @CookieValue(value = "_sid", required = false) String currentToken, // 현재 세션 쿠키 (_sid), 없으면 null
            HttpServletResponse response
    ) {
        // 요청 body에서 "command" 값 추출 (없으면 빈 문자열)
        var command = body.getOrDefault("command", "");

        // 설정된 비밀 명령어와 다르면 아무 동작 없이 ok 반환
        // (명령어 존재 여부를 외부에 노출하지 않기 위해 동일한 응답 사용)
        if (!privateCommand.equals(command)) {
            return Map.of("status", "ok");
        }

        // 이미 private mode 상태면 → 비활성화 (토글)
        if (sessionService.isValidSession(currentToken)) {
            sessionService.invalidateSession(currentToken); // 서버에서 세션 삭제
            var cookie = new Cookie("_sid", "");
            cookie.setHttpOnly(true); // JS에서 쿠키 접근 불가
            cookie.setPath("/");
            cookie.setMaxAge(0); // maxAge=0 → 즉시 쿠키 삭제
            response.addCookie(cookie);
            return Map.of("status", "ok", "privateMode", false);
        }

        // private mode 비활성화 상태면 → 활성화
        var token = sessionService.createSession(); // 새 세션 토큰 생성
        var cookie = new Cookie("_sid", token);
        cookie.setHttpOnly(true); // JS에서 쿠키 접근 불가
        cookie.setPath("/");
        cookie.setMaxAge(-1); // maxAge=-1 → 브라우저 닫으면 자동 만료되는 세션 쿠키
        response.addCookie(cookie);
        return Map.of("status", "ok", "privateMode", true);
    }
}