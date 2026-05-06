package view;

import model.Plate;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.CellaInfo;

public class TrayPanel extends JPanel {
    private static final Color TRAY_COLOR = new Color(160, 160, 160);
    private int rows, cols;
    private PlateComponent[] slots;
    private int slotSize = 128;

    public TrayPanel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        setBackground(TRAY_COLOR);
        setLayout(new GridLayout(rows, cols, 10, 10));
        slots = new PlateComponent[rows * cols];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new PlateComponent(true, slotSize);
            add(slots[i]);
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public void resetHoles() {
        removeAll();
        slots = new PlateComponent[rows * cols];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new PlateComponent(true, slotSize);
            add(slots[i]);
        }
        ensureInitialPlates();
        revalidate();
        repaint();
    }

    public void ensureInitialPlates() {
        int count = slots.length / 3;
        List<Integer> availableIndexes = new ArrayList<>();
        for (int i = 0; i < slots.length; i++) {
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
                PlateComponent newPlate = new PlateComponent(false, slotSize);
                while (newPlate.isComplete()) {
                    newPlate.generateInitialPieces();
                }
                remove(slots[i]);
                slots[i] = newPlate;
                add(newPlate, i);
                used.add(i);
                placed++;
            }
        }
        revalidate();
        repaint();
    }

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

    public PlateComponent getHoleAtPoint(Point point) {
        Component comp = getComponentAt(point);
        if (comp instanceof PlateComponent) {
            return (PlateComponent) comp;
        }
        return null;
    }

    public void replaceHoleWithPlate(PlateComponent targetHole, PlateComponent plate) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == targetHole) {
                slots[i] = plate;
                remove(targetHole);
                add(plate, i);
                revalidate();
                repaint();
                break;
            }
        }
    }

    public List<PlateComponent> getAllPlates() {
        List<PlateComponent> plateList = new ArrayList<>();
        for (PlateComponent pc : slots) {
            if (!pc.isHoleComponent()) {
                plateList.add(pc);
            }
        }
        return plateList;
    }

    public List<PlateComponent> getNeighbors(PlateComponent plate) {
        List<PlateComponent> neighbors = new ArrayList<>();
        int plateIndex = -1;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == plate) {
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
                    PlateComponent neighbor = slots[nr * cols + nc];
                    if (!neighbor.isHoleComponent()) {
                        neighbors.add(neighbor);
                    }
                }
            }
        }
        return neighbors;
    }

    public List<Plate> getAndRemoveCompletedOrEmptyPlates() {
        List<Plate> removedPlates = new ArrayList<>();
        boolean changed = false;
        for (int i = 0; i < slots.length; i++) {
            PlateComponent pc = slots[i];
            if (!pc.isHoleComponent() && (pc.isComplete() || pc.isEmptyAfterMove())) {
                removedPlates.add(pc.getModel());
                remove(pc);
                slots[i] = new PlateComponent(true, slotSize);
                add(slots[i], i);
                changed = true;
            }
        }
        if (changed) {
            revalidate();
            repaint();
        }
        return removedPlates;
    }

    public PlateComponent getPlateByDisplayId(int displayId) {
        for (PlateComponent pc : getAllPlates()) {
            if (pc.getModel().getDisplayId() == displayId) {
                return pc;
            }
        }
        return null;
    }

    public PlateComponent getSlotComponent(int index) {
        if (index >= 0 && index < slots.length) {
            return slots[index];
        }
        return null;
    }

    public void addCellaFacts(List<Object> facts) {
        for (int i = 0; i < slots.length; i++) {
            int row = i / cols + 1; // Coordinate da 1 a N
            int col = i % cols + 1;

            PlateComponent pc = slots[i];
            int plateId = pc.isHoleComponent() ? -1 : pc.getModel().getDisplayId();

            facts.add(new CellaInfo(row, col, plateId));
        }
    }

    public void highlightSlot(int x, int y) {
        int index = (x - 1) * cols + (y - 1);
        if (index >= 0 && index < slots.length) {
            Component c = getComponent(index);
            if (c instanceof PlateComponent && ((PlateComponent) c).isHoleComponent()) {
                // Salva il bordo originale per ripristinarlo
                Border originalBorder = ((JComponent) c).getBorder();
                ((JComponent) c).setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));

                Timer highlightTimer = new Timer(2000, e -> {
                    ((JComponent) c).setBorder(originalBorder); // Ripristina il bordo
                    ((Timer)e.getSource()).stop();
                });
                highlightTimer.setRepeats(false);
                highlightTimer.start();
            }
        }
    }
}