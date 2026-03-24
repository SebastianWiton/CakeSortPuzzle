package view;

import model.CakePiece;
import model.Plate;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PlateComponent extends JPanel {
    private Plate model;
    private boolean isHole;
    private int size;
    private static final int SLICE_SIZE = 32;

    private static Map<String, Image> plateImages = new HashMap<>();
    private static Map<String, Image> sliceImages = new HashMap<>();

    public PlateComponent() { this(false, 128); }

    public PlateComponent(boolean isHole, int size) {
        this.isHole = isHole;
        this.size = size;
        setOpaque(false);

        model = new Plate(new ArrayList<>());
        setPreferredSize(new Dimension(this.size, this.size));

        if (!isHole) {
            generateInitialPieces();
        }
    }

    public void generateInitialPieces() {
        this.isHole = false;
        List<CakePiece> pieces = new ArrayList<>();
        int count = 1 + new Random().nextInt(4); // Da 1 a 4 pezzi
        for (int i = 0; i < count; i++) {
            pieces.add(new CakePiece(randomColor()));
        }
        model = new Plate(pieces);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (isHole) {
            g.setColor(new Color(180, 180, 180, 100));
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        Image bgImage = getPlateImage();
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }

        List<CakePiece> currentPieces = model.getPieces();
        if (currentPieces.isEmpty()) {
            return;
        }

        int count = currentPieces.size();
        int cols = (count > 4) ? 3 : 2;
        int rows = (int) Math.ceil((double) count / cols);
        int totalW = cols * SLICE_SIZE;
        int totalH = rows * SLICE_SIZE;
        int xOff = (getWidth() - totalW) / 2;
        int yOff = (getHeight() - totalH) / 2;

        for (int i = 0; i < count; i++) {
            int r = i / cols;
            int c = i % cols;
            Image sliceImage = getSliceImage(currentPieces.get(i).getColor());
            if (sliceImage != null) {
                g.drawImage(sliceImage, xOff + c * SLICE_SIZE, yOff + r * SLICE_SIZE, SLICE_SIZE, SLICE_SIZE, this);
            }
        }

        int id = model.getDisplayId();
        // l'id è disegnato solo se non vale -1
        if (id != -1) {
            String idString = String.valueOf(id);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.setColor(Color.BLACK);
            g.drawString(idString, getWidth() - 15, getHeight() - 5);
        }
    }

    private Image getPlateImage() {
        String key = "plate" + this.size;
        if (!plateImages.containsKey(key)) {
            try {
                BufferedImage bg = ImageIO.read(getClass().getResource("/img/" + key + ".png"));
                plateImages.put(key, bg.getScaledInstance(this.size, this.size, Image.SCALE_SMOOTH));
            } catch (IOException | IllegalArgumentException e) {
                plateImages.put(key, null); // Metti null per non riprovare
            }
        }
        return plateImages.get(key);
    }

    private Image getSliceImage(String color) {
        String key = "slice_" + color;
        if (!sliceImages.containsKey(key)) {
            try {
                BufferedImage si = ImageIO.read(getClass().getResource("/img/" + key + ".png"));
                sliceImages.put(key, si); // Non serve lo scaling se sono già della dimensione giusta
            } catch (IOException | IllegalArgumentException e) {
                sliceImages.put(key, null);
            }
        }
        return sliceImages.get(key);
    }

    public boolean isComplete() {
        if (isHole || model.getPieces().isEmpty() || model.getPieces().size() != Plate.MAX_PIECES) {
            return false;
        }
        String firstColor = model.getPieces().get(0).getColor();
        for (CakePiece piece : model.getPieces()) {
            if (!piece.getColor().equals(firstColor)) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmptyAfterMove() {
        return !isHole && model.getPieces().isEmpty();
    }

    public Set<String> getUniqueColors() {
        Set<String> colors = new HashSet<>();
        for (CakePiece piece : model.getPieces()) {
            colors.add(piece.getColor());
        }
        return colors;
    }

    public Plate getModel() { return model; }

    private String randomColor() {
        String[] cols = {"blue","brown","orange","purple","red","yellow"};
        return cols[new Random().nextInt(cols.length)];
    }

    public boolean isHoleComponent() {
        return isHole;
    }
}