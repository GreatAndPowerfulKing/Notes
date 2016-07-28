/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author iKing
 */
public interface IChekableAction {
    
    public boolean isChecked();
    public void setChecked(boolean checked);
    public void addListener(CheckableActionListener listener);
    public void removeListener(CheckableActionListener listener);
    public void removeAllListeners();
}
