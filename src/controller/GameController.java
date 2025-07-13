package controller;

import model.*;
import utilities.SoundPlayer;
import view.GamePanel;
import view.PlateComponent;
import view.TablePanel;
import view.TrayPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class GameController {
    private final GamePanel panel;
    private final EmbaspManager manager;
    private final GameLogic gameLogic;
    private Timer gameLoopTimer;

    private List<String> gameLog;
    private int logMoveCounter;
    private int plateDisplayIdCounter;

    public GameController(GamePanel panel, EmbaspManager manager) {
        this.panel = panel;
        this.manager = manager;
        this.gameLogic = new GameLogic();
        this.gameLog = new ArrayList<>();

        setupControllerState();
        setupDragAndDrop();
        setupGameLoopTimer();
        setupLogButton();
    }

    private void setupControllerState() {
        this.logMoveCounter = 1;
        this.plateDisplayIdCounter = 1;
        this.gameLog.clear();

        for (PlateComponent pc : panel.getTrayPanel().getAllPlates()) {
            pc.getModel().setDisplayId(plateDisplayIdCounter++);
        }
    }

    private void setupGameLoopTimer() {
        gameLoopTimer = new Timer(250, e -> runGameLogicStep());
        gameLoopTimer.setRepeats(true);
    }

    private void setupLogButton() {
        panel.getLogButton().addActionListener(e -> showGameLog());
    }

    private void showGameLog() {
        if (gameLog.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "Nessuna mossa ancora registrata.", "Cronologia Mosse", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder logText = new StringBuilder("<html>");
        for (String entry : gameLog) {
            logText.append(entry).append("<br>");
        }
        logText.append("</html>");

        JLabel logLabel = new JLabel(logText.toString());
        JScrollPane scrollPane = new JScrollPane(logLabel);
        scrollPane.setPreferredSize(new Dimension(500, 350));

        JOptionPane.showMessageDialog(panel, scrollPane, "Cronologia Mosse", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupDragAndDrop() {
        TablePanel table = panel.getTablePanel();
        TrayPanel tray = panel.getTrayPanel();

        for (PlateComponent plate : table.getPlateComponents()) {
            setupPlateDrag(plate);
        }

        new DropTarget(tray, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    Transferable tr = dtde.getTransferable();
                    if (tr.isDataFlavorSupported(PlateTransferable.plateFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                        Plate plateModel = (Plate) tr.getTransferData(PlateTransferable.plateFlavor);
                        Point pt = dtde.getLocation();
                        PlateComponent targetHole = tray.getHoleAtPoint(pt);
                        PlateComponent draggedPlate = findDraggedComponent(plateModel);

                        if (targetHole != null && draggedPlate != null && targetHole.isHoleComponent()) {
                            SoundPlayer.playSound("drag-drop.wav");
                            handleDrop(targetHole, draggedPlate);
                        }

                        dtde.dropComplete(true);
                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception e) {
                    dtde.rejectDrop();
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupPlateDrag(PlateComponent plate) {
        plate.setTransferHandler(new TransferHandler("plate") {
            @Override
            public int getSourceActions(JComponent c) { return MOVE; }
            @Override
            protected Transferable createTransferable(JComponent c) { return new PlateTransferable((PlateComponent) c); }
        });

        plate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                JComponent comp = (JComponent) e.getSource();
                comp.getTransferHandler().exportAsDrag(comp, e, TransferHandler.MOVE);
                SoundPlayer.playSound("drag-drop.wav");
            }
        });
    }

    public void handleDrop(PlateComponent targetHole, PlateComponent draggedPlate) {
        if (gameLoopTimer.isRunning()) {
            gameLoopTimer.stop();
        }

        draggedPlate.getModel().setDisplayId(plateDisplayIdCounter++);

        TablePanel table = panel.getTablePanel();
        TrayPanel tray = panel.getTrayPanel();

        tray.replaceHoleWithPlate(targetHole, draggedPlate);
        table.removePlate(draggedPlate);

        processGameLogic(draggedPlate);

        if (table.isEmpty()) {
            panel.getRefillButton().setEnabled(true);
        }
    }

    private void processGameLogic(PlateComponent justPlacedPlate) {
        TrayPanel tray = panel.getTrayPanel();
        String placedContent = justPlacedPlate.getModel().getContentsAsString();
        gameLog.add("--- Mossa #" + logMoveCounter++ + ": Piazzato Piatto " + justPlacedPlate.getModel().getDisplayId() + " con [" + placedContent + "] ---");

        // La logica di aggregazione iniziale può rimanere in Java per semplicità
        for (PlateComponent neighbor : tray.getNeighbors(justPlacedPlate)) {
            for (String color : neighbor.getUniqueColors()) {
                if (justPlacedPlate.getUniqueColors().contains(color)) {
                    logAction("Aggregazione", neighbor, justPlacedPlate, color, gameLogic.movePieces(neighbor, justPlacedPlate, color));
                }
            }
        }

        logRemovedPlates(tray.getAndRemoveCompletedOrEmptyPlates(), " (dopo aggregazione)");
        tray.revalidate();
        tray.repaint();

        gameLoopTimer.start();
    }

    private void runGameLogicStep() {
        TrayPanel tray = panel.getTrayPanel();

        // Converte lo stato del gioco in fatti ASP
        List<Object> facts = new ArrayList<>();
        List<PlateComponent> allPlates = tray.getAllPlates();
        for (PlateComponent pc : allPlates) {
            int plateId = pc.getModel().getDisplayId();
            if (plateId == -1) continue;
            for (String color : pc.getUniqueColors()) {
                int quantity = pc.getModel().countPiecesOfColor(color);
                facts.add(new PlateInfo(plateId, color, quantity));
            }
            for (PlateComponent neighbor : tray.getNeighbors(pc)) {
                facts.add(new NeighborInfo(plateId, neighbor.getModel().getDisplayId()));
            }
        }

        // Chiede a EmbASP la prossima mossa
        Move nextMove = manager.getNextMove(facts);

        boolean actionTaken = false;
        if (nextMove != null) {
            // Esegue la mossa suggerita da EmbASP
            PlateComponent donator = tray.getPlateByDisplayId(nextMove.donatorId);
            PlateComponent receiver = tray.getPlateByDisplayId(nextMove.receiverId);
            String color = nextMove.color;

            if (donator != null && receiver != null) {
                int moved = gameLogic.movePieces(donator, receiver, color);
                if (moved > 0) {
                    logAction("Catena (ASP)", donator, receiver, color, moved);
                    actionTaken = true;
                }
            }
        }

        // Se non ci sono mosse suggerite da ASP, prova a pulire i piatti
        if (!actionTaken) {
            List<Plate> removedPlates = tray.getAndRemoveCompletedOrEmptyPlates();
            if (!removedPlates.isEmpty()) {
                logRemovedPlates(removedPlates, " (in catena)");
                actionTaken = true;
            }
        }

        // Aggiorna la vista e controlla se fermare il timer
        tray.revalidate();
        tray.repaint();

        if (!actionTaken) {
            gameLoopTimer.stop();
        }
    }

    // Metodi di logging e supporto
    private void logAction(String context, PlateComponent donator, PlateComponent receiver, String color, int moved) {
        if (moved == 0) return;
        gameLog.add("  > " + context + ": " + moved + " pezzi " + color + " spostati da Piatto " + donator.getModel().getDisplayId() + " a Piatto " + receiver.getModel().getDisplayId());
    }

    private void logRemovedPlates(List<Plate> removedPlates, String context) {
        for (Plate removedPlate : removedPlates) {
            gameLog.add("  > Piatto " + removedPlate.getDisplayId() + " rimosso" + context + ".");
        }
    }

    private PlateComponent findDraggedComponent(Plate plateModel) {
        for (PlateComponent pc : panel.getTablePanel().getPlateComponents()) {
            if (pc.getModel().getInternalId() == plateModel.getInternalId()) {
                return pc;
            }
        }
        return null;
    }

    public void generateNewPlates() {
        TablePanel table = panel.getTablePanel();
        table.removeAll();
        table.getPlateComponents().clear();
        for (int i = 0; i < 3; i++) {
            PlateComponent plate = new PlateComponent(false, 128);
            setupPlateDrag(plate);
            table.getPlateComponents().add(plate);
            table.add(plate);
        }
        table.revalidate();
        table.repaint();
        panel.getRefillButton().setEnabled(false);
    }

    public void resetGame() {
        gameLoopTimer.stop();
        setupControllerState();
        panel.getTrayPanel().resetHoles();
        generateNewPlates();
        panel.getRefillButton().setEnabled(false);
    }
}