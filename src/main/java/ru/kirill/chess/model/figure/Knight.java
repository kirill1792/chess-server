package ru.kirill.chess.model.figure;


import ru.kirill.chess.model.Board;
import ru.kirill.chess.model.Coordinates;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Figure{
    public Knight(String color) {
        super(color);
    }

    @Override
    public List<Coordinates> calculatePossibleMoves(Coordinates figureCoordinates, Board board) {
        ArrayList<Coordinates> possibleMoves = new ArrayList<>();
        int startRow = figureCoordinates.getRow() - 2;
        int startColumn = figureCoordinates.getColumn() - 2;
        int[] buffers = {1, 1,-1,-1};
        boolean movePoint = false;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if(movePoint){
                    if(board.checkOutOfBounds(startRow, startColumn)){
                        if(!board.isEmptyField(startRow, startColumn)) {
                            if(!board.getFields().get(startRow).get(startColumn).color.equals(this.color)) {
                                possibleMoves.add(new Coordinates(startRow, startColumn));
                            }
                        }
                        else {
                            possibleMoves.add(new Coordinates(startRow, startColumn));
                        }
                    }
                    movePoint = false;
                }
                else {
                    movePoint = true;
                    }

                if(i % 2 == 0) {
                    startColumn += buffers[i];
                   }
                else {
                    startRow += buffers[i];
                    }
                }
            }
        return possibleMoves;
        }
    }
