package com.ss.utopia.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * this filter is used for all authenticated routes.
 * after user log in a jwt token is created as a cookie
 * for each subsequent request, the user will use that cookie to be authorized
 * each request will go through this filter first.
 * JwtTokenFilter first check if the user has the a cookie with key of "session"
 * if not it marks the user as unauthorized
 * if found it checks the validity of the token.
 * if invalid it marks the user as unauthorized
 * if valid it the user can get in
 */
public class JwtTokenFilter extends OncePerRequestFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenFilter.class);

  private final UtopiaUserDetailService utopiaUserDetailService;

  private final JwtProvider jwtProvider;

  @Autowired
  public JwtTokenFilter(UtopiaUserDetailService utopiaUserDetailService, JwtProvider jwtProvider) {
    this.utopiaUserDetailService = utopiaUserDetailService;
    this.jwtProvider = jwtProvider;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    LOGGER.info("Process request to check for a JSON Web Token");
    final Cookie[] cookies = request.getCookies();
//    && cookie.isHttpOnly()
    try {
      final Optional<Cookie> sessionCookie = Arrays.stream(cookies)
        .filter(cookie -> cookie.getName().matches("^session$")).findFirst();
      if (sessionCookie.isPresent()) {
        String parsedToken = jwtProvider.parseTokenRS256(sessionCookie.get().getValue()).orElseThrow(() -> new Exception());
        if (jwtProvider.isValidTokenRS256(parsedToken)) {
//          Optional<UserDetails> userDetails = utopiaUserDetailService.loadUserByJwtToken(parsedToken);
          Optional<UserDetails> userDetails = utopiaUserDetailService.loadUserByJwtTokenRSA256(parsedToken);
          userDetails.ifPresent(userDetail ->
            SecurityContextHolder.getContext().setAuthentication(
              new PreAuthenticatedAuthenticationToken(
                userDetail,
                "",
                userDetail.getAuthorities())));
        } else
          LOGGER.error("Token is not valid");
      }
    } catch (Exception e) {
      LOGGER.error("Not able to verify token");
    }
    filterChain.doFilter(request, response);
  }
}
