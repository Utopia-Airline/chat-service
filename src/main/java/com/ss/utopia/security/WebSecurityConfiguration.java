package com.ss.utopia.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
  private final UtopiaUserDetailService utopiaUserDetailService;
  private final JwtProvider jwtProvider;

  public WebSecurityConfiguration(UtopiaUserDetailService utopiaUserDetailService, JwtProvider jwtProvider) {
    this.utopiaUserDetailService = utopiaUserDetailService;
    this.jwtProvider = jwtProvider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(11);
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    super.configure(auth);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable();
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    http.authorizeRequests().antMatchers("/api/session/**", "/secured/room/**").permitAll();
    http.authorizeRequests().anyRequest().authenticated();
//    http.authorizeRequests().anyRequest().permitAll();
    http.addFilterBefore(new JwtTokenFilter(utopiaUserDetailService, jwtProvider),
      UsernamePasswordAuthenticationFilter.class);
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }
}
