package main.java.service;

import com.mpatric.mp3agic.*;
import main.java.exceptions.FileNameBadFormatException;
import main.java.repositories.Mp3Repository;
import main.java.util.FileHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    public void setTagsForFilesAndSave(boolean isCyrillicTags) {
        Map<String, Mp3File> importedFiles = mp3Repository.getImportedFiles();
        for (Mp3File file : importedFiles.values()) {
            try {
                ID3v1 id3v1Tag = new ID3v1Tag();
                ID3v2 id3v2Tag = FileHelper.crateID3v2Tag(file, isCyrillicTags);
                file.setId3v1Tag(id3v1Tag);
                file.setId3v2Tag(id3v2Tag);
                mp3Repository.saveFile(file);
            } catch (FileNameBadFormatException e) {
                logger.debug("Excepton in method setTagsForFilesAndSave: " + e.getMessage());
            } catch (IOException | NotSupportedException e) {
                logger.debug("Excepton in method setTagsForFilesAndSave: " + e.getMessage());
            }
        }
    }

    public List<Mp3File> getInsertedFiles() {
        return new LinkedList<>(mp3Repository.getImportedFiles().values());
    }
}
