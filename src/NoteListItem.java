
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;
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
public class NoteListItem extends javax.swing.JPanel {
    
    private final String rateDotSymbol = "∙";
    private final String rateStarSymbol = "✭";
        
    private final INote note;
    private final JLabel[] rateLabels;

    /**
     * Creates new form NoteListItem
     * @param note
     */
    public NoteListItem(INote note) {
        initComponents();
        
        this.note = note;
        
        rateLabels = new JLabel[] { 
            rateLabel1, 
            rateLabel2, 
            rateLabel3, 
            rateLabel4, 
            rateLabel5 
        };
        
        String title = note.getTitle();
        int titleLength = 18;
        if (title.length() > titleLength) {
            title = title.substring(0, titleLength) + ". . .";
        }
        titleLabel.setText(title);
        
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        modificationDateLabel.setText(formatter.format(note.getModificationDate()));
        
        updateRateUI();
    }
        
    public void setSelected(boolean selected) {
        UIDefaults defaults = UIManager.getDefaults();
        Color backgroundColor;
        Color foregroundColor;
        if (selected) {
            backgroundColor = defaults.getColor("List.selectionBackground");
            foregroundColor = defaults.getColor("List.selectionForeground");
        } else {
            backgroundColor = defaults.getColor("List.background");
            foregroundColor = defaults.getColor("List.foreground");
        }
        
        setBackground(backgroundColor);
        titleLabel.setForeground(foregroundColor);
        modificationDateLabel.setForeground(foregroundColor);
        for (int i = 0; i < 5; i++) {
            rateLabels[i].setForeground(foregroundColor);
        }
    }
    
    private void updateRateUI() {
        for (int i = 0; i < 5; i++) {
            rateLabels[i].setText(rateDotSymbol);
        }
        for (int i = 0; i < note.getRate(); i++) {
            rateLabels[i].setText(rateStarSymbol);
        }
    }
    
    /**
     * @return the note
     */
    public INote getNote() {
        return note;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        modificationDateLabel = new javax.swing.JLabel();
        rateLabel1 = new javax.swing.JLabel();
        rateLabel2 = new javax.swing.JLabel();
        rateLabel3 = new javax.swing.JLabel();
        rateLabel4 = new javax.swing.JLabel();
        rateLabel5 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(153, 153, 153)));
        setMaximumSize(new java.awt.Dimension(140, 75));
        setMinimumSize(new java.awt.Dimension(116, 71));
        setPreferredSize(new java.awt.Dimension(140, 75));
        setSize(new java.awt.Dimension(150, 75));

        titleLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        titleLabel.setText("Title");

        modificationDateLabel.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        modificationDateLabel.setText("01.01.1970 00:00:00");

        rateLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        rateLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rateLabel1.setText("✭");
        rateLabel1.setMaximumSize(new java.awt.Dimension(16, 16));
        rateLabel1.setMinimumSize(new java.awt.Dimension(16, 16));
        rateLabel1.setPreferredSize(new java.awt.Dimension(16, 16));
        rateLabel1.setSize(new java.awt.Dimension(16, 16));

        rateLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        rateLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rateLabel2.setText("✭");
        rateLabel2.setMaximumSize(new java.awt.Dimension(16, 16));
        rateLabel2.setMinimumSize(new java.awt.Dimension(16, 16));
        rateLabel2.setPreferredSize(new java.awt.Dimension(16, 16));
        rateLabel2.setSize(new java.awt.Dimension(16, 16));

        rateLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        rateLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rateLabel3.setText("✭");
        rateLabel3.setMaximumSize(new java.awt.Dimension(16, 16));
        rateLabel3.setMinimumSize(new java.awt.Dimension(16, 16));
        rateLabel3.setPreferredSize(new java.awt.Dimension(16, 16));
        rateLabel3.setSize(new java.awt.Dimension(16, 16));

        rateLabel4.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        rateLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rateLabel4.setText("∙");
        rateLabel4.setMaximumSize(new java.awt.Dimension(16, 16));
        rateLabel4.setMinimumSize(new java.awt.Dimension(16, 16));
        rateLabel4.setPreferredSize(new java.awt.Dimension(16, 16));
        rateLabel4.setSize(new java.awt.Dimension(16, 16));

        rateLabel5.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        rateLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rateLabel5.setText("∙");
        rateLabel5.setMaximumSize(new java.awt.Dimension(16, 16));
        rateLabel5.setMinimumSize(new java.awt.Dimension(16, 16));
        rateLabel5.setPreferredSize(new java.awt.Dimension(16, 16));
        rateLabel5.setSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modificationDateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rateLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rateLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rateLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rateLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rateLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 51, Short.MAX_VALUE))
                    .addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rateLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rateLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rateLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rateLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rateLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modificationDateLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel modificationDateLabel;
    private javax.swing.JLabel rateLabel1;
    private javax.swing.JLabel rateLabel2;
    private javax.swing.JLabel rateLabel3;
    private javax.swing.JLabel rateLabel4;
    private javax.swing.JLabel rateLabel5;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables

}