package ru.kirill.chess.model.figure;

import ru.kirill.chess.model.Board;
import ru.kirill.chess.model.Coordinates;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Figure {
    public int direction;

    public Pawn(String color, int direction) {
        super(color);
        this.direction = direction;
    }

    @Override
    public List<Coordinates> calculatePossibleMoves(Coordinates figureCoordinates, Board board) {
        ArrayList<Coordinates> possibleMoves = new ArrayList<>();
        int row = figureCoordinates.getRow();
        int column = figureCoordinates.getColumn();
        if (board.checkOutOfBounds(row + direction, column) &&
                board.isEmptyField(row + direction, column)) {
            possibleMoves.add(new Coordinates(row + direction, column));

            if (board.checkOutOfBounds(row + direction * 2, column) &&
                    board.isEmptyField(row + direction * 2, column) && !this.isMoved) {
                possibleMoves.add(new Coordinates(row + direction * 2, column));
            }
        }
        if(checkBeat(row + direction, column + 1, board)){
            possibleMoves.add(new Coordinates(row + direction, column + 1));
        }
        if(checkBeat(row + direction, column - 1, board)){
            possibleMoves.add(new Coordinates(row + direction, column - 1));
        }
        return possibleMoves;
    }

    public void changeDirection() {
        direction *= -1;
    }

    private boolean checkBeat(int row, int column, Board board) {
        return board.checkOutOfBounds(row, column) && !board.isEmptyField(row, column) && !board.getFields().get(row).get(column).color.equals(this.color);
    }
}
