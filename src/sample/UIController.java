package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by sthaz_000 on 15-06-2015.
 */
public class UIController {

    @FXML private ListView<File> trackList;

    private ArrayList<File> soundFiles = new ArrayList<File>();

    private final String musicPath = "D:/MUSIK/";


    @FXML void initialize() {

        System.out.println("CONTROLLER INIT!");
        try {
            Files.walk(Paths.get(musicPath)).forEach(filePath -> {
                if (Files.isRegularFile(filePath) && filePath.toString().toLowerCase().contains("mp3")) {
                    //System.out.println("Mp3 FIL: " + filePath);
                    soundFiles.add(filePath.toFile());
                }
            });

            ObservableList<File> oSoundFiles = FXCollections.observableArrayList(soundFiles);
            if(trackList != null) {
                trackList.setItems(oSoundFiles);

                // setup listview
                trackList.setOnMouseClicked(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        File selected = trackList.getSelectionModel().getSelectedItem();



                        if(event.getClickCount() == 2 && !event.isConsumed()) {
                            if(Main.isPlaying()){
                                Main.getMP().stop();
                            }
                            event.consume();
                            Main.setSelectedAndPlay(selected);
                        } else {
                            Main.setSelected(selected);
                        }
                    }
                });


            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
