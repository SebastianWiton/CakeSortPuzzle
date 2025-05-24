package model;

import java.util.*;

public class Plate {
    private static int counter = 0;

    private final int id;
    private List<CakePiece> pieces = new ArrayList<>();
    public static final int MAX_PIECES = 6;

    public Plate(List<CakePiece> pieces) {
        this.id = counter++;
        this.pieces.addAll(pieces);
    }

    public int getId() {
        return id;
    }

    public List<CakePiece> getPieces() {
        return Collections.unmodifiableList(pieces);
    }

    public void addPiece(CakePiece piece) {
        if (pieces.size() < MAX_PIECES) {
            pieces.add(piece);
        }
    }

    public void removePiece(CakePiece piece) {
        pieces.remove(piece);
    }

    public boolean canMatch(Plate other) {
        for (CakePiece p : pieces) {
            for (CakePiece o : other.pieces) {
                if (p.getColor().equals(o.getColor())) return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Plate other = (Plate) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
