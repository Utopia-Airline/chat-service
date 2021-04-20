package com.ss.utopia.service;

import com.ss.utopia.dao.UserRepository;
import com.ss.utopia.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
  private final UserRepository userRepository;
  private final AuthenticationManager authenticationManager;

  @Autowired
  public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager) {
    this.userRepository = userRepository;
    this.authenticationManager = authenticationManager;
  }

  public Optional<User> signin(String username, String password) throws AuthenticationException {
    LOGGER.info("New user attempting to sign in");
    Optional<User> user = userRepository.findByUsername(username);
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    return user;
  }
}
