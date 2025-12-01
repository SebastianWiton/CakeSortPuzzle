package model;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

@Id("place")
public class Place {
    @Param(0) private int x;
    @Param(1) private int y;
    @Param(2) private int id;

    public Place() {}
    public Place(int x, int y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getId() { return id; }
}
