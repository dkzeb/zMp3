package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main extends Application {

    private static boolean bIsPlaying = false;
    private static Media media;
    private static MediaPlayer mediaPlayer;
    private StackPane root;
    private BorderPane borderP;

    private ArrayList<File> soundFiles = new ArrayList<File>();

    private final String musicPath = "D:/MUSIK/";

    private static File selected;


    @Override
    public void start(Stage stage) throws Exception{


        System.out.println("MAIN INIT!");
        /*media = new Media("file:///D:/MUSIK/Larry%20Graham-%20I%20Feel%20Good%20(1982).mp3");
        mediaPlayer = new MediaPlayer(media);
        */
        Parent root = FXMLLoader.load(getClass().getResource("layout.fxml"));
        Scene scene = new Scene(root, 800, 600);



        stage.setTitle("TouchMediaPlayer");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static void setSelectedAndPlay(File f){
            selected = f;
            playSong();
    }

    public static void setSelected(File f){
          selected = f;
    }

    private static void playSong(){

        // hent den første fil i listen!
        if(selected != null){
            bIsPlaying = true;
            File toPlay = selected;
            media = new Media(toPlay.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        }
    }

    public static boolean isPlaying(){
        return bIsPlaying;
    }

    public static MediaPlayer getMP(){
        return mediaPlayer;
    }

}
