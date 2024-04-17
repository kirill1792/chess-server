package ru.kirill.chess.model;

import ru.kirill.chess.model.figure.Figure;
import ru.kirill.chess.model.figure.King;

import java.util.List;

public class Player {

    private String name;
    private int id;
    private String color;
    private List<Figure> myFigures;
    private King myKing;
    private int rating;

    public Player(String name, int id, int rating) {
        this.name = name;
        this.id = id;
        this.rating = rating;
    }

    public Player(User user) {
        this.name = user.getName();
        this.id = user.getId();
        this.rating = user.getRating();
    }

    public void setMyKing() {
        for (Figure figure : myFigures) {
            if (figure instanceof King) {
                myKing = (King) figure;
                break;
            }
        }
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public King getMyKing() {
        return myKing;
    }

    public List<Figure> getMyFigures() {
        return myFigures;
    }

    public void setMyFigures(List<Figure> myFigures) {
        this.myFigures = myFigures;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", color='" + color + '\'' +
                ", myFigures=" + myFigures +
                ", myKing=" + myKing +
                '}';
    }
}
