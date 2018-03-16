package main.java.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import main.java.controllers.MainScreenController;
import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

@ComponentScan("main.java")
@SpringBootApplication
public class Main extends Application {

    private ConfigurableApplicationContext context;
    private Parent rootNode;

    MainScreenController mainScreenController;

    @Override
    public void init() throws Exception {
        BasicConfigurator.configure();
        context = SpringApplication.run(Main.class);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../resources/view/screen_main.fxml"));
        //loader.setResources(ResourceBundle.getBundle("../../resources/bundles/Bundle", new Locale("en", "EN")));
        loader.setControllerFactory(context::getBean);
        rootNode = loader.load();
        mainScreenController = (MainScreenController) loader.getController();
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("MP3 Tag Generator");
        Scene scene = new Scene(rootNode);
        mainScreenController.setScene(scene);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        context.close();
    }

    private void loadView(Locale locale) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setResources(ResourceBundle.getBundle("bundles.MyBundle", locale));
    }


    public static void main(String[] args) {
        launch(args);
    }
}
