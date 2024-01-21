package ru.kirill.chess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.kirill.chess.service.PlayService;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class ChessApplication {

    @Autowired
    private static PlayService playService;

    public static void main(String[] args) {
        SpringApplication.run(ChessApplication.class, args);
    }
}
