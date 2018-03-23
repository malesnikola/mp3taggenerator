package main.java.domain;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import main.java.enums.Mp3FilePattern;
import main.java.enums.FileState;

import java.io.IOException;

/**
 * Mp3FileWrapper contains all information about ".mp3" file.
 */
public class Mp3FileWrapper extends Mp3File {
    /**
     * State of ".mp3" file. E.g. "SAVED".
     */
    private FileState state;
    /**
     * Pattern of ".mp3" file. E.g. "A-Y-T"
     */
    private Mp3FilePattern pattern;

    /**
     * Create new Mp3FileWrapper based on full file name.
     * @param filename Full path of file.
     * @throws IOException
     * @throws UnsupportedTagException
     * @throws InvalidDataException
     */
    public Mp3FileWrapper(String filename) throws IOException, UnsupportedTagException, InvalidDataException {
        super(filename);
        this.state = FileState.SAVED;
        this.pattern = Mp3FilePattern.UNKNOWN;
    }

    public void setState(FileState state) {
        this.state = state;
    }

    public void setPattern(Mp3FilePattern pattern) {
        this.pattern = pattern;
    }

    public FileState getState() {
        return state;
    }

    public Mp3FilePattern getPattern() {
        return pattern;
    }
}
