package ru.kirill.chess.model;

import ru.kirill.chess.MoveResponse;
import ru.kirill.chess.model.figure.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Game {

    private Board board;
    private int id;
    private Player playerOne;
    private Player playerTwo;
    private Player turn;
    private GetMove currentMove;
    private Coordinates enPassantFrom;
    private Coordinates enPassantTo;
    private int tieMovesCounter = 0;
    private List<List<Class<? extends Figure>>> combinations = Arrays.asList(Arrays.asList(King.class), Arrays.asList(King.class, Knight.class), Arrays.asList(King.class, Bishop.class));
    private List<String> boardStates = new ArrayList<>();
    private boolean repeat = false;

    public GetMove getCurrentMove() {
        return currentMove;
    }

    public void setCurrentMove(GetMove currentMove) {
        this.currentMove = currentMove;
    }

    public Game(int id, Player playerOne, Player playerTwo) {
        this.id = id;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;

        setUpGame();
        boardStates.add(encodeBoard());
    }

    public MoveResponse move(Coordinates moveFrom, Coordinates moveTo, int playerId, NewFigureType figureType){
        System.out.println(turn.getMyFigures());
        Figure currentFigure = board.getFields().get(moveFrom.getRow()).get(moveFrom.getColumn());
        if(turn.getId() != playerId || !turn.getMyFigures().contains(currentFigure)){
            return new MoveResponse(AfterMoveStatus.FAIL, null);
        }
        MoveValidator validator = new MoveValidator(board, currentFigure, enPassantFrom, enPassantTo);
        List<Coordinates> moves = validator.findFinalMoves();
        if(moves.contains(moveTo)){
            if (enPassantFrom != null && enPassantTo != null && currentFigure instanceof Pawn) {
                Pawn pawn = (Pawn) board.getFields().get(enPassantTo.getRow()).get(enPassantTo.getColumn());
                if (moveTo.equals(new Coordinates(enPassantTo.getRow() - pawn.direction, enPassantTo.getColumn()))) {
                    board.setCell(enPassantTo.getRow(), enPassantTo.getColumn(), null);
                    board.setCell(enPassantTo.getRow() - pawn.direction, enPassantTo.getColumn(), pawn);
                }
            }
            enPassantFrom = null;
            enPassantTo = null;
            if (currentFigure instanceof Pawn && Math.abs(moveTo.getRow() - moveFrom.getRow()) == 2){
                enPassantFrom = moveFrom;
                enPassantTo = moveTo;
            }
            Rook rook = validator.dictionary.get(moveTo);
            if(rook != null) {
                Coordinates kingCords = board.getElementCoordinates(currentFigure);
                Coordinates rookCords = board.getElementCoordinates(rook);
                board.setCell(kingCords.getRow(), kingCords.getColumn(), null);
                board.setCell(moveTo.getRow(), moveTo.getColumn(), currentFigure);
                board.setCell(rookCords.getRow(), rookCords.getColumn(), null);
                Coordinates rookTo = new Coordinates(moveTo.getRow(), moveTo.getColumn() + defineRookPlace(kingCords.getColumn(), rookCords.getColumn()));
                board.setCell(rookTo.getRow(), rookTo.getColumn(), rook);
                rook.isMoved = true;
                currentFigure.isMoved = true;
                return afterMoveProcess(currentFigure, moveFrom, moveTo, figureType, rookCords, rookTo);
            }
            if (figureType != null){
                turn.getMyFigures().remove(currentFigure);
                if (figureType == NewFigureType.QUEEN){
                    currentFigure = new Queen(turn.getColor());
                }
                else if(figureType == NewFigureType.ROOK) {
                    currentFigure = new Rook(turn.getColor());
                }
                else if(figureType == NewFigureType.BISHOP) {
                    currentFigure = new Bishop(turn.getColor());
                }
                else if(figureType == NewFigureType.KNIGHT) {
                    currentFigure = new Knight(turn.getColor());
                }
                turn.getMyFigures().add(currentFigure);
            }
            Figure nextCell = board.getFields().get(moveTo.getRow()).get(moveTo.getColumn());
            if(nextCell != null){
                getNoTurn().getMyFigures().remove(nextCell);
            }
            if(reversibleMove(moveTo, currentFigure)){
                tieMovesCounter++;
            }
            else {
                tieMovesCounter = 0;
                boardStates = new ArrayList<>();
            }
            board.setCell(moveFrom.getRow(), moveFrom.getColumn(), null);
            board.setCell(moveTo.getRow(), moveTo.getColumn(), currentFigure);
            currentFigure.isMoved = true;
            turn.getMyKing().isChecked = false;

            String state = encodeBoard();
            boardStates.add(state);
            if(Collections.frequency(boardStates, state) == 3){
                repeat = true;
            }
            return afterMoveProcess(currentFigure, moveFrom, moveTo, figureType, null, null);
        }
        else {
            return new MoveResponse(AfterMoveStatus.FAIL, null);
        }
    }

    private MoveResponse afterMoveProcess(Figure currentFigure, Coordinates moveFrom, Coordinates moveTo, NewFigureType figureType, Coordinates rookFrom, Coordinates rookTo){
        Parser parser = new Parser();
        MoveValidator validator = new MoveValidator(board, currentFigure, enPassantFrom, enPassantTo);
        if (validator.checkForCheck(board.getElementCoordinates(getNoTurn().getMyKing()), board, turn.getColor())){
            getNoTurn().getMyKing().isChecked = true;
            if (validator.checkmate(board, getNoTurn().getMyFigures())){
                currentMove = new GetMove(parser.notate(moveFrom), parser.notate(moveTo), AfterMoveStatus.CHECKMATE, getNoTurn().getId(), figureType, parser.notate(rookFrom), parser.notate(rookTo), null);
                return new MoveResponse(AfterMoveStatus.CHECKMATE, null);
            }
            //return AfterMoveStatus.SUCCESS;
        }
        else {
            if (validator.checkmate(board, getNoTurn().getMyFigures())){
                System.out.println("ПАТ");
                currentMove = new GetMove(parser.notate(moveFrom), parser.notate(moveTo), AfterMoveStatus.TIE, getNoTurn().getId(), figureType, parser.notate(rookFrom), parser.notate(rookTo), TieType.STALEMATE);
                return new MoveResponse(AfterMoveStatus.TIE, TieType.STALEMATE);
            }
        }
        if(repeat){
            currentMove = new GetMove(parser.notate(moveFrom), parser.notate(moveTo), AfterMoveStatus.TIE, getNoTurn().getId(), figureType, parser.notate(rookFrom), parser.notate(rookTo), TieType.REPETITION);
            return new MoveResponse(AfterMoveStatus.TIE, TieType.REPETITION);
        }
        if (lackOfFigs()){
            currentMove = new GetMove(parser.notate(moveFrom), parser.notate(moveTo), AfterMoveStatus.TIE, getNoTurn().getId(), figureType, parser.notate(rookFrom), parser.notate(rookTo), TieType.LACK_OF_FIGS);
            return new MoveResponse(AfterMoveStatus.TIE, TieType.LACK_OF_FIGS);
        }
        if(tieMovesCounter == 100){
            currentMove = new GetMove(parser.notate(moveFrom), parser.notate(moveTo), AfterMoveStatus.TIE, getNoTurn().getId(), figureType, parser.notate(rookFrom), parser.notate(rookTo), TieType.MOVES_LIMITED);
            return new MoveResponse(AfterMoveStatus.TIE, TieType.MOVES_LIMITED);
        }
        currentMove = new GetMove(parser.notate(moveFrom), parser.notate(moveTo), AfterMoveStatus.SUCCESS, getNoTurn().getId(), figureType, parser.notate(rookFrom), parser.notate(rookTo), null);
        return new MoveResponse(AfterMoveStatus.SUCCESS, null);
//        else {
//            if (validator.checkmate(board, getNoTurn().getMyFigures())){
//                System.out.println("ПАТ");
//                return AfterMoveStatus.SUCCESS;
//            }
//            else {
//                return AfterMoveStatus.SUCCESS;
//            }
//        }
    }

    private String encodeBoard(){
        String stateString = "";
        for (int i = 0; i < board.getFields().size(); i++) {
            for (int j = 0; j < board.getFields().get(i).size(); j++) {
                 Figure figure = board.getFields().get(i).get(j);
                 if(figure != null) {
                     stateString += figure.color.charAt(0);
                     if (!(figure instanceof Knight)) {
                         stateString += figure.getClass().getSimpleName().charAt(0);
                     } else {
                         stateString += "N";
                     }
                     if (figure instanceof King || figure instanceof Rook) {
                         if (figure.isMoved) {
                             stateString += "m";
                         }
                     }
                     if(enPassantTo != null) {
                         if (i == enPassantTo.getRow() && j == enPassantTo.getColumn()) {
                             stateString += "e";
                         }
                     }
                 }
                 else {
                     stateString += "0";
                 }
                stateString += "/";
            }
        }
        return stateString;
    }


    private boolean reversibleMove(Coordinates moveTo, Figure currentFigure){
        return board.getFields().get(moveTo.getRow()).get(moveTo.getColumn()) == null && !(currentFigure instanceof Pawn);
    }

    private boolean lackOfFigs(){
        List<Class<? extends Figure>> first = new ArrayList<>();
        List<Class<? extends Figure>> second = new ArrayList<>();
        for(Figure figure: playerOne.getMyFigures()){
            first.add(figure.getClass());
        }
        for(Figure figure: playerTwo.getMyFigures()){
            second.add(figure.getClass());
        }
        return combinations.contains(first) && combinations.contains(second);
    }


    public Player getNoTurn(){
        if (turn.equals(playerOne)){
            return playerTwo;
        }
        else {
            return playerOne;
        }
    }

    private int defineRookPlace(int kingColumn, int rookColumn) {
        if (kingColumn - rookColumn < 0){
            return -1;
        }
        else {
            return 1;
        }
    }

    private void setUpGame(){
        board = new Board();
        board.setFields();
        placePieces();

        if(playerOne.getName().equals("Kirill")){
            playerOne.setColor("white");
            playerTwo.setColor("black");
            playerOne.setMyFigures(board.getFiguresByColor("white"));
            playerTwo.setMyFigures(board.getFiguresByColor("black"));
            turn = playerOne;
            playerOne.setMyKing();
            playerTwo.setMyKing();
            return;
        }
        if(playerTwo.getName().equals("Kirill")){
            playerOne.setColor("black");
            playerTwo.setColor("white");
            playerOne.setMyFigures(board.getFiguresByColor("black"));
            playerTwo.setMyFigures(board.getFiguresByColor("white"));
            turn = playerTwo;
            playerOne.setMyKing();
            playerTwo.setMyKing();
            return;
        }

        int rand = (int) (Math.random() * 2);
        if(rand == 0){
            playerOne.setColor("white");
            playerTwo.setColor("black");
            playerOne.setMyFigures(board.getFiguresByColor("white"));
            playerTwo.setMyFigures(board.getFiguresByColor("black"));
            turn = playerOne;
        }
        else {
            playerOne.setColor("black");
            playerTwo.setColor("white");
            playerOne.setMyFigures(board.getFiguresByColor("black"));
            playerTwo.setMyFigures(board.getFiguresByColor("white"));
            turn = playerTwo;
        }
        playerOne.setMyKing();
        playerTwo.setMyKing();
    }

    public void changeTurn(){
        if(turn == playerOne){
            turn = playerTwo;
        }
        else {
            turn = playerOne;
        }
    }

    public Player getPlayerOne() {
        return playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
    }

    public int getId() {
        return id;
    }

    public void placePieces() {
        Figure[] whiteFigures = {new Rook("white"),
                new Knight("white"),
                new Bishop("white"),
                new Queen("white"),
                new King("white"),
                new Bishop("white"),
                new Knight("white"),
                new Rook("white")};

        Figure[] blackFigures = {new Rook("black"),
                new Knight("black"),
                new Bishop("black"),
                new Queen("black"),
                new King("black"),
                new Bishop("black"),
                new Knight("black"),
                new Rook("black")};

        for (int i = 0; i < whiteFigures.length; i++) {
            Figure currentFig = whiteFigures[i];
            Pawn whitePawn = new Pawn("white", -1);
            board.setCell(7, i, currentFig);
            board.setCell(6, i, whitePawn);
        }
        for (int i = 0; i < blackFigures.length; i++) {
            Figure currentFig = blackFigures[i];
            Pawn blackPawn = new Pawn("black", 1);
            board.setCell(0, i, currentFig);
            board.setCell(1, i, blackPawn);
        }
    }
}


//    public AfterMoveStatus move(Coordinates moveFrom, Coordinates moveTo, int playerId, NewFigureType figureType){
//        System.out.println(turn.getMyFigures());
//        Figure currentFigure = board.getFields().get(moveFrom.getRow()).get(moveFrom.getColumn());
//        if(turn.getId() != playerId || !turn.getMyFigures().contains(currentFigure)){
//            return AfterMoveStatus.FAIL;
//        }
//        MoveValidator validator = new MoveValidator(board, currentFigure);
//        List<Coordinates> moves = validator.findFinalMoves();
//        if(moves.contains(moveTo)){
//            Rook rook = validator.dictionary.get(moveTo);
//            if(rook != null) {
//                Coordinates kingCords = board.getElementCoordinates(currentFigure);
//                Coordinates rookCords = board.getElementCoordinates(rook);
//                board.setCell(kingCords.getRow(), kingCords.getColumn(), null);
//                board.setCell(moveTo.getRow(), moveTo.getColumn(), currentFigure);
//                board.setCell(rookCords.getRow(), rookCords.getColumn(), null);
//                board.setCell(moveTo.getRow(), moveTo.getColumn() + defineRookPlace(kingCords.getColumn(), rookCords.getColumn()), rook);
//                rook.isMoved = true;
//                currentFigure.isMoved = true;
//                return AfterMoveStatus.SUCCESS;
//            }
//            if (figureType != null){
//                turn.getMyFigures().remove(currentFigure);
//                if (figureType == NewFigureType.QUEEN){
//                    currentFigure = new Queen(turn.getColor());
//                }
//                else if(figureType == NewFigureType.ROOK) {
//                    currentFigure = new Rook(turn.getColor());
//                }
//                else if(figureType == NewFigureType.BISHOP) {
//                    currentFigure = new Bishop(turn.getColor());
//                }
//                else if(figureType == NewFigureType.KNIGHT) {
//                    currentFigure = new Knight(turn.getColor());
//                }
//                turn.getMyFigures().add(currentFigure);
//            }
//            Coordinates selectedFigureCoordinates = board.getElementCoordinates(currentFigure);
//            Figure nextCell = board.getFields().get(moveTo.getRow()).get(moveTo.getColumn());
//            if(nextCell != null){
//                getNoTurn().getMyFigures().remove(nextCell);
//            }
//            board.setCell(selectedFigureCoordinates.getRow(), selectedFigureCoordinates.getColumn(), null);
//            board.setCell(moveTo.getRow(), moveTo.getColumn(), currentFigure);
//            currentFigure.isMoved = true;
//            turn.getMyKing().isChecked = false;
//            return afterMoveProcess(validator, moveFrom, moveTo, figureType);
//        }
//        else {
//            return AfterMoveStatus.FAIL;
//        }
//    }