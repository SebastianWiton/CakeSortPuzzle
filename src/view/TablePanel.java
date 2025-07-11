package view;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TablePanel extends JPanel {
    private static final Color TABLE_COLOR = new Color(200,180,150);
    private List<PlateComponent> plates = new ArrayList<>();

    public TablePanel() {
        setBackground(TABLE_COLOR);
        setLayout(new FlowLayout(FlowLayout.CENTER,40,20));
        setPreferredSize(new Dimension(100, 150));
    }


    public boolean removePlate(PlateComponent pc) {
        if (plates.remove(pc)) {
            remove(pc);
            revalidate();
            repaint();
            return true;
        }
        return false;
    }

    public boolean isEmpty() { return plates.isEmpty(); }
    public List<PlateComponent> getPlateComponents() { return plates; }
}