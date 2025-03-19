package com.iblochko.notes.controller;

import com.iblochko.notes.model.Note;
import com.iblochko.notes.model.User;
import com.iblochko.notes.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notes/user")
@AllArgsConstructor
public class UsersController {

    private final UserService userService;

    @GetMapping("/{username}")
    public User findUserByUsername(@PathVariable String username) {
        return userService.getUser(username);
    }

    @PostMapping("/new_user")
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @PutMapping("/update_user")
    public User updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    @DeleteMapping("/delete_user/{username}")
    public void deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
    }
}
