package main.java.workers;

import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import main.java.model.Mp3Model;

public class SaveFilesWorker extends Task<Boolean> {

    private Mp3Model mp3Model;

    public SaveFilesWorker(Mp3Model mp3Model, ProgressBar progressBar) {
        super();
        this.mp3Model = mp3Model;
        this.setOnCancelled(event -> {
            //progressBar.setProgress(0);
        });

        this.setOnSucceeded(event -> {
            //progressBar.setProgress(0);
        });
    }

    @Override
    protected Boolean call() throws Exception {
        mp3Model.saveImportedFiles(this);
        return true;
    }

    public void updateProgress(long workDone, long maxWork){
        super.updateProgress(workDone, maxWork);
    }
}
