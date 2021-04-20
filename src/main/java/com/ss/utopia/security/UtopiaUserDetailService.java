package com.ss.utopia.security;

import com.ss.utopia.dao.UserRepository;
import com.ss.utopia.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.springframework.security.core.userdetails.User.withUsername;

@Service
public class UtopiaUserDetailService implements UserDetailsService {
  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;

  @Autowired
  public UtopiaUserDetailService(UserRepository userRepository, JwtProvider jwtProvider) {
    this.userRepository = userRepository;
    this.jwtProvider = jwtProvider;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username).orElseThrow(() ->
      new UsernameNotFoundException(String.format("User with username %s has not been found.", username)));

    return withUsername(user.getUsername())
      .password(user.getPassword())
      .authorities(user.getRole().toString())
      .accountExpired(false)
      .accountLocked(false)
      .credentialsExpired(false)
      .disabled(false)
      .build();
  }

  public Optional<UserDetails> loadUserByJwtToken(String jwtToken) {
    return Optional.of(
      withUsername(jwtProvider.getUsername(jwtToken))
        .authorities(jwtProvider.getRoles(jwtToken))
        .password("")
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(false)
        .build());
  }

  public Optional<UserDetails> loadUserByJwtTokenRSA256(String jwtToken) {
    Long id = jwtProvider.getUserId(jwtToken);
    User user = userRepository.findById(id).orElse(null);
    var role = user.getRole().getAuthority();
    if (null == user)
      return Optional.empty();
    else
      return Optional.of(
        withUsername(user.getUsername())
          .password("")
          .authorities(user.getRole().getAuthority())
          .accountExpired(false)
          .accountLocked(false)
          .credentialsExpired(false)
          .disabled(false)
          .build());
  }
}
