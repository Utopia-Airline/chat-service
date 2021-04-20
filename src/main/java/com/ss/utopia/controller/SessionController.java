package com.ss.utopia.controller;

import com.ss.utopia.dto.LoginDto;
import com.ss.utopia.model.User;
import com.ss.utopia.security.JwtProvider;
import com.ss.utopia.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/session")
public class SessionController {
  private final static Logger LOGGER = LoggerFactory.getLogger(SessionController.class);
  private final AuthService authService;
  private final JwtProvider jwtProvider;

  @Autowired
  public SessionController(AuthService authService, JwtProvider jwtProvider) {
    this.authService = authService;
    this.jwtProvider = jwtProvider;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public void login(@RequestBody @Valid LoginDto loginDto, HttpServletResponse response) {
    final User user = authService.signin(loginDto.getUsername(), loginDto.getPassword())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login Failed"));
    final Cookie sessionCookie = jwtProvider.createSessionCookie(user)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create a session cookie"));
    LOGGER.info("cookie: {}", sessionCookie.getValue());
    response.addCookie(sessionCookie);
  }
}
