package main.java.controllers;

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
import javafx.util.Callback;
import main.java.domain.FailedFileDetails;
import main.java.domain.Mp3Details;
import main.java.domain.Mp3FileWrapper;
import main.java.model.Mp3Model;
import main.java.util.Constants;
import main.java.service.Mp3Service;
import main.java.workers.GenerateTagsWorker;
import main.java.workers.ImportFilesWorker;
import main.java.workers.SaveFilesWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javafx.scene.control.TableColumn;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MainScreenController implements Mp3Model.Mp3FilesObserver {

    @Autowired
    private Mp3Model mp3Model;

    private Scene scene;
    private ResourceBundle resourceBundle;
    private Task saveFilesWorker;

    // menu
    @FXML
    private Menu fileMenu;
    @FXML
    private Menu languageMenu;

    // menu items
    @FXML
    private MenuItem openFilesMenuItem;
    @FXML
    private MenuItem openFolderMenuItem;

    // labels
    @FXML
    private Label chooseAlphabetLabel;
    @FXML
    private Label choosePatternLabel;
    @FXML
    private Label consoleLabel;

    // radio buttons
    @FXML
    private RadioButton artistYearTitleRadButton;
    @FXML
    private RadioButton artistLiveYearTitleRadButton;
    @FXML
    private RadioButton artistTitleRadButton;
    @FXML
    private RadioButton titleRadButton;
    @FXML
    private RadioButton cyrillicRadioButton;
    @FXML
    private RadioButton latinRadioButton;

    // buttons
    @FXML
    private Button generateButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button stopButton;

    // data table
    private ObservableList<Mp3Details> tableData;
    @FXML
    private TableView<Mp3Details> tableView;
    @FXML
    private TableColumn<Mp3Details, String> fileName;
    @FXML
    private TableColumn<Mp3Details, String> trackArtist;
    @FXML
    private TableColumn<Mp3Details, String> trackYear;
    @FXML
    private TableColumn<Mp3Details, String> trackName;
    private TableColumn<Mp3Details, String> patternColumn;
    private TableColumn stateColumn;

    // text flow
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextFlow infoArea;

    // progress bar
    @FXML
    private ProgressBar progressBar;

    // test speed
    long longerTime = 0;

    private void populateUIWithLocalizedStrings() {
        // menu
        try {
            fileMenu.setText(resourceBundle.getString("menu.file.text"));
            languageMenu.setText(resourceBundle.getString("menu.language.text"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // menu items
        try {
            openFilesMenuItem.setText(resourceBundle.getString("menu.item.openfiles.text"));
            openFolderMenuItem.setText(resourceBundle.getString("menu.item.openfolder.text"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // labels
        try {
            chooseAlphabetLabel.setText(resourceBundle.getString("label.choosealphabet.text"));
            choosePatternLabel.setText(resourceBundle.getString("label.choosepattern.text"));
            consoleLabel.setText(resourceBundle.getString("label.console.text"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // radio buttons
        try {
            String val = resourceBundle.getString("radio.button.cyrillic.text");
            cyrillicRadioButton.setText(new String(val.getBytes("ISO-8859-1"), "cp1250"));
            latinRadioButton.setText(resourceBundle.getString("radio.button.latin.text"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // buttons
        try {
            String val = resourceBundle.getString("button.generate.text");
            generateButton.setText(new String(val.getBytes("ISO-8859-1"), "cp1250"));
            val = resourceBundle.getString("button.save.text");
            saveButton.setText(new String(val.getBytes("ISO-8859-1"), "cp1250"));
            stopButton.setText(resourceBundle.getString("button.stop.text"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // columns on data table
        try {
            fileName.setText(resourceBundle.getString("table.column.filepath.text"));
            trackArtist.setText(resourceBundle.getString("table.column.artist.text"));
            trackYear.setText(resourceBundle.getString("table.column.year.text"));
            trackName.setText(resourceBundle.getString("table.column.title.text"));
            String val = resourceBundle.getString("table.column.pattern.text");
            patternColumn.setText(new String(val.getBytes("ISO-8859-1"), "cp1250"));
            stateColumn.setText(resourceBundle.getString("table.column.state.text"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        mp3Model.registerObserver(this);
        resourceBundle = ResourceBundle.getBundle("main.resources.bundles.Bundle", new Locale("en", "EN"));

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

        patternColumn = new TableColumn(resourceBundle.getString("table.column.pattern.text"));
        patternColumn.setStyle("-fx-alignment: CENTER;");
        patternColumn.setCellValueFactory(new PropertyValueFactory<Mp3Details,String>("filePattern"));
        tableView.getColumns().add(patternColumn);

        stateColumn = new TableColumn(resourceBundle.getString("table.column.state.text"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<Mp3Details,String>("fileState"));

        // ** The TableCell class has the method setTextFill(Paint p) that you
        // ** need to override the text color
        //   To obtain the TableCell we need to replace the Default CellFactory
        //   with one that returns a new TableCell instance,
        //   and @Override the updateItem(String item, boolean empty) method.
        //
        stateColumn.setCellFactory(new Callback<TableColumn, TableCell>() {
            public TableCell call(TableColumn param) {
                return new TableCell<Mp3Details, String>() {

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            Mp3FileWrapper.Mp3FileState state = Mp3FileWrapper.Mp3FileState.fromString(item);

                            switch (state) {
                                case SAVED:
                                    this.setTextFill(Color.GREEN);
                                    break;
                                case MODIFIED:
                                    this.setTextFill(Color.BLUE);
                                    break;
                                case FAILED_MODIFIED:
                                    this.setTextFill(Color.RED);
                                    break;
                                case FAILED_SAVED:
                                    this.setTextFill(Color.RED);
                                    break;
                            }

                            setText(item);
                            this.setStyle("-fx-alignment: CENTER;");
                        } else {
                            setText("");
                        }
                    }
                };
            }
        });

        tableView.getColumns().add(stateColumn);

        populateUIWithLocalizedStrings();
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

    public void generateTags() {
        ProgressForm progressForm = new ProgressForm(scene);

        Mp3FileWrapper.Mp3FilePattern pattern;
        if (artistYearTitleRadButton.isSelected()) {
            pattern = Mp3FileWrapper.Mp3FilePattern.ARTIST_YEAR_TITLE;
        } else if (artistLiveYearTitleRadButton.isSelected()) {
            pattern = Mp3FileWrapper.Mp3FilePattern.ARTIST_LIVE_YEAR_TITLE;
        } else if (artistTitleRadButton.isSelected()) {
            pattern = Mp3FileWrapper.Mp3FilePattern.ARTIST_TITLE;
        } else {
            pattern = Mp3FileWrapper.Mp3FilePattern.TITLE;
        }

        boolean isCyrillicTags = cyrillicRadioButton.isSelected();
        List<String> selectedFilesPath = null;
        ObservableList<Mp3Details> selectedItems = tableView.getSelectionModel().getSelectedItems();
        if (selectedItems.size() > 0) {
            selectedFilesPath = selectedItems.stream()
                    .map(Mp3Details::getFilePath)
                    .collect(Collectors.toList());
        }

        Task generateTagsWorker = new GenerateTagsWorker(mp3Model, progressForm, pattern, isCyrillicTags, selectedFilesPath);

        // binds progress of progress bars to progress of task:
        progressForm.activateProgressBar(generateTagsWorker);

        scene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(true));

        // open progress dialog
        progressForm.getDialogStage().show();
        new Thread(generateTagsWorker).start();
    }

    public void start() {
        saveFilesWorker = new SaveFilesWorker(mp3Model, progressBar);

        progressBar.progressProperty().bind(saveFilesWorker.progressProperty());

        generateButton.setDisable(true);
        saveButton.setDisable(true);
        stopButton.setDisable(false);

        // open progress dialog
        new Thread(saveFilesWorker).start();
    }

    public void stop() {
        if (saveFilesWorker != null) {
            saveFilesWorker.cancel();
        }

        //onSavedImportedFiles();
    }

    private void updateTable() {
        tableData = FXCollections.observableArrayList();
        //mp3Model.getImportedFiles().values().parallelStream().forEach(f -> tableData.add(Mp3Details.deserialize(f)));
        for (Mp3FileWrapper mp3File : mp3Model.getImportedFiles().values()) {
            tableData.add(Mp3Details.deserialize(mp3File));
        }

        fileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        trackArtist.setCellValueFactory(new PropertyValueFactory<>("trackArtist"));
        trackName.setCellValueFactory(new PropertyValueFactory<>("trackName"));
        trackYear.setCellValueFactory(new PropertyValueFactory<>("trackYear"));
        patternColumn.setCellValueFactory(new PropertyValueFactory<>("filePattern"));

        tableView.setItems(null);
        tableView.setItems(tableData);

        // Sort files by name ascending
        fileName.setSortType(TableColumn.SortType.ASCENDING);
        tableView.getSortOrder().add(fileName);
        tableView.sort();
    }

    private void addFileToTable(Mp3FileWrapper file) {
        tableData.add(Mp3Details.deserialize(file));
        //tableView.sort();
    }

    @Override
    public void onImportedFileChanged(Mp3FileWrapper file) {
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

    private void updateErrorsOnInfoArea(String errorMessage, List<FailedFileDetails> failedFiles) {
        Text importErrorText = new Text();
        importErrorText.setText(errorMessage);
        importErrorText.setFill(Color.RED);
        infoArea.getChildren().addAll(importErrorText);

        for (FailedFileDetails failedFileDetails : failedFiles) {
            Text fileErrorText = new Text("\t\t " + failedFileDetails.getFilePath() + " (" + failedFileDetails.getErrorMessage() + ")\n");
            fileErrorText.setFill(Color.RED);
            infoArea.getChildren().addAll(fileErrorText);
        }
    }

    private void updateInfoArea(){
        Calendar rightNowCalendar = Calendar.getInstance();
        int hours = rightNowCalendar.get(Calendar.HOUR_OF_DAY);
        int minutes = rightNowCalendar.get(Calendar.MINUTE);
        int seconds = rightNowCalendar.get(Calendar.SECOND);
        String dateString = ((hours < 10) ? ("0" + hours) : hours) + ":"
                + ((minutes < 10) ? ("0" + minutes) : minutes) + ":"
                + ((seconds < 10) ? ("0" + seconds) : seconds) + " - ";

        Mp3Model.Mp3ModelState modelLastState = mp3Model.getLastModelState();
        switch (modelLastState) {
            case IMPORTED:
                if (mp3Model.getLastFailedLoadingFiles().size() > 0) {
                    updateErrorsOnInfoArea(dateString + "This files cannot be imported:\n", mp3Model.getLastFailedLoadingFiles());
                } else {
                    Text importFilesSuccessText = new Text(dateString + "Files imported successfully.\n");
                    importFilesSuccessText.setFill(Color.BLUE);
                    infoArea.getChildren().addAll(importFilesSuccessText);
                }

                break;
            case REMOVED:
                Text removedFilesSuccessText = new Text(dateString + "Files removed successfully.\n");
                removedFilesSuccessText.setFill(Color.ORANGE);
                infoArea.getChildren().addAll(removedFilesSuccessText);

                break;
            case GENRATED:
                if (mp3Model.getLastFailedGeneratingTags().size() > 0) {
                    updateErrorsOnInfoArea(dateString + "This files cannot be generated:\n", mp3Model.getLastFailedGeneratingTags());
                } else {
                    Text importFilesSuccessText = new Text(dateString + "Files generated successfully.\n");
                    importFilesSuccessText.setFill(Color.PURPLE);
                    infoArea.getChildren().addAll(importFilesSuccessText);
                }

                break;
            case SAVED:
                if (mp3Model.getLastFailedSavingFiles().size() > 0) {
                    updateErrorsOnInfoArea(dateString + "This files cannot be saved:\n", mp3Model.getLastFailedSavingFiles());
                } else {
                    Text importFilesSuccessText = new Text(dateString + "Files saved successfully.\n");
                    importFilesSuccessText.setFill(Color.GREEN);
                    infoArea.getChildren().addAll(importFilesSuccessText);
                }

                break;
            default:
                break;
        }

        scrollPane.setVvalue(1.0); //1.0 means 100% at the bottom
    }

    @Override
    public void onImportedFilesChanged() {
        Platform.runLater(() -> {
            updateTable();
            updateInfoArea();
        });
    }

    @Override
    public void onSavedImportedFiles() {
        Platform.runLater(() -> {
            updateTable();
            updateInfoArea();
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            generateButton.setDisable(false);
            saveButton.setDisable(false);
            stopButton.setDisable(true);
            saveFilesWorker = null;
        });
    }

    @Override
    public void onTagGenerated() {
        Platform.runLater(() -> {
            updateTable();
            updateInfoArea();
        });
    }
}
