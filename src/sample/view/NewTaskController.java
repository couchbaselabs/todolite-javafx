package sample.view;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sample.StorageManager;
import sample.model.List;
import sample.model.Task;
import sample.util.ModelHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by jamesnocentini on 26/10/15.
 */
public class NewTaskController extends BaseController {

    @FXML
    private TextField taskTitleField;

    private List list;
    private Stage dialogStage;
    private Boolean okClicked = false;

    /**
     * Initializes the controller class. This method is automatically called after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the list to be edited in the dialog.
     *
     * @param list
     */
    public void setList(List list) {
        this.list = list;
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     *
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        System.out.println("create a new task");

        Task newTask = new Task();
        newTask.setChecked(Boolean.FALSE);
        newTask.setTitle(taskTitleField.getText());
        newTask.setListId(list.getDocumentId());
        newTask.setType("task");
        newTask.setUserId("wayne");

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Calendar calendar = GregorianCalendar.getInstance();
        String currentTimeString = dateFormatter.format(calendar.getTime());

        newTask.setCurrentTimeString(currentTimeString);
        newTask.setUpdatedAt(currentTimeString);

        ModelHelper.save(StorageManager.getInstance().database, newTask);

        dialogStage.close();
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

}
