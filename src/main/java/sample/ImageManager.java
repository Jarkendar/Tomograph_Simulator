package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageManager {
    int[][][] bitmap;//[row][column][alpha]

    public ImageManager(BufferedImage bufferedImage) {
        bitmap = new int[bufferedImage.getWidth()][bufferedImage.getHeight()][2];
        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
                Color color = new Color(bufferedImage.getRGB(i, j));
                int gray = (color.getBlue() + color.getGreen() + color.getRed()) / 3;
                bitmap[i][j][0] = gray;
                bitmap[i][j][1] = color.getAlpha();
            }
        }
    }

    public Image getOriginalInGrayscale() {
        BufferedImage image = new BufferedImage(bitmap.length, bitmap[0].length, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < bitmap.length; i++) {
            for (int j = 0; j < bitmap[i].length; j++) {
                Color color = new Color(bitmap[i][j][0], bitmap[i][j][0], bitmap[i][j][0], bitmap[i][j][1]);
                image.setRGB(i, j, color.getRGB());
            }
        }
        return castBufferedImageToImage(image);
    }

    private Image castBufferedImageToImage(BufferedImage bufferedImage) {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

}
