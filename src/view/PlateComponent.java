package view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.*;
import java.util.List;

import model.*;


public class PlateComponent extends JLayeredPane {
    private Plate model;
    private boolean isHole;
    private int size;
    private static final int SLICE_SIZE = 32;

    public PlateComponent() { this(false, 64); }
    public PlateComponent(boolean isHole, int size) {
        this.isHole = isHole;
        this.size = size;
        setLayout(null);
        setOpaque(false);
        model = new Plate(new ArrayList<>());
        if (isHole) {
            size = 64;
            setPreferredSize(new Dimension(size,size));
            setBackground(new Color(180,180,180,100));
            setOpaque(true);
        } else {
            generatePlate();
        }
    }

    public void generatePlate() {
        removeAll();
        List<CakePiece> pieces = new ArrayList<>();
        int count = 1 + new Random().nextInt(Plate.MAX_PIECES-1);
        for (int i = 0; i < count; i++) {
            pieces.add(new CakePiece(randomColor()));
        }
        model = new Plate(pieces);
        size = (count <= 3) ? 64 : 128;
        setPreferredSize(new Dimension(size,size));
        try {
            BufferedImage bg = ImageIO.read(getClass().getResource("/img/plate"+size+".png"));
            Image scaledBg = bg.getScaledInstance(size,size,Image.SCALE_SMOOTH);
            JLabel bgLabel = new JLabel(new ImageIcon(scaledBg));
            bgLabel.setBounds(0,0,size,size);
            add(bgLabel, Integer.valueOf(0));
        } catch(IOException e) { e.printStackTrace(); }
        int cols = (int)Math.ceil(Math.sqrt(count));
        int rows = (int)Math.ceil((double)count/cols);
        int totalW = cols*SLICE_SIZE, totalH = rows*SLICE_SIZE;
        int xOff = (size-totalW)/2, yOff = (size-totalH)/2;
        for (int i = 0; i < count; i++) {
            int r = i/cols, c = i%cols;
            try {
                BufferedImage si = ImageIO.read(getClass().getResource("/img/slice_"+pieces.get(i).getColor()+".png"));
                Image sc = si.getScaledInstance(SLICE_SIZE,SLICE_SIZE,Image.SCALE_SMOOTH);
                JLabel sl = new JLabel(new ImageIcon(sc));
                sl.setBounds(xOff + c*SLICE_SIZE, yOff + r*SLICE_SIZE, SLICE_SIZE, SLICE_SIZE);

                add(sl, Integer.valueOf(1));
            } catch(IOException e) { e.printStackTrace(); }
        }
        revalidate(); repaint();
    }

    public void refresh() { generatePlate(); }
    public Plate getModel() { return model; }
    private String randomColor() {
        String[] cols = {"blue","brown","orange","purple","red","yellow"};
        return cols[new Random().nextInt(cols.length)];
    }

    public void setHole(boolean isHole) {
        this.isHole = isHole;
    }

    public boolean isHoleComponent() {
        return isHole;
    }

}