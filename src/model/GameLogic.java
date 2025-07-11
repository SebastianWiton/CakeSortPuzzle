
package model;

import view.PlateComponent;
import java.util.*;

public class GameLogic {

    // private EmbaspManager ai;

    public GameLogic() {
        // this.ai = ai;
    }

    /**
     * Sposta i pezzi di un dato colore da un piatto donatore a uno ricevente.
     * Gestisce il trasferimento parziale se il ricevente si riempie.
     * @return il numero di pezzi effettivamente spostati.
     */
    public int movePieces(PlateComponent donator, PlateComponent receiver, String color) {
        List<CakePiece> piecesToMove = new ArrayList<>();
        // Trova tutti i pezzi del colore giusto sul donatore
        for (CakePiece piece : donator.getModel().getPieces()) {
            if (piece.getColor().equals(color)) {
                piecesToMove.add(piece);
            }
        }

        int piecesMoved = 0;
        for (CakePiece piece : piecesToMove) {
            // Controlla se c'è ancora spazio sul ricevente
            if (receiver.getModel().getPieces().size() < Plate.MAX_PIECES) {
                donator.getModel().removePiece(piece);
                receiver.getModel().addPiece(piece);
                piecesMoved++;
            } else {
                break; // Il ricevente è pieno, fermati
            }
        }
        return piecesMoved;
    }
}