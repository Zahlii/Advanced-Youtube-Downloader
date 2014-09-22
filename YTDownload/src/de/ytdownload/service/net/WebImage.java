/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 */
package de.ytdownload.service.net;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import de.ytdownload.controller.ConfigManager;
import de.ytdownload.service.Logging;

/**
 * TODO INSERT CLASS DESCRIPTION
 * 
 * @author jfruehau
 * 
 */
public class WebImage {
	private File imageFile;

	public WebImage(String ur) {

		try {
			File i = new File(ConfigManager.TEMP_DIR + ConfigManager.DS + "thumbHTTP_"
					+ System.currentTimeMillis() + ".png");

			Logging.log("[GRACE]\tsaving image " + ur + " to " + i.getName());

			InputStream iSReader = WebNavigator.getInstance().navigateStream(ur);
			BufferedImage bufferedImage = ImageIO.read(iSReader);
			imageFile = i;
			ImageIO.write(bufferedImage, "png", i);
		} catch (IOException e) {
			Logging.log("failed loading web image", e);
		}

	}

	public File getImageFile() {
		return imageFile;
	}

}
