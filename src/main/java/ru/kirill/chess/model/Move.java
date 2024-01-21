package ru.kirill.chess.model;

public class Move {

    private int playerId;
    private int gameId;
    private String moveFrom;
    private String moveTo;
    private NewFigureType figureType;

    public Move(String moveFrom, String moveTo, int playerId, int gameId, NewFigureType figureType) {
        this.moveFrom = moveFrom;
        this.moveTo = moveTo;
        this.playerId = playerId;
        this.gameId = gameId;
        this.figureType = figureType;
    }

    public NewFigureType getFigureType() {
        return figureType;
    }

    public void setFigureType(NewFigureType figureType) {
        this.figureType = figureType;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getMoveFrom() {
        return moveFrom;
    }

    public void setMoveFrom(String moveFrom) {
        this.moveFrom = moveFrom;
    }

    public String getMoveTo() {
        return moveTo;
    }

    public void setMoveTo(String moveTo) {
        this.moveTo = moveTo;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

}
