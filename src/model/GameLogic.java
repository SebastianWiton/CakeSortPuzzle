
package model;

import view.PlateComponent;
import java.util.*;

public class GameLogic {

    // private EmbaspManager ai;

    public GameLogic() {
        // this.ai = ai;
    }
    // Contiene regole immediate del gioco che non richiedono l'AI

    /* Prende un piatto donatore, un ricevente e un colore.
    Prende tutte le fette di quel colore dal donatore e le sposta nel ricevente,
    fermandosi automaticamente se il ricevente raggiunge il limite di 6 fette.
    Restituisce il numero di fette effettivamente spostate.  */
    public int movePieces(PlateComponent donator, PlateComponent receiver, String color) {
        List<CakePiece> piecesToMove = new ArrayList<>();
        for (CakePiece piece : donator.getModel().getPieces()) {
            if (piece.getColor().equals(color)) {
                piecesToMove.add(piece);
            }
        }

        int piecesMoved = 0;
        for (CakePiece piece : piecesToMove) {
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