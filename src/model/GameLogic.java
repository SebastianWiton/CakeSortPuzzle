
package model;

import view.PlateComponent;
import java.util.*;

public class GameLogic {

    // private EmbaspManager ai;

    public GameLogic() {
        // this.ai = ai;
    }
    // Contiene regole immediate del gioco che non richiedono l'AI

    /* Prende in input due componenti grafici (donator e receiver) e un colore
    * Accede ai loro modelli
    * Crea una lista di tutti i pezzi del colore specificato presenti sul donatore
    * Itera sulla lista e per ogni pezzo, lo rimuove dal modello del donatore e lo aggiunge
    * a quello del ricevente, a meno che il ricevente sia pieno  */
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