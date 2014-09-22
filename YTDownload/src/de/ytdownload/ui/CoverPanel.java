/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.jaudiotagger.tag.images.Artwork;

import de.ytdownload.controller.ConfigManager;
import de.ytdownload.model.Song;
import de.ytdownload.service.Logging;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class CoverPanel extends JPanel {
	private static final int size = 270;

	private Song s;
	private BufferedImage img;
	private File imageFile;

	public CoverPanel(Song s) {
		this.s = s;
		this.setPreferredSize(new Dimension(size, size));
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		List<Artwork> art = null;
		try {
			art = s.getTag().getArtworkList();
		} catch (NullPointerException e) {
			return;
		}
		if (art.size() <= 0) {
			String stringTime = "no artwork";

			FontMetrics fm = g2d.getFontMetrics();
			Rectangle2D r = fm.getStringBounds(stringTime, g2d);
			int x = (int) (((double) this.getWidth() - (double) r.getWidth()) / 2.0);
			int y = (int) (((double) this.getHeight() - (double) r.getHeight()) / 2.0 + (double) fm
					.getAscent());
			g.drawString(stringTime, x, y);
		} else {
			getImageFile();

			g.drawImage(img, 0, 0, size, size, Color.BLACK, null);

		}

	}

	public File getImageFile() {
		if (img != null)
			return imageFile;

		List<Artwork> art = null;
		art = s.getTag().getArtworkList();

		if (art.isEmpty())
			return null;

		byte[] data = art.get(0).getBinaryData();
		try {
			img = ImageIO.read(new ByteArrayInputStream(data));

			imageFile = new File(ConfigManager.TEMP_DIR + ConfigManager.DS + "thumbEXTRACT_"
					+ System.currentTimeMillis() + ".png");

			ImageIO.write(img, "png", imageFile);
			return imageFile;
		} catch (IOException e) {
			Logging.log("failed loading artwork", e);
		}
		return null;
	}
}
