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
import java.util.Observable;
import java.util.Observer;

public class Controller implements Observer {
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
    private SinogramCreator sinogramCreator;

    public void initialize() {
        detectorNumberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (canCastToInteger(newValue)) {
                int number = Integer.parseInt(newValue);
                if (number > 0) {
                    transformButton.setDisable(false);
                } else {
                    transformButton.setDisable(true);
                }
            } else {
                transformButton.setDisable(true);
            }
        });
        degreesRangeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (canCastToInteger(newValue)) {
                int number = Integer.parseInt(newValue);
                if (number > 0 && number < 360) {
                    transformButton.setDisable(false);
                } else {
                    transformButton.setDisable(true);
                }
            } else {
                transformButton.setDisable(true);
            }
        });
        measureNumberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (canCastToInteger(newValue)) {
                int number = Integer.parseInt(newValue);
                if (number > 0) {
                    transformButton.setDisable(false);
                } else {
                    transformButton.setDisable(true);
                }
            } else {
                transformButton.setDisable(true);
            }
        });
        transformButton.setDisable(!allInputDataIsCorrect());
    }

    private boolean canCastToInteger(String text) {
        return text.matches("[0-9]+");
    }

    private boolean allInputDataIsCorrect() {
        if (file == null) return false;
        if (!canCastToInteger(detectorNumberTextField.getText())) return false;
        if (!canCastToInteger(degreesRangeTextField.getText())) return false;
        if (!canCastToInteger(measureNumberTextField.getText())) return false;
        return true;
    }

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
                    sinogramCreator = new SinogramCreator(imageManager.getBitmap(), 5, 360, 180);
                    Thread thread = new Thread(sinogramCreator);
                    thread.start();
                    break;
                }
                case "jpg": {
                    readImage(file);
                    break;
                }
            }
            transformButton.setDisable(!allInputDataIsCorrect());
        } else {
            transformButton.setDisable(allInputDataIsCorrect());
        }
    }

    private void readImage(File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            imageManager = new ImageManager(bufferedImage);
            inputImage.setImage(imageManager.getOriginalInGrayscale());

            int[][] ints = new int[180][90];
            for (int i = 0; i < ints.length; i++) {
                for (int j = 0; j < ints[0].length; j++) {
                    ints[i][j] = 255;
                }
            }


            setSinogramImage(imageManager.createImageFromSinogram(ints));
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

    @Override
    public void update(Observable observable, Object arg) {
        if (observable instanceof SinogramCreator) {
            ((SinogramCreator) observable).getSinogramBitmap();
        }
    }

    private void setSinogramImage(Image image) {
        sinogramImage.setImage(image);
    }
}
