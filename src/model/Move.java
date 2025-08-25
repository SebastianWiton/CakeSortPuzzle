package model;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

// Mappa la classe Java Move sul predicato di output move(DonatorID, ReceiverID, Color)
@Id("move")
public class Move {
    @Param(0)
    public int donatorId;
    @Param(1)
    public int receiverId;
    @Param(2)
    public String color;

    public Move() {}

    public Move(int donatorId, int receiverId, String color) {
        this.donatorId = donatorId;
        this.receiverId = receiverId;
        this.color = color;
    }
}