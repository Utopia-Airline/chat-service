package com.ss.utopia.controller;

import com.ss.utopia.dao.UserRepository;
import com.ss.utopia.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserRepository userRepository;

  @Autowired
  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping
  public List<User> getAll() {
    return userRepository.findAll();
  }
}
