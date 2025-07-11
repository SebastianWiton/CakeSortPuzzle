package view;

import model.Plate;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrayPanel extends JPanel {
    private static final Color TRAY_COLOR = new Color(160, 160, 160);
    private int rows, cols;
    private PlateComponent[] holes;
    private int holeSize = 128;

    public TrayPanel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        setBackground(TRAY_COLOR);
        setLayout(new GridLayout(rows, cols, 10, 10));
        holes = new PlateComponent[rows * cols];
        for (int i = 0; i < holes.length; i++) {
            holes[i] = new PlateComponent(true, holeSize);
            add(holes[i]);
        }
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    // Ripristina completamente tutti i buchi e assegna piatti iniziali
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

    // Genera piatti iniziali in posizioni casuali e isolate
    public void ensureInitialPlates() {
        int count = holes.length / 3;
        List<Integer> availableIndexes = new ArrayList<>();
        for (int i = 0; i < holes.length; i++) {
            int r = i / cols;
            int c = i % cols;
            if (r > 0 && r < rows - 1 && c > 0 && c < cols - 1) {
                availableIndexes.add(i);
            }
        }

        Collections.shuffle(availableIndexes);
        Set<Integer> used = new HashSet<>();
        int placed = 0;
        for (int i : availableIndexes) {
            if (placed >= count) break;
            if (isIsolated(i, used)) {
                PlateComponent newPlate = new PlateComponent(false, holeSize);
                while (newPlate.isComplete()) {
                    newPlate.generateInitialPieces();
                }
                remove(holes[i]);
                holes[i] = newPlate;
                add(newPlate, i);
                used.add(i);
                placed++;
            }
        }
        revalidate();
        repaint();
    }

    // Controlla se un indice è isolato rispetto ai piatti già piazzati
    private boolean isIsolated(int index, Set<Integer> used) {
        int r = index / cols;
        int c = index % cols;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = r + dr;
                int nc = c + dc;
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                    if (used.contains(nr * cols + nc)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Trova il buco (o piatto) sotto un punto specifico
    public PlateComponent getHoleAtPoint(Point point) {
        Component comp = getComponentAt(point);
        if (comp instanceof PlateComponent) {
            return (PlateComponent) comp;
        }
        return null;
    }

    // Sostituisce un componente buco con un componente piatto
    public void replaceHoleWithPlate(PlateComponent targetHole, PlateComponent plate) {
        for (int i = 0; i < holes.length; i++) {
            if (holes[i] == targetHole) {
                holes[i] = plate;
                remove(targetHole);
                add(plate, i);
                revalidate();
                repaint();
                break;
            }
        }
    }

    // Restituisce una lista di tutti i piatti (non buchi) presenti sul vassoio
    public List<PlateComponent> getAllPlates() {
        List<PlateComponent> plateList = new ArrayList<>();
        for (PlateComponent pc : holes) {
            if (!pc.isHoleComponent()) {
                plateList.add(pc);
            }
        }
        return plateList;
    }

    // Trova i vicini di un dato piatto
    public List<PlateComponent> getNeighbors(PlateComponent plate) {
        List<PlateComponent> neighbors = new ArrayList<>();
        int plateIndex = -1;
        for (int i = 0; i < holes.length; i++) {
            if (holes[i] == plate) {
                plateIndex = i;
                break;
            }
        }
        if (plateIndex == -1) return neighbors;

        int r = plateIndex / cols;
        int c = plateIndex % cols;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr;
                int nc = c + dc;
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                    PlateComponent neighbor = holes[nr * cols + nc];
                    if (!neighbor.isHoleComponent()) {
                        neighbors.add(neighbor);
                    }
                }
            }
        }
        return neighbors;
    }

    // Rimuove i piatti completati o vuoti e restituisce una lista dei loro modelli
    public List<Plate> getAndRemoveCompletedOrEmptyPlates() {
        List<Plate> removedPlates = new ArrayList<>();
        boolean changed = false;
        for (int i = 0; i < holes.length; i++) {
            PlateComponent pc = holes[i];
            if (!pc.isHoleComponent() && (pc.isComplete() || pc.isEmptyAfterMove())) {
                removedPlates.add(pc.getModel());
                remove(pc);
                holes[i] = new PlateComponent(true, holeSize);
                add(holes[i], i);
                changed = true;
            }
        }
        if (changed) {
            revalidate();
            repaint();
        }
        return removedPlates;
    }

    public boolean removeCompletedOrEmptyPlates() {
        return !getAndRemoveCompletedOrEmptyPlates().isEmpty();
    }
}