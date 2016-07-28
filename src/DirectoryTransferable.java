
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author iKing
 */
public class DirectoryTransferable implements Transferable {

    public static final DataFlavor DirectoryDataFlavor
            = new DataFlavor(IDirectory.class, "Directory");

    private final IDirectory directory;

    public DirectoryTransferable(IDirectory dir) {
        directory = dir;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DirectoryDataFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DirectoryDataFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) {
        return directory;
    }
}
