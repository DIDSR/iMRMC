package mrmc.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;

/**
 * Renders table headers for rows
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 * @author Qi Gong
 * 
 * @version 1.0 11/09/98
 */
public class RowHeaderRenderer extends JLabel implements ListCellRenderer<Object> {

	private static final long serialVersionUID = 1L;

	/**
	 * Sole constructor. Creates a row header for a table so that each row can
	 * be labeled
	 * 
	 * @param table Table for which the row header is being created
	 */
	public RowHeaderRenderer(JTable table) {
		JTableHeader header = table.getTableHeader();
		setOpaque(true);
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		setHorizontalAlignment(CENTER);
		setForeground(header.getForeground());
		setBackground(header.getBackground());
		setFont(header.getFont());
	}

	/**
	 * Gets the individual row header
	 */
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		setText((value == null) ? "" : value.toString());
		return this;
	}
}
