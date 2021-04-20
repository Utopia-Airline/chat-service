package com.ss.utopia.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.utopia.dto.SessionCookieDto;
import com.ss.utopia.model.User;
import com.ss.utopia.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtProvider {
  private final String ROLES_KEY = "roles";
  private final String secretKey;
  private final long validityInMilliseconds;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private KeyPair keyPair;

  @Autowired
  public JwtProvider(@Value("${security.jwt.token.secret-key}") String secretKey,
                     @Value("${security.jwt.token.expiration}") long validityInMilliseconds,
                     @Value("${AUTH_JWT_PRIVATE_KEY}") final String privateKey,
                     @Value("${AUTH_JWT_PUBLIC_KEY}") final String publicKey) {
    this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    this.validityInMilliseconds = validityInMilliseconds;
    this.keyPair = generateKeyPair(publicKey, privateKey);
  }

  public String createToken(String username, List<UserRole> roles) {
    Claims claims = Jwts.claims().setSubject(username);
    claims.put(ROLES_KEY, roles.stream().map(role ->
      new SimpleGrantedAuthority(role.getAuthority()))
      .filter(Objects::nonNull).collect(Collectors.toList()));
    Date now = new Date();
    Date expiresAt = new Date(now.getTime() + validityInMilliseconds);
    return Jwts.builder()
      .setClaims(claims)
      .setIssuedAt(now)
      .setExpiration(expiresAt)
      .signWith(SignatureAlgorithm.HS256, secretKey)
      .compact();
  }

  public boolean isValidToken(String token) {
    try {
      Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public String getUsername(String token) {
    return Jwts.parser().setSigningKey(secretKey)
      .parseClaimsJws(token).getBody().getSubject();
  }

  public List<GrantedAuthority> getRoles(String token) {
    List<Map<String, String>> roleClaims = Jwts.parser()
      .setSigningKey(secretKey).parseClaimsJws(token).getBody()
      .get(ROLES_KEY, List.class);
    return roleClaims.stream().map(roleClaim -> new SimpleGrantedAuthority(
      roleClaim.get("authority"))).collect(Collectors.toList());
  }

  public Optional<String> parseToken(String token) {
    try {
      final String decoded = decode(token);
      return Optional.of(decoded);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Optional<Cookie> createSessionCookie(final User user) {
    final String token = createToken(user.getUsername(), Arrays.asList(user.getRole()));
    final Cookie sessionCookie = new Cookie("session", encode(token));
    sessionCookie.setPath("/");
    sessionCookie.setSecure(false);
//    sessionCookie.setDomain("/");
    sessionCookie.setHttpOnly(true);
    return Optional.of(sessionCookie);
  }

  private String encode(final String str) {
    return Base64.getEncoder().encodeToString(str.getBytes());
  }

  private String decode(final String str) {
    return new String(Base64.getDecoder().decode(str));
  }

  /* =======================================private public key======================================= */
  public Long getUserId(String token) {
    return Long.parseLong(Jwts.parser().setSigningKey(keyPair.getPublic())
      .parseClaimsJws(token).getBody().getSubject(), 16);
  }

  public boolean isValidTokenRS256(String token) {
    try {
      final Claims claims = Jwts.parser().setSigningKey(keyPair.getPublic()).parseClaimsJws(token).getBody();
      if (claims.getExpiration().before(new Date()))
        return false;
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public Optional<String> parseTokenRS256(String token) {
    try {
      final String decoded = decode(token);
      final SessionCookieDto session = objectMapper.readValue(decoded, SessionCookieDto.class);
      return Optional.of(session.getJwt());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private PrivateKey generatePrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
    final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    privateKey = parseKey(privateKey);
    final byte[] decodedPrivate = Base64.getDecoder().decode(privateKey);
    final KeySpec keySpec = new PKCS8EncodedKeySpec(decodedPrivate);
    return keyFactory.generatePrivate(keySpec);
  }

  private PublicKey generatePublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
    final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    publicKey = parseKey(publicKey);
    final byte[] decodedPublic = Base64.getDecoder().decode(publicKey);
    final KeySpec keySpec = new X509EncodedKeySpec(decodedPublic);
    return keyFactory.generatePublic(keySpec);
  }

  private String parseKey(String key) {
    return key.replaceAll("-----BEGIN (.*?)-----", "").replaceAll("-----END (.*)----", "")
      .replaceAll("\r\n", "").replaceAll("\\\\n", "").replaceAll("\n", "").trim();
  }

  public KeyPair generateKeyPair(String publicKey, String privateKey) {
    try {
      return new KeyPair(generatePublicKey(publicKey), generatePrivateKey(privateKey));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      return null;
    }
  }
}
