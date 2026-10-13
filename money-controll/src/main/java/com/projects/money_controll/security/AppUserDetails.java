package com.projects.money_controll.security;

import com.projects.money_controll.model.User;
import lombok.Getter;
import org.springframework.security.core.authority.AuthorityUtils;

/** Authenticated principal carrying the user id, so controllers don't need an extra lookup. */
@Getter
public class AppUserDetails extends org.springframework.security.core.userdetails.User {

    private final Long id;

    public AppUserDetails(User user) {
        super(user.getUsername(), user.getPasswordHash(), AuthorityUtils.createAuthorityList("ROLE_USER"));
        this.id = user.getId();
    }
}
