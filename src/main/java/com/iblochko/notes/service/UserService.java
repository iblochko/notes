package com.iblochko.notes.service;

import com.iblochko.notes.dto.UserDto;
import com.iblochko.notes.model.User;
import java.util.List;

public interface UserService {
    User getUserByUsername(String username);

    List<User> getAllUsers();

    UserDto createUser(UserDto userDto);

    UserDto updateUser(String username, UserDto userDto);

    void deleteUser(String username);
}
