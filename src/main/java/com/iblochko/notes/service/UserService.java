package com.iblochko.notes.service;

import com.iblochko.notes.dto.UserDto;
import java.util.List;

public interface UserService {
    UserDto getUserByUsername(String username);

    List<UserDto> getAllUsers();

    UserDto createUser(UserDto userDto);

    UserDto updateUser(String username, UserDto userDto);

    void deleteUser(String username);
}
