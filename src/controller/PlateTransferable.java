package controller;

import view.PlateComponent;
import model.Plate;

import java.awt.datatransfer.*;

public class PlateTransferable implements Transferable {
    public static final DataFlavor plateFlavor = new DataFlavor(Plate.class, "Plate");
    private final Plate plateModel;

    public PlateTransferable(PlateComponent plateComp) {
        this.plateModel = plateComp.getModel();
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{plateFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return plateFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor))
            throw new UnsupportedFlavorException(flavor);
        return plateModel;
    }
}