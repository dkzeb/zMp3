import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class DnDListView extends Application {

    private int counter = 0 ;

    private final ObjectProperty<ListCell<String>> dragSource = new SimpleObjectProperty<>();

    @Override
    public void start(Stage primaryStage) {
        populateStage(primaryStage);
        primaryStage.show();

        Stage anotherStage = new Stage();
        populateStage(anotherStage);
        anotherStage.setX(primaryStage.getX() + 300);
        anotherStage.show();
    }

    private void populateStage(Stage stage) {
        ListView<String> listView = new ListView<>();
        for (int i=0; i<5; i++ ) {
            listView.getItems().add("Item "+(++counter));
        }

        listView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>(){
                @Override
                public void updateItem(String item , boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                }
            };

            cell.setOnDragDetected(event -> {
                if (! cell.isEmpty()) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(cell.getItem());
                    db.setContent(cc);
                    dragSource.set(cell);
                }
            });

            cell.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
            });

            cell.setOnDragDone(event -> listView.getItems().remove(cell.getItem()));

            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString() && dragSource.get() != null) {
                    // in this example you could just do
                    // listView.getItems().add(db.getString());
                    // but more generally:

                    ListCell<String> dragSourceCell = dragSource.get();
                    listView.getItems().add(dragSourceCell.getItem());
                    event.setDropCompleted(true);
                    dragSource.set(null);
                } else {
                    event.setDropCompleted(false);
                }
            });

            return cell ;
        });

        BorderPane root = new BorderPane(listView);
        Scene scene = new Scene(root, 250, 450);
        stage.setScene(scene);

    }

    public static void main(String[] args) {
        launch(args);
    }
}