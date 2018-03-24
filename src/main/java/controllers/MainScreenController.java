package main.java.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import main.java.dialogs.ProgressForm;
import main.java.domain.FailedFileDetails;
import main.java.domain.Mp3Details;
import main.java.domain.Mp3FileWrapper;
import main.java.enums.Mp3FilePattern;
import main.java.enums.FileState;
import main.java.enums.Mp3ModelState;
import main.java.model.Mp3Model;
import main.java.util.Constants;
import main.java.service.Mp3Service;
import main.java.workers.GenerateTagsWorker;
import main.java.workers.ImportFilesWorker;
import main.java.workers.SaveFilesWorker;
import org.apache.log4j.Logger;
import javafx.scene.control.TableColumn;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class MainScreenController implements Mp3Model.Mp3FilesObserver {

    private static Logger logger = Logger.getLogger(MainScreenController.class);

    private Mp3Model mp3Model;

    private Scene scene;
    private String chosenLanguage; // en (English), rs (Serbian)
    private ResourceBundle resourceBundle;
    private Task saveFilesWorker;
    private TableColumn chosenSortingColumn;

    // menu
    @FXML
    private Menu fileMenu;
    @FXML
    private Menu languageMenu;
    @FXML
    private Menu helpMenu;

    // menu items
    @FXML
    private MenuItem openFilesMenuItem;
    @FXML
    private MenuItem openFolderMenuItem;
    @FXML
    private CheckMenuItem englishMenuItem;
    @FXML
    private CheckMenuItem serbianMenuItem;
    @FXML
    private MenuItem aboutMenuItem;

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
    @FXML
    private Button clearConsoleButton;

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
    private TextFlow consoleTextFlow;

    // progress bar
    @FXML
    private ProgressBar progressBar;

    /**
     * Get localized string from Bundle_{chosenLanguage}.properties in charset cp1250 (because of Serbian latin letters).
     * @param key Key of the required string.
     * @return Returns value of localized sting if exist, else returns empty string.
     */
    private String getLocalizedString(String key) {
        try {
            String value = resourceBundle.getString(key);
            return new String(value.getBytes("ISO-8859-1"), "cp1250");
        } catch (Exception e) {
            logger.debug("Exception in method getLocalizedString: " + e.getMessage());
            return "";
        }
    }

    /**
     * Populate UI element with localized strings from Bundle_{chosenLanguage}.properties.
     */
    private void populateUIWithLocalizedStrings() {
        // menu
        fileMenu.setText(getLocalizedString("menu.file.text"));
        languageMenu.setText(getLocalizedString("menu.language.text"));
        helpMenu.setText(getLocalizedString("menu.help.text"));

        // menu items
        openFilesMenuItem.setText(getLocalizedString("menu.item.openfiles.text"));
        openFolderMenuItem.setText(getLocalizedString("menu.item.openfolder.text"));
        englishMenuItem.setText(getLocalizedString("menu.item.english.text"));
        serbianMenuItem.setText(getLocalizedString("menu.item.serbian.text"));
        aboutMenuItem.setText(getLocalizedString("menu.item.about.text"));

        // labels
        chooseAlphabetLabel.setText(getLocalizedString("label.choosealphabet.text"));
        choosePatternLabel.setText(getLocalizedString("label.choosepattern.text"));
        consoleLabel.setText(getLocalizedString("label.console.text"));

        // radio buttons
        cyrillicRadioButton.setText(getLocalizedString("radio.button.cyrillic.text"));
        latinRadioButton.setText(getLocalizedString("radio.button.latin.text"));

        // buttons
        generateButton.setText(getLocalizedString("button.generate.text"));
        saveButton.setText(getLocalizedString("button.save.text"));
        stopButton.setText(getLocalizedString("button.stop.text"));
        clearConsoleButton.setTooltip(new Tooltip(getLocalizedString("button.clearconsole.tooltip")));

        // data table
        tableView.setPlaceholder(new Label(getLocalizedString("table.placeholder.text")));

        // columns in data table
        fileName.setText(getLocalizedString("table.column.filename.text"));
        trackArtist.setText(getLocalizedString("table.column.artist.text"));
        trackYear.setText(getLocalizedString("table.column.year.text"));
        trackName.setText(getLocalizedString("table.column.title.text"));
        patternColumn.setText(getLocalizedString("table.column.pattern.text"));
        stateColumn.setText(getLocalizedString("table.column.state.text"));
    }

    @FXML
    public void initialize() {
        mp3Model = Mp3Model.getInstance();
        mp3Model.registerObserver(this);

        // by default, languague is English
        resourceBundle = ResourceBundle.getBundle("bundles.Bundle", new Locale("en", "EN"));
        chosenLanguage = "en";
        englishMenuItem.setSelected(true);

        // enable multiple selection in table
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // add key event handling for deleting files and for clearing selection
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

        // add mouse event handling for clearing selection on empty row click
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

        // save user selected column for sorting
        tableView.setOnSort(event -> {
            if (tableView.getSortOrder().size() == 1) {
                chosenSortingColumn = tableView.getSortOrder().get(0);
            }
        });

        // by default sorting column is file name
        fileName.setSortType(TableColumn.SortType.ASCENDING);
        chosenSortingColumn = fileName;

        // create and add pattern column
        patternColumn = new TableColumn(getLocalizedString("table.column.pattern.text"));
        patternColumn.setStyle("-fx-alignment: CENTER;");
        patternColumn.setCellValueFactory(new PropertyValueFactory<Mp3Details,String>("filePattern"));
        tableView.getColumns().add(patternColumn);

        // create state column
        stateColumn = new TableColumn(getLocalizedString("table.column.state.text"));
        stateColumn.setPrefWidth(100);
        stateColumn.setCellValueFactory(new PropertyValueFactory<Mp3Details,String>("fileState"));

        // add cell factory for coloring states:
        //   To obtain the TableCell we need to replace the Default CellFactory with one that returns a new TableCell instance,
        //   and @Override the updateItem(String item, boolean empty) method.
        stateColumn.setCellFactory(new Callback<TableColumn, TableCell>() {
            public TableCell call(TableColumn param) {
                return new TableCell<Mp3Details, String>() {

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            FileState state = FileState.fromString(item);

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

        // add state column
        tableView.getColumns().add(stateColumn);

        populateUIWithLocalizedStrings();
    }

    /**
     * Set javafx scene.
     * @param scene Javafx scene.
     */
    public void setScene(Scene scene) {
        this.scene = scene;

        // add drag over event handling
        scene.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                // if at least one of the dragged files has ".mp3" extension enable importing
                if (db.hasFiles() && Mp3Service.ifContainsAnyMp3File(db.getFiles())) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });

        // add drop over event handling
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

                    // binds progress of progress form to progress of task
                    progressForm.activateProgressBar(importFilesWorker);

                    // disable all elements on scene
                    scene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(true));

                    // open progress dialog
                    progressForm.getDialogStage().show();

                    // saveFiles new thread
                    new Thread(importFilesWorker).start();
                }

                event.setDropCompleted(success);
                event.consume();
            }
        });
    }

    /**
     * Set English as chosen language.
     * @param e
     */
    public void englishMenuItemCheckChanged(ActionEvent e){
        if (!"en".equals(chosenLanguage)) {
            resourceBundle = ResourceBundle.getBundle("bundles.Bundle", new Locale("en", "EN"));
            chosenLanguage = "en";
            serbianMenuItem.setSelected(false);
            populateUIWithLocalizedStrings();
        }

        englishMenuItem.setSelected(true);
    }

    /**
     * Set Serbian as chosen language.
     * @param e
     */
    public void serbianMenuItemCheckChanged(ActionEvent e){
        if (!"rs".equals(chosenLanguage)) {
            resourceBundle = ResourceBundle.getBundle("bundles.Bundle", new Locale("rs", "RS"));
            chosenLanguage = "rs";
            englishMenuItem.setSelected(false);
            populateUIWithLocalizedStrings();
        }

        serbianMenuItem.setSelected(true);
    }

    public void openAboutDialog() {
        AboutDialogController aboutDialogController = new AboutDialogController(resourceBundle);
        aboutDialogController.getStage().show();
    }

    /**
     * Clear console.
     */
    public void clearConsole(){
        consoleTextFlow.getChildren().clear();
    }

    /**
     * Add localized error message on console.
     * @param currentTime String which represent current date time.
     * @param key Key of the required localized string.
     * @param failedFiles List of failed files with details.
     */
    private void addErrorsOnConsole(String currentTime, String key, List<FailedFileDetails> failedFiles) {
        Text importErrorText = new Text();
        importErrorText.setText(currentTime + getLocalizedString(key) + "\n");
        importErrorText.setFill(Color.RED);
        consoleTextFlow.getChildren().addAll(importErrorText);

        for (FailedFileDetails failedFileDetails : failedFiles) {
            Text fileErrorText = new Text("\t\t " + failedFileDetails.getFilePath() + " (" + failedFileDetails.getErrorMessage() + ")\n");
            fileErrorText.setFill(Color.RED);
            consoleTextFlow.getChildren().addAll(fileErrorText);
        }
    }

    /**
     * Add localized info on console based on previous user action.
     */
    private void addInfoOnConsole(){
        // get current time
        Calendar rightNowCalendar = Calendar.getInstance();
        int hours = rightNowCalendar.get(Calendar.HOUR_OF_DAY);
        int minutes = rightNowCalendar.get(Calendar.MINUTE);
        int seconds = rightNowCalendar.get(Calendar.SECOND);
        String currentTime = ((hours < 10) ? ("0" + hours) : hours) + ":"
                + ((minutes < 10) ? ("0" + minutes) : minutes) + ":"
                + ((seconds < 10) ? ("0" + seconds) : seconds) + " - ";

        Mp3ModelState modelLastState = mp3Model.getLastModelState();
        switch (modelLastState) {
            case IMPORTED:
                if (mp3Model.getLastFailedLoadingFiles().size() > 0) {
                    addErrorsOnConsole(currentTime,"files.cannot.be.imported", mp3Model.getLastFailedLoadingFiles());
                } else {
                    Text importFilesSuccessText = new Text(currentTime + getLocalizedString("files.imported.successfully") + "\n");
                    importFilesSuccessText.setFill(Color.BLUE);
                    consoleTextFlow.getChildren().addAll(importFilesSuccessText);
                }

                break;
            case REMOVED:
                Text removedFilesSuccessText = new Text(currentTime + getLocalizedString("files.removed.successfully") + "\n");
                removedFilesSuccessText.setFill(Color.ORANGE);
                consoleTextFlow.getChildren().addAll(removedFilesSuccessText);

                break;
            case GENRATED:
                if (mp3Model.getLastFailedGeneratingTags().size() > 0) {
                    addErrorsOnConsole(currentTime,"files.cannot.be.generated", mp3Model.getLastFailedGeneratingTags());
                } else {
                    Text importFilesSuccessText = new Text(currentTime + getLocalizedString("files.generated.successfully") + "\n");
                    importFilesSuccessText.setFill(Color.PURPLE);
                    consoleTextFlow.getChildren().addAll(importFilesSuccessText);
                }

                break;
            case SAVED:
                if (mp3Model.getLastFailedSavingFiles().size() > 0) {
                    addErrorsOnConsole(currentTime,"files.cannot.be.saved", mp3Model.getLastFailedSavingFiles());
                } else {
                    Text importFilesSuccessText = new Text(currentTime + getLocalizedString("files.saved.successfully") + "\n");
                    importFilesSuccessText.setFill(Color.GREEN);
                    consoleTextFlow.getChildren().addAll(importFilesSuccessText);
                }

                break;
            default:
                break;
        }

        scrollPane.setVvalue(1.0); // 1.0 means 100% at the bottom
    }

    /**
     * Open file chooser dialog and import selected files into mp3Model.
     */
    public void openFiles() {
        FileChooser fileChooser = new FileChooser();
        // set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(Constants.MP3_FILE_TYPE_DESCRIPTION, "*" + Constants.MP3_FILE_TYPE_EXTENSION);
        fileChooser.getExtensionFilters().add(extFilter);
        // Open dialog for choosing mp3 files
        List<File> files = fileChooser.showOpenMultipleDialog(tableView.getScene().getWindow());
        if (files != null) {
            ProgressForm progressForm = new ProgressForm(scene);
            Task importFilesWorker = new ImportFilesWorker(mp3Model, progressForm, files);

            // binds progress of progress form to progress of task
            progressForm.activateProgressBar(importFilesWorker);

            // disable all elements on scene
            scene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(true));

            // open progress dialog
            progressForm.getDialogStage().show();

            // saveFiles new thread
            new Thread(importFilesWorker).start();
        }
    }

    /**
     * Open directory chooser dialog and import all ".mp3" files from chosen directory into mp3Model.
     */
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

            ProgressForm progressForm = new ProgressForm(scene);
            Task importFilesWorker = new ImportFilesWorker(mp3Model, progressForm, filesForImport);

            // binds progress of progress form to progress of task
            progressForm.activateProgressBar(importFilesWorker);

            // disable all elements on scene
            scene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(true));

            // open progress dialog
            progressForm.getDialogStage().show();

            // saveFiles new thread
            new Thread(importFilesWorker).start();
        }
    }

    /**
     * Generate tags for selected files in table according to chosen pattern and alphabet.
     * If there is no selected files, generate tags for all files.
     */
    public void generateTags() {
        ProgressForm progressForm = new ProgressForm(scene);

        Mp3FilePattern pattern;
        if (artistYearTitleRadButton.isSelected()) {
            pattern = Mp3FilePattern.ARTIST_YEAR_TITLE;
        } else if (artistLiveYearTitleRadButton.isSelected()) {
            pattern = Mp3FilePattern.ARTIST_LIVE_YEAR_TITLE;
        } else if (artistTitleRadButton.isSelected()) {
            pattern = Mp3FilePattern.ARTIST_TITLE;
        } else {
            pattern = Mp3FilePattern.TITLE;
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

        // binds progress of progress form to progress of task:
        progressForm.activateProgressBar(generateTagsWorker);

        // disable all elements on scene
        scene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(true));

        // open progress dialog
        progressForm.getDialogStage().show();

        // saveFiles new thread
        new Thread(generateTagsWorker).start();
    }

    /**
     * Save all imported files on disk with same path.
     */
    public void saveFiles() {
        saveFilesWorker = new SaveFilesWorker(mp3Model, progressBar);

        // binds progress of progress bar to progress of task:
        progressBar.progressProperty().bind(saveFilesWorker.progressProperty());

        generateButton.setDisable(true);
        saveButton.setDisable(true);
        stopButton.setDisable(false);

        // start new thread
        new Thread(saveFilesWorker).start();
    }

    /**
     * Stop saving imported files. This is not recommended.
     */
    public void stop() {
        if (saveFilesWorker != null) {
            saveFilesWorker.cancel();
        }
    }

    /**
     * Update table view with data.
     */
    private void updateTable() {
        tableData = FXCollections.observableArrayList();
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

        // sort table
        tableView.getSortOrder().add(chosenSortingColumn);
        tableView.sort();
    }

    @Override
    public void onImportedFilesChanged() {
        Platform.runLater(() -> {
            updateTable();
            addInfoOnConsole();
        });
    }

    @Override
    public void onTagGenerated() {
        Platform.runLater(() -> {
            updateTable();
            addInfoOnConsole();
        });
    }

    @Override
    public void onSavedImportedFiles() {
        Platform.runLater(() -> {
            updateTable();
            addInfoOnConsole();

            // reset progressBar
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            generateButton.setDisable(false);

            saveButton.setDisable(false);
            stopButton.setDisable(true);
            saveFilesWorker = null;
        });
    }

}
