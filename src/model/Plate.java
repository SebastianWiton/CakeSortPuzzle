package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Plate {
    /* Per garantire di distinguere due piatti con lo steso contenuto */

    /* Gli ID sono alti pechè internalCounterr è static e non viene mai resettato,
    e ogni componente della griglia
    (anche i buchi) crea un'istanza di Plate al momento della sua creazione. */

    private static int internalCounter = 0;
    private final int internalId;
    // ID visualizzato all'utente , assegnato dal controller.
    private int displayId;

    // Lista dei pezzi di torta sul piatto
    private List<CakePiece> pieces = new ArrayList<>();
    // Numero massimo di pezzi che un piatto può contenere
    public static final int MAX_PIECES = 6;

    // Crea un piatto con una lista iniziale di pezzi
    public Plate(List<CakePiece> pieces) {
        this.internalId = internalCounter++;
        // per piatti non ancora in gioco
        this.displayId = -1;
        this.pieces.addAll(pieces);
    }

    public int getDisplayId() {
        return displayId;
    }

    public void setDisplayId(int displayId) {
        this.displayId = displayId;
    }

    public int getInternalId() {
        return internalId;
    }

    // Restituisce una lista non modificabile dei pezzi
    public List<CakePiece> getPieces() {
        return Collections.unmodifiableList(pieces);
    }

    // Aggiunge un pezzo al piatto se non è pieno
    public void addPiece(CakePiece piece) {
        if (pieces.size() < MAX_PIECES) {
            this.pieces.add(piece);
        }
    }

    // Rimuove un pezzo specifico dal piatto
    public void removePiece(CakePiece piece) {
        this.pieces.remove(piece);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Plate other = (Plate) obj;
        return this.internalId == other.internalId;
    }


    @Override
    public int hashCode() {
        return Objects.hash(internalId);
    }

    // Conta quanti pezzi di un determinato colore sono presenti sul piatto
    public int countPiecesOfColor(String color) {
        int count = 0;
        for (CakePiece piece : pieces) {
            if (piece.getColor().equals(color)) {
                count++;
            }
        }
        return count;
    }

    // Crea una stringa descrittiva del contenuto del piatto
    public String getContentsAsString() {
        if (pieces.isEmpty()) {
            return "vuoto";
        }
        // Raggruppa i pezzi per colore e conta le occorrenze
        return pieces.stream()
                .collect(Collectors.groupingBy(CakePiece::getColor, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> entry.getValue() + " " + entry.getKey())
                .collect(Collectors.joining(", "));
    }
}