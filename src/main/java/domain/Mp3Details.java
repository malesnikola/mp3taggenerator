package main.java.domain;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.Mp3File;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;

public class Mp3Details {

    private final StringProperty filePath;
    private final StringProperty fileName;
    private final StringProperty trackArtist;
    private final StringProperty trackName;
    private final StringProperty trackYear;

    private Mp3Details(String filePath, String fileName, String trackArtist, String trackName, String trackYear) {
        this.filePath = new SimpleStringProperty(filePath);
        this.fileName = new SimpleStringProperty(fileName);
        this.trackArtist = new SimpleStringProperty(trackArtist);
        this.trackName = new SimpleStringProperty(trackName);
        this.trackYear = new SimpleStringProperty(trackYear);
    }

    public static Mp3Details deserialize(Mp3File file) {
        String filePath = file.getFilename();
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator));
        ID3v1 id3v1Tag = file.getId3v1Tag();
        if (id3v1Tag != null) {
            String trackArtist = id3v1Tag.getArtist();
            String trackName = id3v1Tag.getTrack();
            String trackYear = id3v1Tag.getYear();
            return new Mp3Details(filePath, fileName, trackArtist, trackName, trackYear);
        }

        return new Mp3Details(filePath, fileName, null, null, null);
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
}
