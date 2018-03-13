package main.java.domain;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.jetbrains.annotations.Contract;

import java.io.IOException;

public class Mp3FileWrapper extends Mp3File {

    public enum Mp3FileState {
        SAVED ("saved"),
        MODIFIED("modified"),
        FAILED_SAVED("failed to save");

        private String stateText;

        Mp3FileState(String stateText) {
            this.stateText = stateText;
        }

        @Contract(pure = true)
        public String getStateText() {
            return stateText;
        }

        public static Mp3FileState fromString(String text) {
            for (Mp3FileState b : Mp3FileState.values()) {
                if (b.stateText.equalsIgnoreCase(text)) {
                    return b;
                }
            }

            return null;
        }
    }

    public enum Mp3FilePattern {
        ARTIST_YEAR_TITLE("A-Y-T"),
        ARTIST_LIVE_YEAR_TITLE("A-L-Y-T"),
        ARTIST_TITLE("A-T"),
        TITLE("T");

        private String patternCode;

        Mp3FilePattern(String patternCode) {
            this.patternCode = patternCode;
        }

        public String getPatternCode() {
            return patternCode;
        }
    }

    private Mp3FileState state;
    private Mp3FilePattern pattern;

    public Mp3FileWrapper(String filename) throws IOException, UnsupportedTagException, InvalidDataException {
        super(filename);
        this.state = Mp3FileState.SAVED;
        this.pattern = Mp3FilePattern.ARTIST_YEAR_TITLE;
    }

    public Mp3FileWrapper(String filename, Mp3FileState state) throws IOException, UnsupportedTagException, InvalidDataException {
        super(filename);
        this.state = state;
        this.pattern = Mp3FilePattern.ARTIST_YEAR_TITLE;
    }

    public Mp3FileWrapper(String filename, Mp3FileState state, Mp3FilePattern pattern) throws IOException, UnsupportedTagException, InvalidDataException {
        super(filename);
        this.state = state;
        this.pattern = pattern;
    }

    public void setState(Mp3FileState state) {
        this.state = state;
    }

    public void setPattern(Mp3FilePattern pattern) {
        this.pattern = pattern;
    }

    public Mp3FileState getState() {
        return state;
    }

    public Mp3FilePattern getPattern() {
        return pattern;
    }
}
