
import java.io.Serializable;
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
public interface INotesSorter extends Serializable {
    
    public List<INote> sort(List<INote> notes, SortingDirection direction);
    public List<INote> sortAscending(List<INote> notes);
    public List<INote> sortDescending(List<INote> notes);
    
    public enum SortingDirection {
        Ascending,
        Descending
    }
}
