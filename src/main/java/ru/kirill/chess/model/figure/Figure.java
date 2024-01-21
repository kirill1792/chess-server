package ru.kirill.chess.model.figure;


import ru.kirill.chess.model.Board;
import ru.kirill.chess.model.Coordinates;

import java.util.List;

public abstract class Figure {
    public boolean isMoved = false;
    public String color;

    public Figure(String color) {
        this.color = color;
    }

    public abstract List<Coordinates> calculatePossibleMoves(Coordinates figureCoordinates, Board board);

    @Override
    public String toString() {
        return  getClass().getSimpleName() + "{" +
                "isMoved=" + isMoved +
                ", color='" + color + '\'' +
                '}';
    }
}
