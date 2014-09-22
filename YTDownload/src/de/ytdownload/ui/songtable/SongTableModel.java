/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload.ui.songtable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import de.ytdownload.controller.SongManager;
import de.ytdownload.model.Song;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class SongTableModel extends DefaultTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3355927915651845691L;

	private List<Song> data;

	private SongTable table;

	public SongTableModel(SongTable table) {
		this.data = SongManager.getInstance().getSongs();

		this.table = table;
	}

	private String[] head = new String[] {
			"Title", "Author", "Album", "Length", "Genre", "Rate", "Changed"
	};

	public void update() {
		this.data = SongManager.getInstance().getSongs();
		table.repaint();
	}

	/**
	 * TODO ENTER DESCRIPTION HERE
	 * 
	 * @author jfruehau
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 * 
	 * @return
	 */
	@Override
	public int getRowCount() {
		return data == null ? 0 : getSize();
	}

	/**
	 * TODO ADD METHOD DESCRIPTION
	 * 
	 * @author jfruehau
	 * 
	 * @return
	 */
	private int getSize() {
		return data.size();
	}

	/**
	 * TODO ENTER DESCRIPTION HERE
	 * 
	 * @author jfruehau
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 * 
	 * @return
	 */
	@Override
	public int getColumnCount() {
		return head.length;
	}

	/**
	 * TODO ENTER DESCRIPTION HERE
	 * 
	 * @author jfruehau
	 * 
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 * 
	 * @param columnIndex
	 * @return
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return head[columnIndex];
	}

	/**
	 * TODO ENTER DESCRIPTION HERE
	 * 
	 * @author jfruehau
	 * 
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 * 
	 * @param columnIndex
	 * @return
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	/**
	 * TODO ENTER DESCRIPTION HERE
	 * 
	 * @author jfruehau
	 * 
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	private String toTime(int n) {
		int[] times = new int[3];
		for (int j = 0; j < times.length; j++) {
			times[j] = n % 60;
			n -= times[j];
			n = n / 60;
		}

		return String.format("%02d", times[2]) + ":" + String.format("%02d", times[1]) + ":"
				+ String.format("%02d", times[0]);
	}

	/**
	 * TODO ENTER DESCRIPTION HERE
	 * 
	 * @author jfruehau
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Song d = getSongAt(rowIndex);
		Tag t = d.getTag();
		switch (columnIndex) {
		case 0:
			return t.getFirst(FieldKey.TITLE);
		case 1:
			return t.getFirst(FieldKey.ARTIST);
		case 2:
			return t.getFirst(FieldKey.ALBUM);
		case 3:
			return toTime(d.getHeader().getTrackLength());
		case 4:
			return t.getFirst(FieldKey.GENRE);
		case 5:
			return d.getHeader().getBitRate();
		case 6:
			long f = d.getAttrib().lastModifiedTime().toMillis();
			return (new SimpleDateFormat("dd.MM.YYYY")).format(new Date(f));
		default:
			return "";

		}
	}

	/**
	 * TODO ENTER DESCRIPTION HERE
	 * 
	 * @author jfruehau
	 * 
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 * 
	 * @param aValue
	 * @param rowIndex
	 * @param columnIndex
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub

	}

	/**
	 * TODO ADD METHOD DESCRIPTION
	 * 
	 * @author jfruehau
	 * 
	 * @param row
	 * @return
	 */
	public Song getSongAt(int row) {
		return data.get(row);
	}
}
