package model;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

// Mappa la classe Java CellaInfo sul fatto ASP cella(X, Y, ID).
@Id("cella")
public class CellaInfo {
    @Param(0)
    private int x;

    @Param(1)
    private int y;

    @Param(2)
    private int id;

    public CellaInfo() {}

    public CellaInfo(int x, int y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    @Override
    public String toString() {
        // Se l'ID è -1, viene stampato. Altrimenti, viene formattato come "piattoX".
        String idString = (id == -1) ? "-1" : "piatto" + id;
        return "cella(" + x + "," + y + "," + idString + ").";
    }
}