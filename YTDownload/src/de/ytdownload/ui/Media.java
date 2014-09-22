/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload.ui;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import de.ytdownload.service.Logging;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class Media {
	public static final ImageIcon ICON_MUSIC = getImageIcon("icon_music.png");
	public static final ImageIcon ICON_DOWNLOAD = getImageIcon("icon_download.png");
	public static final ImageIcon ICON_ADD = getImageIcon("icon_add.png");
	public static final ImageIcon ICON_SEARCH = getImageIcon("icon_search.png");
	public static final ImageIcon ICON_CANCEL = getImageIcon("icon_cross.png");
	public static final ImageIcon ICON_YTDL = getImageIcon("icon_YT.png");
	public static final ImageIcon ICON_OK = getImageIcon("icon_ok.png");

	private static ImageIcon getImageIcon(String imageName) {
		InputStream imagePathName = tryGet(imageName);
		try {
			BufferedImage image = ImageIO.read(imagePathName);
			ImageIcon icon = new ImageIcon(image);
			return icon;
		} catch (IOException e) {
			Logging.log("failed loading image icon", e);
		}
		return null;
	}

	private static InputStream tryGet(String path) {
		InputStream img = Media.class.getResourceAsStream("/" + path);
		if (img == null) {
			Logging.log("failed loading " + path);
			return null;
		} else {
			return img;
		}
	}

	/**
	 * TODO ADD METHOD DESCRIPTION
	 * 
	 * @author jfruehau
	 * 
	 * @param main
	 * @return
	 */
	public static String sanitize(String main) {
		return main.replace("<", "").replace(">", "").replace(":", "").replace("/", "")
				.replace("\\", "").replace("|", "").replace("?", "").replace("*", "")
				.replace("\"", "");
	}

	/**
	 * Resizes an image using a Graphics2D object backed by a BufferedImage.
	 * 
	 * @param srcImg
	 *            - source image to scale
	 * @param w
	 *            - desired width
	 * @param h
	 *            - desired height
	 * @return - the new resized image
	 */
	public static BufferedImage getScaledImage(BufferedImage src, int w, int h) {
		int finalw = w;
		int finalh = h;
		double factor = 1.0d;
		if (src.getWidth() > src.getHeight()) {
			factor = ((double) src.getHeight() / (double) src.getWidth());
			finalh = (int) (finalw * factor);
		} else {
			factor = ((double) src.getWidth() / (double) src.getHeight());
			finalw = (int) (finalh * factor);
		}

		BufferedImage resizedImg = new BufferedImage(finalw, finalh, BufferedImage.TRANSLUCENT);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(src, 0, 0, finalw, finalh, null);
		g2.dispose();
		return resizedImg;
	}
}
