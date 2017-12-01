package main.java.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.java.util.Constants;

import java.io.File;
import java.util.List;

public class MainScreenController {

    private Stage stage;

    @FXML
    private Button openFiles;

    public void openFiles () {
        FileChooser fileChooser = new FileChooser();
        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(Constants.MP3_FILE_TYPE_DESCRIPTION, Constants.MP3_FILE_TYPE_EXTENSION);
        fileChooser.getExtensionFilters().add(extFilter);
        // Open dialog for choosing mp3 files
        List<File> file = fileChooser.showOpenMultipleDialog(stage);
    }

    public void init(Stage stage) {
        this.stage = stage;
    }
}
