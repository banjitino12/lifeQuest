package com.lifequest.auth.service;

import com.lifequest.auth.model.CurrentUserPrincipal;
import com.lifequest.common.exception.BusinessException;
import com.lifequest.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public CurrentUserPrincipal requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUserPrincipal principal)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return principal;
    }

    public Long requireCurrentUserId() {
        return requireCurrentUser().userId();
    }
}
