package sample.view.overview;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import sample.StorageManager;
import sample.model.Task;
import sample.util.ModelHelper;
import sun.jvm.hotspot.utilities.BitMap;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jamesnocentini on 25/10/15.
 */
class TaskTableCell extends TableCell<Task, String> {
    HBox hBox;
    Label title;
    ImageView imageView;
    CheckBox checkBox;
    javafx.beans.value.ChangeListener<Boolean> mListener;

    public TaskTableCell() {
        hBox = new HBox();
        hBox.setSpacing(10);

        title = new Label();
        imageView = new ImageView();
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);

        hBox.getChildren().addAll(imageView, title);
        HBox.setMargin(title, new Insets(15, 0, 15, 0));

        checkBox = new CheckBox();
        checkBox.setPrefHeight(20);
        StackPane stack = new StackPane();
        stack.getChildren().addAll(checkBox);
        stack.setAlignment(Pos.CENTER_RIGHT);

        hBox.getChildren().addAll(stack);
        HBox.setHgrow(stack, Priority.ALWAYS);

        setGraphic(hBox);
    }

    @Override
    protected void updateItem(String documentId, boolean empty) {
        if (documentId != null) {
            imageView.setImage(null);
            checkBox.setVisible(Boolean.TRUE);
            if (mListener != null) {
                checkBox.selectedProperty().removeListener(mListener);
            }

            Database database = StorageManager.getInstance().database;
            Document document = database.getDocument(documentId);
            Task task = ModelHelper.modelForDocument(document, Task.class);

            title.setText((String) document.getProperty("title"));

            Attachment attachment = document.getCurrentRevision().getAttachment("image");
            if (attachment != null) {
                Image image = null;
                try {
                    image = new Image(attachment.getContent());
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
                imageView.setImage(image);
            }

            checkBox.setSelected(task.getChecked());
            checkBox.selectedProperty().addListener(addListener(document));
        } else {
            if (empty) {
                title.setText("");
                imageView.setImage(null);
                checkBox.setVisible(Boolean.FALSE);
            }
        }
    }

    private javafx.beans.value.ChangeListener<Boolean> addListener(Document document) {
        mListener =  new javafx.beans.value.ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                changeCheck(document, newValue);
            }
        };
        return mListener;
    };

    private void changeCheck(Document document, Boolean isChecked) {
        System.out.println("checked");
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.putAll(document.getProperties());
        properties.put("checked", isChecked);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Calendar calendar = GregorianCalendar.getInstance();
        String currentTimeString = dateFormatter.format(calendar.getTime());

        properties.put("updated_at", currentTimeString);
        properties.put("user_id", "wayne");

        try {
            document.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}
