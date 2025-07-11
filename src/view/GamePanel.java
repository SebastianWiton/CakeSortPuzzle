package view;

import controller.GameController;
import model.EmbaspManager;
import utilities.SoundPlayer;
import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private GameController controller;
    private TablePanel table;
    private TrayPanel tray;
    private JButton refill;
    private JButton reset;
    private JButton logButton; // Pulsante per la cronologia

    public GamePanel(AppFrame app) {
        setLayout(new BorderLayout());

        // Pulsante per tornare al menu principale
        JButton back = new JButton("← Menu");
        back.addActionListener(e -> {
            SoundPlayer.playSound("button.wav");
            app.showMenu();
        });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(back);
        add(top, BorderLayout.NORTH);

        // Aree di gioco
        table = new TablePanel();
        tray = new TrayPanel(5, 4);
        tray.ensureInitialPlates();
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, table, tray);
        split.setResizeWeight(0.3);
        add(split, BorderLayout.CENTER);

        // Pulsanti di controllo del gioco
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

        logButton = new JButton("Cronologia");

        // Pannello inferiore per i pulsanti
        JPanel bot = new JPanel();
        bot.add(refill);
        bot.add(reset);
        bot.add(logButton);
        add(bot, BorderLayout.SOUTH);

        // Creazione del controller
        controller = new GameController(this, new EmbaspManager("lib/dlv2.exe", "/encodings/cake_rules.asp"));
        controller.generateNewPlates();
    }

    // Metodi getter per permettere al controller di accedere ai componenti
    public GameController getGameController() { return controller; }
    public TablePanel getTablePanel() { return table; }
    public TrayPanel getTrayPanel() { return tray; }
    public JButton getRefillButton() { return refill; }
    public JButton getLogButton() { return logButton; }
}