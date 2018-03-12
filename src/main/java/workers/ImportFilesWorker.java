package main.java.workers;

import javafx.concurrent.Task;
import main.java.controllers.ProgressForm;
import main.java.model.Mp3Model;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;

public class ImportFilesWorker extends Task<Boolean>{

    private Mp3Model mp3Model;

    private List<File> files;

    public ImportFilesWorker(Mp3Model mp3Model, ProgressForm progressForm, List<File> files) {
        super();
        this.mp3Model = mp3Model;
        this.files = files;
        this.setOnCancelled(event -> {
            progressForm.closeDialogStage();
        });

        this.setOnSucceeded(event -> {
            progressForm.closeDialogStage();
        });
    }

    @Override
    protected Boolean call() throws Exception {
        mp3Model.importFiles(files, this);

        return true;
    }

    public void updateProgress(long workDone){
        super.updateProgress(workDone, files.size());
    }
}
