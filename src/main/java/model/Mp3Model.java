package main.java.model;

import com.mpatric.mp3agic.*;
import main.java.domain.FailedFileDetails;
import main.java.domain.Mp3FileWrapper;
import main.java.enums.Mp3FilePattern;
import main.java.enums.FileState;
import main.java.enums.Mp3ModelState;
import main.java.exceptions.FileNameBadFormatException;
import main.java.service.Mp3Service;
import main.java.workers.GenerateTagsWorker;
import main.java.workers.ImportFilesWorker;
import main.java.workers.SaveFilesWorker;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class Mp3Model {
    private static Logger logger = Logger.getLogger(Mp3Model.class);

    /**
     * Last model state represent last executed action (e.g. "saved" for saving).
     */
    private Mp3ModelState lastModelState = Mp3ModelState.NONE;

    /**
     * Contains all imported ".mp3" files. Key is file path (full file name), value is Mp3FileWrapper.
     */
    private Map<String, Mp3FileWrapper> importedFiles = new ConcurrentHashMap<>();

    /**
     * Contains list of all files (with error details) which cannot be imported.
     */
    private List<FailedFileDetails> lastFailedLoadingFiles = Collections.synchronizedList(new ArrayList<>());

    /**
     * Contains list of all files (with error details) which cannot be saved.
     */
    private List<FailedFileDetails> lastFailedSavingFiles = new LinkedList<>();

    /**
     * Contains list of all files (with error details) for which tags cannot be generated.
     */
    private List<FailedFileDetails> lastFailedGeneratingTags = new LinkedList<>();

    /**
     * Contains set of registered observers.
     */
    private Set<Mp3FilesObserver> observers = new HashSet<>();

    /**
     * Get all imported files.
     * @return Returns ConcurrentHashMap where key is file path (full file name) and value is Mp3FileWrapper.
     */
    public Map<String, Mp3FileWrapper> getImportedFiles() {
        return importedFiles;
    }

    /**
     * Get list of last failed loaded files.
     * @return Returns list of all files (with error details) which cannot be imported.
     */
    public List<FailedFileDetails> getLastFailedLoadingFiles() {
        return lastFailedLoadingFiles;
    }

    /**
     * Get list of last failed saved files.
     * @return Returns list of all files (with error details) which cannot be saved.
     */
    public List<FailedFileDetails> getLastFailedSavingFiles() {
        return lastFailedSavingFiles;
    }

    /**
     * Returns list of last failed generated files.
     * @return List of all files (with error details) for which tags cannot be generated.
     */
    public List<FailedFileDetails> getLastFailedGeneratingTags() {
        return lastFailedGeneratingTags;
    }

    /**
     * Get last state of Mp3Model.
     * @return Returns last model state which represent last executed action (e.g. "saved" for saving).
     */
    public Mp3ModelState getLastModelState() {
        return lastModelState;
    }

    public void importFiles(List<File> files) {
        importFiles(files, null);
    }

    public void importFiles(List<File> files, ImportFilesWorker worker) {
        if (files != null && !files.isEmpty()) {
            lastModelState = Mp3ModelState.IMPORTED;

            int previousSizeOfImportedFiles = importedFiles.size();
            lastFailedLoadingFiles = Collections.synchronizedList(new ArrayList<>());

            AtomicInteger progress = new AtomicInteger();

            files.parallelStream().forEach(file -> {
                String filePath = file.getPath();
                if (!importedFiles.containsKey(filePath)) {
                    try {
                        Mp3FileWrapper mp3FileWrapper = new Mp3FileWrapper(filePath);
                        importedFiles.put(filePath, mp3FileWrapper);
                    } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                        logger.debug("Excepton in method importFiles: " + e.getMessage());
                        lastFailedLoadingFiles.add(new FailedFileDetails(filePath, e.getMessage()));
                    } catch (Exception e) {
                        logger.debug("Unexpected excepton in method importFiles: " + e.getMessage());
                        lastFailedLoadingFiles.add(new FailedFileDetails(filePath, "Unexpected excepton: " + e.getMessage()));
                    }
                }

                if (worker != null) {
                    worker.updateProgress(progress.incrementAndGet());
                }
            });

            if (previousSizeOfImportedFiles != importedFiles.size() || !lastFailedLoadingFiles.isEmpty()) {
                observers.forEach(Mp3FilesObserver::onImportedFilesChanged);
            }
        }
    }

    public void removeImportedFiles(List<String> filePaths) {
        if (filePaths != null) {
            lastModelState = Mp3ModelState.REMOVED;

            int previousSizeOfImportedFiles = importedFiles.size();

            for (String filePath : filePaths) {
                importedFiles.remove(filePath);
            }

            if (previousSizeOfImportedFiles != importedFiles.size()) {
                observers.forEach(Mp3FilesObserver::onImportedFilesChanged);
            }
        }
    }

    public void generateTagsForImportedFiles(Mp3FilePattern pattern, boolean isCyrillicTags, GenerateTagsWorker worker) {
        generateTagsForImportedFiles(pattern, isCyrillicTags, worker, null);
    }

    public void generateTagsForImportedFiles(Mp3FilePattern pattern, boolean isCyrillicTags, GenerateTagsWorker worker, List<String> selectedFilesPath) {
        lastFailedGeneratingTags = new LinkedList<>();

        if (importedFiles.size() > 0) {
            lastModelState = Mp3ModelState.GENRATED;
        }

        AtomicInteger progress = new AtomicInteger();
        long maxProgress = importedFiles.size();

        Collection<Mp3FileWrapper> selectedFiles = importedFiles.values();
        if (selectedFilesPath != null && selectedFilesPath.size() > 0) {
            List<Mp3FileWrapper> listOfSelectedMp3Files = new LinkedList<>();
            selectedFilesPath.forEach(path -> listOfSelectedMp3Files.add(importedFiles.get(path)));
            selectedFiles = listOfSelectedMp3Files;
        }

        selectedFiles.parallelStream().forEach(file -> {
            try {
                file.setPattern(pattern);
                ID3v2 id3v2Tag = Mp3Service.crateID3v2Tag(file, isCyrillicTags);
                file.removeId3v2Tag();
                file.setId3v2Tag(id3v2Tag);
                file.setState(FileState.MODIFIED);
            } catch (FileNameBadFormatException e) {
                logger.debug("Excepton in method generateTagsForImportedFiles: " + e.getMessage());
                lastFailedGeneratingTags.add(new FailedFileDetails(file.getFilename(), "Failed to generate tags for file '" + file.getFilename() + "'. Reason: " + e.getMessage()));
                file.setState(FileState.FAILED_MODIFIED);
            } catch (Exception e) {
                logger.debug("Unexpected excepton in method importFiles: " + e.getMessage());
                lastFailedGeneratingTags.add(new FailedFileDetails(file.getFilename(), "Unexpected excepton: " + e.getMessage()));
                file.setState(FileState.FAILED_MODIFIED);
            }

            if (worker != null) {
                worker.updateProgress(progress.incrementAndGet(), maxProgress);
            }
        });

        observers.forEach(Mp3FilesObserver::onTagGenerated);
    }

    public void saveImportedFiles() {
        saveImportedFiles(null);
    }

    public void saveImportedFiles(SaveFilesWorker worker) {
        lastFailedSavingFiles = new LinkedList<>();

        if (importedFiles.size() > 0) {
            lastModelState = Mp3ModelState.SAVED;
        }

        AtomicInteger progress = new AtomicInteger();
        long maxProgress = importedFiles.size();

        importedFiles.values().parallelStream().forEach(file -> {
            String originalFileName = file.getFilename();
            String modifiedFileName = originalFileName + ".modified";
            boolean isSuccess = false;
            try {
                if (file.getState() != FileState.SAVED && file.getState() != FileState.FAILED_MODIFIED) {
                    file.save(modifiedFileName);
                    isSuccess = true;
                }
            } catch (IOException | NotSupportedException e) {
                logger.debug("Excepton in method saveImportedFiles: " + e.getMessage());
                lastFailedSavingFiles.add(new FailedFileDetails(originalFileName, e.getMessage()));
                file.setState(FileState.FAILED_SAVED);
            } catch (Exception e) {
                logger.debug("Unexpected excepton in method importFiles: " + e.getMessage());
                lastFailedSavingFiles.add(new FailedFileDetails(file.getFilename(), "Unexpected excepton: " + e.getMessage()));
                file.setState(FileState.FAILED_SAVED);
            }

            if (isSuccess) {
                File oldFile = new File(originalFileName);
                oldFile.delete();
                File newlySavedFile = new File(modifiedFileName);
                boolean isRenameSuccess = newlySavedFile.renameTo(new File(originalFileName));
                if (!isRenameSuccess) {
                    logger.debug("Excepton in method saveImportedFiles: " + "Failed to rename newly created file. Newly saved file is: " + modifiedFileName);
                    lastFailedSavingFiles.add(new FailedFileDetails(originalFileName, "Failed to rename newly created file. Newly saved file is: " + modifiedFileName));
                    file.setState(FileState.FAILED_SAVED);
                } else {
                    file.setState(FileState.SAVED);
                }
            }

            if (worker != null) {
                worker.updateProgress(progress.incrementAndGet(), maxProgress);
            }
        });

        observers.forEach(Mp3FilesObserver::onSavedImportedFiles);
    }

    public void registerObserver(Mp3FilesObserver observer) {
        observers.add(observer);
    }

    public void unregisterObserver(Mp3FilesObserver observer) {
        observers.remove(observer);
    }

    public interface Mp3FilesObserver {
        void onImportedFilesChanged();

        void onSavedImportedFiles();

        void onTagGenerated();
    }

}
