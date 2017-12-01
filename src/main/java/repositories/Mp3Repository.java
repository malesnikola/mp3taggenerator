package main.java.repositories;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Mp3Repository {

    private static Mp3Repository instance;

    private Map<String, Mp3File> importedFiles = new HashMap<String, Mp3File>();

    private Mp3Repository() {
        // prevent for instantiate
    }

    public static Mp3Repository getInstance() {
        if (instance == null) {
            instance = new Mp3Repository();
        }

        return instance;
    }

    public void importFiles (List<File> files) {
        for (File f : files) {
            String fileName = f.getPath();
            if (!importedFiles.containsKey(fileName)) {
                try {
                    Mp3File mp3File = new Mp3File(fileName);
                    importedFiles.put(fileName, mp3File);
                } catch (IOException ex) {

                } catch (UnsupportedTagException ex) {

                } catch (InvalidDataException ex) {

                }
            }
        }
    }

    public List<Mp3File> getFiles() {
        return new LinkedList<Mp3File>(importedFiles.values());
    }
}
