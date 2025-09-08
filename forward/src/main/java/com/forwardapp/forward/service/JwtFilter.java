package com.forwardapp.forward.service;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserDetailsService uds;

  public JwtFilter(JwtService jwtService, UserDetailsService uds) {
    this.jwtService = jwtService; this.uds = uds;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String hdr = req.getHeader("Authorization");
    if (hdr == null || !hdr.startsWith("Bearer ")) { chain.doFilter(req, res); return; }
    String token = hdr.substring(7);
    String email;
    try { email = jwtService.extractUsername(token); } catch (Exception e) { chain.doFilter(req, res); return; }

    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      try {
        UserDetails ud = uds.loadUserByUsername(email);
        if (jwtService.isTokenValid(token, ud)) {
          var auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
          auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (UsernameNotFoundException ignore) {}
    }
    chain.doFilter(req, res);
  }
}