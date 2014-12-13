package de.zahlii.youtube.download.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class CoverPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int size = 270;

	private BufferedImage img;

	public CoverPanel(final BufferedImage img) {
		this.img = img;

		setPreferredSize(new Dimension(size, size));

	}

	public BufferedImage getImage() {
		return img;
	}

	@Override
	public void paintComponent(final Graphics g) {
		final Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (img == null) {
			final String stringTime = "no artwork";

			g2d.setFont(g2d.getFont().deriveFont(28.0f));
			final FontMetrics fm = g2d.getFontMetrics();
			final Rectangle2D r = fm.getStringBounds(stringTime, g2d);
			final int x = (int) ((getWidth() - r.getWidth()) / 2.0);
			final int y = (int) ((getHeight() - r.getHeight()) / 2.0 + fm.getAscent());
			g.drawString(stringTime, x, y);
		} else {
			g.drawImage(img, 0, 0, size, size, Color.BLACK, null);

		}

	}

	public void setImage(final BufferedImage img) {
		this.img = img;
		this.repaint();
	}
}
