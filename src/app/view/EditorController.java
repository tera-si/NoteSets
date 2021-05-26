package app.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import app.MainApp;
import app.Util.EditableModifierHTML;
import app.model.Note;
import app.model.NoteSetStatus;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Controller class for MainApp
 */
public class EditorController
{
    private final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern(
        "dd/MM/yyyy HH:mm:ss"
    );
    private final String EMPTY_HTML =
        "<html dir=\"ltr\"><head></head><body contenteditable=\"true\"></body></html>";

    @FXML
    private AnchorPane rightPane;
    @FXML
    private ListView<String> previewList;
    @FXML
    private HTMLEditor editor;
    @FXML
    private ButtonBar deleteBar;
    @FXML
    private ButtonBar addBar;

    private MainApp mainApp;
    private ArrayList<Note> notes;
    private File noteSetFile;
    private NoteSetStatus noteSetStatus;

    @FXML
    private void initialize()
    {
        noteSetFile = null;

        rightPane.setVisible(false);
        deleteBar.setVisible(false);

        previewList.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldSelection, newSelection) -> {
                int index = previewList.getSelectionModel().getSelectedIndex();
                if (index > -1)
                {
                    Note note = notes.get(index);
                    editor.setHtmlText(note.getFullNote());
                }
            }
        );
    }

    @FXML
    private void handleNew()
    {
        addEditorContentsToList();

        if (noteSetStatus == NoteSetStatus.CHANGED)
        {
            Alert alert = new Alert(
                AlertType.CONFIRMATION,
                "Changes detected. Save current note set before creating anew?"
            );
            alert.setTitle("Save Confirmation");
            alert.setHeaderText(null);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.initStyle(StageStyle.UTILITY);

            ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Save");
            ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Don't Save");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK)
            {
                if (noteSetFile == null)
                {
                    chooseSaveFile();
                }
                else
                {
                    saveToFile();
                }
            }
        }

        noteSetFile = null;
        notes = new ArrayList<Note>();
        updatePreviewList();
        rightPane.setVisible(true);
        editor.setHtmlText("");
        deleteBar.setVisible(true);
        noteSetStatus = NoteSetStatus.NEW;
        mainApp.setPrimaryStageTitleModifier(noteSetFile, noteSetStatus);
    }

    @FXML
    private void handleLoad()
    {
        addEditorContentsToList();

        if (noteSetStatus == NoteSetStatus.CHANGED)
        {
            Alert alert = new Alert(
                AlertType.CONFIRMATION,
                "Changes detected. Save current note set before loading?"
            );
            alert.setTitle("Save Confirmation");
            alert.setHeaderText(null);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.initStyle(StageStyle.UTILITY);

            ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Save");
            ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Don't Save");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK)
            {
                if (noteSetFile == null)
                {
                    chooseSaveFile();
                }
                else
                {
                    saveToFile();
                }
            }
        }

        chooseLoadFile();
        updatePreviewList();
        if (noteSetFile != null)
        {
            rightPane.setVisible(true);
            editor.setHtmlText("");
            deleteBar.setVisible(true);
        }
    }

    @FXML
    private void handleSave()
    {
        if (noteSetFile == null)
        {
            chooseSaveFile();
        }
        else
        {
            saveToFile();
        }
    }

    @FXML
    private void handleSaveAs()
    {
        chooseSaveFile();
    }

    @FXML
    private void handleAddToNote()
    {
        String now = DATE_TIME_FORMAT.format(LocalDateTime.now());
        String fullHTML = editor.getHtmlText();
        Note newNote = new Note(now, fullHTML);
        notes.add(newNote);
        notes.trimToSize();

        updatePreviewList();

        editor.setHtmlText("");

        noteSetStatus = NoteSetStatus.CHANGED;
        mainApp.setPrimaryStageTitleModifier(noteSetFile, noteSetStatus);
    }

    @FXML
    private void handleDelete()
    {
        int index = previewList.getSelectionModel().getSelectedIndex();
        if (index > -1)
        {
            String preview = notes.get(index).getParsedPreview();

            Alert alert = new Alert(
                AlertType.CONFIRMATION,
                "Delete selected note?\nPreview of selected note:\n\t" +
                preview
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("Delete Confirmation");
            alert.setHeaderText(null);
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK)
            {
                notes.remove(index);
                notes.trimToSize();

                updatePreviewList();
                editor.setHtmlText("");

                noteSetStatus = NoteSetStatus.CHANGED;
                mainApp.setPrimaryStageTitleModifier(noteSetFile, noteSetStatus);
            }
        }
        else
        {
            Alert alert = new Alert(
                AlertType.ERROR,
                "No selected note"
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Delete Failed");
            alert.setHeaderText(null);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleTutorial()
    {
        VBox root = new VBox();
        WebView webView = new WebView();
        root.getChildren().add(webView);
        VBox.setVgrow(webView, Priority.ALWAYS);

        webView.getEngine().load(
            mainApp.getClass().getResource("resources/tutorial.html").toString()
        );

        Scene scene = new Scene(root);
        Stage tutorialStage = new Stage();
        tutorialStage.setTitle("Tutorial");
        tutorialStage.setScene(scene);
        tutorialStage.initOwner(mainApp.getPrimaryStage());
        tutorialStage.getIcons().add(
            new Image(mainApp.getClass().getResourceAsStream("resources/images/logo.png"))
        );

        tutorialStage.showAndWait();
    }

    @FXML
    private void handleAbout()
    {
        Alert alert = new Alert(
            AlertType.INFORMATION,
            "NoteSets\nVersion 1.0\n\nAuthor: TeraSi\nWebsite: " +
            "https://github.com/tera-si"
        );
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("About");
        alert.initOwner(mainApp.getPrimaryStage());
        alert.setHeaderText(null);

        alert.showAndWait();
    }

    @FXML
    private void handleExportSelected()
    {
        int index = previewList.getSelectionModel().getSelectedIndex();
        if (index > -1)
        {
            FileChooser fileChooser = new FileChooser();

            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "html files (*.html)", "*.html"
            );
            fileChooser.getExtensionFilters().add(extFilter);

            File newFile = fileChooser.showSaveDialog(mainApp.getPrimaryStage());
            if (newFile != null)
            {
                Alert waitAlert = new Alert(
                    AlertType.INFORMATION,
                    "Exporting, please wait until a success message is shown"
                );
                waitAlert.initStyle(StageStyle.UTILITY);
                waitAlert.initOwner(mainApp.getPrimaryStage());
                waitAlert.setTitle("Exporting...");
                waitAlert.setHeaderText(null);
                waitAlert.show();

                if (!newFile.getPath().endsWith(".html"))
                {
                    newFile = new File(newFile.getPath() + ".html");
                }

                exportAsHTML(newFile, index);
                waitAlert.close();

                Alert alert = new Alert(
                    AlertType.INFORMATION,
                    "Selected note successfully exported"
                );
                alert.initStyle(StageStyle.UTILITY);
                alert.initOwner(mainApp.getPrimaryStage());
                alert.setTitle("Export completed");
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        }
        else
        {
            Alert alert = new Alert(
                AlertType.ERROR,
                "No selected note"
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    @FXML
    private void handleExportAll()
    {
        addEditorContentsToList();

        if ((notes != null) && (!notes.isEmpty()))
        {
            File zipFile = chooseZipLocation();
            if (zipFile != null)
            {
                Alert waitAlert = new Alert(
                    AlertType.INFORMATION,
                    "Exporting, please wait until a success message is shown"
                );
                waitAlert.initStyle(StageStyle.UTILITY);
                waitAlert.initOwner(mainApp.getPrimaryStage());
                waitAlert.setTitle("Exporting...");
                waitAlert.setHeaderText(null);
                waitAlert.show();

                String parentLocation = zipFile.getParent();

                try
                {
                    FileOutputStream fOutStream = new FileOutputStream(zipFile);
                    ZipOutputStream zOutStream = new ZipOutputStream(fOutStream);

                    for (int i = 0; i < notes.size(); i++)
                    {
                        String htmlPath = notes.get(i).getDateTimeOfNote();
                        htmlPath = parentLocation + "\\" + i + "_" +
                                   replaceNonValidFileNameChars(htmlPath) +
                                   ".html";
                        File htmlFile = new File(htmlPath);
                        exportAsHTML(htmlFile, i);

                        ZipEntry entry = new ZipEntry(htmlFile.getName());
                        zOutStream.putNextEntry(entry);
                        byte[] bytes = Files.readAllBytes(Paths.get(htmlPath));
                        zOutStream.write(bytes, 0, bytes.length);
                        zOutStream.closeEntry();

                        htmlFile.delete();
                    }

                    zOutStream.close();
                    waitAlert.close();

                    Alert alert = new Alert(
                        AlertType.INFORMATION,
                        "Note set successfully exported"
                    );
                    alert.initStyle(StageStyle.UTILITY);
                    alert.initOwner(mainApp.getPrimaryStage());
                    alert.setTitle("Export completed");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                }
                catch (FileNotFoundException e)
                {
                    Alert alert = new Alert(
                        AlertType.ERROR,
                        "Cannot create file " + zipFile.getPath() +
                        "\nNote not exported!"
                    );
                    alert.initStyle(StageStyle.UTILITY);
                    alert.setTitle("Export Failed");
                    alert.setHeaderText(null);
                    alert.initOwner(mainApp.getPrimaryStage());
                    alert.showAndWait();

                    e.printStackTrace();
                }
                catch (SecurityException e)
                {
                    Alert alert = new Alert(
                        AlertType.ERROR,
                        "Access to file " + zipFile.getPath() + " denied" +
                        "\nNote not exported!"
                    );
                    alert.initStyle(StageStyle.UTILITY);
                    alert.setTitle("Export Failed");
                    alert.setHeaderText(null);
                    alert.initOwner(mainApp.getPrimaryStage());
                    alert.showAndWait();

                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    Alert alert = new Alert(
                        AlertType.ERROR,
                        "Error writing to " + zipFile.getPath()
                    );
                    alert.initStyle(StageStyle.UTILITY);
                    alert.setTitle("Export Failed");
                    alert.setHeaderText(null);
                    alert.initOwner(mainApp.getPrimaryStage());
                    alert.showAndWait();

                    e.printStackTrace();
                }
            }
        }
        else
        {
            Alert alert = new Alert(
                AlertType.ERROR,
                "Note set is empty, no note to export"
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Export failed");
            alert.setHeaderText(null);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait();
        }
    }

    private File chooseZipLocation()
    {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
            "zip files (*.zip)", "*.zip"
        );
        fileChooser.getExtensionFilters().add(extFilter);

        File newFile = fileChooser.showSaveDialog(mainApp.getPrimaryStage());
        if (newFile != null)
        {
            if (!newFile.getPath().endsWith(".zip"))
            {
                newFile = new File(newFile.getPath() + ".zip");
            }
        }

        return newFile;
    }

    private String replaceNonValidFileNameChars(String fileName)
    {
        String newName = fileName.replaceAll("/", "-");
        newName = newName.replaceAll(":", "-");
        newName = newName.replaceAll(" ", "_");

        return newName;
    }

    private void exportAsHTML(File exportLocation, int noteIndex)
    {
        try
        {
            PrintWriter textWriter = new PrintWriter(exportLocation);
            String noteHTML = notes.get(noteIndex).getFullNote();

            noteHTML = EditableModifierHTML.setNonEditable(noteHTML);

            textWriter.print(noteHTML);
            textWriter.close();
        }
        catch (FileNotFoundException e)
        {
            Alert alert = new Alert(
                AlertType.ERROR,
                "Cannot create file " + exportLocation.getPath() +
                "\nNote not exported!"
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait();

            e.printStackTrace();
        }
        catch (SecurityException e)
        {
            Alert alert = new Alert(
                AlertType.ERROR,
                "Access to file " + exportLocation.getPath() + " denied" +
                "\nNote not exported!"
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait();

            e.printStackTrace();
        }
    }

    public void setMainApp(MainApp mainApp)
    {
        this.mainApp = mainApp;
    }

    private void updatePreviewList()
    {
        previewList.getItems().clear();

        if (notes != null)
        {
            for (Note note : notes)
            {
                previewList.getItems().add(
                    note.getDateTimeOfNote() + "\n" + note.getParsedPreview()
                );
            }
        }
    }

    private void chooseLoadFile()
    {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
            "noteset files (*.noteset)", "*.noteset"
        );
        fileChooser.getExtensionFilters().add(extFilter);

        File newFile = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
        if (newFile != null)
        {
            if (!newFile.getPath().endsWith(".noteset"))
            {
                Alert alert = new Alert(
                    AlertType.ERROR,
                    "File " + noteSetFile.getPath() + " is not a .noteset file"
                );
                alert.initStyle(StageStyle.UTILITY);
                alert.initOwner(mainApp.getPrimaryStage());
                alert.setTitle("Invalid file");
                alert.setHeaderText(null);
                alert.showAndWait();
            }

            noteSetFile = newFile;
            loadFromFile();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile()
    {
        try
        {
            FileInputStream fInStream = new FileInputStream(noteSetFile);
            ObjectInputStream oInStream = new ObjectInputStream(fInStream);

            notes = (ArrayList<Note>) oInStream.readObject(); // unchecked

            oInStream.close();
            fInStream.close();

            noteSetStatus = NoteSetStatus.LOADED;
            mainApp.setPrimaryStageTitleModifier(noteSetFile, noteSetStatus);
        }
        catch (FileNotFoundException e)
        {
            Alert alert = new Alert(
                AlertType.ERROR,
                "File " + noteSetFile.getPath() + " not found"
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Load Failed");
            alert.setHeaderText(null);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait();

            e.printStackTrace();
        }
        catch (SecurityException e)
        {
            Alert alert = new Alert(
                AlertType.ERROR,
                "Access to file " + noteSetFile.getPath() + " denied"
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Load Failed");
            alert.setHeaderText(null);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait();

            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            Alert alert = new Alert(
                AlertType.ERROR,
                "No valid note set found in file " + noteSetFile.getPath()
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Load Failed");
            alert.setHeaderText(null);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait();

            e.printStackTrace();
        }
        catch (IOException e)
        {
            Alert alert = new Alert(
                AlertType.ERROR,
                "Error reading from file " + noteSetFile.getPath()
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Load Failed");
            alert.setHeaderText(null);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait();

            e.printStackTrace();
        }
    }

    public void chooseSaveFile()
    {
        if ((notes != null) && (!notes.isEmpty()))
        {
            FileChooser fileChooser = new FileChooser();

            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "noteset files (*.noteset)", "*.noteset"
            );
            fileChooser.getExtensionFilters().add(extFilter);

            File newFile = fileChooser.showSaveDialog(mainApp.getPrimaryStage());
            if (newFile != null)
            {
                // Make sure chosen file is in the correct extension
                if (!newFile.getPath().endsWith(".noteset"))
                {
                    newFile = new File(newFile.getPath() + ".noteset");
                }

                noteSetFile = newFile;
                saveToFile();
            }
        }
        else
        {
            Alert alert = new Alert(
                AlertType.INFORMATION,
                "Note set is empty, no save needed"
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Empty Note Set");
            alert.setHeaderText(null);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait();
        }
    }

    public void saveToFile()
    {
        try
        {
            FileOutputStream fOutStream = new FileOutputStream(noteSetFile);
            ObjectOutputStream oOutStream = new ObjectOutputStream(fOutStream);

            oOutStream.writeObject(notes);

            oOutStream.close();
            fOutStream.close();

            noteSetStatus = NoteSetStatus.SAVED;
            mainApp.setPrimaryStageTitleModifier(noteSetFile, noteSetStatus);
        }
        catch (FileNotFoundException e)
        {
            Alert alert = new Alert(
                AlertType.ERROR,
                "Cannot create file " + noteSetFile.getPath() +
                "\nNote Set not saved!"
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Save Failed");
            alert.setHeaderText(null);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait();

            e.printStackTrace();
        }
        catch (SecurityException e)
        {
            Alert alert = new Alert(
                AlertType.ERROR,
                "Access to file " + noteSetFile.getPath() + " denied" +
                "\nNote Set not saved!"
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Save Failed");
            alert.setHeaderText(null);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait();

            e.printStackTrace();
        }
        catch (IOException e)
        {
            Alert alert = new Alert(
                AlertType.ERROR,
                "Error writing to file " + noteSetFile.getPath() +
                "\nNote Set not saved!"
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Save Failed");
            alert.setHeaderText(null);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.showAndWait();

            e.printStackTrace();
        }
    }

    public void addEditorContentsToList()
    {
        String html = editor.getHtmlText();

        if (editor.isVisible() && !html.equalsIgnoreCase(EMPTY_HTML))
        {
            Alert alert = new Alert(
                AlertType.CONFIRMATION,
                "Add editor content to current note set before proceeding?"
            );
            alert.initStyle(StageStyle.UTILITY);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("Add note confirmation");
            alert.setHeaderText(null);

            ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Add");
            ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Don't Add");

            Optional<ButtonType> response = alert.showAndWait();
            if (response.get() == ButtonType.OK)
            {
                handleAddToNote();
            }
        }
    }

    public File getNoteSetFile()
    {
        return noteSetFile;
    }

    public NoteSetStatus getNoteSetStatus()
    {
        return noteSetStatus;
    }
}
