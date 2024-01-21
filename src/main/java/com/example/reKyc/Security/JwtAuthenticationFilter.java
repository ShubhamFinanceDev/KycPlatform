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
    private Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);


    @Autowired
    private UserDetailsService userDetailsService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        if (request.getRequestURI().startsWith("/shubham")) {

            String requestHeader = request.getHeader("Authorization");
            logger.info(" Header :  {}" + requestHeader);
            String username = null;
            String token = null;
            HashMap<String, Object> errorMsg = new HashMap<>();
            response.setContentType("application/json");

            if (requestHeader != null && requestHeader.startsWith("shubham")) {
                //looking good
                token = requestHeader.substring(8);
                try {

                    username = this.jwtHelper.getUsernameFromToken(token);

                } catch (IllegalArgumentException e) {
                    logger.info("Illegal Argument while fetching the username !!");
                    e.printStackTrace();
                    errorMsg.put("code", "1111");
                    errorMsg.put("msg", "Illegal Argument while fetching the username !!");
                    response.getWriter().write(objectMapper.writeValueAsString(errorMsg));
                    response.getWriter().flush();
                    return;

                } catch (ExpiredJwtException e) {
                    logger.info("Given jwt token is expired !!");
                    e.printStackTrace();
                    errorMsg.put("code", "1111");
                    errorMsg.put("msg", "Given jwt token is expired !!");
                    response.getWriter().write(objectMapper.writeValueAsString(errorMsg));
                    response.getWriter().flush();
                    return;

                } catch (MalformedJwtException e) {
                    logger.info("Some changed has done in token !! Invalid Token");
                    e.printStackTrace();
                    errorMsg.put("code", "1111");
                    errorMsg.put("msg", "Some changed has done in token !! Invalid Token");
                    response.getWriter().write(objectMapper.writeValueAsString(errorMsg));
                    response.getWriter().flush();
                    return;

                } catch (Exception e) {
                    e.printStackTrace();

                }


            } else {

                logger.info("Invalid Header Value !! ");
                errorMsg.put("code", "1111");
                errorMsg.put("msg", "Invalid Header Value !!");
                response.getWriter().write(objectMapper.writeValueAsString(errorMsg));
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
                    logger.info("Validation fails !!");
                }


            }
        }

        filterChain.doFilter(request, response);


    }
}

