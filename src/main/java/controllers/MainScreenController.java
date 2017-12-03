package main.java.controllers;

import com.mpatric.mp3agic.Mp3File;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import main.java.domain.Mp3Details;
import main.java.service.Mp3Service;
import main.java.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class MainScreenController {

    @Autowired
    Mp3Service mp3Service;

    @FXML
    private TableView<Mp3Details> tableView;
    @FXML
    private TableColumn<Mp3Details, String> fileName;
    @FXML
    private TableColumn<Mp3Details, String> trackArtist;
    @FXML
    private TableColumn<Mp3Details, String> trackName;
    @FXML
    private TableColumn<Mp3Details, String> trackYear;

    private ObservableList<Mp3Details> tableData;

    @FXML
    private Button openFiles;

    public void openFiles() {
        FileChooser fileChooser = new FileChooser();
        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(Constants.MP3_FILE_TYPE_DESCRIPTION, Constants.MP3_FILE_TYPE_EXTENSION);
        fileChooser.getExtensionFilters().add(extFilter);
        // Open dialog for choosing mp3 files
        List<File> files = fileChooser.showOpenMultipleDialog(tableView.getScene().getWindow());
        if (files != null) {
            boolean isSuccess = mp3Service.insertFiles(files);
            if (isSuccess) {
                updateTable();
            }
        }

    }

    private void updateTable() {
        tableData = FXCollections.observableArrayList();
        List<Mp3File> importedFiles = mp3Service.getInsertedFiles();
        for (Mp3File mp3File : importedFiles) {
            tableData.add(Mp3Details.deserialize(mp3File));
        }

        fileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        trackArtist.setCellValueFactory(new PropertyValueFactory<>("trackArtist"));
        trackName.setCellValueFactory(new PropertyValueFactory<>("trackName"));
        trackYear.setCellValueFactory(new PropertyValueFactory<>("trackYear"));

        tableView.setItems(null);
        tableView.setItems(tableData);
    }
}
