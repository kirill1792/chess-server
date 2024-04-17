package ru.kirill.chess.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.kirill.chess.model.ChessGame;
import ru.kirill.chess.model.User;
import ru.kirill.chess.rest.RestVK;
import ru.kirill.chess.service.UserService;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RestVK restVK;
    @GetMapping("/games")
    public List<ChessGame> getGames(@RequestParam int id){
        return userService.getUserGames(id);
    }

    @PostMapping("/registration")
    public User register(@RequestBody User newUser){
        String token = newUser.getVkid();
        int a = 0;
        String r = restVK.getUserId(token);
        newUser.setVkid(r);
        return userService.createUser(newUser);
    }

    @GetMapping("/login")
    public User login(@RequestParam String token) throws Exception {
        String r = restVK.getUserId(token);
        return userService.checkId(r);
    }
}
