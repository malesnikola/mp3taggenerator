package main.java.model;

import com.mpatric.mp3agic.*;
import main.java.domain.FailedFileDetails;
import main.java.domain.Mp3FileWrapper;
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

    public enum Mp3ModelState {
        NONE,
        IMPORTED,
        REMOVED,
        GENRATED,
        SAVED
    }

    private static Logger logger = Logger.getLogger(Mp3Model.class);

    private Mp3ModelState lastModelState = Mp3ModelState.NONE;

    private Map<String, Mp3FileWrapper> importedFiles = new ConcurrentHashMap<>();

    private List<FailedFileDetails> lastFailedLoadingFiles = Collections.synchronizedList(new ArrayList<>());

    private List<FailedFileDetails> lastFailedSavingFiles = new LinkedList<>();

    private List<FailedFileDetails> lastFailedGeneratingTags = new LinkedList<>();

    private Set<Mp3FilesObserver> observers = new HashSet<>();

    public Map<String, Mp3FileWrapper> getImportedFiles() {
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

    public Mp3ModelState getLastModelState() {
        return lastModelState;
    }

    public void importFile(File file){
        String filePath = file.getPath();
        if (!importedFiles.containsKey(filePath)) {
            try {
                Mp3FileWrapper newFile = new Mp3FileWrapper(filePath);
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

    public void generateTagsForImportedFiles(Mp3FileWrapper.Mp3FilePattern pattern, boolean isCyrillicTags, GenerateTagsWorker worker) {
        generateTagsForImportedFiles(pattern, isCyrillicTags, worker, null);
    }

    public void generateTagsForImportedFiles(Mp3FileWrapper.Mp3FilePattern pattern, boolean isCyrillicTags, GenerateTagsWorker worker, List<String> selectedFilesPath) {
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
                file.setState(Mp3FileWrapper.Mp3FileState.MODIFIED);
            } catch (FileNameBadFormatException e) {
                logger.debug("Excepton in method generateTagsForImportedFiles: " + e.getMessage());
                lastFailedGeneratingTags.add(new FailedFileDetails(file.getFilename(), "Failed to generate tags for file '" + file.getFilename() + "'. Reason: " + e.getMessage()));
                file.setState(Mp3FileWrapper.Mp3FileState.FAILED_MODIFIED);
            } catch (Exception e) {
                logger.debug("Unexpected excepton in method importFiles: " + e.getMessage());
                lastFailedGeneratingTags.add(new FailedFileDetails(file.getFilename(), "Unexpected excepton: " + e.getMessage()));
                file.setState(Mp3FileWrapper.Mp3FileState.FAILED_MODIFIED);
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
                if (file.getState() != Mp3FileWrapper.Mp3FileState.SAVED && file.getState() != Mp3FileWrapper.Mp3FileState.FAILED_MODIFIED) {
                    file.save(modifiedFileName);
                    isSuccess = true;
                }
            } catch (IOException | NotSupportedException e) {
                logger.debug("Excepton in method saveImportedFiles: " + e.getMessage());
                lastFailedSavingFiles.add(new FailedFileDetails(originalFileName, e.getMessage()));
                file.setState(Mp3FileWrapper.Mp3FileState.FAILED_SAVED);
            } catch (Exception e) {
                logger.debug("Unexpected excepton in method importFiles: " + e.getMessage());
                lastFailedSavingFiles.add(new FailedFileDetails(file.getFilename(), "Unexpected excepton: " + e.getMessage()));
                file.setState(Mp3FileWrapper.Mp3FileState.FAILED_SAVED);
            }

            if (isSuccess) {
                File oldFile = new File(originalFileName);
                oldFile.delete();
                File newlySavedFile = new File(modifiedFileName);
                boolean isRenameSuccess = newlySavedFile.renameTo(new File(originalFileName));
                if (!isRenameSuccess) {
                    logger.debug("Excepton in method saveImportedFiles: " + "Failed to rename newly created file. Newly saved file is: " + modifiedFileName);
                    lastFailedSavingFiles.add(new FailedFileDetails(originalFileName, "Failed to rename newly created file. Newly saved file is: " + modifiedFileName));
                    file.setState(Mp3FileWrapper.Mp3FileState.FAILED_SAVED);
                } else {
                    file.setState(Mp3FileWrapper.Mp3FileState.SAVED);
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
        void onImportedFileChanged(Mp3FileWrapper mp3File);

        void onImportedFilesChanged();

        void onSavedImportedFiles();

        void onTagGenerated();
    }

}
