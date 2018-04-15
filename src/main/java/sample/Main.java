package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    private static final FileChooser fileChooser = new FileChooser();
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/sample.fxml"));
        primaryStage.setTitle("Tomograph_Simulator");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }

    public static File openFileChooser() {
        fileChooser.setTitle("Choose file:");
        File file = fileChooser.showOpenDialog(stage);
        return file;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
