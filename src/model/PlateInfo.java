package model;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

// Un'oggetto di questa classe corrisponde a un fatto ASP chiamato plateInfo
@Id("plateInfo")
public class PlateInfo {
    @Param(0)
    private int id;
    @Param(1)
    private String color;
    @Param(2)
    private int quantity;

    public PlateInfo() {}

    public PlateInfo(int id, String color, int quantity) {
        this.id = id;
        this.color = color;
        this.quantity = quantity;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    @Override
    public String toString() {
        String idString = String.valueOf(id);
        return "plateInfo(" + idString + ", " + color + ", " + quantity + ").";
    }
}