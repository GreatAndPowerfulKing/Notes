
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
public interface INote {
    
    public void reload();
    
    public void reload(boolean loadContent);
    
    /**
     * 
     * @param fileName absolute path to .note file
     * @param loadContent
     */
    public void reload(String fileName, boolean loadContent);
    
    public void save();
    
    public void modified();
    
    /**
     * @return the title
     */
    public String getTitle();

    /**
     * @param title the title to set
     */
    public void setTitle(String title);

    /**
     * @return the modificationDate
     */
    public Date getModificationDate();

    /**
     * @param modificationDate the modificationDate to set
     */
    public void setModificationDate(Date modificationDate);

    /**
     * @return the rate
     */
    public int getRate();

    /**
     * @param rate the rate to set
     */
    public void setRate(int rate);

    /**
     * @return the content
     */
    public byte[] getContent();

    /**
     * @param content the content to set
     */
    public void setContent(byte[] content);

    /**
     * @return the path
     */
    public String getPath();

    /**
     * @param path the path to set
     */
    public void setPath(String path);

    /**
     * @return the hash
     */
    public String getName();
    
    /**
     * @param name the name to set
     */
    public void setName(String name);
    
    public void addListener(NoteListener listener);
    
    public void removeListener(NoteListener listener);
    
    public void removeAllListeners();

    @Override
    public String toString();
}