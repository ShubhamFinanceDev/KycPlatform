package com.example.reKyc.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtHelper jwtHelper;
    private final Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);


    @Autowired
    private UserDetailsService userDetailsService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


            String requestHeader = request.getHeader("Authorization");
            logInfo(" Header :  {}" + requestHeader);
            String username = null;
            String token ;
            HashMap<String, Object> errorMsg = new HashMap<>();
            response.setContentType("application/json");

            if (requestHeader != null && requestHeader.startsWith("shubham")) {
                //looking good
                token = requestHeader.substring(8);
                try {

                    username = this.jwtHelper.getUsernameFromToken(token);

                } catch (IllegalArgumentException e) {
                    logInfo("Illegal Argument while fetching the username !!");
                    logger.error(e.toString());
                    errorMsg.put("code", "1111");
                    errorMsg.put("msg", "Illegal Argument while fetching the username !!");
                    response.getWriter().write(objectMapper.writeValueAsString(errorMsg));
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().flush();
                    return;

                } catch (ExpiredJwtException e) {
                    logInfo("Session has been expired !!");
                    logger.error(e.toString());
                    errorMsg.put("code", "1111");
                    errorMsg.put("msg", "Session has been expired !!");
                    response.getWriter().write(objectMapper.writeValueAsString(errorMsg));
                    response.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);
                    response.getWriter().flush();
                    return;

                } catch (MalformedJwtException e) {
                    logInfo("Some changed has done in token !! Invalid Token");
                    logger.error(e.toString());
                    errorMsg.put("code", "1111");
                    errorMsg.put("msg", "Some changed has done in token !! Invalid Token");
                    response.getWriter().write(objectMapper.writeValueAsString(errorMsg));
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().flush();
                    return;

                } catch (Exception e) {
                    logger.error(e.toString());


                }


            } else {

                logInfo("Authorisation required !! ");
                errorMsg.put("code", "1111");
                errorMsg.put("msg", "Authorisation required !!");
                response.getWriter().write(objectMapper.writeValueAsString(errorMsg));
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().flush();

                return;
            }


            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {


                //fetch user detail from username
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                Boolean validateToken = this.jwtHelper.validateToken(token, userDetails);
                if (validateToken) {

                    //set the authentication
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);


                } else {
                    logInfo("Validation fails !!");
                }


            }

        filterChain.doFilter(request, response);


    }

    private void logInfo(String msg)
    {
        logger.info(msg);

    }
}

