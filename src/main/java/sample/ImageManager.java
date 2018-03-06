package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageManager {
    private int[][][] bitmap;//[row][column][alpha]

    public ImageManager(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int matrixSize = height > width ? width : height;

        bitmap = new int[matrixSize][matrixSize][2];

        int startIForImage = (bufferedImage.getWidth()-matrixSize)/2;
        int startJForImage = (bufferedImage.getHeight()-matrixSize)/2;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                Color color = new Color(bufferedImage.getRGB(startIForImage+i, startJForImage+j));
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

    public int[][][] getBitmap() {
        return bitmap;
    }

    public Image createImageFromArray(int[][][] imageArray){
        BufferedImage image = new BufferedImage(imageArray.length, imageArray[0].length, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < imageArray.length; i++) {
            for (int j = 0; j < imageArray[i].length; j++) {
                Color color = new Color(imageArray[i][j][0], imageArray[i][j][0], imageArray[i][j][0], imageArray[i][j][1]);
                image.setRGB(i, j, color.getRGB());
            }
        }
        return castBufferedImageToImage(image);
    }
}
