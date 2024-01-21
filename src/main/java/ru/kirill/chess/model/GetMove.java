package ru.kirill.chess.model;

public class GetMove {

    private String moveFrom;
    private String moveTo;
    private AfterMoveStatus status;
    private int id;
    private NewFigureType figureType;
    private String rookFrom;
    private String rookTo;
    private TieType tieType;

    public GetMove(String moveFrom, String moveTo, AfterMoveStatus status, int id, NewFigureType figureType, String rookFrom, String rookTo, TieType tieType){
        this.moveFrom = moveFrom;
        this.moveTo = moveTo;
        this.status = status;
        this.id = id;
        this.figureType = figureType;
        this.rookFrom = rookFrom;
        this.rookTo = rookTo;
        this.tieType = tieType;
    }

    public TieType getTieType() {
        return tieType;
    }

    public void setTieType(TieType tieType) {
        this.tieType = tieType;
    }


    public NewFigureType getFigureType() {
        return figureType;
    }

    public void setFigureType(NewFigureType figureType) {
        this.figureType = figureType;
    }

    public String getRookFrom() {
        return rookFrom;
    }

    public void setRookFrom(String rookFrom) {
        this.rookFrom = rookFrom;
    }

    public String getRookTo() {
        return rookTo;
    }

    public void setRookTo(String rookTo) {
        this.rookTo = rookTo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMoveFrom() {
        return moveFrom;
    }

    public void setMoveFrom(String moveFrom) {
        this.moveFrom = moveFrom;
    }

    public String getMoveTo() {
        return moveTo;
    }

    public void setMoveTo(String moveTo) {
        this.moveTo = moveTo;
    }

    public AfterMoveStatus getStatus() {
        return status;
    }

    public void setStatus(AfterMoveStatus status) {
        this.status = status;
    }

}
