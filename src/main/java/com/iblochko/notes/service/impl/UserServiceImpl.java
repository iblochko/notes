package com.iblochko.notes.service.impl;

import com.iblochko.notes.model.User;
import com.iblochko.notes.repository.UserRepository;
import com.iblochko.notes.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Primary
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public User saveUser(User user) {
        return repository.save(user);
    }

    @Override
    public User getUser(String username) {
        return repository.findByUsername(username);
    }

    @Override
    public User updateUser(User user) {
        return repository.save(user);
    }

    @Override
    public void deleteUser(String username) {
        repository.deleteByUsername(username);
    }
}
