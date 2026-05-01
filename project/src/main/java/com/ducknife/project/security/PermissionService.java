package com.ducknife.project.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.modules.user.User;
import com.ducknife.project.modules.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service("perm")
@RequiredArgsConstructor
public class PermissionService {
    private final UserRepository userRepository;

    public boolean canUpdateUser(Long userId, Authentication authentication) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            boolean isOwner = userId.equals(userDetails.getUserId());
            boolean isPrivilege = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                                    || a.getAuthority().equals("ROLE_COLLABORATOR"));
            
            return isOwner || isPrivilege;
        }
        return false;
    }
}
