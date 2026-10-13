package com.projects.money_controll.service;

import com.projects.money_controll.dto.RegisterRequest;
import com.projects.money_controll.dto.UserResponse;
import com.projects.money_controll.model.User;
import com.projects.money_controll.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (users.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        User user = users.save(new User(request.username(), passwordEncoder.encode(request.password())));
        return new UserResponse(user.getId(), user.getUsername());
    }
}
