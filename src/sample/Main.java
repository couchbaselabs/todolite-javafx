package sample;

import com.couchbase.lite.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import sample.model.List;
import sample.model.Task;
import sample.util.ModelHelper;
import sample.view.overview.OverviewController;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class Main extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    /**
     * The data as an observable list of Lists.
     */
    private ObservableList<List> listData = FXCollections.observableArrayList();

    /**
     * Constructor
     */
    public Main() {
    }

    /**
     * Returns the data as an observable list of Lists.
     * @return
     */
    public ObservableList<List> getListData() {
        return listData;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;

        StorageManager.getInstance().startReplications();

        LiveQuery liveQuery = List.getQuery(StorageManager.getInstance().database).toLiveQuery();
        QueryEnumerator enumerator = liveQuery.run();
        for(QueryRow row : enumerator) {
            Document document = row.getDocument();
            List list = ModelHelper.modelForDocument(document, List.class);
            listData.add(list);
        }
        liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent changeEvent) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        QueryEnumerator enumerator = null;
                        try {
                            enumerator = liveQuery.run();
                        } catch (CouchbaseLiteException e) {
                            e.printStackTrace();
                        }
                        System.out.println(toDoOverviewController.selectedList);
                        // diff between

                        listData.clear();
                        System.out.println(toDoOverviewController.selectedList);
                        for(QueryRow row : enumerator) {
                            Document document = row.getDocument();
                            List list = ModelHelper.modelForDocument(document, List.class);
                            listData.add(list);
                        }
                        System.out.println(toDoOverviewController.selectedList);
                    }
                });

            }
        });

        initRootLayout();

        showToDoOverview();

        createUserProfile();


    }

    /**
     * Initializes the root layout
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/sample/view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout
            Scene scene = new Scene(rootLayout);
            scene.getStylesheets().add("styles.css");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    OverviewController toDoOverviewController;
    /**
     * Show the todo overview inside the root layout
     */
    public void showToDoOverview() {
        try {
            // Load ToDo overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/sample/view/overview/Overview.fxml"));
            AnchorPane toDoOverview = (AnchorPane) loader.load();

            // Set the todo overview into the center of root layout.
            rootLayout.setCenter(toDoOverview);

            // Give the controller access to the main app.
            toDoOverviewController = loader.getController();
            toDoOverviewController.setMainApp(this);
            toDoOverviewController.setData(getListData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createUserProfile() {
        Map<String, Object> properties = new HashMap<String, Object>();

        properties.put("name", "Wayne Carter");
        properties.put("type", "profile");
        properties.put("user_id", "wayne");

        Document document = StorageManager.getInstance().database.getDocument("p:wayne");
        try {
            document.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void startCouchbase() {
        JavaContext context = new JavaContext();
        Manager manager = null;
        try {
            manager = new Manager(context, Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Database database = null;
        try {
            database = manager.getDatabase("myapp");
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        Map<String, Object> properties = new HashMap<String, Object>();

        properties.put("session", "Couchbase Mobile");
        properties.put("conference", "JavaOne");

        Document document = database.createDocument();
        try {
            document.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}
