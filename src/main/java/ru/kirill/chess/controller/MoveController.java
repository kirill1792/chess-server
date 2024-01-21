package ru.kirill.chess.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.kirill.chess.MoveResponse;
import ru.kirill.chess.model.*;
import ru.kirill.chess.service.PlayService;

@RestController
public class MoveController {

    @Autowired
    private PlayService playService;

    @Autowired
    private PlayServer playServer;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/move")
    public MoveResponse sendMove(@RequestBody Move move) {
        Game game = playService.getGameById(move.getGameId());
        Parser parser = new Parser();
        Coordinates coordinatesFrom = parser.parse(move.getMoveFrom());
        Coordinates coordinatesTo = parser.parse(move.getMoveTo());
        MoveResponse result = game.move(coordinatesFrom, coordinatesTo, move.getPlayerId(), move.getFigureType());
        try {
            int noTurnId = game.getNoTurn().getId();
            String currMoveJson = objectMapper.writeValueAsString(game.getCurrentMove());
            String res = playServer.sendDataToPlayer(noTurnId, currMoveJson);
            System.out.printf("Sent move to opponent player (id=%s), response: %s\n", noTurnId, res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        game.setCurrentMove(null);
        game.changeTurn();
        return result;
    }

    @GetMapping("/move")
    public GetMove getMove(@RequestParam int gameId, @RequestParam int playerId) throws Exception {
        GetMove move = null;
        Game game = playService.getGameById(gameId);
        while (move == null){
            if (game.getCurrentMove() != null && game.getCurrentMove().getId() == playerId){
                move = game.getCurrentMove();
                game.setCurrentMove(null);
                game.changeTurn();
            }
        }
        return move;
    }
}
