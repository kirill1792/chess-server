package ru.kirill.chess.service;


import org.springframework.stereotype.Service;
import ru.kirill.chess.model.Game;
import ru.kirill.chess.model.Player;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PlayService {

    private final Map<Integer, Player> players = new ConcurrentHashMap<>();
    private final Map<Integer, Game> games = new ConcurrentHashMap<>();
    private final Map<Integer, Game> gamesByPlayer = new ConcurrentHashMap<>();
    private final AtomicInteger playerIdCounter = new AtomicInteger();
    private final AtomicInteger gameIdCounter = new AtomicInteger();

    public PlayService() {
        Runnable task = () -> {
            while (true) {
                Player prevPlayer = null;
                for (var player: new ArrayList<>(players.values())) {
                    if (prevPlayer != null) {
                        final int gameId = gameIdCounter.incrementAndGet();
                        Game game = new Game(gameId, prevPlayer, player);
                        games.put(game.getId(), game);
                        gamesByPlayer.put(prevPlayer.getId(), game);
                        gamesByPlayer.put(player.getId(), game);
                        players.remove(prevPlayer.getId());
                        players.remove(player.getId());
                        prevPlayer = null;
                    } else {
                        prevPlayer = player;
                    }
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    public Player create(String name){
        final int playerId =  playerIdCounter.incrementAndGet();
        Player player = new Player(name, playerId);
        players.put(playerId, player);
        return player;
    }

    public Game getGameByPlayerId(int playerId) {
        return gamesByPlayer.get(playerId);
    }

    public Game getGameById(int id){
        return games.get(id);
    }
}
