package ru.kirill.chess.model.figure;

import ru.kirill.chess.model.Board;
import ru.kirill.chess.model.Coordinates;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;


public class King extends Figure{
    public boolean isChecked = false;
    public King(String color) {
        super(color);
    }

    @Override
    public List<Coordinates> calculatePossibleMoves(Coordinates figureCoordinates, Board board) {
        ArrayList<Coordinates> possibleMoves = new ArrayList<>();
        int row = figureCoordinates.getRow();
        int column = figureCoordinates.getColumn();
        int currentRow = row - 1;
        int currentColumn = column - 1;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(board.checkOutOfBounds(currentRow, currentColumn) && !asList(currentRow, currentColumn).equals(asList(row, column))){
                        if(!board.isEmptyField(currentRow, currentColumn)) {
                            if(!board.getFields().get(currentRow).get(currentColumn).color.equals(this.color)) {
                                possibleMoves.add(new Coordinates(currentRow, currentColumn));
                            }
                        }
                        else {
                            possibleMoves.add(new Coordinates(currentRow, currentColumn));
                        }
                }
                currentRow++;
            }
            currentRow = row - 1;
            currentColumn++;
        }
        System.out.println("Возможные ходы короля" + possibleMoves);
        return possibleMoves;
    }
}


/*
    @Override
    public List<List<Integer>> calculatePossibleMoves(List<Integer> figureCoordinates, Board board) {
        ArrayList<List<Integer>> possibleMoves = new ArrayList<>();
        int row = figureCoordinates.get(0);
        int column = figureCoordinates.get(1);
        int currentRow = row - 1;
        int currentColumn = column - 1;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(currentRow >= 0 & currentRow <= 7 & currentColumn >= 0 & currentColumn <= 7){
                    String needColor;
                    if(this.color.equals("white")){
                        needColor = "black";
                    }
                    else {
                        needColor = "white";
                    }
                    List<Figure> enemyFigs = board.getFiguresByColor(needColor);
                    System.out.println("Вражеские фигуры: " + enemyFigs);
                    if(!checkForCheck(asList(currentRow, currentColumn), board, enemyFigs) &&
                            !asList(currentRow, currentColumn).equals(asList(row, column))) {
                        if(board.getFields().get(currentRow).get(currentColumn) != null) {
                            if(!board.getFields().get(currentRow).get(currentColumn).color.equals(this.color)) {
                                possibleMoves.add(asList(currentRow, currentColumn));
                            }
                        }
                        else {
                            possibleMoves.add(asList(currentRow, currentColumn));
                        }
                    }
                }
                currentRow++;
            }
            currentRow = row - 1;
            currentColumn++;
        }
        System.out.println("Возможные ходы короля" + possibleMoves);
        return possibleMoves;
    }

    public boolean checkForCheck(List<Integer> figureCoordinates, Board board, List<Figure> enemyFigures) {
        List<List<Integer>> totalEnemyMoves = new ArrayList<>();
        for(Figure enemyFigure : enemyFigures) {
            List<List<Integer>> moves = enemyFigure.calculatePossibleMoves(board.getElementCoordinates(enemyFigure), board);
            totalEnemyMoves.addAll(moves);
        }
        if(findMatch(figureCoordinates, totalEnemyMoves)){
            System.out.println("Объявлен шах!");
            return true;
        }
        else {
            return false;
        }
    }

    private boolean findMatch(List<Integer> kingCoords, List<List<Integer>> enemyFiguresMoves) {
        for (List<Integer> move: enemyFiguresMoves) {
            if(move.equals(kingCoords)) {
                return true;
            }
        }
        return false;
    }
}
 */