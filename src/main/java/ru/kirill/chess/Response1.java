package ru.kirill.chess;

public class Response1 {

    private String opponentName;
    private String color;
    private int gameId;
    private int playerId;

    public Response1(String opponentName, String color, int gameId, int playerId) {
        this.opponentName = opponentName;
        this.color = color;
        this.gameId = gameId;
        this.playerId = playerId;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
}
