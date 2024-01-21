package ru.kirill.chess.model.figure;


import ru.kirill.chess.model.Board;
import ru.kirill.chess.model.Coordinates;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Figure{
    public Bishop(String color) {
        super(color);
    }

    @Override
    public List<Coordinates> calculatePossibleMoves(Coordinates figureCoordinates, Board board) {
        ArrayList<Coordinates> possibleMoves = new ArrayList<>();
        int startRow = figureCoordinates.getRow();
        int startColumn = figureCoordinates.getColumn();
        int[][] directions = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};

        for (int i = 0; i < 4; i++) {
            int rowBuff = directions[i][0];
            int columnBuff = directions[i][1];
            int currentRow = startRow + rowBuff;
            int currentColumn = startColumn + columnBuff;
            for (int j = 0; j < 8; j++) {
                if (!board.checkOutOfBounds(currentRow, currentColumn)) {
                    break;
                }
                else if (!board.isEmptyField(currentRow, currentColumn)) {
                    Figure figure = board.getFields().get(currentRow).get(currentColumn);
                    if (!figure.color.equals(this.color)) {
                        possibleMoves.add(new Coordinates(currentRow, currentColumn));
                    }
                    break;
                }
                possibleMoves.add(new Coordinates(currentRow, currentColumn));
                currentRow += rowBuff;
                currentColumn += columnBuff;
            }
        }
        return possibleMoves;
       }
}
