package main.java.model;

import com.mpatric.mp3agic.*;
import main.java.domain.FailedFileDetails;
import main.java.exceptions.FileNameBadFormatException;
import main.java.service.Mp3Service;
import main.java.util.Constants;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Mp3Model {

    private static Logger logger = Logger.getLogger(Mp3Model.class);

    private Map<String, Mp3File> importedFiles = new ConcurrentHashMap<>();

    private List<FailedFileDetails> lastFailedLoadingFiles = Collections.synchronizedList(new ArrayList<>());

    private List<FailedFileDetails> lastFailedSavingFiles = new LinkedList<>();

    private List<FailedFileDetails> lastFailedGeneratingTags = new LinkedList<>();

    private Set<Mp3FilesObserver> observers = new HashSet<>();

    public Map<String, Mp3File> getImportedFiles() {
        return importedFiles;
    }

    public List<FailedFileDetails> getLastFailedLoadingFiles() {
        return lastFailedLoadingFiles;
    }

    public List<FailedFileDetails> getLastFailedSavingFiles() {
        return lastFailedSavingFiles;
    }

    public List<FailedFileDetails> getLastFailedGeneratingTags() {
        return lastFailedGeneratingTags;
    }

    public void importFile(File file){
        String filePath = file.getPath();
        if (!importedFiles.containsKey(filePath)) {
            try {
                Mp3File newFile = new Mp3File(filePath);
                importedFiles.put(filePath, newFile);
                observers.forEach(o -> o.onImportedFileChanged(newFile));
            } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                logger.debug("Excepton in method importFiles: " + e.getMessage());
                lastFailedLoadingFiles.add(new FailedFileDetails(filePath, e.getMessage()));
            } catch (Exception e) {
                logger.debug("Unexpected excepton in method importFiles: " + e.getMessage());
                lastFailedLoadingFiles.add(new FailedFileDetails(filePath, "Unexpected excepton: " + e.getMessage()));
            }
        }
    }

    public void importFiles(List<File> files) {
        if (files != null && !files.isEmpty()) {
            int previousSizeOfImportedFiles = importedFiles.size();
            lastFailedLoadingFiles = Collections.synchronizedList(new ArrayList<>());

            files.parallelStream().forEach(file -> {
                String filePath = file.getPath();
                if (Constants.MP3_FILE_TYPE_EXTENSION.equalsIgnoreCase(Mp3Service.getFileExtension(file)) && !importedFiles.containsKey(filePath)) {
                    try {
                        importedFiles.put(filePath, new Mp3File(filePath));
                    } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                        logger.debug("Excepton in method importFiles: " + e.getMessage());
                        lastFailedLoadingFiles.add(new FailedFileDetails(filePath, e.getMessage()));
                    } catch (Exception e) {
                        logger.debug("Unexpected excepton in method importFiles: " + e.getMessage());
                        lastFailedLoadingFiles.add(new FailedFileDetails(filePath, "Unexpected excepton: " + e.getMessage()));
                    }
                }
            });

            if (previousSizeOfImportedFiles != importedFiles.size() || !lastFailedLoadingFiles.isEmpty()) {
                observers.forEach(Mp3FilesObserver::onImportedFilesChanged);
            }
        }
    }

    public void removeImportedFiles(List<String> filePaths) {
        if (filePaths != null) {
            int previousSizeOfImportedFiles = importedFiles.size();

            for (String filePath : filePaths) {
                importedFiles.remove(filePath);
            }

            if (previousSizeOfImportedFiles != importedFiles.size()) {
                observers.forEach(Mp3FilesObserver::onImportedFilesChanged);
            }
        }
    }

    public void generateTagsForImportedFiles(boolean isCyrillicTags) {
        lastFailedGeneratingTags = new LinkedList<>();

        for (Mp3File file : importedFiles.values()) {
            try {
                ID3v2 id3v2Tag = Mp3Service.crateID3v2Tag(file, isCyrillicTags);
                file.removeId3v2Tag();
                file.setId3v2Tag(id3v2Tag);
            } catch (FileNameBadFormatException e) {
                logger.debug("Excepton in method generateTagsForImportedFiles: " + e.getMessage());
                lastFailedGeneratingTags.add(new FailedFileDetails(file.getFilename(), "Failed to generate tags for file '" + file.getFilename() + "'. Reason: " + e.getMessage()));
            } catch (Exception e) {
                logger.debug("Unexpected excepton in method importFiles: " + e.getMessage());
                lastFailedGeneratingTags.add(new FailedFileDetails(file.getFilename(), "Unexpected excepton: " + e.getMessage()));
            }
        }

        observers.forEach(Mp3FilesObserver::onTagGenerated);
    }

    public void saveImportedFiles() {
        lastFailedSavingFiles = new LinkedList<>();

        for (Mp3File file : importedFiles.values()) {
            String originalFileName = file.getFilename();
            String modifiedFileName = originalFileName + ".modified";
            try {
                file.save(modifiedFileName);
            } catch (IOException | NotSupportedException e) {
                logger.debug("Excepton in method saveImportedFiles: " + e.getMessage());
                lastFailedSavingFiles.add(new FailedFileDetails(originalFileName, e.getMessage()));
                continue;
            } catch (Exception e) {
                logger.debug("Unexpected excepton in method importFiles: " + e.getMessage());
                lastFailedSavingFiles.add(new FailedFileDetails(file.getFilename(), "Unexpected excepton: " + e.getMessage()));
                continue;
            }

            File oldFile = new File(originalFileName);
            oldFile.delete();
            File newlySavedFile = new File(modifiedFileName);
            boolean isRenameSuccess = newlySavedFile.renameTo(new File(originalFileName));
            if (!isRenameSuccess) {
                logger.debug("Excepton in method saveImportedFiles: " + "Failed to rename newly created file. Newly saved file is: " + modifiedFileName);
                lastFailedSavingFiles.add(new FailedFileDetails(originalFileName, "Failed to rename newly created file. Newly saved file is: " + modifiedFileName));
            }
        }

        observers.forEach(Mp3FilesObserver::onSavedImportedFiles);
    }

    public void registerObserver(Mp3FilesObserver observer) {
        observers.add(observer);
    }

    public void unregisterObserver(Mp3FilesObserver observer) {
        observers.remove(observer);
    }

    public interface Mp3FilesObserver {
        void onImportedFileChanged(Mp3File mp3File);

        void onImportedFilesChanged();

        void onSavedImportedFiles();

        void onTagGenerated();
    }

}
