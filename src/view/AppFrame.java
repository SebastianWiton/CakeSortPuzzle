package view;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class AppFrame extends JFrame {
    private CardLayout cards = new CardLayout();
    private JPanel root = new JPanel(cards);

    public AppFrame() throws IOException {
        setTitle("Cake Sort Puzzle 3D");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        root.add(new MenuPanel(this), "MENU");
        root.add(new GamePanel(this), "GAME");

        add(root);
        cards.show(root, "MENU");
    }

    // Cambio schermata
    public void showGame() { cards.show(root, "GAME"); }
    public void showMenu() { cards.show(root, "MENU"); }
}

