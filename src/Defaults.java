
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.swing.UIManager;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author iKing
 */
public class Defaults implements Serializable {
    
    public static final String NoteFileExtension = "note";
    public static final String RootDirectoryPath = System.getProperty("user.home") + "/Notes";
    public static final String NewDirectoryName = "New folder";
    
    public static final String DefaultsPath = RootDirectoryPath + "/.defaults";
    
    public static final String[] AvailableFontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    
    private static Defaults defaults = null;
    private INotesSorter notesSorter = new AlphabeticalNotesSorter();
    private INotesSorter.SortingDirection notesSortingDirection = INotesSorter.SortingDirection.Ascending;
    private final Font defaultFont;
    
    private Defaults() {
        defaultFont = UIManager.getDefaults().getFont("TextPane.font");
    }
    
    public static Defaults getDefaults() {
        if (defaults == null) {
            if (!load()) {
                defaults = new Defaults();
                save();
            }
        }
        return defaults;
    }
    
    private static boolean load() {
        try {
            FileInputStream fileStream = new FileInputStream(DefaultsPath);
            try (ObjectInputStream objectStream = new ObjectInputStream(fileStream)) {
                defaults = (Defaults) objectStream.readObject();
            }
        } catch (IOException | ClassNotFoundException ex) {
            return false;
        }
        return true;
    }
    
    private static boolean save() {
        if (defaults != null) {
            try (FileOutputStream fileStream = new FileOutputStream(DefaultsPath)) {
                try (ObjectOutputStream objectStream = new ObjectOutputStream(fileStream)) {
                    objectStream.writeObject(defaults);
                }
                return true;
            } catch (Exception ex) {
                
            }
        }
        return false;
    }

    public INotesSorter getNotesSorter() {
        return notesSorter;
    }

    public void setNotesSorter(INotesSorter notesSorter) {
        this.notesSorter = notesSorter;
        save();
    }

    public INotesSorter.SortingDirection getNotesSortingDirection() {
        return notesSortingDirection;
    }

    public void setNotesSortingDirection(INotesSorter.SortingDirection notesSortingDirection) {
        this.notesSortingDirection = notesSortingDirection;
        save();
    }

    public Font getDefaultFont() {
        return defaultFont;
    }
    
}
