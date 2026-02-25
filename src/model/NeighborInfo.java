package model;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

// Mappa la classe Java NeighborInfo sul fatto ASP neighborInfo(ID1, ID2)
@Id("neighborInfo")
public class NeighborInfo {
    @Param(0)
    public int id1;
    @Param(1)
    public int id2;

    public NeighborInfo() {}

    public NeighborInfo(int id1, int id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    public int getId1() { return id1; }
    public void setId1(int id1) { this.id1 = id1; }
    public int getId2() { return id2; }
    public void setId2(int id2) { this.id2 = id2; }
    @Override
    public String toString() {
        return "neighborInfo(" + id1 + "," + id2 + ").";
    }
}