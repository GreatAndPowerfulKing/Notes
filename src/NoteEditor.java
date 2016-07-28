
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.io.IOException;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

/**
 *
 * @author iKing
 */
public class NoteEditor extends javax.swing.JPanel {
    
    private final String rateDotSymbol = "∙";
    private final String rateStarSymbol = "✭";
    
    private INote note;
    private final JLabel[] rateLabels;
    
    public final Actions Actions = new Actions();
    
    private boolean styleActionsEnabled = true;
    
    private int lastSelectionStart = 0;
    private int lastSelectionEnd = 0;
    private int lastDocumentLength = 0;
    
    private final DocumentListener documentListener = new DocumentListener() {
        
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateNote();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateNote();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateNote();
            }
            
            private void updateNote() {
                if (note == null) {
                    return;
                }
                note.setContent(getContent());
                String text = editorTextPane.getText();
                int newLineCharacterIndex = text.indexOf("\n");
                String title = text;
                if (newLineCharacterIndex >= 0) {
                    title = text.substring(0, text.indexOf("\n")).replace("\r", "");
                }
                note.setTitle(title);
                note.modified();
            }
        };
    
    private final UndoableEditListener undoableEditListener = new UndoableEditListener() {

            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                Actions.undoManager.addEdit(e.getEdit());
                Actions.Undo.updateState();
                Actions.Redo.updateState();
            }
        };
    
    private final NoteListener noteListener = new NoteListener() {

        @Override
        public void modificationDateChanged(INote note) {
            updateModificationDateLabel();
        }

        @Override
        public void rateChanged(INote note) {
            updateRateUI();
        }
    };
    
    private final Color defaultSpinnerForeground;
    private final Color undefinedSpinnerForeground = Color.GRAY;
    
    /**
     * Creates new form NoteEditor
     */
    public NoteEditor() {
        initComponents();
        
        editorTextPane.addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        updateEditorUI();
                    }
                });
            }
        });
        
        Component component = fontSizeSpinner.getEditor().getComponent(0);
        defaultSpinnerForeground = component.getForeground();
        
        rateLabels = new JLabel[] { 
            rateLabel1, 
            rateLabel2, 
            rateLabel3, 
            rateLabel4, 
            rateLabel5 
        };
          
        updateRateUI(); 
        updateEditorUI();
        
        Actions.Bold.addListener(new CheckableActionListener() {
            @Override
            public void checkChanged(boolean checked) {
                boldButton.setSelected(checked);
            }
        });
        
        Actions.Italic.addListener(new CheckableActionListener() {
            @Override
            public void checkChanged(boolean checked) {
                italicButton.setSelected(checked);
            }
        });
        
        Actions.Undeline.addListener(new CheckableActionListener() {
            @Override
            public void checkChanged(boolean checked) {
                underlineButton.setSelected(checked);
            }
        });
    }

    /**
     * @return the note
     */
    public INote getNote() {
        return note;
    }

    /**
     * @param note the note to set
     */
    public void setNote(INote note) {
        if (this.note != note) {
            if (this.note != null) {
                this.note.removeListener(noteListener);
            }
            this.note = note;
            if (this.note != null) {
                setContent(this.note.getContent());
                this.note.setContent(getContent());
                this.note.addListener(noteListener);
            } else {
                setContent(new byte[0]);
            }
            updateEditorUI();
            updateModificationDateLabel();
            updateRateUI();
            Actions.undoManager = new UndoManager();
            Actions.Undo.updateState();
            Actions.Redo.updateState();
        }        
    }
    
    public byte[] getContent() {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectStream = new ObjectOutputStream(byteArrayStream)) {
            objectStream.writeObject(editorTextPane.getStyledDocument());
        } catch (IOException ex) {
            System.err.println("Unable to get content: " + ex.getLocalizedMessage());
        }
        return byteArrayStream.toByteArray(); 
    }
    
    public void setContent(byte[] content) {
        Document document;
        try (ObjectInputStream objectStream = new ObjectInputStream(new ByteArrayInputStream(content))) {
            document = (Document) objectStream.readObject();
            objectStream.close();
        } catch (IOException | ClassNotFoundException ex) {
            document = new DefaultStyledDocument();
            MutableAttributeSet attributes = new SimpleAttributeSet();
            Font defaultFont = Defaults.getDefaults().getDefaultFont();
            StyleConstants.setFontFamily(attributes, defaultFont.getFamily());
            StyleConstants.setFontSize(attributes, defaultFont.getSize());
            ((DefaultStyledDocument) document).setCharacterAttributes(0, 1, attributes, true);
            ((StyledEditorKit) editorTextPane.getEditorKit()).getInputAttributes().addAttributes(attributes);
            updateEditorUI();
        }
        
        document.addDocumentListener(documentListener);
        document.addUndoableEditListener(undoableEditListener);
        editorTextPane.setDocument(document);
    }
    
    private void updateModificationDateLabel() {
        if (note == null) {
            modificationDateLabel.setText(" ");
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            modificationDateLabel.setText(formatter.format(note.getModificationDate()));
        }
    }
    
    private void updateRateUI() {
        for (int i = 0; i < 5; i++) {
            rateLabels[i].setText(rateDotSymbol);
        }
        int rate = 0;
        if (note != null) {
            rate = note.getRate();
        }
        rate = Math.max(0, Math.min(rate, rateLabels.length));
        for (int i = 0; i < rate; i++) {
            rateLabels[i].setText(rateStarSymbol);
        }
    }

    @Override
    public final void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        
        modificationDateLabel.setText(" ");
        Actions.Paste.updateState();
        Actions.SelectAll.updateState();
        Actions.Bold.setEnabled(enabled);
        Actions.Italic.setEnabled(enabled);
        Actions.Undeline.setEnabled(enabled);
        Actions.FontFamily.setEnabled(enabled);
        Actions.FontSize.setEnabled(enabled);
        if (!enabled) {
            setContent(new byte[0]);
//            fontComboBox.setSelectedIndex(0);
//            editorTextPane.setDocument(new DefaultStyledDocument());
//            editorTextPane.setCaretPosition(0);
//            fontComboBox.setSelectedIndex(-1);
        }
        enableComponents(this, enabled);
    }
    
    private void enableComponents(Container container, boolean enabled) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enabled);
            if (component instanceof Container) {
                enableComponents((Container) component, enabled);
            }
        }
    }
    
    private void updateEditorUI() {
        Actions.Cut.updateState();
        Actions.Copy.updateState();
        
        StyledDocument document = editorTextPane.getStyledDocument();
        
        boolean mulpipleFontFamilies = false;
        boolean mulpipleFontSizes = false;
        
        int selectionStart = editorTextPane.getSelectionStart();
        int selectionEnd = editorTextPane.getSelectionEnd();
        
        AttributeSet inputAttributes = editorTextPane.getInputAttributes();
        final String fontFamily = StyleConstants.getFontFamily(inputAttributes);
        final Integer fontSize = StyleConstants.getFontSize(inputAttributes);
        
        Actions.Bold.setChecked(StyleConstants.isBold(inputAttributes));
        Actions.Italic.setChecked(StyleConstants.isItalic(inputAttributes));
        Actions.Undeline.setChecked(StyleConstants.isUnderline(inputAttributes));
        
        if (selectionStart != selectionEnd) {
            String previousFontFamily = null;
            Integer previousFontSize = null;
            for (int i = selectionStart; i < selectionEnd; i++) {
                Element currentElement = document.getCharacterElement(i);
                AttributeSet currentAttributes = currentElement.getAttributes();
                String currentFontFamily = StyleConstants.getFontFamily(currentAttributes);
                Integer currentFontSize = StyleConstants.getFontSize(currentAttributes);
                
                if (previousFontFamily != null && !currentFontFamily.equals(previousFontFamily)) {
                    mulpipleFontFamilies = true;
                }
                previousFontFamily = currentFontFamily;
                
                if (previousFontSize != null && !currentFontSize.equals(previousFontSize)) {
                    mulpipleFontSizes = true;
                }
                previousFontSize = currentFontSize;
                
                if (mulpipleFontFamilies && mulpipleFontSizes) {
                    break;
                }
            }
        }
        
        styleActionsEnabled = false;
        
        if (mulpipleFontFamilies) {
            fontComboBox.setSelectedIndex(-1);
        } else {
            fontComboBox.setSelectedItem(fontFamily);
        }
        
        if (!mulpipleFontSizes) {
            fontSizeSpinner.setValue(fontSize);
        }
        
        styleActionsEnabled = true;
        
        Component component = ((JSpinner.DefaultEditor) fontSizeSpinner.getEditor()).getTextField();
        component.setForeground(mulpipleFontSizes ? undefinedSpinnerForeground : defaultSpinnerForeground);
    }
    
    private void saveSelection() {
        lastSelectionStart = editorTextPane.getSelectionStart();
        lastSelectionEnd = editorTextPane.getSelectionEnd();
        lastDocumentLength = editorTextPane.getDocument().getLength();
    }
    
    private void restoreSelection() {
        if (editorTextPane.getDocument().getLength() == lastDocumentLength) {
            editorTextPane.setSelectionStart(lastSelectionStart);
            editorTextPane.setSelectionEnd(lastSelectionEnd);
        }
    }
    
    public final class Actions {
        
        // Style actions
        public CheckableStyleAction Bold = new BoldAction();
        public CheckableStyleAction Italic = new ItalicAction();
        public CheckableStyleAction Undeline = new UnderlineAction();
        public StyleAction FontFamily = new FontFamilyAction();
        public StyleAction FontSize = new FontSizeAction();
        
        // Edit actions
        private UndoManager undoManager = new UndoManager();
        public EditAction Undo = new UndoAction();
        public EditAction Redo = new RedoAction();
        public EditAction Cut = new CutAction();
        public EditAction Copy = new CopyAction();
        public EditAction Paste = new PasteAction();
        public EditAction SelectAll = new SelectAllAction();
        
        // Rate action
        public Action Rate = new RateAction();
        
        // Style actions
        public abstract class StyleAction extends Action {
            
            protected final void setAttributes(AttributeSet attributes) {
                int selectionStart = editorTextPane.getSelectionStart();
                int selectionEnd = editorTextPane.getSelectionEnd();
                if (selectionStart != selectionEnd) {
                    StyledDocument document = editorTextPane.getStyledDocument();
                    document.setCharacterAttributes(selectionStart, selectionEnd - selectionStart, attributes, false);
                }
                ((StyledEditorKit) editorTextPane.getEditorKit()).getInputAttributes().addAttributes(attributes);
            }
        }
        
        public abstract class CheckableStyleAction extends StyleAction implements IChekableAction {

            private boolean checked = false;
            private ArrayList<CheckableActionListener> listeners = new ArrayList<>();
            
            @Override
            public boolean isChecked() {
                return checked;
            }

            @Override
            public void setChecked(boolean checked) {
                this.checked = checked;
                for (CheckableActionListener listener : listeners) {
                    listener.checkChanged(checked);
                }
            }
            
            @Override
            public void addListener(CheckableActionListener listener) {
                listeners.add(listener);
            }
            
            @Override
            public void removeListener(CheckableActionListener listener) {
                listeners.remove(listener);
            }
            
            @Override
            public void removeAllListeners() {
                listeners = new ArrayList<>();
            }
            
        }
        
        private class BoldAction extends CheckableStyleAction {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!styleActionsEnabled) {
                    return;
                }
                StyledEditorKit editorKit = (StyledEditorKit) editorTextPane.getEditorKit();
                MutableAttributeSet attributes = editorKit.getInputAttributes();
                SimpleAttributeSet attributeSet = new SimpleAttributeSet();
                StyleConstants.setBold(attributeSet, !(StyleConstants.isBold(attributes)));
                setAttributes(attributeSet);
                updateEditorUI();
            }
        }
        
        private class ItalicAction extends CheckableStyleAction {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!styleActionsEnabled) {
                    return;
                }
                StyledEditorKit editorKit = (StyledEditorKit) editorTextPane.getEditorKit();
                MutableAttributeSet attributes = editorKit.getInputAttributes();
                SimpleAttributeSet attributeSet = new SimpleAttributeSet();
                StyleConstants.setItalic(attributeSet, !(StyleConstants.isItalic(attributes)));
                setAttributes(attributeSet);
                updateEditorUI();
            }
        }
        
        private class UnderlineAction extends CheckableStyleAction {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!styleActionsEnabled) {
                    return;
                }
                StyledEditorKit editorKit = (StyledEditorKit) editorTextPane.getEditorKit();
                MutableAttributeSet attributes = editorKit.getInputAttributes();
                SimpleAttributeSet attributeSet = new SimpleAttributeSet();
                StyleConstants.setUnderline(attributeSet, !(StyleConstants.isUnderline(attributes)));
                setAttributes(attributeSet);
                updateEditorUI();
            }
        }
        
        private class FontFamilyAction extends StyleAction {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!styleActionsEnabled) {
                    return;
                }
                if (e instanceof FontFamilyEvent) {
                    String fontFamily = ((FontFamilyEvent) e).getFontFamily();
                    if (Arrays.asList(Defaults.AvailableFontFamilyNames).contains(fontFamily)) {
                        MutableAttributeSet attributes = new SimpleAttributeSet();
                        StyleConstants.setFontFamily(attributes, fontFamily);
                        setAttributes(attributes);
                        updateEditorUI();
                    }
                }
            }
        }
        
        private class FontSizeAction extends StyleAction {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!styleActionsEnabled) {
                    return;
                }
                if (e instanceof FontSizeEvent) {
                    int fontSize = ((FontSizeEvent) e).getFontSize();
                    if (fontSize > 0) {
                        MutableAttributeSet attributes = new SimpleAttributeSet();
                        StyleConstants.setFontSize(attributes, fontSize);
                        setAttributes(attributes);
                        updateEditorUI();
                    }
                }
            }
        }
        
        // Edit actions
        public abstract class EditAction extends Action {
            
            public void updateState() {
            };
        }
        
        private class UndoAction extends EditAction {
            
            public UndoAction() {
                updateState();
            }
            
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSelection();
                try {
                    undoManager.undo();
                } catch (CannotUndoException ex) {
                    System.err.println("Unable to undo: " + ex);
                }
                restoreSelection();
                updateState();
                Redo.updateState();
            }
            
            @Override
            public final void updateState() {
                setEnabled(undoManager.canUndo());
                putValue(javax.swing.Action.NAME, undoManager.getUndoPresentationName());
            }
        }
        
        private class RedoAction extends EditAction {
            
            public RedoAction() {
                updateState();
            }
            
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSelection();
                try {
                    undoManager.redo();
                } catch (CannotRedoException ex) {
                    System.err.println("Unable to redo: " + ex);
                }
                restoreSelection();
                updateState();
                Undo.updateState();
            }
            
            @Override
            public final void updateState() {
                setEnabled(undoManager.canRedo());
                putValue(javax.swing.Action.NAME, undoManager.getRedoPresentationName());
            }
        }
        
        private class CutAction extends EditAction {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                editorTextPane.cut();
            }

            @Override
            public final void updateState() {
                setEnabled(NoteEditor.this.isEnabled() &&
                        editorTextPane.getSelectionStart() != editorTextPane.getSelectionEnd());
            }
        }
        
        private class CopyAction extends EditAction {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                editorTextPane.copy();
            }

            @Override
            public final void updateState() {
                setEnabled(NoteEditor.this.isEnabled() && 
                        editorTextPane.getSelectionStart() != editorTextPane.getSelectionEnd());
            }
        }
        
        private class PasteAction extends EditAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                editorTextPane.paste();
            }
            
            @Override
            public void updateState() {
                setEnabled(NoteEditor.this.isEnabled());
            }
        }
        
        private class SelectAllAction extends EditAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                editorTextPane.selectAll();
            }

            @Override
            public void updateState() {
                setEnabled(NoteEditor.this.isEnabled());
            }
        }
        
        // Rate action
        private class RateAction extends Action {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e instanceof RateEvent) {
                    if (note != null) {
                        note.setRate(((RateEvent) e).getRate());
                    }
                }
            }
            
        }
        
        // ActionEvents
        public class FontFamilyEvent extends ActionEvent {

            private String fontFamily;
            
            public FontFamilyEvent(Object source, int id, String command, String fontFamily) {
                super(source, id, command);
                
                this.fontFamily = fontFamily;
            }

            /**
             * @return the fontFamily
             */
            public String getFontFamily() {
                return fontFamily;
            }

            /**
             * @param fontFamily the fontFamily to set
             */
            public void setFontFamily(String fontFamily) {
                this.fontFamily = fontFamily;
            }
        }
        
        public class FontSizeEvent extends ActionEvent {

            private int fontSize;
            
            public FontSizeEvent(Object source, int id, String command, int fontSize) {
                super(source, id, command);
                
                this.fontSize = fontSize;
            }

            /**
             * @return the fontSize
             */
            public int getFontSize() {
                return fontSize;
            }

            /**
             * @param fontSize the fontSize to set
             */
            public void setFontSize(int fontSize) {
                this.fontSize = fontSize;
            }
        }
        
        public class RateEvent extends ActionEvent {

            private int rate;
            
            public RateEvent(Object source, int id, String command, int rate) {
                super(source, id, command);
                
                this.rate = rate;
            }

            /**
             * @return the rate
             */
            public int getRate() {
                return rate;
            }

            /**
             * @param rate the rate to set
             */
            public void setRate(int rate) {
                this.rate = rate;
            }

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

        jScrollPane1 = new javax.swing.JScrollPane();
        editorTextPane = new javax.swing.JTextPane();
        fontComboBox = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        fontSizeSpinner = new javax.swing.JSpinner();
        jSeparator2 = new javax.swing.JSeparator();
        boldButton = new javax.swing.JButton();
        italicButton = new javax.swing.JButton();
        underlineButton = new javax.swing.JButton();
        rateLabel1 = new javax.swing.JLabel();
        rateLabel2 = new javax.swing.JLabel();
        rateLabel3 = new javax.swing.JLabel();
        rateLabel4 = new javax.swing.JLabel();
        rateLabel5 = new javax.swing.JLabel();
        modificationDateLabel = new javax.swing.JLabel();

        jScrollPane1.setViewportView(editorTextPane);

        fontComboBox.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selectedItem = fontComboBox.getSelectedItem();
                if (selectedItem != null) {
                    Actions.FontFamily.actionPerformed(
                        Actions.new FontFamilyEvent(
                            e.getSource(),
                            ActionEvent.ACTION_PERFORMED,
                            "",
                            selectedItem.toString()));
                }
            }
        });
        fontComboBox.setModel(new DefaultComboBoxModel(Defaults.AvailableFontFamilyNames));

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        fontSizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Actions.FontSize.actionPerformed(
                    Actions.new FontSizeEvent(e.getSource(),
                        ActionEvent.ACTION_PERFORMED,
                        "",
                        (Integer) fontSizeSpinner.getValue()));
            }
        });
        fontSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(11, 1, 96, 1));
        ((JSpinner.DefaultEditor) fontSizeSpinner.getEditor()).getTextField().addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                Component component = ((JSpinner.DefaultEditor )fontSizeSpinner.getEditor()).getTextField();
                component.setForeground(defaultSpinnerForeground);
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        boldButton.setAction(Actions.Bold);
        boldButton.setText("B");
        boldButton.setFocusable(false);
        boldButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        boldButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        italicButton.setAction(Actions.Italic);
        italicButton.setText("I");
        italicButton.setFocusable(false);
        italicButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        italicButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        underlineButton.setAction(Actions.Undeline);
        underlineButton.setText("U");
        underlineButton.setFocusable(false);
        underlineButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        underlineButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        rateLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        rateLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rateLabel1.setText("✭");
        rateLabel1.setMaximumSize(new java.awt.Dimension(16, 16));
        rateLabel1.setMinimumSize(new java.awt.Dimension(16, 16));
        rateLabel1.setPreferredSize(new java.awt.Dimension(16, 16));
        rateLabel1.setSize(new java.awt.Dimension(16, 16));
        rateLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rateLabel1MouseClicked(evt);
            }
        });

        rateLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        rateLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rateLabel2.setText("✭");
        rateLabel2.setMaximumSize(new java.awt.Dimension(16, 16));
        rateLabel2.setMinimumSize(new java.awt.Dimension(16, 16));
        rateLabel2.setPreferredSize(new java.awt.Dimension(16, 16));
        rateLabel2.setSize(new java.awt.Dimension(16, 16));
        rateLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rateLabel2MouseClicked(evt);
            }
        });

        rateLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        rateLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rateLabel3.setText("✭");
        rateLabel3.setMaximumSize(new java.awt.Dimension(16, 16));
        rateLabel3.setMinimumSize(new java.awt.Dimension(16, 16));
        rateLabel3.setPreferredSize(new java.awt.Dimension(16, 16));
        rateLabel3.setSize(new java.awt.Dimension(16, 16));
        rateLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rateLabel3MouseClicked(evt);
            }
        });

        rateLabel4.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        rateLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rateLabel4.setText("∙");
        rateLabel4.setMaximumSize(new java.awt.Dimension(16, 16));
        rateLabel4.setMinimumSize(new java.awt.Dimension(16, 16));
        rateLabel4.setPreferredSize(new java.awt.Dimension(16, 16));
        rateLabel4.setSize(new java.awt.Dimension(16, 16));
        rateLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rateLabel4MouseClicked(evt);
            }
        });

        rateLabel5.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        rateLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rateLabel5.setText("∙");
        rateLabel5.setMaximumSize(new java.awt.Dimension(16, 16));
        rateLabel5.setMinimumSize(new java.awt.Dimension(16, 16));
        rateLabel5.setPreferredSize(new java.awt.Dimension(16, 16));
        rateLabel5.setSize(new java.awt.Dimension(16, 16));
        rateLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rateLabel5MouseClicked(evt);
            }
        });

        modificationDateLabel.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        modificationDateLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        modificationDateLabel.setText("01.01.1970 00:00:00");
        modificationDateLabel.setMaximumSize(new java.awt.Dimension(99, 16));
        modificationDateLabel.setMinimumSize(new java.awt.Dimension(99, 16));
        modificationDateLabel.setPreferredSize(new java.awt.Dimension(99, 16));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(boldButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(italicButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(underlineButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 43, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(modificationDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rateLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rateLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rateLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rateLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rateLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(fontComboBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(fontSizeSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(boldButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(italicButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(underlineButton, javax.swing.GroupLayout.Alignment.TRAILING)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modificationDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rateLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rateLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rateLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rateLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rateLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rateLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rateLabel1MouseClicked
        int rate = 1;
        if (note.getRate() == rate) {
            rate = 0;
        }
        Actions.Rate.actionPerformed(Actions.new RateEvent(
                rateLabel1, 
                ActionEvent.ACTION_PERFORMED, 
                "", 
                rate));
    }//GEN-LAST:event_rateLabel1MouseClicked

    private void rateLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rateLabel2MouseClicked
        int rate = 2;
        if (note.getRate() == rate) {
            rate = 0;
        }
        Actions.Rate.actionPerformed(Actions.new RateEvent(
                rateLabel2, 
                ActionEvent.ACTION_PERFORMED, 
                "", 
                rate));
    }//GEN-LAST:event_rateLabel2MouseClicked

    private void rateLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rateLabel3MouseClicked
        int rate = 3;
        if (note.getRate() == rate) {
            rate = 0;
        }
        Actions.Rate.actionPerformed(Actions.new RateEvent(
                rateLabel3, 
                ActionEvent.ACTION_PERFORMED, 
                "", 
                rate));
    }//GEN-LAST:event_rateLabel3MouseClicked

    private void rateLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rateLabel4MouseClicked
        int rate = 4;
        if (note.getRate() == rate) {
            rate = 0;
        }
        Actions.Rate.actionPerformed(Actions.new RateEvent(
                rateLabel4, 
                ActionEvent.ACTION_PERFORMED, 
                "", 
                rate));
    }//GEN-LAST:event_rateLabel4MouseClicked

    private void rateLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rateLabel5MouseClicked
        int rate = 5;
        if (note.getRate() == rate) {
            rate = 0;
        }
        Actions.Rate.actionPerformed(Actions.new RateEvent(
                rateLabel5, 
                ActionEvent.ACTION_PERFORMED, 
                "", 
                rate));
    }//GEN-LAST:event_rateLabel5MouseClicked
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton boldButton;
    private javax.swing.JTextPane editorTextPane;
    private javax.swing.JComboBox fontComboBox;
    private javax.swing.JSpinner fontSizeSpinner;
    private javax.swing.JButton italicButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel modificationDateLabel;
    private javax.swing.JLabel rateLabel1;
    private javax.swing.JLabel rateLabel2;
    private javax.swing.JLabel rateLabel3;
    private javax.swing.JLabel rateLabel4;
    private javax.swing.JLabel rateLabel5;
    private javax.swing.JButton underlineButton;
    // End of variables declaration//GEN-END:variables
}
