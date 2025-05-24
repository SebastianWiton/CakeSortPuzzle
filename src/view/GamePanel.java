package view;

import controller.GameController;
import model.EmbaspManager;
import utilities.SoundPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.*;

public class GamePanel extends JPanel {
    private GameController controller;
    private TablePanel table;
    private TrayPanel tray;
    private JButton refill;
    private JButton reset;

    public GamePanel(AppFrame app) {
        setLayout(new BorderLayout());

        // Back button
        JButton back = new JButton("← Menu");
        back.addActionListener(e -> {
            SoundPlayer.playSound("button.wav");
            app.showMenu();});
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(back);
        add(top, BorderLayout.NORTH);

        table = new TablePanel();
        tray = new TrayPanel(5, 5);
        tray.ensureInitialPlates();
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, table, tray);
        split.setResizeWeight(0.3);
        add(split, BorderLayout.CENTER);

        refill = new JButton("Nuovi piatti");
        refill.setEnabled(false);
        refill.addActionListener(e -> {
            SoundPlayer.playSound("button.wav");
            controller.generateNewPlates();
            refill.setEnabled(false);
        });

        reset = new JButton("Reset Gioco");
        reset.addActionListener(e -> {
            SoundPlayer.playSound("button.wav");
            controller.resetGame();
        });
        JPanel bot = new JPanel();
        bot.add(refill);
        bot.add(reset);
        add(bot, BorderLayout.SOUTH);


        controller = new GameController(this, new EmbaspManager("lib/dlv2.exe", "/encodings/cake_rules.asp"));
        controller.generateNewPlates();
    }

    public GameController getGameController() {
        return controller;
    }
    public TablePanel getTablePanel() { return table; }
    public TrayPanel getTrayPanel() { return tray; }
    public JButton getRefillButton() { return refill; }
}