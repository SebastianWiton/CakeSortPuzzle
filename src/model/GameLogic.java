package model;

import java.util.*;

public class GameLogic {
    private EmbaspManager ai;

    public GameLogic(EmbaspManager ai) {
        this.ai = ai;
    }
// va cambiato
    public void matchAndMove(Plate from, Plate to) {
        Iterator<CakePiece> it = from.getPieces().iterator();
        while (it.hasNext()) {
            CakePiece p = it.next();
            if (to.getPieces().stream().anyMatch(o -> o.getColor().equals(p.getColor()))) {
                it.remove();
                to.addPiece(p);
            }
        }
    }
}