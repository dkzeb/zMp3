package sample;

import com.mpatric.mp3agic.Mp3File;
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
import javafx.util.Duration;
import jdk.nashorn.internal.runtime.URIUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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

    private static Mp3File selected;

    private static Duration currDurr;

    @Override
    public void start(Stage stage) throws Exception{


        System.out.println("MAIN INIT!");

        // Load our Layout, create the scene and set titles and visibility
        Parent root = FXMLLoader.load(getClass().getResource("layout.fxml"));
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("TouchMediaPlayer");
        stage.setScene(scene);
        //stage.setFullScreen(true);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


    public static void setSelectedAndPlay(Mp3File f){
            selected = f;
            System.out.println("Selected: "+selected);
            playSong();
    }


    public static void setSelected(Mp3File f){
         selected = f;
        System.out.println("Selected: "+selected);
    }

    private static void playSong(){
        // check to see that a
        if(selected != null){
            bIsPlaying = true;
            Mp3File toPlay = selected;
            File f = new File(toPlay.getFilename());
            media = new Media(f.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        }
    }

    public static void pausePlayer(){
        currDurr = mediaPlayer.getCurrentTime();
        mediaPlayer.pause();
    }

    public static void unpausePlayer(){
        mediaPlayer.setStartTime(currDurr);
        mediaPlayer.play();
    }

    public static boolean isPlaying(){
        return bIsPlaying;
    }

    public static MediaPlayer getMP(){
        return mediaPlayer;
    }

}
