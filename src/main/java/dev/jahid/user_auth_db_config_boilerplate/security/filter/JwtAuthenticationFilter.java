package dev.jahid.user_auth_db_config_boilerplate.security.filter;

import dev.jahid.user_auth_db_config_boilerplate.security.CustomUserDetails;
import dev.jahid.user_auth_db_config_boilerplate.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter( JwtUtil jwtUtil ) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal( HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain ) throws ServletException, IOException {

        final String authHeader = request.getHeader( "Authorization" );

        if ( authHeader == null || !authHeader.startsWith( "Bearer " ) ) {
            chain.doFilter( request, response );
            return;
        }

        final String token = authHeader.substring( 7 );
        CustomUserDetails userDetails = jwtUtil.extractUser( token );

        if ( userDetails != null && SecurityContextHolder.getContext().getAuthentication() == null ) {

            if ( jwtUtil.validateToken( token ) ) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken( userDetails, null, userDetails.getAuthorities() );

                authenticationToken.setDetails( new WebAuthenticationDetailsSource().buildDetails( request ) );
                SecurityContextHolder.getContext().setAuthentication( authenticationToken );
            }
        }
        chain.doFilter( request, response );
    }
}

