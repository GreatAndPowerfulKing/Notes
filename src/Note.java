
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author iKing
 */
public class Note implements INote, Serializable {
    
    private String title = "New note";
    private Date modificationDate = new Date();
    private int rate = 0;
    private byte[] content = new byte[0];
    private String path;
    private String name;
    private transient ArrayList<NoteListener> listeners = new ArrayList<>();
        
    public Note(String path, String name) {
        this.path = path;
        this.name = name;
    }
    
    /**
     * 
     * @param fileName absolute path to .note file
     * @param loadContent
     * @return 
     */
    public static Note load(String fileName, boolean loadContent) {
        if (!fileName.startsWith(Defaults.RootDirectoryPath)) {
            return null;
        }
        try {
            FileInputStream fileStream = new FileInputStream(fileName);
            Note note;
            try (ObjectInputStream objectStream = new ObjectInputStream(fileStream)) {
                note = (Note) objectStream.readObject();
            }
            if (note != null) {
                note.path = fileName.substring(Defaults.RootDirectoryPath.length(), fileName.lastIndexOf("/"));
                note.name = fileName.substring(
                        fileName.lastIndexOf("/"),
                        fileName.lastIndexOf(".")).replace("/", "");
                if (!loadContent) {
                    note.content = new byte[0];
                }
                note.listeners = new ArrayList<>();
            }
            return note;
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(ex);
            return null;
        }
    }
    
    @Override
    public void reload() {
        reload(true);
    }
    
    @Override
    public void reload(boolean loadContent) {
        reload(Directory.absolutePathForNote(this), loadContent);
    }
    
    /**
     * 
     * @param fileName absolute path to .note file
     * @param loadContent
     */
    @Override
    public void reload(String fileName, boolean loadContent) {
        try {
            FileInputStream fileStream = new FileInputStream(fileName);
            Note note;
            try (ObjectInputStream objectStream = new ObjectInputStream(fileStream)) {
                note = (Note) objectStream.readObject();
            }
            if (note != null) {
                note.path = fileName.substring(Defaults.RootDirectoryPath.length(), fileName.lastIndexOf("/"));
                note.name = fileName.substring(
                        fileName.lastIndexOf("/"),
                        fileName.lastIndexOf(".")).replace("/", "");
                
                title = note.title;
                modificationDate = note.modificationDate;
                rate = note.rate;
                path = note.path;
                name = note.name;
                if (loadContent) {
                    content = note.content;
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Unable to reload note: " + ex.getLocalizedMessage());
        }
    }
    
    @Override
    public void save() {
        try {
            String fileName = Directory.absolutePathForNote(this);
            try (FileOutputStream fileStream = new FileOutputStream(fileName)) {
                try (ObjectOutputStream objectStream = new ObjectOutputStream(fileStream)) {
                    objectStream.writeObject(this);
                }
            }
        } catch (Exception ex) {
            System.err.println("Unable to save note: " + ex.getLocalizedMessage());
        }
    }
    
    @Override
    public void modified() {
        setModificationDate(new Date());
    }
    
    /**
     * @return the title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    @Override
    public void setTitle(String title) {
        if (!this.title.equals(title)) {
            this.title = title;
            if (this.title.isEmpty()) {
                this.title = "Empty note";
            }
            for (NoteListener listener : listeners) {
                listener.titleChanged(this);
            }
        }
    }

    /**
     * @return the modificationDate
     */
    @Override
    public Date getModificationDate() {
        return modificationDate;
    }

    /**
     * @param modificationDate the modificationDate to set
     */
    public void setModificationDate(Date modificationDate) {
        if (!this.modificationDate.equals(modificationDate)) {
            this.modificationDate = modificationDate;
            for (NoteListener listener : listeners) {
                listener.modificationDateChanged(this);
            }
        }
    }

    /**
     * @return the rate
     */
    @Override
    public int getRate() {
        return rate;
    }

    /**
     * @param rate the rate to set
     */
    @Override
    public void setRate(int rate) {
        if (this.rate != rate) {
            this.rate = rate;
            for (NoteListener listener : listeners) {
                listener.rateChanged(this);
            }
        }
    }

    /**
     * @return the content
     */
    @Override
    public byte[] getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    @Override
    public void setContent(byte[] content) {
        if (!Arrays.equals(this.content, content)) {
            this.content = content;
            for (NoteListener listener : listeners) {
                listener.contentChanged(this);
            }
        }
    }

    /**
     * @return the path
     */
    @Override
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    @Override
    public void setPath(String path) {
        if (!this.path.equals(path)) {
            this.path = path;
        }
    }

    /**
     * @return the hash
     */
    @Override
    public String getName() {
        return name;
    }
    
    /**
     * @param name the name to set
     */
    @Override
    public void setName(String name) {
        if (!this.name.equals(name)) {
            this.name = name;
        }
    }
    
    @Override
    public void addListener(NoteListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public void removeListener(NoteListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public void removeAllListeners() {
        listeners = new ArrayList<>();
    }

    @Override
    public String toString() {
        return name;
    }
    
}
