package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FileManager {

    private static final String OUTPUT_DIRECTORY = "output";
    private static final String SINOGRAMS_DIRECTORY = "sinograms";
    private static final String INDIRECT_IMAGES_DIRECTORY = "indirect_images";
    private static final int ALPHA_CHANNEL = 255;
    private static final String FORMAT_JPG = "JPG";
    private static final String FORMAT_PNG = "PNG";


    public FileManager() {
        checkDirectory(new File(OUTPUT_DIRECTORY));
        checkDirectory(new File(OUTPUT_DIRECTORY + "/" + SINOGRAMS_DIRECTORY));
        File temporaryDirectory = new File(OUTPUT_DIRECTORY + "/" + INDIRECT_IMAGES_DIRECTORY);
        checkDirectory(temporaryDirectory);
        clearTemporaryDirectory(temporaryDirectory);
    }

    private void checkDirectory(File file) {
        if (!file.exists() || file.isFile()) {
            file.mkdir();
        }
    }

    private void clearTemporaryDirectory(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                f.delete();
            }
        }
    }

    public BufferedImage readImageFromFile(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error in read image.");
            return null;
        }
    }

    public void saveSinogram(int[][] sinogram, String name, String extension, int range) {
        BufferedImage bufferedImage = new BufferedImage(sinogram.length, sinogram[0].length, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < sinogram.length; i++) {
            for (int j = 0; j < sinogram[0].length; j++) {
                int colorValue = sinogram[i][j];
                if (colorValue < 0) colorValue = 0;
                if (colorValue > 255) colorValue = 255;
                Color color = new Color(colorValue, colorValue, colorValue, ALPHA_CHANNEL);
                bufferedImage.setRGB(i, j, color.getRGB());
            }
        }
        String filename = OUTPUT_DIRECTORY + "/" + SINOGRAMS_DIRECTORY + "/" + "sinogram-" + sinogram.length + "x" + range + "x" + sinogram[0].length + name;
        writeImageToFile(bufferedImage, filename, extension);
    }

    private void writeImageToFile(BufferedImage bufferedImage, String filename, String extension) {
        try {
            File file = new File(filename);
            switch (extension) {
                case "png": {
                    ImageIO.write(bufferedImage, FORMAT_PNG, file);
                    break;
                }
                case "jpg": {
                    ImageIO.write(bufferedImage, FORMAT_JPG, file);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveOutputImage(int[][] bitmap, String name, String extension) {
        BufferedImage bufferedImage = makeBufferedImageFromBitmap(bitmap);
        String filename = OUTPUT_DIRECTORY + "/" + "output-" + name;
        writeImageToFile(bufferedImage, filename, extension);
    }

    public void saveIndirectImage(int[][] bitmap, String name, int iteration) {
        BufferedImage bufferedImage = makeBufferedImageFromBitmap(bitmap);
        String path = OUTPUT_DIRECTORY + "/" + INDIRECT_IMAGES_DIRECTORY + "/" + name + "_" + iteration;
        writeImageToFile(bufferedImage, path, "jpg");
    }

    private BufferedImage makeBufferedImageFromBitmap(int[][] bitmap) {
        BufferedImage bufferedImage = new BufferedImage(bitmap.length, bitmap[0].length, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < bitmap.length; i++) {
            for (int j = 0; j < bitmap[0].length; j++) {
                Color color = new Color(bitmap[i][j], bitmap[i][j], bitmap[i][j], ALPHA_CHANNEL);
                bufferedImage.setRGB(i, j, color.getRGB());
            }
        }
        return bufferedImage;
    }

    public Image readTmpFile(String name, int iteration) {
        String path = OUTPUT_DIRECTORY + "/" + INDIRECT_IMAGES_DIRECTORY + "/" + name + "_" + iteration;
        BufferedImage bufferedImage = readImageFromFile(new File(path));
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
}
