package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
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
    public CheckBox mseCheckBox;
    public Button transformButton;
    public LineChart<Integer, Double> lineChart;
    public NumberAxis lineChartXAxis;
    public NumberAxis lineChartYAxis;

    private File file = null;
    private ImageManager imageManager;
    private FileManager fileManager;

    private long start;
    private long stop;

    public void initialize() {
        fileManager = new FileManager();
        detectorNumberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (canCastToInteger(newValue)) {
                int value = Integer.parseInt(newValue);
                if (value > 0) {
                    transformButton.setDisable(!canPressStartButton());
                } else {
                    transformButton.setDisable(!canPressStartButton());
                }
            } else {
                transformButton.setDisable(!canPressStartButton());
            }
        });
        degreesRangeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (canCastToInteger(newValue)) {
                int number = Integer.parseInt(newValue);
                if (number > 0 && number < 360) {
                    transformButton.setDisable(!canPressStartButton());
                } else {
                    transformButton.setDisable(canPressStartButton());
                }
            } else {
                transformButton.setDisable(!canPressStartButton());
            }
        });
        measureNumberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (canCastToInteger(newValue)) {
                int number = Integer.parseInt(newValue);
                if (number > 0 && number < 10001) {
                    transformButton.setDisable(!canPressStartButton());
                    if (stepSlider.isDisable()) {
                        setMaxSliderStep(number);
                    }
                } else {
                    transformButton.setDisable(!canPressStartButton());
                }
            } else {
                transformButton.setDisable(!canPressStartButton());
            }
        });
        stepSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int position = newValue.intValue();
            if (newValue.intValue() > stepSlider.getMax()) {
                position = (int) stepSlider.getMax() - 1;
            }
            stepImage.setImage(fileManager.readTmpFile(file.getName(), position));
        });
        transformButton.setDisable(!canPressStartButton());
        mseCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (mseCheckBox.isSelected()) {
                inputImage.setVisible(false);
                sinogramImage.setVisible(false);
                outputImage.setVisible(false);
                stepImage.setVisible(false);
                lineChart.setVisible(true);
            } else {
                inputImage.setVisible(true);
                sinogramImage.setVisible(true);
                outputImage.setVisible(true);
                stepImage.setVisible(true);
                lineChart.setVisible(false);
            }
        });
    }

    private void createMSELineChart(SinogramCreator sinogramCreator) {
        Platform.runLater(() -> {
            XYChart.Series<Integer,Double> series = new XYChart.Series<>();
            series.setName("MSE");
            double[] mseArray = sinogramCreator.getRmseArray();
            for (int i = 1; i < mseArray.length; i++) {
                XYChart.Data<Integer,Double> data = new XYChart.Data<>(i, (mseArray[i] / mseArray[0]) * 100);
                series.getData().add(data);
            }
            lineChart.getXAxis().setLabel("Steps");
            lineChart.getYAxis().setLabel("Error [%]");

            lineChartXAxis.setAutoRanging(true);
            lineChartYAxis.setAutoRanging(true);

            lineChart.setCreateSymbols(false);

            lineChart.getData().add(series);
        });
    }


    private boolean canCastToInteger(String text) {
        return text.matches("[0-9]+");
    }

    private boolean canPressStartButton() {
        return file != null
                && canCastToInteger(detectorNumberTextField.getText())
                && canCastToInteger(degreesRangeTextField.getText())
                && canCastToInteger(measureNumberTextField.getText());
    }

    public void clickChooseFile(ActionEvent actionEvent) {
        actionEvent.getSource();
        file = Main.openFileChooser();

        if (file != null) {
            String fileExtension = getFileExtension(file);
            switch (fileExtension) {
                case "png": {
                    readImage(file);
                    break;
                }
                case "jpg": {
                    readImage(file);
                    break;
                }
            }
            transformButton.setDisable(!canPressStartButton());
        } else {
            transformButton.setDisable(canPressStartButton());
        }
    }

    private void readImage(File file) {
        imageManager = new ImageManager(fileManager.readImageFromFile(file));
        inputImage.setImage(imageManager.getOriginalInGrayscale());
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (observable instanceof SinogramCreator) {
            String message = (String) arg;
            switch (message) {
                case SinogramCreator.SINOGRAM_IS_END: {
                    fileManager.saveSinogram(((SinogramCreator) observable).getSinogramBitmap(), file.getName(), getFileExtension(file), Integer.parseInt(degreesRangeTextField.getText()));
                    setSinogramImage(imageManager.createImageFromSinogram(((SinogramCreator) observable).getSinogramBitmap()));
                    break;
                }
                case SinogramCreator.REVERSE_IS_END: {
                    fileManager.saveOutputImage(((SinogramCreator) observable).getOutputBitmap(), file.getName(), getFileExtension(file));
                    setOutputImage(imageManager.createImageFromArray(((SinogramCreator) observable).getOutputBitmap()));
                    transformButton.setDisable(false);
                    setMaxSliderStep(Integer.parseInt(measureNumberTextField.getText()));
                    stepSlider.setDisable(false);
                    stepImage.setImage(fileManager.readTmpFile(file.getName(), (int) stepSlider.getValue()));
                    createMSELineChart((SinogramCreator) observable);
                    break;
                }
            }
        }
    }

    private void setMaxSliderStep(int i) {
        stepSlider.setMax(i - 1);
    }

    private void setOutputImage(Image image) {
        outputImage.setImage(image);
        reverseDisableFields();
        stop = System.currentTimeMillis();
        System.out.println("Time = " + (stop - start) + "ms");
    }

    private void setSinogramImage(Image image) {
        sinogramImage.setImage(image);
    }

    public void clickStartButton(ActionEvent actionEvent) {
        lineChart.getData().clear();
        SinogramCreator sinogramCreator = new SinogramCreator(imageManager.getBitmap(), Integer.parseInt(detectorNumberTextField.getText()),
                Integer.parseInt(measureNumberTextField.getText()), Integer.parseInt(degreesRangeTextField.getText()),
                file.getName(), filteringCheckBox.isSelected());
        sinogramCreator.addObserver(this);
        new Thread(sinogramCreator).start();
        reverseDisableFields();
        stepSlider.setDisable(true);
        start = System.currentTimeMillis();
    }

    private void reverseDisableFields() {
        chooseFileButton.setDisable(!chooseFileButton.isDisable());
        filteringCheckBox.setDisable(!filteringCheckBox.isDisable());
        transformButton.setDisable(!transformButton.isDisable());
        measureNumberTextField.setDisable(!measureNumberTextField.isDisable());
        detectorNumberTextField.setDisable(!detectorNumberTextField.isDisable());
        degreesRangeTextField.setDisable(!degreesRangeTextField.isDisable());
    }
}
