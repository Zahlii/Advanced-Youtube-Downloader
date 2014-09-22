/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload.ui.songtable;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;

import javax.swing.DefaultRowSorter;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import de.ytdownload.model.Song;
import de.ytdownload.ui.frame.MetaSearchUI;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class SongTable extends JTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1248710957557064111L;

	// "Title", "Author", "Length", "Genre", "Rate", "Changed"
	private int[] width = new int[] {
			180, 180, 120, 50, 70, 50, 100
	};

	private MetaSearchUI ui;

	private DefaultRowSorter<SongTableModel, Object> sort;

	private Comparator<?>[] comp = new Comparator<?>[] {
			null, null, null, null, null, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return f(o1) - f(o2);
				}

				private int f(String f) {
					return Integer.valueOf(f.replace("~", "")) + (f.contains("~") ? 1 : 1);
				}

			}, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return t(o1) - t(o2);
				}

				private int t(String f) {
					String[] p = f.split("\\.");
					return Integer.valueOf(p[2] + p[1] + p[0]);
				}

			}
	};

	public SongTable(MetaSearchUI ui) {
		super();
		this.ui = ui;
		update();
	}

	public void update() {
		final SongTableModel m = new SongTableModel(this);
		this.setRowSorter(new TableRowSorter<SongTableModel>(m));
		this.setModel(m);
		TableColumn column = null;
		this.setRowHeight(30);
		sort = ((DefaultRowSorter<SongTableModel, Object>) this.getRowSorter());
		for (int i = 0; i < width.length; i++) {
			column = this.getColumnModel().getColumn(i);
			column.setPreferredWidth(width[i]);
			if (comp[i] != null)
				sort.setComparator(i, comp[i]);

			else
				sort.setComparator(i, new Comparator<String>() {

					@Override
					public int compare(String o1, String o2) {
						return o1.compareToIgnoreCase(o2);
					}

				});
		}

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = rowAtPoint(e.getPoint());
					int modelRow = convertRowIndexToModel(row);
					Song s = m.getSongAt(modelRow);
					ui.handleMetaSearch(s);
				}

			}
		});
	}

	public void filterSongs(String search) {
		if (search.trim().equals("") || search.equals(SearchField.SEARCH))
			search = "\\.*";

		RowFilter<SongTableModel, Object> rf = null;
		// If current expression doesn't parse, don't update.
		try {
			rf = RowFilter.regexFilter("(?i)" + search);
		} catch (java.util.regex.PatternSyntaxException e) {
			return;
		}
		sort.setRowFilter(rf);
	}
}
