/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload.ui.songtable;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTextField;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class SearchField extends JTextField implements MouseListener {
	final static String SEARCH = "Search...";

	public SearchField() {
		super(SEARCH);

		setToolTipText("search using regex expressions");
		addMouseListener(this);

	}

	/**
	 * TODO ENTER DESCRIPTION HERE
	 * 
	 * @author jfruehau
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 * 
	 * @param e
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (getText().equals(SEARCH))
			setText("");

	}

	/**
	 * TODO ENTER DESCRIPTION HERE
	 * 
	 * @author jfruehau
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 * 
	 * @param e
	 */
	@Override
	public void mousePressed(MouseEvent e) {

	}

	/**
	 * TODO ENTER DESCRIPTION HERE
	 * 
	 * @author jfruehau
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 * 
	 * @param e
	 */
	@Override
	public void mouseReleased(MouseEvent e) {

	}

	/**
	 * TODO ENTER DESCRIPTION HERE
	 * 
	 * @author jfruehau
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 * 
	 * @param e
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * TODO ENTER DESCRIPTION HERE
	 * 
	 * @author jfruehau
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 * 
	 * @param e
	 */
	@Override
	public void mouseExited(MouseEvent e) {

		if (getText().equals(""))
			setText(SEARCH);

	}

}
