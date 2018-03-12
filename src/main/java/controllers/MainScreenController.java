package main.java.controllers;

import com.mpatric.mp3agic.Mp3File;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import main.java.domain.FailedFileDetails;
import main.java.domain.Mp3Details;
import main.java.model.Mp3Model;
import main.java.util.Constants;
import main.java.service.Mp3Service;
import main.java.workers.ImportFilesWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MainScreenController implements Mp3Model.Mp3FilesObserver {

    @Autowired
    private Mp3Model mp3Model;

    private Scene scene;

    @FXML
    private RadioButton cyrillicRadioButton;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextFlow infoArea;

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
    @FXML
    private ProgressBar progressBar;

    private ObservableList<Mp3Details> tableData;

    // test speed
    long longerTime = 0;

    @FXML
    public void initialize() {
        mp3Model.registerObserver(this);

        tableView.getSelectionModel().setSelectionMode(
                SelectionMode.MULTIPLE
        );

        // Add event handling for deleting multiple files
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case DELETE:
                    ObservableList<Mp3Details> selectedItems = tableView.getSelectionModel().getSelectedItems();
                    List<String> filePathsForRemove = selectedItems.stream().map(Mp3Details::getFilePath).collect(Collectors.toList());
                    mp3Model.removeImportedFiles(filePathsForRemove);
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

    public void setScene(Scene scene) {
        this.scene = scene;

        scene.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles() && Mp3Service.ifContainsAnyMp3File(db.getFiles())) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });

        // Dropping over surface
        scene.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {

                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    List<File> dbFiles = db.getFiles();

                    ProgressForm progressForm = new ProgressForm(scene);
                    Task importFilesWorker = new ImportFilesWorker(mp3Model, progressForm, dbFiles);

                    // binds progress of progress bars to progress of task:
                    progressForm.activateProgressBar(importFilesWorker);

                    scene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(true));

                    // open progress dialog
                    progressForm.getDialogStage().show();
                    new Thread(importFilesWorker).start();
                }

                event.setDropCompleted(success);
                event.consume();
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
            mp3Model.importFiles(files);
        }
    }

    public void openFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(tableView.getScene().getWindow());
        if (selectedDirectory != null) {
            File[] selectedFiles = selectedDirectory.listFiles();
            List<File> filesForImport = new LinkedList<>();
            for (File selectedFile : selectedFiles) {
                if (selectedFile.isFile() && (Constants.MP3_FILE_TYPE_EXTENSION.equalsIgnoreCase(Mp3Service.getFileExtension(selectedFile)))) {
                    filesForImport.add(selectedFile);
                }
            }

            mp3Model.importFiles(filesForImport);
        }
    }

    public void start() {
        mp3Model.generateTagsForImportedFiles(cyrillicRadioButton.isSelected());
        mp3Model.saveImportedFiles();
    }

    private void updateTable() {
        tableData = FXCollections.observableArrayList();
        //mp3Model.getImportedFiles().values().parallelStream().forEach(f -> tableData.add(Mp3Details.deserialize(f)));
        for (Mp3File mp3File : mp3Model.getImportedFiles().values()) {
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

    private void addFileToTable(Mp3File file) {
        tableData.add(Mp3Details.deserialize(file));
        //tableView.sort();
    }

    @Override
    public void onImportedFileChanged(Mp3File file) {
        Platform.runLater ( () -> {
            /*long startTime = System.currentTimeMillis();*/
            addFileToTable(file);
            /*long spendTime = System.currentTimeMillis() - startTime;
            if (spendTime > longerTime) {
                longerTime = spendTime;
                System.out.println("MAXTIME = " + longerTime);
            }*/
        });
    }

    @Override
    public void onImportedFilesChanged() {

        Platform.runLater(() -> {
            updateTable();

            Calendar rightNowCalendar = Calendar.getInstance();
            int hours = rightNowCalendar.get(Calendar.HOUR_OF_DAY);
            int minutes = rightNowCalendar.get(Calendar.MINUTE);
            int seconds = rightNowCalendar.get(Calendar.SECOND);
            String dateString = ((hours < 10) ? ("0" + hours) : hours) + ":"
                    + ((minutes < 10) ? ("0" + minutes) : minutes) + ":"
                    + ((seconds < 10) ? ("0" + seconds) : seconds) + " - ";

            if (mp3Model.getLastFailedLoadingFiles() != null) {
                if (mp3Model.getLastFailedLoadingFiles().size() > 0) {
                    Text importErrorText = new Text();
                    importErrorText.setText(dateString + "This files cannot be imported:\n");
                    importErrorText.setFill(Color.RED);
                    infoArea.getChildren().addAll(importErrorText);
                    //scrollPane.setVvalue(1.0);           //1.0 means 100% at the bottom

                    for (FailedFileDetails fileDetails : mp3Model.getLastFailedLoadingFiles()) {
                        Text fileErrorText = new Text();
                        fileErrorText.setText("\t\t " + fileDetails.getFilePath() + " (" + fileDetails.getErrorMessage() + ")\n");
                        fileErrorText.setFill(Color.RED);
                        infoArea.getChildren().addAll(fileErrorText);
                        //scrollPane.setVvalue(1.0);           //1.0 means 100% at the bottom
                    }
                } else {
                    Text importFilesSuccessText = new Text();
                    importFilesSuccessText.setText(dateString + "Files imported successfully.\n");
                    importFilesSuccessText.setFill(Color.BLUE);
                    infoArea.getChildren().addAll(importFilesSuccessText);
                }
            } else {
                Text importFilesSuccessText = new Text();
                importFilesSuccessText.setText(dateString + "Files imported successfully.\n");
                importFilesSuccessText.setFill(Color.BLUE);
                infoArea.getChildren().addAll(importFilesSuccessText);
            }

            scrollPane.setVvalue(1.0);           //1.0 means 100% at the bottom
        });
    }

    @Override
    public void onSavedImportedFiles() {
        System.out.println("On saved imported files");
        updateTable();
    }

    @Override
    public void onTagGenerated() {
        System.out.println("On tag generated");
    }
}
