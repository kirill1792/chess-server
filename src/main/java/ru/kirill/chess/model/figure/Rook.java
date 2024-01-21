package ru.kirill.chess.model.figure;

import ru.kirill.chess.model.Board;
import ru.kirill.chess.model.Coordinates;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rook extends Figure{
    public Rook(String color) {
        super(color);
    }

    @Override
    public List<Coordinates> calculatePossibleMoves(Coordinates figureCoordinates, Board board) {
        ArrayList<Coordinates> possibleMoves = new ArrayList<>();
        int startRow = figureCoordinates.getRow();
        int startColumn = figureCoordinates.getColumn();
        int[] iterations = {startRow, 7 - startColumn, 7 - startRow, startColumn};
        int[] dirBuffs = {-1, 1, 1,-1};
        String currentDir = "row";

        for (int i = 0; i < 4; i++) {
            int currentRow = startRow;
            int currentColumn = startColumn;
            for (int j = 0; j < iterations[i]; j++) {

                if(currentDir.equals("row")) {
                    currentRow += dirBuffs[i];
                }
                else {
                    currentColumn += dirBuffs[i];
                }

                if (board.getFields().get(currentRow).get(currentColumn) != null) {
                    Figure figure = board.getFields().get(currentRow).get(currentColumn);
                    if (!figure.color.equals(this.color)) {
                        possibleMoves.add(new Coordinates(currentRow, currentColumn));
                    }
                    break;
                }
                possibleMoves.add(new Coordinates(currentRow, currentColumn));
            }
            if(currentDir.equals("row")) {
                currentDir = "column";
            }
            else {
                currentDir = "row";
            }
        }
        return possibleMoves;
    }
}
