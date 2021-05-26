package app;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import app.model.NoteSetStatus;
import app.view.EditorController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application
{
    private Stage primaryStage;
    private SplitPane root;
    private EditorController controller;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        this.primaryStage = primaryStage;

        loadLayoutAndControllerFromFXML();

        primaryStage.setOnCloseRequest(
            e -> {
                controller.addEditorContentsToList();

                if (controller.getNoteSetStatus() == NoteSetStatus.CHANGED)
                {
                    Alert alert = new Alert(
                        AlertType.CONFIRMATION,
                        "Changes detected. Save current note set before exiting?"
                    );
                    alert.initStyle(StageStyle.UTILITY);
                    alert.setTitle("Save Confirmation");
                    alert.setHeaderText(null);
                    alert.initOwner(primaryStage);

                    // Change the text of the alert buttons
                    ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Save");
                    ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Don't Save");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK)
                    {
                        if (controller.getNoteSetFile() == null)
                        {
                            controller.chooseSaveFile();
                        }
                        else
                        {
                            controller.saveToFile();
                        }
                    }

                    primaryStage.close();
                }
            }
        );
    }

    public void loadLayoutAndControllerFromFXML()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/editor.fxml"));
            root = loader.<SplitPane>load();

            controller = loader.getController();
            controller.setMainApp(this);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("NoteSets");
            primaryStage.getIcons().add(
                new Image(MainApp.class.getResourceAsStream("resources/images/logo.png"))
            );

            primaryStage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setPrimaryStageTitleModifier(File noteSetFile, NoteSetStatus setStatus)
    {
        String fileName;
        if (noteSetFile != null)
        {
            fileName = noteSetFile.getName();
        }
        else
        {
            fileName = "Untitled";
        }

        switch (setStatus)
        {
            case NEW:
                primaryStage.setTitle("NoteSets - Untitled - New");
                break;
            case LOADED:
                primaryStage.setTitle("NoteSets - " + fileName);
                break;
            case CHANGED:
                primaryStage.setTitle("NoteSets - " + fileName + " - Edited");
                break;
            case SAVED:
                primaryStage.setTitle("NoteSets - " + fileName + " - Saved");
        }
    }

    public Stage getPrimaryStage()
    {
        return primaryStage;
    }
}
