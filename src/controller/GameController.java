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
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
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
        tray.revalidate();
        tray.repaint();
        runGameLogicStep();
        if (!gameLoopTimer.isRunning()) {
            gameLoopTimer.start();
        }
    }

    private void runGameLogicStep() {
        TrayPanel tray = panel.getTrayPanel();
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
                int nId = neighbor.getModel().getDisplayId();
                if (nId != -1) { // Filtro per evitare errori di parsing dei numeri negativi
                    facts.add(new NeighborInfo(plateId, nId));
                }
            }
        }

        Move nextMove = manager.getNextMove(facts);
        boolean actionTaken = false;
        if (nextMove != null) {
            PlateComponent donator = tray.getPlateByDisplayId(nextMove.donatorId);
            PlateComponent receiver = tray.getPlateByDisplayId(nextMove.receiverId);
            String color = nextMove.color;
            if (donator != null && receiver != null) {
                int moved = gameLogic.movePieces(donator, receiver, color);
                if (moved > 0) {
                    logAction("IA Move (ASP)", donator, receiver, color, moved);
                    actionTaken = true;
                }
            }
        }

        // Rimuove piatti completi o vuoti ad ogni passo
        List<Plate> removedPlates = tray.getAndRemoveCompletedOrEmptyPlates();
        for (Plate removedPlate : removedPlates) {
            gameLog.add("  > Piatto " + removedPlate.getDisplayId() + " rimosso.");
            actionTaken = true;
        }

        tray.revalidate();
        tray.repaint();
        if (!actionTaken) {
            gameLoopTimer.stop();
        }
    }

    private void logAction(String context, PlateComponent donator, PlateComponent receiver, String color, int moved) {
        if (moved == 0) return;
        gameLog.add("  > " + context + ": " + moved + " pezzi " + color + " da Piatto " + donator.getModel().getDisplayId() + " a Piatto " + receiver.getModel().getDisplayId());
    }

    private PlateComponent findDraggedComponent(Plate plateModel) {
        for (PlateComponent pc : panel.getTablePanel().getPlateComponents()) {
            if (pc.getModel().getInternalId() == plateModel.getInternalId()) return pc;
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

    public void suggestPlacement() {
        List<Object> facts = new ArrayList<>();
        List<PlateComponent> tablePlates = panel.getTablePanel().getPlateComponents();
        if (tablePlates.isEmpty()) return;

        for (PlateComponent tp : tablePlates) {
            int internalId = tp.getModel().getInternalId();
            facts.add(new PiattoDaInserireInfo(internalId));
            for (String color : tp.getUniqueColors()) {
                facts.add(new PlateInfo(internalId, color, tp.getModel().countPiecesOfColor(color)));
            }
        }

        for (PlateComponent pc : panel.getTrayPanel().getAllPlates()) {
            int displayId = pc.getModel().getDisplayId();
            if (displayId == -1) continue;
            for (String color : pc.getUniqueColors()) {
                facts.add(new PlateInfo(displayId, color, pc.getModel().countPiecesOfColor(color)));
            }
        }

        panel.getTrayPanel().addCellaFacts(facts);

        Place suggestion = manager.getBestPlacement(facts);

        if (suggestion != null) {
            String content = "";
            for(PlateComponent tp : tablePlates) {
                if(tp.getModel().getInternalId() == suggestion.getId())
                    content = tp.getModel().getContentsAsString();
            }
            String message = "IA SUGGERISCE:\nPrendi il piatto con [" + content + "]\ne mettilo nella cella evidenziata (Riga " + suggestion.getX() + ", Colonna " + suggestion.getY() + ")";
            JOptionPane.showMessageDialog(panel, message, "IA Hint", JOptionPane.INFORMATION_MESSAGE);
            panel.getTrayPanel().highlightSlot(suggestion.getX(), suggestion.getY());
        }
    }

    public void resetGame() {
        gameLoopTimer.stop();
        panel.getTrayPanel().resetHoles();
        setupControllerState();
        generateNewPlates();
        panel.getRefillButton().setEnabled(false);
    }
}