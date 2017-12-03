package main.java.controllers;

import com.mpatric.mp3agic.Mp3File;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import main.java.domain.Mp3Details;
import main.java.service.Mp3Service;
import main.java.util.Constants;
import main.java.util.FileHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MainScreenController {

    @Autowired
    Mp3Service mp3Service;

    @FXML
    private RadioButton cyrillicRadioButton;

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

    private void insertFiles(List<File> filesForImport) {
        boolean isSuccess = mp3Service.insertFiles(filesForImport);
        if (isSuccess) {
            updateTable();
        }
    }

    @FXML
    public void initialize() {
        tableView.getSelectionModel().setSelectionMode(
                SelectionMode.MULTIPLE
        );

        // Add event handling for deleting multiple files
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case DELETE:
                    ObservableList<Mp3Details> selectedItems = tableView.getSelectionModel().getSelectedItems();
                    List<String> filePathsForRemove = selectedItems.stream().map(Mp3Details::getFilePath).collect(Collectors.toList());
                    mp3Service.removeInsertedFiles(filePathsForRemove);
                    updateTable();
                    break;
                case ESCAPE:
                    tableView.getSelectionModel().clearSelection();
                    break;
                default:
                    break;
            }
        });

        // Clear selection in table view on clicking on empty rows
        tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            Node source = event.getPickResult().getIntersectedNode();

            // move up through the node hierarchy until a TableRow or scene root is found
            while (source != null && !(source instanceof TableRow)) {
                source = source.getParent();
            }

            // clear selection on click anywhere but on a filled row
            if (source == null || (source instanceof TableRow && ((TableRow) source).isEmpty())) {
                tableView.getSelectionModel().clearSelection();
            }
        });
    }

    public void openFiles() {
        FileChooser fileChooser = new FileChooser();
        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(Constants.MP3_FILE_TYPE_DESCRIPTION, "*" + Constants.MP3_FILE_TYPE_EXTENSION);
        fileChooser.getExtensionFilters().add(extFilter);
        // Open dialog for choosing mp3 files
        List<File> files = fileChooser.showOpenMultipleDialog(tableView.getScene().getWindow());
        if (files != null) {
            insertFiles(files);
        }
    }

    public void openFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(tableView.getScene().getWindow());
        if (selectedDirectory != null) {
            File[] selectedFiles = selectedDirectory.listFiles();
            List<File> filesForImport = new LinkedList<>();
            for (File selectedFile : selectedFiles) {
                if (selectedFile.isFile() && (Constants.MP3_FILE_TYPE_EXTENSION.equalsIgnoreCase(FileHelper.getFileExtension(selectedFile)))) {
                    filesForImport.add(selectedFile);
                }
            }

            insertFiles(filesForImport);
        }
    }

    public void start() {
        mp3Service.setTagsForFilesAndSave(cyrillicRadioButton.isSelected());
        updateTable();
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

        // Sort files by name ascending
        fileName.setSortType(TableColumn.SortType.ASCENDING);
        tableView.getSortOrder().add(fileName);
        tableView.sort();
    }
}
