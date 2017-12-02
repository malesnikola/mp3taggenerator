package main.java.repositories;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Repository
public class Mp3Repository {

    private Map<String, Mp3File> importedFiles = new HashMap<String, Mp3File>();

    public void importFileIfNotExist(String fileName) throws IOException, UnsupportedTagException, InvalidDataException {
        if (!importedFiles.containsKey(fileName)) {
            importedFiles.put(fileName, new Mp3File(fileName));
        }
    }

    public List<Mp3File> getInsertedFiles() {
        return new LinkedList<Mp3File>(importedFiles.values());
    }
}
