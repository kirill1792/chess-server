package ru.kirill.chess.model;

import ru.kirill.chess.model.figure.Figure;
import ru.kirill.chess.model.figure.King;
import ru.kirill.chess.model.figure.Pawn;
import ru.kirill.chess.model.figure.Rook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoveValidator {
    private Board board;
    private Figure selectedFigure;
    public Map<Coordinates, Rook> dictionary = new HashMap<>();
    private Coordinates enPassantFrom;
    private Coordinates enPassantTo;

    public MoveValidator(Board board, Figure selectedFigure, Coordinates enPassantFrom, Coordinates enPassantTo){
        this.board = board;
        this.selectedFigure = selectedFigure;
        this.enPassantFrom = enPassantFrom;
        this.enPassantTo = enPassantTo;
    }

    public List<Coordinates> findFinalMoves(){
        List<Coordinates> finalMoves = new ArrayList<>();
        List<Coordinates> possibleMoves = possiblesWithEnPassant(selectedFigure, board);
        for (Coordinates move: possibleMoves){
            if(canMove(move, board, selectedFigure, getOpponentColor(selectedFigure.color), selectedFigure.color)){
                finalMoves.add(move);
            }
        }
        if (selectedFigure instanceof King){
           List<Coordinates> result = castlingValidation((King) selectedFigure, board);
           finalMoves.addAll(result);

        }
        return finalMoves;
    }
    public boolean checkmate(Board board, List<Figure> figures) {
        for (Figure myFigure : figures) {
            for (Coordinates coords : possiblesWithEnPassant(myFigure, board)) {
                boolean result = canMove(coords, board, myFigure, selectedFigure.color, getOpponentColor(selectedFigure.color));
                if (result) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<Coordinates> possiblesWithEnPassant(Figure figure, Board board){
        if(figure instanceof Pawn && enPassantFrom != null && enPassantTo != null && board.getElementCoordinates(figure).getRow() == enPassantTo.getRow()){
            Board copyBoard = new Board();
            copyBoard.setFields(board.getFields());
            Pawn pawn = (Pawn) copyBoard.getFields().get(enPassantTo.getRow()).get(enPassantTo.getColumn());
            copyBoard.setCell(enPassantTo.getRow(), enPassantTo.getColumn(), null);
            copyBoard.setCell(enPassantTo.getRow() - pawn.direction, enPassantTo.getColumn(), pawn);
            return figure.calculatePossibleMoves(board.getElementCoordinates(figure), copyBoard);
        }
        else {
            return figure.calculatePossibleMoves(board.getElementCoordinates(figure), board);
        }
    }

    public boolean checkForCheck(Coordinates figureCoordinates, Board board, String color) {
        List<Coordinates> totalEnemyMoves = new ArrayList<>();
        for (Figure enemyFigure : board.getFiguresByColor(color)) {
            List<Coordinates> moves = enemyFigure.calculatePossibleMoves(board.getElementCoordinates(enemyFigure), board);
            totalEnemyMoves.addAll(moves);
        }
        return findMatch(figureCoordinates, totalEnemyMoves);
    }

    private boolean findMatch(Coordinates kingCords, List<Coordinates> enemyFiguresMoves) {
        for (Coordinates move : enemyFiguresMoves) {
            if (move.equals(kingCords)) {
                return true;
            }
        }
        return false;
    }



    public boolean canMove(Coordinates coordinatesToMove, Board board, Figure figure, String figuresColor, String kingColor) {
        Coordinates selfCoordinates = board.getElementCoordinates(figure);
        List<Coordinates> result = possiblesWithEnPassant(figure, board);
        System.out.println(result);
        for (Coordinates coordinates : result) {
            if (coordinates.equals(coordinatesToMove)) {
                Board copyBoard = new Board();
                copyBoard.setFields(board.getFields());
                if (enPassantFrom != null && enPassantTo != null && figure instanceof Pawn) {
                    Pawn pawn = (Pawn) copyBoard.getFields().get(enPassantTo.getRow()).get(enPassantTo.getColumn());
                    if (coordinates.equals(new Coordinates(enPassantTo.getRow() - pawn.direction, enPassantTo.getColumn()))) {
                        copyBoard.setCell(enPassantTo.getRow(), enPassantTo.getColumn(), null);
                        copyBoard.setCell(enPassantTo.getRow() - pawn.direction, enPassantTo.getColumn(), pawn);
                    }
                }
                copyBoard.setCell(selfCoordinates.getRow(), selfCoordinates.getColumn(), null);
                copyBoard.setCell(coordinatesToMove.getRow(), coordinatesToMove.getColumn(), figure);
                return !checkForCheck(findKing(copyBoard, kingColor), copyBoard, figuresColor);
            }
        }
        return false;
    }

    private Coordinates findKing(Board copyBoard, String color) {
        for (int i = 0; i < copyBoard.getFields().size(); i++) {
            for (int j = 0; j < copyBoard.getFields().get(i).size(); j++) {
                if(copyBoard.getFields().get(i).get(j) != null && copyBoard.getFields().get(i).get(j) instanceof King && copyBoard.getFields().get(i).get(j).color.equals(color)){
                    return new Coordinates(i, j);
                }
            }
        }
        return null;
    }

//    private List<Figure> getEnemyFigures(Board board) {
//        String needColor;
//        if (selectedFigure.color.equals("white")) {
//            needColor = "black";
//        } else {
//            needColor = "white";
//        }
//        return board.getFiguresByColor(needColor);
//    }

    private String getOpponentColor(String color) {
        String needColor;
        if (color.equals("white")) {
            needColor = "black";
        } else {
            needColor = "white";
        }
        return needColor;
    }

    private List<Coordinates> castlingValidation(King king, Board board){
        List<Coordinates> coordinates = new ArrayList<>();
        Coordinates kingCoordinates = board.getElementCoordinates(king);
        if(kingCondition(king.isChecked, king.isMoved)){
            return new ArrayList<>();
        }
        Rook first = checkSide(new Coordinates(kingCoordinates.getRow(), kingCoordinates.getColumn() + 2), king);
        Rook second = checkSide(new Coordinates(kingCoordinates.getRow(), kingCoordinates.getColumn() - 2), king);
        if (first != null){
            coordinates.add(new Coordinates(kingCoordinates.getRow(), kingCoordinates.getColumn() + 2));
            dictionary.put(new Coordinates(kingCoordinates.getRow(), kingCoordinates.getColumn() + 2), first);
        }
        if (second != null){
            coordinates.add(new Coordinates(kingCoordinates.getRow(), kingCoordinates.getColumn() - 2));
            dictionary.put(new Coordinates(kingCoordinates.getRow(), kingCoordinates.getColumn() - 2), second);
        }
        return coordinates;
    }

    private Rook checkSide(Coordinates cords, King king){
        Coordinates startKingCords = board.getElementCoordinates(king);
        Board copyBoard = new Board();
        copyBoard.setFields(board.getFields());
        int buff = getBuffer(cords.getColumn() - startKingCords.getColumn());
        int current = startKingCords.getColumn() + buff;
        for (int i = 0; i < 2; i++){
            if(!copyBoard.isEmptyField(startKingCords.getRow(), current)){
                return null;
            }
            copyBoard.setCell(startKingCords.getRow(), current, king);
            if (checkForCheck(new Coordinates(startKingCords.getRow(), current), copyBoard, getOpponentColor(selectedFigure.color))){
                return null;
            }
            current += buff;
        }
        return defineRook(cords);
    }

    private int getBuffer(int variance){
        if(variance > 0){
            return 1;
        }
        else {
            return -1;
        }
    }

    public static boolean kingCondition(boolean isChecked, boolean isMoved){
        return isChecked || isMoved;
    }

    private Rook defineRook(Coordinates castlingCords){
        Rook castlingRook;
        if(board.getFields().get(castlingCords.getRow()).get(findRookPoint(castlingCords.getColumn())) instanceof Rook) {
            castlingRook = (Rook) board.getFields().get(castlingCords.getRow()).get(findRookPoint(castlingCords.getColumn()));
            System.out.println(castlingRook);
            if (castlingRook != null && !castlingRook.isMoved) {
                return castlingRook;
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    private int findRookPoint(int castlingColumn){
        if (Math.abs( -castlingColumn) < Math.abs(7 - castlingColumn)){
            return 0;
        }
        else return 7;
    }
}
