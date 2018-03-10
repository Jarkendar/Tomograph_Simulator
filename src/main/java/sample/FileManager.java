package sample;

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
        checkDirectory(new File(OUTPUT_DIRECTORY + "/" + INDIRECT_IMAGES_DIRECTORY));
    }

    private void checkDirectory(File file) {
        if (!file.exists() || file.isFile()) {
            file.mkdir();
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
                Color color = new Color(sinogram[i][j], sinogram[i][j], sinogram[i][j], ALPHA_CHANNEL);
                bufferedImage.setRGB(i, j, color.getRGB());
            }
        }
        String filename = OUTPUT_DIRECTORY + "/" + SINOGRAMS_DIRECTORY + "/" + "sinogram-" + sinogram.length + "x" + range + "x" + sinogram[0].length + name;
        writeImageToFile(bufferedImage, filename, extension);
    }

    private void writeImageToFile(BufferedImage bufferedImage, String filename, String extension) {
        try {
            System.out.println(filename);
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

    public void saveEndImage(int[][] bitmap, String name, String extension) {
        BufferedImage bufferedImage = new BufferedImage(bitmap.length, bitmap[0].length, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < bitmap.length; i++) {
            for (int j = 0; j < bitmap[0].length; j++) {
                Color color = new Color(bitmap[i][j], bitmap[i][j], bitmap[i][j], ALPHA_CHANNEL);
                bufferedImage.setRGB(i, j, color.getRGB());
            }
        }
        String filename = OUTPUT_DIRECTORY + "/" + "output-" + name;
        writeImageToFile(bufferedImage, filename, extension);
    }

}
