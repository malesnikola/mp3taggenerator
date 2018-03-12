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

    private Mp3FileState state;

    public Mp3FileWrapper(String filename) throws IOException, UnsupportedTagException, InvalidDataException {
        super(filename);
        this.state = Mp3FileState.SAVED;
    }

    public Mp3FileWrapper(String filename, Mp3FileState state) throws IOException, UnsupportedTagException, InvalidDataException {
        super(filename);
        this.state = state;
    }

    public void setState(Mp3FileState state) {
        this.state = state;
    }

    public Mp3FileState getState() {
        return state;
    }
}
