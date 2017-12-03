package main.java.repositories;

import com.mpatric.mp3agic.*;
import main.java.util.Constants;
import org.springframework.stereotype.Repository;

import java.io.File;
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

    public void saveFile(Mp3File file) throws IOException, NotSupportedException {
        String originalFileName = file.getFilename();
        String modifiedFileName = originalFileName + ".modified";
        file.save(modifiedFileName);
        File oldFile = new File(originalFileName);
        oldFile.delete();
        File newlySavedFile = new File(modifiedFileName);
        boolean success = newlySavedFile.renameTo(new File(originalFileName));
//        System.out.println(success);
    }

    public Map<String, Mp3File> getImportedFiles() {
        return importedFiles;
    }
}
