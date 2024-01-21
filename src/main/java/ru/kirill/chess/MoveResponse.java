package ru.kirill.chess;

import ru.kirill.chess.model.AfterMoveStatus;
import ru.kirill.chess.model.TieType;

public class MoveResponse {
    private AfterMoveStatus status;
    private TieType tieType;

    public MoveResponse(AfterMoveStatus status, TieType tieType) {
        this.status = status;
        this.tieType = tieType;
    }

    public AfterMoveStatus getStatus() {
        return status;
    }

    public void setStatus(AfterMoveStatus status) {
        this.status = status;
    }

    public TieType getTieType() {
        return tieType;
    }

    public void setTieType(TieType tieType) {
        this.tieType = tieType;
    }
}
