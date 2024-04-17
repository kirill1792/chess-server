package ru.kirill.chess.model;

import ru.kirill.chess.MoveResponse;
import ru.kirill.chess.dao.UserDao;
import ru.kirill.chess.model.figure.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    private HashMap<NewFigureType, String> figCodes = new HashMap<>();

    private String curGame = "";

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
        setFigCodes();

        setUpGame();
        boardStates.add(encodeBoard());
    }

    private void setFigCodes(){
        figCodes.put(NewFigureType.BISHOP, "B");
        figCodes.put(NewFigureType.KNIGHT, "K");
        figCodes.put(NewFigureType.ROOK, "R");
        figCodes.put(NewFigureType.QUEEN, "Q");
    }

    public MoveResponse move(Coordinates moveFrom, Coordinates moveTo, int playerId, NewFigureType figureType){
        System.out.println(turn.getMyFigures());
        Figure currentFigure = board.getFields().get(moveFrom.getRow()).get(moveFrom.getColumn());
        boolean encoded = false;
        if(turn.getId() != playerId || !turn.getMyFigures().contains(currentFigure)){
            return new MoveResponse(AfterMoveStatus.FAIL, null, -1);
        }
        MoveValidator validator = new MoveValidator(board, currentFigure, enPassantFrom, enPassantTo);
        List<Coordinates> moves = validator.findFinalMoves();
        if(moves.contains(moveTo)){
            if (enPassantFrom != null && enPassantTo != null && currentFigure instanceof Pawn) {
                Pawn pawn = (Pawn) board.getFields().get(enPassantTo.getRow()).get(enPassantTo.getColumn());
                if (moveTo.equals(new Coordinates(enPassantTo.getRow() - pawn.direction, enPassantTo.getColumn()))) {
                    board.setCell(enPassantTo.getRow(), enPassantTo.getColumn(), null);
                    board.setCell(enPassantTo.getRow() - pawn.direction, enPassantTo.getColumn(), pawn);
                    curGame = encodeMove(moveFrom, moveTo, enPassantTo);
                    encoded = true;
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
                curGame = encodeMove(moveFrom, moveTo, rookCords, rookTo);
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
                curGame = encodeMove(moveFrom, moveTo, figureType);
                encoded = true;
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
            if(!encoded) {
                curGame = encodeMove(moveFrom, moveTo);
            }

            String state = encodeBoard();
            boardStates.add(state);
            if(Collections.frequency(boardStates, state) == 3){
                repeat = true;
            }
            return afterMoveProcess(currentFigure, moveFrom, moveTo, figureType, null, null);
        }
        else {
            return new MoveResponse(AfterMoveStatus.FAIL, null, -1);
        }
    }

    private MoveResponse afterMoveProcess(Figure currentFigure, Coordinates moveFrom, Coordinates moveTo, NewFigureType figureType, Coordinates rookFrom, Coordinates rookTo){
        Parser parser = new Parser();
        MoveValidator validator = new MoveValidator(board, currentFigure, enPassantFrom, enPassantTo);
        if (validator.checkForCheck(board.getElementCoordinates(getNoTurn().getMyKing()), board, turn.getColor())){
            getNoTurn().getMyKing().isChecked = true;
            if (validator.checkmate(board, getNoTurn().getMyFigures())){
                ArrayList<Integer> ratings = new ArrayList<>();
                ratings.add(turn.getRating() + 20);
                ratings.add(getNoTurn().getRating() - 20);
                currentMove = new GetMove(parser.notate(moveFrom), parser.notate(moveTo), AfterMoveStatus.CHECKMATE, getNoTurn().getId(), figureType, parser.notate(rookFrom), parser.notate(rookTo), null, ratings.get(1));
                updateData(ratings, turn, getNoTurn(), false);
                return new MoveResponse(AfterMoveStatus.CHECKMATE, null, ratings.get(0));
            }
            //return AfterMoveStatus.SUCCESS;
        }
        else {
            if (validator.checkmate(board, getNoTurn().getMyFigures())){
                System.out.println("ПАТ");
                ArrayList<Integer> ratings = calcRating(true);
                currentMove = new GetMove(parser.notate(moveFrom), parser.notate(moveTo), AfterMoveStatus.TIE, getNoTurn().getId(), figureType, parser.notate(rookFrom), parser.notate(rookTo), TieType.STALEMATE, ratings.get(1));
                updateData(ratings, turn, getNoTurn(), true);
                return new MoveResponse(AfterMoveStatus.TIE, TieType.STALEMATE, ratings.get(0));
            }
        }
        if(repeat){
            ArrayList<Integer> ratings = calcRating(true);
            currentMove = new GetMove(parser.notate(moveFrom), parser.notate(moveTo), AfterMoveStatus.TIE, getNoTurn().getId(), figureType, parser.notate(rookFrom), parser.notate(rookTo), TieType.REPETITION, ratings.get(1));
            updateData(ratings, turn, getNoTurn(), true);
            return new MoveResponse(AfterMoveStatus.TIE, TieType.REPETITION, ratings.get(0));
        }
        if (lackOfFigs()){
            ArrayList<Integer> ratings = calcRating(true);
            currentMove = new GetMove(parser.notate(moveFrom), parser.notate(moveTo), AfterMoveStatus.TIE, getNoTurn().getId(), figureType, parser.notate(rookFrom), parser.notate(rookTo), TieType.LACK_OF_FIGS, ratings.get(1));
            updateData(ratings, turn, getNoTurn(), true);
            return new MoveResponse(AfterMoveStatus.TIE, TieType.LACK_OF_FIGS, ratings.get(0));
        }
        if(tieMovesCounter == 100){
            ArrayList<Integer> ratings = calcRating(true);
            currentMove = new GetMove(parser.notate(moveFrom), parser.notate(moveTo), AfterMoveStatus.TIE, getNoTurn().getId(), figureType, parser.notate(rookFrom), parser.notate(rookTo), TieType.MOVES_LIMITED, ratings.get(1));
            updateData(ratings, turn, getNoTurn(), true);
            return new MoveResponse(AfterMoveStatus.TIE, TieType.MOVES_LIMITED, ratings.get(0));
        }
        currentMove = new GetMove(parser.notate(moveFrom), parser.notate(moveTo), AfterMoveStatus.SUCCESS, getNoTurn().getId(), figureType, parser.notate(rookFrom), parser.notate(rookTo), null, -1);
        return new MoveResponse(AfterMoveStatus.SUCCESS, null, -1);
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

    private void addGame(UserDao dao, Player first, Player second, boolean isTie){
        try {
            dao.insertGame(new ChessGame(0, getCurrentTime(), new User(first.getId(), first.getName(), "", 0),
                    new User(second.getId(), second.getName(), "", 0), movedFirst(first, second), curGame, isTie));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateData(ArrayList<Integer> arr, Player first, Player second, boolean isTie){
        UserDao dao = new UserDao();
        updateRatings(arr, first.getId(), second.getId(), dao);
        addGame(dao, first, second, isTie);
    }

    private int movedFirst(Player first, Player second){
        if(first.getColor().equals("white")){
            return first.getId();
        }
        else {
            return second.getId();
        }
    }

    private String getCurrentTime(){
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return myDateObj.format(myFormatObj);
    }

    private String encodeMove(Coordinates from, Coordinates to){
        return curGame + from.getRow() + from.getColumn() + "-" + to.getRow() + to.getColumn() + "|";
    }

    private String encodeMove(Coordinates from, Coordinates to, Coordinates rookFrom, Coordinates rookTo){
        String type = "S";
        if(Math.abs(rookTo.getColumn() - rookFrom.getColumn()) == 3){
            type = "L";
        }
        return curGame + type + from.getRow() + from.getColumn() + "-" + to.getRow() + to.getColumn() + ":" + rookFrom.getRow() + rookFrom.getColumn() + "-" + rookTo.getRow() + rookTo.getColumn() + "|";
    }

    private String encodeMove(Coordinates from, Coordinates to, Coordinates epTo){
        return curGame + "E" + from.getRow() + from.getColumn() + "-" + to.getRow() + to.getColumn() + ":" + epTo.getRow() + epTo.getColumn() + "|";
    }

    private String encodeMove(Coordinates from, Coordinates to, NewFigureType type){
        return curGame + figCodes.get(type) + from.getRow() + from.getColumn() + "-" + to.getRow() + to.getColumn() + "|";
    }


    private void updateRatings(ArrayList<Integer> arr, int idFirst, int idSecond, UserDao dao){
        try {
            dao.updateRating(idFirst, arr.get(0));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            dao.updateRating(idSecond, arr.get(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<Integer> calcRating(boolean isTie){
        ArrayList<Integer> arr = new ArrayList<>();
        double sA = 1;
        double sB = 0;
        if(isTie){
            sA = 0.5;
            sB = 0.5;
        }
        double eA = (1 / (1 + Math.pow(10, getNoTurn().getRating() - turn.getRating())));
        double eB = (1 / (1 + Math.pow(10, turn.getRating() - getNoTurn().getRating())));
        int rA = (int) Math.round(turn.getRating() + 20 * (sA - eA));
        int rB = (int) Math.round(getNoTurn().getRating() + 20 * (sB - eB));
        rA = turn.getRating() + 20;
        rB = getNoTurn().getRating() - 20;
        arr.add(rA);
        arr.add(rB);
        return arr;
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