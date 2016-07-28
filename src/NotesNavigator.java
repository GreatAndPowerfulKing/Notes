
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.MOVE;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author iKing
 */
public class NotesNavigator extends javax.swing.JFrame {
    
    public final Actions Actions = new Actions();
    
    private final NoteListener noteListener = new NoteListener() {

        @Override
        public void titleChanged(INote note) {
            updateNotesListSorting();
        }

        @Override
        public void modificationDateChanged(INote note) {
            updateNotesListSorting();
        }

        @Override
        public void rateChanged(INote note) {
            updateNotesListSorting();
        }
    };
        
    private boolean treeSelectionListenerEnabled = true;
    private boolean listSelectionListenerEnabled = true;
    
    /**
     * Creates new form NotesNavigator
     */
    public NotesNavigator() {
        initComponents();
                
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) directoriesTree.getCellRenderer();
        renderer.setLeafIcon(UIManager.getIcon("FileView.directoryIcon"));
                
        directoriesTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()) {

            @Override
            public void valueForPathChanged(TreePath path, Object newValue) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) directoriesTree.getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }
                if (node == directoriesTree.getModel().getRoot()) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    INote currentNote = noteEditor.getNote();
                    if (currentNote != null) {
                        currentNote.save();
                    }
                    IDirectory directory = (IDirectory) node.getUserObject();
                    String newName = newValue.toString().trim().replace("/", "");
                    if (newName.equals(directory.getName()) || newName.isEmpty()) {
                        return;
                    }
                    if (directory.setName(newName)) {
                        ListModel model = notesList.getModel();
                        int size = model.getSize();
                        for (int i = 0; i < size; i++) {
                            INote note = ((INote) model.getElementAt(i));
                            note.reload(directory.getAbsolutePath() + "/" +
                                    note.getName() + "." +
                                    Defaults.NoteFileExtension, false);
                        }
                    }
                }
                
                super.valueForPathChanged(path, node.getUserObject()); 
                moveDirectoryNode(node, (DefaultMutableTreeNode) node.getParent());
            }
        });
        
        directoriesTree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (treeSelectionListenerEnabled) {
                    setCurrentNote(null);
                    updateNotesList();
                }
                Actions.RemoveDirectory.updateState();
                Actions.RenameDirectory.updateState();
            }
        });
        
        notesList.setCellRenderer(new ListCellRenderer() {
            
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                NoteListItem item = new NoteListItem((INote) value);
                item.setSelected(isSelected);
                return item;
            }
        });
        
        notesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (listSelectionListenerEnabled) {
                    Actions.RemoveNote.updateState();
                    setCurrentNote((INote) notesList.getSelectedValue());
                }
            }
        });
        
        Actions.RemoveNote.updateState();
        Actions.RemoveDirectory.updateState();
        Actions.RenameDirectory.updateState();
                
        directoriesTree.setTransferHandler(new DirectoriesTreeTransferHandler());
        notesList.setTransferHandler(new NotesListTransferHandler());
        
        Defaults defaults = Defaults.getDefaults();
        INotesSorter sorter = defaults.getNotesSorter();
        if (sorter instanceof AlphabeticalNotesSorter) {
            alphabeticalRadioButtonMenuItem.setSelected(true);
        } else if (sorter instanceof RateNotesSorter) {
            rateRadioButtonMenuItem.setSelected(true);
        } else if (sorter instanceof ModificationDateNotesSorter) {
            modificationDateRadioButtonMenuItem.setSelected(true);
        }
        
        INotesSorter.SortingDirection direction = defaults.getNotesSortingDirection();
        switch (direction) {
            case Ascending:
                ascendingRadioButtonMenuItem.setSelected(true);
                break;
            case Descending:
                descendingRadioButtonMenuItem.setSelected(true);
                break;
        }
        
        buildDirectoriesTree();   
        
        noteEditor.Actions.Bold.addListener(new CheckableActionListener() {
            @Override
            public void checkChanged(boolean checked) {
                boldCheckBoxMenuItem.setSelected(checked);
            }
        });
        
        noteEditor.Actions.Italic.addListener(new CheckableActionListener() {
            @Override
            public void checkChanged(boolean checked) {
                italicCheckBoxMenuItem.setSelected(checked);
            }
        });
        
        noteEditor.Actions.Undeline.addListener(new CheckableActionListener() {
            @Override
            public void checkChanged(boolean checked) {
                underlineCheckBoxMenuItem.setSelected(checked);
            }
        });
        
        for (final String fontFamily : Defaults.AvailableFontFamilyNames) {
            JMenuItem fontFamilyMenuItem = new JMenuItem(new Action() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    noteEditor.Actions.FontFamily.actionPerformed(noteEditor.Actions.new FontFamilyEvent(
                            e.getSource(),
                            ActionEvent.ACTION_PERFORMED,
                            "",
                            fontFamily)
                    );
                }
            });
            fontFamilyMenuItem.setText(fontFamily);
            fontFamilyMenu.add(fontFamilyMenuItem);
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    noteEditor.getNote().save();
                } catch (Exception ex) {
                    
                }
            }
        });        
    }
    
    private void buildDirectoriesTree() {
        DefaultMutableTreeNode root = createTreeNodeForDirectory(new Directory("/"));
        ((DefaultTreeModel) directoriesTree.getModel()).setRoot(root);
        directoriesTree.getSelectionModel().setSelectionPath(new TreePath(root));
    }
    
    private DefaultMutableTreeNode createTreeNodeForDirectory(IDirectory directory) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(directory);
        List<IDirectory> subDirectories = directory.getDirectories();
        for (IDirectory dir : subDirectories) {
            insertDirectoryNode(createTreeNodeForDirectory(dir), node);
        }
        return node;
    }
    
    private void moveDirectoryNode(DefaultMutableTreeNode node, DefaultMutableTreeNode destination) {
        insertDirectoryNode(node, destination);
    }
    
    private void insertDirectoryNode(DefaultMutableTreeNode node, DefaultMutableTreeNode destination) {
        if (node == null || destination == null) {
            return;
        }
        
        treeSelectionListenerEnabled = false;
        
        HashMap<Object, Boolean> nodesExpandingState = new HashMap<>();
        List<DefaultMutableTreeNode> nodes = getAllChildNodes(node);
        nodes.add(node);
        
        DefaultTreeModel model = (DefaultTreeModel) directoriesTree.getModel();
        for (DefaultMutableTreeNode currentNode : nodes) {
            TreePath path = new TreePath(model.getPathToRoot(currentNode));
            nodesExpandingState.put(currentNode.getUserObject(), directoriesTree.isExpanded(path));
        }
        
        String directoryName = ((IDirectory) node.getUserObject()).getName();
        int childCount = destination.getChildCount();
        int index = 0;
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) destination.getChildAt(i);
            String currentDirrectoryName = ((IDirectory) currentNode.getUserObject()).getName();
            if (directoryName.compareTo(currentDirrectoryName) > 0) {
                index++;
            }
        }
        
        if (node.getParent() != null) {
            model.removeNodeFromParent(node);
        }
        model.insertNodeInto(node, destination, index);
        TreePath selectionPath = new TreePath(model.getPathToRoot(node));
        directoriesTree.getSelectionModel().setSelectionPath(selectionPath);
        
        updateDirectiesPathes(node);
        
        for (DefaultMutableTreeNode currentNode : nodes) {
            if (nodesExpandingState.get(currentNode.getUserObject())) {
                TreePath path = new TreePath(model.getPathToRoot(currentNode));
                directoriesTree.expandPath(path);
            }
        }
        
        treeSelectionListenerEnabled = true;
    }
    
    private List<DefaultMutableTreeNode> getAllChildNodes(DefaultMutableTreeNode node) {
        ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<>();
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            nodes.add(childNode);
            nodes.addAll(getAllChildNodes(childNode));
        }      
        return nodes;
    }
    
    private void updateDirectiesPathes(DefaultMutableTreeNode node) {
        Directory parentDirectory = (Directory) node.getUserObject();
        DefaultTreeModel model = (DefaultTreeModel) directoriesTree.getModel();
        int childCount = model.getChildCount(node);
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) model.getChild(node, i);
            Directory directory = (Directory) child.getUserObject();
            directory.setPath(parentDirectory.getPath() + "/" + directory.getName());
            updateDirectiesPathes(child);
        }
    }
    
    private void updateNotesList() {
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) directoriesTree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }
        Directory directory = (Directory) node.getUserObject();
        final List<INote> notes = directory.getNotes(false);
        notesList.setListData(notes.toArray());
        updateNotesListSorting();
    }
    
    private void updateNotesListSorting() {

        listSelectionListenerEnabled = false;
        
        ListModel model = notesList.getModel();
        List<INote> notes = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            notes.add((INote) model.getElementAt(i));
        }
        Defaults defaults = Defaults.getDefaults();
        notes = defaults.getNotesSorter().sort(notes, defaults.getNotesSortingDirection());

        Object selectedNote = notesList.getSelectedValue();
        notesList.setListData(notes.toArray());
        notesList.setSelectedValue(selectedNote, true);
        
        listSelectionListenerEnabled = true;
    }
    
    private void setSelectedNote(INote note) {
//        notesList.setSelectedValue(note, true);
        ListModel model = notesList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (((Note) model.getElementAt(i)).getName().equals(note.getName())) {
                notesList.setSelectedIndex(i);
                break;
            }
        }
    }
    
    private void setCurrentNote(INote note) {
        INote previousEditingNote = noteEditor.getNote();
        if (previousEditingNote != null) {
            previousEditingNote.setContent(noteEditor.getContent());
            previousEditingNote.save();
            previousEditingNote.removeAllListeners();
            previousEditingNote.setContent(new byte[0]);
        }
        if (note == null) {
            noteEditor.setEnabled(false);
            
            // Font menus
            fontFamilyMenu.setEnabled(false);
            fontSizeMenu.setEnabled(false);
        } else {
            note.reload();
            note.addListener(noteListener);
            noteEditor.setEnabled(true);
            
            // Font menus
            fontFamilyMenu.setEnabled(true);
            fontSizeMenu.setEnabled(true);
        }
        noteEditor.setNote(note);
    }
    
    public final class Actions {
        
        // File actions
        public FileAction NewNote = new NewNoteAction();
        public FileAction NewDirectory = new NewDirectoryAction();
        public FileAction RemoveNote = new RemoveNoteAction();
        public FileAction RemoveDirectory = new RemoveDirectoryAction();
        public FileAction RenameDirectory = new RenameDirectoryAction();
        
        //Sort actions
        public SortAction SortAscending = new SortAscendingAction();
        public SortAction SortDescending = new SortDescendingAction();
        public SortAction SortAlphabetical = new SortAlpabeticalAction();
        public SortAction SortByRate = new SortByRateAction();
        public SortAction SortByModificationDate = new SortByModificationDateAction();
        
        // File actions
        public abstract class FileAction extends Action {

            public void updateState() {
            };
        }
        
        private class NewNoteAction extends FileAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) directoriesTree.getLastSelectedPathComponent();
                if (node != null) {
                    IDirectory directory = (IDirectory) node.getUserObject();
                    INote note = directory.createNote();
                    setCurrentNote(null);
                    updateNotesList();
                    setSelectedNote(note);
                }
            }
        }
        
        private class NewDirectoryAction extends FileAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode desinationNode = (DefaultMutableTreeNode) directoriesTree.getLastSelectedPathComponent();
                if (desinationNode != null) {
                    IDirectory destinationDirectory = (IDirectory) desinationNode.getUserObject();
                    IDirectory newDirectory = destinationDirectory.createDirectory();
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newDirectory);
                    insertDirectoryNode(newNode, desinationNode);
                    DefaultTreeModel model = (DefaultTreeModel) directoriesTree.getModel();
                    TreePath newNodePath = new TreePath(model.getPathToRoot(newNode));
                    directoriesTree.getSelectionModel().setSelectionPath(newNodePath);
                    updateNotesList();
                    RenameDirectory.actionPerformed(e);
                }
            }
        }
        
        private class RemoveNoteAction extends FileAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) directoriesTree.getLastSelectedPathComponent();
                if (node != null) {
                    IDirectory directory = (IDirectory) node.getUserObject();
                    setCurrentNote(null);
                    directory.removeNote((INote) notesList.getSelectedValue());
                    updateNotesList();
                }
            }

            @Override
            public void updateState() {
                setEnabled(notesList.getSelectedIndex() != -1);
            }
        }
        
        private class RemoveDirectoryAction extends FileAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) directoriesTree.getLastSelectedPathComponent();
                if (node != null) {
                    IDirectory directory = (IDirectory) node.getUserObject();
                    String message =  "Do you really want to remove folder \"" + directory.getName() +  "\"?";
                    int result = JOptionPane.showConfirmDialog(NotesNavigator.this, message,
                            "Are you sure?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (result == 0) {
                        directory.remove();
                        DefaultTreeModel model = (DefaultTreeModel) directoriesTree.getModel();
                        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
                        if (parentNode != null) {
                            model.removeNodeFromParent(node);
                            TreePath parentPath = new TreePath(model.getPathToRoot(parentNode));
                            directoriesTree.getSelectionModel().setSelectionPath(parentPath);
                        }
                    }
                }
            }

            @Override
            public void updateState() {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) directoriesTree.getLastSelectedPathComponent();
                setEnabled(node != directoriesTree.getModel().getRoot());
            }
        }
        
        private class RenameDirectoryAction extends FileAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) directoriesTree.getLastSelectedPathComponent();
                if (node != null) {
                    DefaultTreeModel model = (DefaultTreeModel) directoriesTree.getModel();
                    TreePath nodePath = new TreePath(model.getPathToRoot(node));
                    directoriesTree.startEditingAtPath(nodePath);
                }
            }

            @Override
            public void updateState() {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) directoriesTree.getLastSelectedPathComponent();
                setEnabled(node != directoriesTree.getModel().getRoot());
            }
        }
        
        // Sort actions
        public abstract class SortAction extends Action {
            
        }
        
        private class SortAscendingAction extends SortAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                Defaults.getDefaults().setNotesSortingDirection(INotesSorter.SortingDirection.Ascending);
                updateNotesListSorting();
            }
            
        }
        
        private class SortDescendingAction extends SortAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                Defaults.getDefaults().setNotesSortingDirection(INotesSorter.SortingDirection.Descending);
                updateNotesListSorting();
            }
            
        }
        
        private class SortAlpabeticalAction extends SortAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                Defaults.getDefaults().setNotesSorter(new AlphabeticalNotesSorter());
                updateNotesListSorting();
            }
            
        }
        
        private class SortByRateAction extends SortAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                Defaults.getDefaults().setNotesSorter(new RateNotesSorter());
                updateNotesListSorting();
            }
            
        }
        
        private class SortByModificationDateAction extends SortAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                Defaults.getDefaults().setNotesSorter(new ModificationDateNotesSorter());
                updateNotesListSorting();
            }
            
        }
        
    }
    
    private class DirectoriesTreeTransferHandler extends TransferHandler {
        
        private DefaultMutableTreeNode dragSourceNode = null;
        
        public DirectoriesTreeTransferHandler() {
            super();
            
//            setDragImage(UIManager.getIcon("FileView.directoryIcon"));
        }

        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop()) {
                return false;
            }
            if (!support.isDataFlavorSupported(DirectoryTransferable.DirectoryDataFlavor) &&
                    !support.isDataFlavorSupported(NoteTransferable.NoteDataFlavor)) {
                return false;
            }
            return true;
        }
        
        @Override
        public boolean importData(TransferSupport support) {
            boolean accept = false;
            if (canImport(support)) {
                if (support.getComponent() == directoriesTree) {
                    Transferable transferable = support.getTransferable();
                    try {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) directoriesTree.getLastSelectedPathComponent();
                        IDirectory destinationDirectory = (IDirectory) node.getUserObject();
                        
                        if (support.isDataFlavorSupported(DirectoryTransferable.DirectoryDataFlavor)) {
                            IDirectory sourceDirectory = (IDirectory) transferable.getTransferData(
                                    DirectoryTransferable.DirectoryDataFlavor);
                            if (sourceDirectory.move(destinationDirectory)) {
                                moveDirectoryNode(dragSourceNode, node);
                                updateNotesList();
                                accept = true;
                            } else {
                                Toolkit.getDefaultToolkit().beep();
                            }
                        } else if (support.isDataFlavorSupported(NoteTransferable.NoteDataFlavor)) {
                            INote note = (INote) transferable.getTransferData(
                                    NoteTransferable.NoteDataFlavor);
                            IDirectory sourceDirectory = new Directory(note.getPath());
                            if (sourceDirectory.moveNote(note, destinationDirectory)) {
                                updateNotesList();
                                accept = true;
                            } else {
                                Toolkit.getDefaultToolkit().beep();
                            }
                            setSelectedNote(note);
                        }
                    } catch (UnsupportedFlavorException | IOException ex) {
                        System.err.println("Something gone wrong: " + ex);
                    }
                }
            }
            return accept;
        }

        @Override
        public int getSourceActions(JComponent c) {
            DefaultMutableTreeNode node
                    = (DefaultMutableTreeNode) directoriesTree.getLastSelectedPathComponent();
            if (node == directoriesTree.getModel().getRoot()) {
                return NONE;
            }
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            if (c == directoriesTree) {
                dragSourceNode = (DefaultMutableTreeNode) directoriesTree.getLastSelectedPathComponent();
                if (dragSourceNode != directoriesTree.getModel().getRoot()) {
                    IDirectory directory = (IDirectory) dragSourceNode.getUserObject();
                    return new DirectoryTransferable(directory);
                }
            }
            return null;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            dragSourceNode = null;
        }
    }
    
    private class NotesListTransferHandler extends TransferHandler {
        
        public NotesListTransferHandler() {
            super();
            
//            setDragImage(UIManager.getIcon("FileView.directoryIcon"));
        }
        
        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            if (c == notesList) {
                INote note = (INote) notesList.getSelectedValue();
                return new NoteTransferable(note); 
            }
            return null;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sortDirectionButtonGroup = new javax.swing.ButtonGroup();
        sortAlgorithButtonGroup = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        directoriesTree = new javax.swing.JTree();
        jScrollPane2 = new javax.swing.JScrollPane();
        notesList = new javax.swing.JList();
        noteEditor = new NoteEditor();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newNoteMenuItem = new javax.swing.JMenuItem();
        newFolderMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        removeNoteMenuItem = new javax.swing.JMenuItem();
        removeFolderMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        renameFolderMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenyItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        selectAllMenuItem = new javax.swing.JMenuItem();
        styleMenu = new javax.swing.JMenu();
        boldCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        italicCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        underlineCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        fontFamilyMenu = new javax.swing.JMenu();
        fontSizeMenu = new javax.swing.JMenu();
        fontSize12MenuItem = new javax.swing.JMenuItem();
        fontSize14MenuItem = new javax.swing.JMenuItem();
        fontSize18MenuItem = new javax.swing.JMenuItem();
        fontSize24MenuItem = new javax.swing.JMenuItem();
        sortNotesMenu = new javax.swing.JMenu();
        ascendingRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        descendingRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        alphabeticalRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        rateRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        modificationDateRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(850, 400));
        setPreferredSize(new java.awt.Dimension(850, 500));

        directoriesTree.setDragEnabled(true);
        directoriesTree.setEditable(true);
        directoriesTree.setShowsRootHandles(true);
        directoriesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jScrollPane1.setViewportView(directoriesTree);

        notesList.setDragEnabled(true);
        notesList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(notesList);

        fileMenu.setText("File");

        newNoteMenuItem.setAction(Actions.NewNote);
        newNoteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        newNoteMenuItem.setText("New note");
        fileMenu.add(newNoteMenuItem);

        newFolderMenuItem.setAction(Actions.NewDirectory);
        newFolderMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + java.awt.event.InputEvent.SHIFT_MASK));
        newFolderMenuItem.setText("New folder");
        fileMenu.add(newFolderMenuItem);
        fileMenu.add(jSeparator2);

        removeNoteMenuItem.setAction(Actions.RemoveNote);
        removeNoteMenuItem.setText("Remove Note");
        fileMenu.add(removeNoteMenuItem);

        removeFolderMenuItem.setAction(Actions.RemoveDirectory);
        removeFolderMenuItem.setText("Remove Folder");
        fileMenu.add(removeFolderMenuItem);
        fileMenu.add(jSeparator3);

        renameFolderMenuItem.setAction(Actions.RenameDirectory);
        renameFolderMenuItem.setText("Rename Folder");
        fileMenu.add(renameFolderMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");

        undoMenuItem.setAction(noteEditor.Actions.Undo);
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        undoMenuItem.setText("Undo");
        editMenu.add(undoMenuItem);

        redoMenuItem.setAction(noteEditor.Actions.Redo);
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + java.awt.event.InputEvent.SHIFT_MASK));
        redoMenuItem.setText("Redo");
        editMenu.add(redoMenuItem);
        editMenu.add(jSeparator1);

        cutMenuItem.setAction(noteEditor.Actions.Cut);
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenyItem.setAction(noteEditor.Actions.Copy);
        copyMenyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        copyMenyItem.setText("Copy");
        editMenu.add(copyMenyItem);

        pasteMenuItem.setAction(noteEditor.Actions.Paste);
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        selectAllMenuItem.setAction(noteEditor.Actions.SelectAll);
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        selectAllMenuItem.setText("Select All");
        editMenu.add(selectAllMenuItem);

        menuBar.add(editMenu);

        styleMenu.setText("Style");

        boldCheckBoxMenuItem.setAction(noteEditor.Actions.Bold);
        boldCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        boldCheckBoxMenuItem.setText("Bold");
        styleMenu.add(boldCheckBoxMenuItem);

        italicCheckBoxMenuItem.setAction(noteEditor.Actions.Italic);
        italicCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        italicCheckBoxMenuItem.setText("Italic");
        styleMenu.add(italicCheckBoxMenuItem);

        underlineCheckBoxMenuItem.setAction(noteEditor.Actions.Undeline);
        underlineCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        underlineCheckBoxMenuItem.setText("Underline");
        styleMenu.add(underlineCheckBoxMenuItem);
        styleMenu.add(jSeparator5);

        fontFamilyMenu.setText("Font Family");
        styleMenu.add(fontFamilyMenu);

        fontSizeMenu.setText("Font Size");

        fontSize12MenuItem.setAction(new Action() {
            @Override
            public void actionPerformed(ActionEvent e) {
                noteEditor.Actions.FontSize.actionPerformed(noteEditor.Actions.new FontSizeEvent(
                    e.getSource(),
                    ActionEvent.ACTION_PERFORMED,
                    "",
                    12));
        }
    });
    fontSize12MenuItem.setText("12 pt");
    fontSizeMenu.add(fontSize12MenuItem);

    fontSize14MenuItem.setAction(new Action() {
        @Override
        public void actionPerformed(ActionEvent e) {
            noteEditor.Actions.FontSize.actionPerformed(noteEditor.Actions.new FontSizeEvent(
                e.getSource(),
                ActionEvent.ACTION_PERFORMED,
                "",
                14));
    }
    });
    fontSize14MenuItem.setText("14 pt");
    fontSizeMenu.add(fontSize14MenuItem);

    fontSize18MenuItem.setAction(new Action() {
        @Override
        public void actionPerformed(ActionEvent e) {
            noteEditor.Actions.FontSize.actionPerformed(noteEditor.Actions.new FontSizeEvent(
                e.getSource(),
                ActionEvent.ACTION_PERFORMED,
                "",
                18));
    }
    });
    fontSize18MenuItem.setText("18 pt");
    fontSizeMenu.add(fontSize18MenuItem);

    fontSize24MenuItem.setAction(new Action() {
        @Override
        public void actionPerformed(ActionEvent e) {
            noteEditor.Actions.FontSize.actionPerformed(noteEditor.Actions.new FontSizeEvent(
                e.getSource(),
                ActionEvent.ACTION_PERFORMED,
                "",
                24));
    }
    });
    fontSize24MenuItem.setText("24 pt");
    fontSizeMenu.add(fontSize24MenuItem);

    styleMenu.add(fontSizeMenu);

    menuBar.add(styleMenu);

    sortNotesMenu.setText("Sort notes");

    sortDirectionButtonGroup.add(ascendingRadioButtonMenuItem);
    ascendingRadioButtonMenuItem.setAction(Actions.SortAscending);
    ascendingRadioButtonMenuItem.setSelected(true);
    ascendingRadioButtonMenuItem.setText("Ascending");
    sortNotesMenu.add(ascendingRadioButtonMenuItem);

    sortDirectionButtonGroup.add(descendingRadioButtonMenuItem);
    descendingRadioButtonMenuItem.setAction(Actions.SortDescending);
    descendingRadioButtonMenuItem.setText("Descending");
    sortNotesMenu.add(descendingRadioButtonMenuItem);
    sortNotesMenu.add(jSeparator4);

    sortAlgorithButtonGroup.add(alphabeticalRadioButtonMenuItem);
    alphabeticalRadioButtonMenuItem.setAction(Actions.SortAlphabetical);
    alphabeticalRadioButtonMenuItem.setSelected(true);
    alphabeticalRadioButtonMenuItem.setText("Alphabetical");
    sortNotesMenu.add(alphabeticalRadioButtonMenuItem);

    sortAlgorithButtonGroup.add(rateRadioButtonMenuItem);
    rateRadioButtonMenuItem.setAction(Actions.SortByRate);
    rateRadioButtonMenuItem.setText("Rate");
    sortNotesMenu.add(rateRadioButtonMenuItem);

    sortAlgorithButtonGroup.add(modificationDateRadioButtonMenuItem);
    modificationDateRadioButtonMenuItem.setAction(Actions.SortByModificationDate);
    modificationDateRadioButtonMenuItem.setText("Modification Date");
    sortNotesMenu.add(modificationDateRadioButtonMenuItem);

    menuBar.add(sortNotesMenu);

    setJMenuBar(menuBar);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(noteEditor, javax.swing.GroupLayout.PREFERRED_SIZE, 329, Short.MAX_VALUE)
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(noteEditor, javax.swing.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addContainerGap())))
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | 
                InstantiationException | 
                IllegalAccessException | 
                javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NotesNavigator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new NotesNavigator().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButtonMenuItem alphabeticalRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem ascendingRadioButtonMenuItem;
    private javax.swing.JCheckBoxMenuItem boldCheckBoxMenuItem;
    private javax.swing.JMenuItem copyMenyItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JRadioButtonMenuItem descendingRadioButtonMenuItem;
    private javax.swing.JTree directoriesTree;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu fontFamilyMenu;
    private javax.swing.JMenuItem fontSize12MenuItem;
    private javax.swing.JMenuItem fontSize14MenuItem;
    private javax.swing.JMenuItem fontSize18MenuItem;
    private javax.swing.JMenuItem fontSize24MenuItem;
    private javax.swing.JMenu fontSizeMenu;
    private javax.swing.JCheckBoxMenuItem italicCheckBoxMenuItem;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JRadioButtonMenuItem modificationDateRadioButtonMenuItem;
    private javax.swing.JMenuItem newFolderMenuItem;
    private javax.swing.JMenuItem newNoteMenuItem;
    private NoteEditor noteEditor;
    private javax.swing.JList notesList;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JRadioButtonMenuItem rateRadioButtonMenuItem;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JMenuItem removeFolderMenuItem;
    private javax.swing.JMenuItem removeNoteMenuItem;
    private javax.swing.JMenuItem renameFolderMenuItem;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.ButtonGroup sortAlgorithButtonGroup;
    private javax.swing.ButtonGroup sortDirectionButtonGroup;
    private javax.swing.JMenu sortNotesMenu;
    private javax.swing.JMenu styleMenu;
    private javax.swing.JCheckBoxMenuItem underlineCheckBoxMenuItem;
    private javax.swing.JMenuItem undoMenuItem;
    // End of variables declaration//GEN-END:variables
}
