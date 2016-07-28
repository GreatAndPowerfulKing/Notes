
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author iKing
 */
public interface IDirectory {
    
    public List<String> getDirectoriesNames();
    
    public List<String> getNotesNames();
    
    public List<IDirectory> getDirectories();
    
    public List<INote> getNotes(boolean loadContent);
        
    public INote createNote();
    
    public IDirectory createDirectory();
    
    public IDirectory createDirectory(String name);
    
    public String getPath();
    
    public String getAbsolutePath();
    
    public String getName();
    
    public boolean setName(String name);
    
    public boolean move(IDirectory desination);
    
    public boolean move(IDirectory destination, String name);
    
    public boolean remove();

    public boolean removeNote(INote note);
    
    public boolean moveNote(INote note, IDirectory destination);
    
    public String generateNoteName();
    
    public String getUniqueName(String name);
}
