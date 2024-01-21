package ru.kirill.chess.model;

public class Parser {

    private String boardNums = "87654321";
    private String boardLetters = "abcdefgh";

    public Coordinates parse(String move){
        int column = boardLetters.indexOf(move.charAt(0));
        int row = boardNums.indexOf(move.charAt(1));
        return new Coordinates(row, column);
    }

    public String notate(Coordinates coordinates){
        if(coordinates == null){
            return null;
        }
        return "" + boardLetters.charAt(coordinates.getColumn()) + boardNums.charAt(coordinates.getRow());
    }
}
