
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
public class NoteTransferable implements Transferable {

    public static final DataFlavor NoteDataFlavor
            = new DataFlavor(INote.class, "Note");

    private final INote note;

    public NoteTransferable(INote note) {
        this.note = note;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{ NoteDataFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(NoteDataFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) {
        return note;
    }
}
