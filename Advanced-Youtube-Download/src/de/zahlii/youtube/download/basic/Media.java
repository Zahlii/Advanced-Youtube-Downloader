package de.zahlii.youtube.download.basic;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Media {
	public static final ImageIcon ICON_MUSIC = getImageIcon("icon_music.png");
	public static final ImageIcon ICON_DOWNLOAD = getImageIcon("icon_download.png");
	public static final ImageIcon ICON_ADD = getImageIcon("icon_add.png");
	public static final ImageIcon ICON_SEARCH = getImageIcon("icon_search.png");
	public static final ImageIcon ICON_CANCEL = getImageIcon("icon_cross.png");
	public static final ImageIcon ICON_YTDL = getImageIcon("icon_YT.png");
	public static final ImageIcon ICON_OK = getImageIcon("icon_ok.png");
	public static final ImageIcon ICON_PREF = getImageIcon("icon_pref.png");

	private static ImageIcon getImageIcon(final String imageName) {
		final InputStream imagePathName = tryGet(imageName);
		try {
			final BufferedImage image = ImageIO.read(imagePathName);
			final ImageIcon icon = new ImageIcon(image);
			return icon;
		} catch (final IOException e) {
			Logging.log("failed loading image icon", e);
		}
		return null;
	}

	public static BufferedImage getScaledImage(final BufferedImage src,
			final int w, final int h) {
		int finalw = w;
		int finalh = h;
		double factor = 1.0d;
		if (src.getWidth() > src.getHeight()) {
			factor = (double) src.getHeight() / (double) src.getWidth();
			finalh = (int) (finalw * factor);
		} else {
			factor = (double) src.getWidth() / (double) src.getHeight();
			finalw = (int) (finalh * factor);
		}

		final BufferedImage resizedImg = new BufferedImage(finalw, finalh,
				Transparency.TRANSLUCENT);
		final Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(src, 0, 0, finalw, finalh, null);
		g2.dispose();
		return resizedImg;
	}

	private static InputStream tryGet(final String path) {
		final InputStream img = Media.class.getResourceAsStream("/" + path);
		if (img == null) {
			Logging.log("failed loading " + path);
			return null;
		} else
			return img;
	}
}
