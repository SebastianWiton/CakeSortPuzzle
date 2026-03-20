package model;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

@Id("piattoDaInserire")
public class PiattoDaInserireInfo {
    @Param(0)
    public int id;

    // private String color;
    // private int quantity;

    public PiattoDaInserireInfo() {}
    public PiattoDaInserireInfo(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    @Override
    public String toString() {
        return "piattoDaInserire(" + id + ").";
    }
}