package main.java.service;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import main.java.repositories.Mp3Repository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class Mp3Service {

    private static Logger logger = Logger.getLogger(Mp3Service.class);

    @Autowired
    Mp3Repository mp3Repository;

    public boolean insertFiles(List<File> files) {
        boolean isEverythingSuccess = true;
        for (File f : files) {
            try {
                mp3Repository.importFileIfNotExist(f.getPath());
            } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                logger.debug("Excepton in method insertFiles: " + e.getMessage());
                isEverythingSuccess = false;
            }
        }

        return isEverythingSuccess;
    }

    public void removeInsertedFiles(List<String> filePath) {
        filePath.stream().forEach(mp3Repository::removeImportedFile);
    }

    public List<Mp3File> getInsertedFiles() {
        return mp3Repository.getInsertedFiles();
    }
}
