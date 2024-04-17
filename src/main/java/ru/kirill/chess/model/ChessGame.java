package ru.kirill.chess.model;

public class ChessGame {
    private Integer id;
    private String date;
    private User winner;
    private User loser;
    private Integer movedFirst;
    private String moves;

    private boolean tie;

    public ChessGame(int id, String date, User winner, User loser, int movedFirst, String moves, boolean tie){
        this.id = id;
        this.date = date;
        this.winner = winner;
        this.loser = loser;
        this.movedFirst = movedFirst;
        this.moves = moves;
        this.tie = tie;
    }

    public boolean isTie() {
        return tie;
    }

    public void setTie(boolean tie) {
        this.tie = tie;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    public User getLoser() {
        return loser;
    }

    public void setLoser(User loser) {
        this.loser = loser;
    }

    public Integer getMovedFirst() {
        return movedFirst;
    }

    public void setMovedFirst(Integer movedFirst) {
        this.movedFirst = movedFirst;
    }

    public String getMoves() {
        return moves;
    }

    public void setMoves(String moves) {
        this.moves = moves;
    }
}
