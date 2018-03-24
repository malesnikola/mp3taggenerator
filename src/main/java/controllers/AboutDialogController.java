package main.java.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ResourceBundle;

public class AboutDialogController {
    private static Logger logger = Logger.getLogger(MainScreenController.class);

    private ResourceBundle resourceBundle;
    private Stage dialogStage;

    @FXML
    private Label appNameLabel;
    @FXML
    private Label appVersionLabel;
    @FXML
    private Label copyrightLabel;

    /**
     * Get localized string from Bundle_{chosenLanguage}.properties in charset cp1250 (because of Serbian latin letters).
     * @param key Key of the required string.
     * @return Returns value of localized sting if exist, else returns empty string.
     */
    private String getLocalizedString(String key) {
        try {
            String value = resourceBundle.getString(key);
            return new String(value.getBytes("ISO-8859-1"), "cp1250");
        } catch (Exception e) {
            logger.debug("Exception in method getLocalizedString: " + e.getMessage());
            return "";
        }
    }

    /**
     * Populate UI element with localized strings from Bundle_{chosenLanguage}.properties.
     */
    private void populateUIWithLocalizedStrings() {
        // labels
        appNameLabel.setText(getLocalizedString("dialog.about.appname.text"));
        appVersionLabel.setText(getLocalizedString("dialog.about.appversion.text"));
        copyrightLabel.setText(getLocalizedString("dialog.about.copyright.text"));
    }

    @FXML
    public void initialize() {
        //populateUIWithLocalizedStrings();
    }

    public AboutDialogController(ResourceBundle resourceBundle){
        this.resourceBundle = resourceBundle;
        dialogStage = new Stage();
        dialogStage.setTitle(getLocalizedString("dialog.about.title"));
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dialog_about.fxml"));
        try {
            Parent root = loader.load();
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getStage() {
        return dialogStage;
    }
}
