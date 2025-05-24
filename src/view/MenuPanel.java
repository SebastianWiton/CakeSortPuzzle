package view;

import utilities.SoundPlayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class MenuPanel extends JPanel {
    private BufferedImage background;

    public MenuPanel(AppFrame app) throws IOException {
        // Carica l'immagine di sfondo
        try {
            background = ImageIO.read(getClass().getResource("/img/background.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false); // per avere il disegno personalizzato


        // Spazio superiore
        add(Box.createVerticalStrut(80));

        // Immagine
        try (InputStream is = getClass().getResourceAsStream("/img/img1.jpg")) {
            BufferedImage image = ImageIO.read(is);
            Image scaledImage = image.getScaledInstance(340, 260, Image.SCALE_SMOOTH);
            JLabel cakeImage = new JLabel(new ImageIcon(scaledImage));
            cakeImage.setAlignmentX(Component.CENTER_ALIGNMENT);
            cakeImage.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3, true));
            add(cakeImage);
        } catch (IOException e) {
            e.printStackTrace();
        }


        add(Box.createVerticalStrut(80));

        // Bottone Play Classic
        JButton playBtn = new JButton("PLAY CLASSIC");
        playBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        playBtn.setPreferredSize(new Dimension(200, 60));
        playBtn.setMaximumSize(new Dimension(200, 60));
        playBtn.setFont(new Font("Arial", Font.BOLD, 18));
        playBtn.setForeground(Color.WHITE);
        playBtn.setBackground(new Color(0, 180, 0)); // verde acceso
        playBtn.setFocusPainted(false);
        playBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3, true));
        playBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Per l'hover
        playBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                playBtn.setBackground(new Color(0, 140, 0));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                playBtn.setBackground(new Color(0, 180, 0));
            }
        });

        playBtn.addActionListener(e -> {
            SoundPlayer.playSound("click.wav");
            app.showGame();
        });
        add(playBtn);

        add(Box.createVerticalGlue());
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Disegna lo sfondo ridimensionato
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }

}

