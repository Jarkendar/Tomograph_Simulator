package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Controller {
    public ImageView inputImage;
    public ImageView sinogramImage;
    public ImageView outputImage;
    public ImageView stepImage;
    public Button chooseFileButton;
    public TextField detectorNumberTextField;
    public TextField degreesRangeTextField;
    public TextField measureNumberTextField;
    public Slider stepSlider;
    public CheckBox filteringCheckBox;
    public Button transformButton;

    private File file;
    private String fileExtension;
    private Image originalImage;

    public void clickChooseFile(ActionEvent actionEvent) {
        actionEvent.getSource();
        file = Main.openFileChooser();

        if (file != null) {
            fileExtension = getFileExtension(file);
            switch (fileExtension){
                case "DICOM":{
                    //todo do something
                    break;
                }
                case "png":{
                    readImage(file);
                }
                case "jpg":{
                    readImage(file);
                }
            }
            transformButton.setDisable(false);
        }else {
            transformButton.setDisable(true);
        }
    }

    private void readImage(File file){
        try {
            BufferedImage img = ImageIO.read(file);
            int width = img.getWidth();
            int height = img.getHeight();
            int[][] imgArr = new int[width][height];
            Raster raster = img.getData();
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    imgArr[i][j] = raster.getSample(i, j, 0);
                }
            }
            inputImage.setImage(castBufferedImageToImage(img));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private Image castBufferedImageToImage(BufferedImage bufferedImage){
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

}
