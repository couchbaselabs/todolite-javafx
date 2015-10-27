package sample.view.overview;

import com.couchbase.lite.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import sample.Main;
import sample.StorageManager;
import sample.model.List;
import sample.model.Profile;
import sample.model.Task;
import sample.util.ModelHelper;
import sample.view.BaseController;
import sample.view.NewListController;
import sample.view.NewTaskController;

import javax.xml.crypto.Data;
import java.io.IOException;


/**
 * Created by jamesnocentini on 18/10/15.
 */
public class OverviewController extends BaseController {

    /**
     * The list of lists
     */
    @FXML
    private TableView<List> listTableView;
    @FXML
    private TableColumn<List, String> listColumn;

    /**
     * The list of tasks
     */
    @FXML
    private TableView<Task> taskTableView;
    @FXML
    private TableColumn<Task, String> taskColumn;

    /**
     * The list of profiles
     */
    @FXML
    private TableView<Profile> profileTableView;
    @FXML
    private TableColumn<Profile, String> profileColumn;
    @FXML
    private ListView<Profile> profileListView;


    /**
     * The data as an observable list of Tasks.
     */
    private ObservableList<Task> taskData = FXCollections.observableArrayList();

    private ObservableList<Profile> profileData = FXCollections.observableArrayList();
    /**
     * Returns the data as an observable list of Tasks.
     */
    public ObservableList<Task> getTaskData() {
        return taskData;
    }

    public ObservableList<Profile> getProfileData() {
        return profileData;
    }

    private LiveQuery taskQuery;
    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public OverviewController() {
    }

    /**
     * Initializes the controller class. This method is automatically called after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize the person table with the two columns.
        listColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));

        // Listen for selection changes and show the list details.
        listTableView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> showListDetails(newValue)));

        listTableView.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                Database database = StorageManager.getInstance().database;
                Query tasksInSelectedList = Task.getQuery(database, selectedList.getDocumentId());
                QueryEnumerator enumerator = null;
                try {
                    enumerator = tasksInSelectedList.run();
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
                for (QueryRow row : enumerator) {
                    try {
                        row.getDocument().delete();
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                    }
                }
                Document listDocument = database.getDocument(selectedList.getDocumentId());
                try {
                    listDocument.delete();
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        });

        taskColumn.setCellValueFactory(new PropertyValueFactory<Task, String>("documentId"));

        taskColumn.setCellFactory(new Callback<TableColumn<Task, String>, TableCell<Task, String>>() {
            @Override
            public TableCell<Task, String> call(TableColumn<Task, String> param) {
                TableCell<Task, String> cell = new TaskTableCell();
                return cell;
            }
        });
        taskColumn.getTableView().addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                Task selectedTask = taskTableView.getSelectionModel().getSelectedItem();
                Document document = StorageManager.getInstance().database.getDocument(selectedTask.getDocumentId());
                try {
                    document.delete();
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        });

//        taskTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> changeCheck(newValue));
        profileListView.setCellFactory(CheckBoxListCell.forListView(new Callback<Profile, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(Profile profile) {
                List list = selectedList;
                Boolean isUserInList = list.getMembers().contains(profile.getDocumentId());
                Boolean isUserOwner = list.getOwner().equals(profile.getDocumentId());
                System.out.println(profile.getDocumentId());
                System.out.println(list.getOwner());

                BooleanProperty observable = new SimpleBooleanProperty();
                if (isUserOwner) {
                    observable.setValue(isUserOwner);
                } else {
                    observable.setValue(isUserInList);
                }
                observable.addListener((observable1, oldValue, newValue) -> {
                    System.out.println(selectedList);
                    Boolean isUserSelectedAlreadyMember = selectedList.getMembers().contains(profile.getDocumentId());
                    System.out.println(selectedList.getMembers());
                    if (isUserSelectedAlreadyMember) {
                        selectedList.getMembers().remove(profile.getDocumentId());
                    } else {
                        selectedList.getMembers().add(profile.getDocumentId());
                    }
                    ModelHelper.save(StorageManager.getInstance().database, selectedList);
                    System.out.println(selectedList);
                });

                return observable;
            }
        }));

//        profileColumn.setCellValueFactory(cellData -> {
//            List list = listTableView.getSelectionModel().getSelectedItem();
//            Boolean isUserInList = list.getMembers().contains(cellData.getValue().getDocumentId());
//            if (isUserInList) {
//                return new SimpleStringProperty("x    " + cellData.getValue().getName());
//            }
//            return new SimpleStringProperty("      " + cellData.getValue().getName());
//        });
//

    }

    public void setData(ObservableList<List> data) {
        listTableView.setItems(data);
    }

    public List selectedList;

    /**
     * Fills all text fields to show details about the list.
     * If the specified list is null, all text fields are cleared.
     *
     * @param list the list or null
     */
    private void showListDetails(List list) {
        if (list != null) {
            selectedList = list;
            String listId = list.getDocumentId();

            taskTableView.refresh();
            taskData.clear();
            taskTableView.setItems(getTaskData());
            startTaskLiveQueryForList(listId);
            profileListView.refresh();
            profileData.clear();
            profileListView.setItems(getProfileData());
            startProfilesLiveQuery();

        } else {
            // List is null, remove all text
        }
    }

    private void startProfilesLiveQuery() {
        LiveQuery liveQuery = Profile.getQuery(StorageManager.getInstance().database, "wayne").toLiveQuery();
        QueryEnumerator enumerator = null;
        try {
            enumerator = liveQuery.run();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        for (QueryRow row : enumerator) {
            Document document = row.getDocument();
            Profile profile = ModelHelper.modelForDocument(document, Profile.class);
            profileData.add(profile);
        }
        liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent changeEvent) {
                System.out.println("update");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        QueryEnumerator enumerator = null;
                        try {
                            enumerator = liveQuery.run();
                        } catch (CouchbaseLiteException e) {
                            e.printStackTrace();
                        }
                        profileData.clear();
                        for (QueryRow row : enumerator) {
                            Document document = row.getDocument();
                            Profile profile = ModelHelper.modelForDocument(document, Profile.class);
                            profileData.add(profile);
                        }
                    }
                });

            }
        });
    }

    private void startTaskLiveQueryForList(String listId) {
        if (taskQuery != null) {
            taskQuery = null;
        }
        LiveQuery liveQuery = Task.getQuery(StorageManager.getInstance().database, listId).toLiveQuery();
        QueryEnumerator enumerator = null;
        try {
            enumerator = liveQuery.run();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        for(QueryRow row : enumerator) {
            Document document = row.getDocument();
            Task task = ModelHelper.modelForDocument(document, Task.class);
            taskData.add(task);
        }
        liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent changeEvent) {
                System.out.println("update");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        QueryEnumerator enumerator = changeEvent.getRows();
                        taskData.clear();
                        for(QueryRow row : enumerator) {
                            Document document = row.getDocument();
                            Task task = ModelHelper.modelForDocument(document, Task.class);
                            System.out.println(task.getTitle());
                            taskData.add(task);
                        }
                    }
                });

            }
        });
    }

    /**
     * Called when the user clicks the new button. Opens a dialog to edit details for a new list.
     */
    @FXML
    private void handleNewList() {
        List tempList = new List();
        showListEditDialog(tempList);
    }

    @FXML
    private void handleNewTask() {
        showNewTaskDialog(selectedList);
    }

    private void shareWithUser(Profile profile) {
        List list = listTableView.getSelectionModel().getSelectedItem();
        Boolean isUserSelectedAlreadyMember = list.getMembers().contains(profile.getDocumentId());
        if (isUserSelectedAlreadyMember) {
            selectedList.getMembers().remove(profile.getDocumentId());
        } else {
            selectedList.getMembers().add(profile.getDocumentId());
        }
        ModelHelper.save(StorageManager.getInstance().database, selectedList);
    }

    /**
     * Opens a dialog to edit details for the specified person. If the user
     * clicks OK, the changes are saved into the provided person object and true
     * is returned.
     *
     * @param list the person object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showListEditDialog(List list) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/NewListDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit List");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the person into the controller.
            NewListController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setList(list);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean showNewTaskDialog(List list) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/NewTask.fxml"));
        AnchorPane page = null;
        try {
            page = (AnchorPane) loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("New Task");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(mainApp.getPrimaryStage());
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        NewTaskController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setList(list);

        dialogStage.showAndWait();
        return controller.isOkClicked();
    }


}
