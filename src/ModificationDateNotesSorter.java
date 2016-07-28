
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public class ModificationDateNotesSorter implements INotesSorter {

    @Override
    public List<INote> sort(List<INote> notes, SortingDirection direction) {
        switch (direction) {
            case Ascending:
                return sortAscending(notes);
            case Descending:
                return sortDescending(notes);
        }
        return notes;
    }
    
    @Override
    public List<INote> sortAscending(List<INote> notes) {
        List<INote> sortedNotes = new ArrayList<>();
        for (INote note : notes) {
            sortedNotes.add(note);
        }
        Collections.sort(sortedNotes, new Comparator<INote>() {

            @Override
            public int compare(INote o1, INote o2) {
                return o1.getModificationDate().compareTo(o2.getModificationDate());
            }
            
        });
        return sortedNotes;
    }

    @Override
    public List<INote> sortDescending(List<INote> notes) {
        List<INote> sortedNotes = sortAscending(notes);
        Collections.reverse(sortedNotes);
        return sortedNotes;
    }
}
