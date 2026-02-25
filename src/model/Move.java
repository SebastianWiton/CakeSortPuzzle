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
    public int getDonatorId() { return donatorId; }
    public void setDonatorId(int donatorId) { this.donatorId = donatorId; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

    public String getColor() { return color; }
    public void setColor(String color) {
        // Rimuove le virgolette se presenti
        this.color = color.replace("\"", "");
    }
}