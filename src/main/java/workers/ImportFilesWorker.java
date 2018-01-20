package main.java.workers;

import javafx.concurrent.Task;
import main.java.model.Mp3Model;
import org.springframework.beans.factory.annotation.Autowired;

public class ImportFilesWorker extends Task<Void>{

    @Autowired
    private Mp3Model mp3Model;

    @Override
    protected Void call() throws Exception {


        return null;
    }
}
