package main.java.domain;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.Mp3File;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.ImageView;

import java.io.File;

public class Mp3Details {
    public static final String SAVED_FILE_IMAGE_PATH = "../../resources/images/saved.png";

    private final StringProperty filePath;
    private final StringProperty fileName;
    private final StringProperty trackArtist;
    private final StringProperty trackName;
    private final StringProperty trackYear;
    private final StringProperty fileState;

    private Mp3Details(String filePath, String fileName, String trackArtist, String trackName, String trackYear, String fileState) {
        this.filePath = new SimpleStringProperty(filePath);
        this.fileName = new SimpleStringProperty(fileName);
        this.trackArtist = new SimpleStringProperty(trackArtist);
        this.trackName = new SimpleStringProperty(trackName);
        this.trackYear = new SimpleStringProperty(trackYear);
        this.fileState = new SimpleStringProperty(fileState);
    }

    public static Mp3Details deserialize(Mp3FileWrapper file) {
        String filePath = file.getFilename();
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        String fileState = file.getState().getStateText();
        ID3v1 id3v2Tag = file.getId3v2Tag();
        if (id3v2Tag != null) {
            String trackArtist = id3v2Tag.getArtist();
            String trackName = id3v2Tag.getTitle();
            String trackYear = id3v2Tag.getYear();
            return new Mp3Details(filePath, fileName, trackArtist, trackName, trackYear, fileState);
        }

        return new Mp3Details(filePath, fileName, null, null, null, fileState);
    }

    public String getFilePath() {
        return filePath.get();
    }

    public StringProperty filePathProperty() {
        return filePath;
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public String getTrackArtist() {
        return trackArtist.get();
    }

    public StringProperty trackArtistProperty() {
        return trackArtist;
    }

    public String getTrackName() {
        return trackName.get();
    }

    public StringProperty trackNameProperty() {
        return trackName;
    }

    public String getTrackYear() {
        return trackYear.get();
    }

    public StringProperty trackYearProperty() {
        return trackYear;
    }

    public String getFileState() {
        return fileState.get();
    }

    public StringProperty fileStateProperty() {
        return fileState;
    }

    public void setFilePath(String filePath) {
        this.filePath.set(filePath);
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public void setTrackArtist(String trackArtist) {
        this.trackArtist.set(trackArtist);
    }

    public void setTrackName(String trackName) {
        this.trackName.set(trackName);
    }

    public void setTrackYear(String trackYear) {
        this.trackYear.set(trackYear);
    }

    public void setFileState(String fileState) {
        this.fileState.set(fileState);
    }

}
