package main.java.repositories;

import com.mpatric.mp3agic.*;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Repository
public class Mp3Repository {

    private Map<String, Mp3File> importedFiles = new HashMap<String, Mp3File>();

    public void importFileIfNotExist(String filePath) throws IOException, UnsupportedTagException, InvalidDataException {
        if (!importedFiles.containsKey(filePath)) {
            importedFiles.put(filePath, new Mp3File(filePath));
        }
    }

    public void removeImportedFile(String filePath) {
        importedFiles.remove(filePath);
    }

    public void saveFile(Mp3File file) throws IOException, NotSupportedException  {
        file.save(file.getFilename() + "2");
    }

    public Map<String, Mp3File> getImportedFiles() {
        return importedFiles;
    }
}
