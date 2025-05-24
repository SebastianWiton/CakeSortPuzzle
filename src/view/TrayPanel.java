package view;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TrayPanel extends JPanel {
    private static final Color TRAY_COLOR = new Color(160,160,160);
    private int rows, cols;
    private PlateComponent[] holes;
    private int holeSize = 128;

    public TrayPanel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        setBackground(TRAY_COLOR);
        setLayout(new GridLayout(rows,cols,10,10));
        holes = new PlateComponent[rows*cols];
        for (int i = 0; i < holes.length; i++) {
            holes[i] = new PlateComponent(true, holeSize);
            add(holes[i]);
        }
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    // Ripristina completamente tutti i buchi e ci assegna piatti iniziali in modo casuale
    public void resetHoles() {
        removeAll();
        holes = new PlateComponent[rows * cols];
        for (int i = 0; i < holes.length; i++) {
            holes[i] = new PlateComponent(true, holeSize);
            add(holes[i]);
        }
        ensureInitialPlates();
        revalidate();
        repaint();
    }

    // Genera un piatto in modo casuale in circa holes.length/3 buchi
    public void ensureInitialPlates() {
        int count = holes.length/3;
        List<Integer> idxs = new ArrayList<>();
        for (int i = 0; i < holes.length; i++) idxs.add(i);
        Collections.shuffle(idxs);
        for (int i = 0; i < count; i++) {
            holes[idxs.get(i)].generatePlate();
        }
    }

    // Trova il buco sotto il punto di drop
    public PlateComponent getHoleAtPoint(Point point) {
        for (Component comp : getComponents()) {
            if (comp instanceof PlateComponent && comp.getBounds().contains(point)) {
                return (PlateComponent) comp;
            }
        }
        return null;
    }

    // Sostituisce un buco con un piatto
    public void replaceHoleWithPlate(PlateComponent targetHole, PlateComponent plate) {
        int index = -1;
        Component[] comps = getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] == targetHole) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            holes[index] = plate;
            remove(index);
            add(plate, index);
            revalidate();
            repaint();
        }
    }

    // Controlla se quel buco è già occupato
    public boolean isHoleOccupied(PlateComponent hole) {
        // Se hole contiene un piatto, ritorna true, altrimenti false
        return hole.getModel().getPieces().size() > 0;
    }


    public Collection<PlateComponent> getAllHoles() {
        return Arrays.asList(holes);
    }
}