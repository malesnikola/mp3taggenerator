package main.java.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class ErrorDialogController {

    @FXML
    private Label label;

    @FXML
    private TextArea textArea;

    private Stage dialogStage;

    public ErrorDialogController(){
        try {
            dialogStage = new Stage();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../resources/view/dialog_error.fxml"));
        try {
            Parent root = (Parent)loader.load();
            dialogStage.setScene(root.getScene());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getStage() {
        return dialogStage;
    }
}
