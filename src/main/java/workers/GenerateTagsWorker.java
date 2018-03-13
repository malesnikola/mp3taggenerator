package main.java.workers;

import javafx.concurrent.Task;
import main.java.controllers.ProgressForm;
import main.java.model.Mp3Model;

public class GenerateTagsWorker extends Task<Boolean> {

    private Mp3Model mp3Model;

    private boolean isCyrillicTags;

    public GenerateTagsWorker(Mp3Model mp3Model, ProgressForm progressForm, boolean isCyrillicTags) {
        super();
        this.mp3Model = mp3Model;
        this.isCyrillicTags = isCyrillicTags;

        this.setOnCancelled(event -> {
            progressForm.closeDialogStage();
        });

        this.setOnSucceeded(event -> {
            progressForm.closeDialogStage();
        });
    }

    @Override
    protected Boolean call() throws Exception {
        mp3Model.generateTagsForImportedFiles(isCyrillicTags, this);

        return true;
    }

    public void updateProgress(long workDone, long maxWork){
        super.updateProgress(workDone, maxWork);
    }
}
