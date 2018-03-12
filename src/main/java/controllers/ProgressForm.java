package main.java.controllers;

import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class ProgressForm {
    private final Scene parentScene;
    private final Stage dialogStage;
    //private final ProgressBar progressBar = new ProgressBar();
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private Task<?> task;

    public ProgressForm(Scene parentStage) {
        this.parentScene = parentStage;
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        // PROGRESS BAR
        final Label label = new Label();
        label.setText("alerto");

        //progressBar.setProgress(-1F);
        progressIndicator.setProgress(-1F);

        /*final HBox hb = new HBox();
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().addAll(progressBar, progressIndicator);*/

        Scene scene = new Scene(progressIndicator);
        dialogStage.setScene(scene);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ESCAPE:
                        System.out.println("ESCCCCCCCCCCCcc");
                        if (task != null) {
                            task.cancel();
                        }

                        break;
                }
            }
        });

        dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                event.consume();
            }
        });

    }

    public void activateProgressBar(final Task<?> task)  {
        this.task = task;
        //progressBar.progressProperty().bind(task.progressProperty());
        progressIndicator.progressProperty().bind(task.progressProperty());
        dialogStage.show();
    }

    public void closeDialogStage(){
        parentScene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(false));
        dialogStage.close();
    }

    public Stage getDialogStage() {
        return dialogStage;
    }

}
