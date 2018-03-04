package sample;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
    private Image originalImageInGrayScale;
    private ImageManager imageManager;

    public void clickChooseFile(ActionEvent actionEvent) {
        actionEvent.getSource();
        file = Main.openFileChooser();

        if (file != null) {
            fileExtension = getFileExtension(file);
            switch (fileExtension) {
                case "DICOM": {
                    //todo do something
                    break;
                }
                case "png": {
                    readImage(file);
                }
                case "jpg": {
                    readImage(file);
                }
            }
            transformButton.setDisable(false);
        } else {
            transformButton.setDisable(true);
        }
    }

    private void readImage(File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            imageManager = new ImageManager(bufferedImage);
            inputImage.setImage(imageManager.getOriginalInGrayscale());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
