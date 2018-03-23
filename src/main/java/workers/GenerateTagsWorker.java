package main.java.workers;

import javafx.concurrent.Task;
import main.java.dialogs.ProgressForm;
import main.java.enums.Mp3FilePattern;
import main.java.model.Mp3Model;

import java.util.List;

public class GenerateTagsWorker extends Task<Boolean> {

    private Mp3Model mp3Model;

    private Mp3FilePattern pattern;

    private boolean isCyrillicTags;

    private List<String> selectedFilesPath;

    public GenerateTagsWorker(Mp3Model mp3Model, ProgressForm progressForm, Mp3FilePattern pattern, boolean isCyrillicTags, List<String> selectedFilesPath) {
        super();
        this.mp3Model = mp3Model;
        this.pattern = pattern;
        this.isCyrillicTags = isCyrillicTags;
        this.selectedFilesPath = selectedFilesPath;

        this.setOnCancelled(event -> {
            progressForm.closeDialogStage();
        });

        this.setOnSucceeded(event -> {
            progressForm.closeDialogStage();
        });
    }

    @Override
    protected Boolean call() throws Exception {
        mp3Model.generateTagsForImportedFiles(pattern, isCyrillicTags, this, selectedFilesPath);

        return true;
    }

    public void updateProgress(long workDone, long maxWork){
        super.updateProgress(workDone, maxWork);
    }
}
