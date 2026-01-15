package com.hth.udecareer.security;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hth.udecareer.entities.User;
import com.hth.udecareer.repository.UserMetaRepository;
import com.hth.udecareer.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMetaRepository userMetaRepository;

    // Pattern to extract roles from WordPress serialized capabilities
    // Format: a:1:{s:13:"administrator";b:1;}
    private static final Pattern ROLE_PATTERN = Pattern.compile("\"([a-z_]+)\";b:1");

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) {
        // Support both email and username login
        final User user = userRepository
                .findByEmail(usernameOrEmail)
                .or(() -> userRepository.findByUsername(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email/username: " + usernameOrEmail));

        // Load user roles from wp_usermeta
        List<SimpleGrantedAuthority> authorities = loadUserAuthorities(user.getId());

        log.debug("User {} loaded with roles: {}", usernameOrEmail, authorities);

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }

    /**
     * Load user authorities from WordPress wp_usermeta table.
     * WordPress stores capabilities in serialized PHP format: a:1:{s:13:"administrator";b:1;}
     */
    private List<SimpleGrantedAuthority> loadUserAuthorities(Long userId) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        try {
            userMetaRepository.findByUserIdAndMetaKey(userId, "wp_capabilities")
                    .ifPresent(meta -> {
                        String capabilities = meta.getMetaValue();
                        if (capabilities != null && !capabilities.isEmpty()) {
                            Matcher matcher = ROLE_PATTERN.matcher(capabilities);
                            while (matcher.find()) {
                                String role = matcher.group(1).toUpperCase();
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                                log.debug("Added role: ROLE_{} for user {}", role, userId);
                            }
                        }
                    });
        } catch (Exception e) {
            log.warn("Failed to load authorities for user {}: {}", userId, e.getMessage());
        }

        return authorities;
    }
}
