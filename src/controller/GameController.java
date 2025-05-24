package controller;

import model.EmbaspManager;
import utilities.SoundPlayer;
import view.GamePanel;
import view.PlateComponent;
import view.TablePanel;
import view.TrayPanel;
import model.Plate;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

public class GameController {
    private final GamePanel panel;
    private final EmbaspManager manager;

    public GameController(GamePanel panel, EmbaspManager manager) {
        this.panel = panel;
        this.manager = manager;
        setupDragAndDrop();
    }

    // Configura drag sui piatti del tavolo e drop sul vassoio
    private void setupDragAndDrop() {
        TablePanel table = panel.getTablePanel();
        TrayPanel tray = panel.getTrayPanel();

        // Drag sui piatti
        for (PlateComponent plate : table.getPlateComponents()) {
            setupPlateDrag(plate);
        }

        // Drop sul vassoio
        new DropTarget(tray, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                boolean dropAccepted = false;
                try {
                    Transferable tr = dtde.getTransferable();
                    if (tr.isDataFlavorSupported(PlateTransferable.plateFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                        dropAccepted = true;

                        Plate plateModel = (Plate) tr.getTransferData(PlateTransferable.plateFlavor);
                        Point pt = dtde.getLocation();
                        PlateComponent target = tray.getHoleAtPoint(pt);

                        PlateComponent dragged = null;
                        for (PlateComponent pc : table.getPlateComponents()) {
                            if (pc.getModel().equals(plateModel)) {
                                dragged = pc;
                                break;
                            }
                        }

                        // Solo se il buco è valido e non già occupato
                        if (target != null && dragged != null && target.isHoleComponent()
                                && !tray.isHoleOccupied(target)) {
                            SoundPlayer.playSound("drag-drop.wav");
                            handleDrop(target, dragged);
                        }

                        dtde.dropComplete(true);
                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception e) {
                    if (!dropAccepted) {
                        try { dtde.rejectDrop(); } catch (IllegalStateException ignored) {}
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    // Imposta il TransferHandler e il listener per avviare il drag
    private void setupPlateDrag(PlateComponent plate) {
        plate.setTransferHandler(new TransferHandler("plate") {
            @Override
            public int getSourceActions(JComponent c) {
                return MOVE;
            }
            @Override
            protected Transferable createTransferable(JComponent c) {
                return new PlateTransferable((PlateComponent) c);
            }
        });

        // Aggiungo l’ascoltatore per cambiare il cursore durante il drag
        plate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                JComponent comp = (JComponent) e.getSource();
                // Imposta il cursore a mano
                comp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                comp.getTransferHandler().exportAsDrag(comp, e, TransferHandler.MOVE);
                SoundPlayer.playSound("drag-drop.wav");
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                ((Component) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            public void mouseReleased(java.awt.event.MouseEvent e) {
                // Ripristina cursore di default
                ((JComponent)e.getSource()).setCursor(Cursor.getDefaultCursor());
            }
        });
    }


    // Rimuove il piatto dal tavolo e lo mette nel vassoio
    public void handleDrop(PlateComponent targetHole, PlateComponent draggedPlate) {
        TablePanel table = panel.getTablePanel();
        TrayPanel tray = panel.getTrayPanel();

        tray.replaceHoleWithPlate(targetHole, draggedPlate);
        table.removePlate(draggedPlate);
        panel.repaint();

        // Abilita refill se tavolo vuoto
        if (table.isEmpty()) {
            panel.getRefillButton().setEnabled(true);
        }
    }

    // Rigenera i piatti sul tavolo
    public void generateNewPlates() {
        TablePanel table = panel.getTablePanel();
        table.removeAll();
        table.getPlateComponents().clear();
        for (int i = 0; i < 3; i++) {
            PlateComponent plate = new PlateComponent(false, 64);
            setupPlateDrag(plate);
            table.getPlateComponents().add(plate);
            table.add(plate);
        }
        table.revalidate();
        table.repaint();
        panel.getRefillButton().setEnabled(false);
    }

    // Reset completo del gioco: piatti nuovi e vassoio ripulito
    public void resetGame() {
        panel.getTrayPanel().resetHoles();
        generateNewPlates();
        panel.getRefillButton().setEnabled(false);
    }
}
