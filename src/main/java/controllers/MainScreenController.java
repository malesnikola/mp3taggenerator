package main.java.controllers;

import com.mpatric.mp3agic.Mp3File;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.java.repositories.Mp3Repository;
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
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        Mp3Repository.getInstance().importFiles(files);
        for (Mp3File mp3File : Mp3Repository.getInstance().getFiles()) {
            System.out.println(mp3File.getFilename());
        }
    }

    public void init(Stage stage) {
        this.stage = stage;
    }
}
