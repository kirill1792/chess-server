package ru.kirill.chess.service;

import org.springframework.stereotype.Service;
import ru.kirill.chess.dao.UserDao;
import ru.kirill.chess.model.ChessGame;
import ru.kirill.chess.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private UserDao userDao;
    private final int startRating = 1200;

    public UserService(){
        this.userDao = new UserDao();
    }

    public User checkId(String id){
        ArrayList<User> arr = userDao.findUserByVkid(id);
        if(!arr.isEmpty()){
            return arr.get(0);
        }
        else {
            return null;
        }
    }

    public User createUser(User user){
        user.setRating(startRating);
        try {
            userDao.insertUser(user);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ChessGame> getUserGames(int id){
        return userDao.findUserGames(id);
    }
}
