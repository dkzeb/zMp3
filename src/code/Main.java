package code;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;


public class Main extends Application {

    private static boolean bIsPlaying = false;
    private static boolean bIsPaused = false;
    private static boolean bIsStopped = false;

    private static MediaPlayer mediaPlayer;

    private static Mp3File selected;

    private static Duration currDurr;

    public static final String[] libDir = {"D:/MUSIK/TEST1/", "D:/MUSIK/TEST2/"};

    private static String layoutMode = "none";
    private static String fullscreenMode = "no";
    private static Timer timeTracker;

    private Stage fxStage;
    private static Scene fxScene;


    // KeyListener for the F10 - fullscreen keymode
    private EventHandler<KeyEvent> keyListener = new EventHandler<KeyEvent>(){
        @Override
        public void handle(KeyEvent event) {
            if(event.getCode() == KeyCode.F10) {
                if(fullscreenMode.equals("no")){
                    fxStage.setFullScreen(true);
                    fullscreenMode = "yes";
                } else if(fullscreenMode.equals("yes")){
                    fxStage.setFullScreen(false);
                    fullscreenMode = "no";
                }
                event.consume();
            }
        }
    };

    // init function - this is used to listen for our startup params,
    // layout mode and fullscreen switches!
    @Override
    public void init() throws Exception{
        super.init();
        Map<String, String> params = getParameters().getNamed();

        for(Map.Entry<String,String> entry : params.entrySet()){
            if(entry.getKey().equals("layoutMode")){
                layoutMode = entry.getValue();
            }

            if(entry.getKey().equals("fullscreen")){
                fullscreenMode = entry.getValue();
            }
        }

    }

    @Override
    public void start(Stage stage) throws Exception{

        System.out.println("MAIN INIT!");

        timeTracker = new Timer();
        Parent root;
        // Load our Layout, create the scene and set titles and visibility
        if(layoutMode.equals("pane")) {
            root = FXMLLoader.load(getClass().getResource("/res/paneLayout.fxml"));
        } else if(layoutMode.equals("split")){
            root = FXMLLoader.load(getClass().getResource("/res/split_view.fxml"));
        } else {
            root = FXMLLoader.load(getClass().getResource("/res/layout.fxml"));
        }
        root.getStylesheets().add("/res/style.css");

        root.setOnKeyPressed(keyListener);

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("zMp3");
        stage.setScene(scene);

        fxScene = scene;

        if(fullscreenMode.equals("yes"))
            stage.setFullScreen(true);

        fxStage = stage;
        stage.show();


    }

    public static Scene getScene(){
        return fxScene;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void setSelectedAndPlay(Mp3File f){
            selected = f;
            System.out.println("Selected: "+selected);
            playSong();
    }

    public static void createPlayer(Mp3File file){
        File f = new File(file.getFilename());
        mediaPlayer = new MediaPlayer(new Media(f.toURI().toString()));
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
            Media media = new Media(f.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnPaused(new Runnable() {
                @Override
                public void run() {
                    bIsPaused = true;
                    bIsPlaying = false;
                    bIsStopped = false;
                }
            });

            mediaPlayer.setOnPlaying(new Runnable() {
                @Override
                public void run() {
                    bIsPaused = false;
                    bIsPlaying = true;
                    bIsStopped = false;

                }
            });

            mediaPlayer.setOnStopped(new Runnable() {
                @Override
                public void run() {
                    bIsPaused = false;
                    bIsPlaying = false;
                    bIsStopped = true;
                }
            });
            mediaPlayer.play();
        }
    }

    public static void pausePlayer(){
        currDurr = mediaPlayer.getCurrentTime();
        mediaPlayer.pause();
    }


    public static String getTime(){
        if(mediaPlayer != null) {
            currDurr = mediaPlayer.getCurrentTime();
            return formatTime(currDurr, mediaPlayer.getMedia().getDuration());
        } else {
            return "00:00/00:00";
        }

    }

    public static void unpausePlayer(){
        mediaPlayer.setStartTime(currDurr);
        mediaPlayer.play();
    }

    public static boolean isPlaying(){
        return bIsPlaying;
    }
    public static boolean isPaused() { return bIsPaused; }
    public static boolean isStopped() { return bIsStopped; }

    public static MediaPlayer getMP(){
        return mediaPlayer;
    }

    public static String getMediaInfo(String type){
        String toReturn = "";
        if(selected != null) {
            if (type.equals("artTrk")) {
                ID3v2 tag = selected.getId3v2Tag();
                if (tag != null) {
                    if (tag.getArtist() == null && tag.getTitle() == null) {
                        toReturn = selected.getFilename();
                    } else if (tag.getArtist().equals("") || tag.getTitle().equals("")) {
                        toReturn = selected.getFilename();
                    } else {
                        toReturn = tag.getArtist() + " - " + tag.getTitle();
                    }
                }
            } else if (type.equals("album")) {
                ID3v2 tag = selected.getId3v2Tag();
                if (tag != null) {
                    toReturn = tag.getAlbum();
                }
            } else if (type.equals("time")) {
                toReturn = mediaPlayer.getCurrentTime().toString();
            }
            return toReturn;
        } else {
            return null;
        }
    }

    public static Image getAlbumArt(){
        Image albumImg = null;
        if(selected != null) {
            ID3v2 tag = selected.getId3v2Tag();
            if(tag.getAlbumImage() != null){
                byte[] imgBytes = tag.getAlbumImage();
                InputStream in = new ByteArrayInputStream(imgBytes);
                albumImg = new Image(in);
            } else {
                albumImg = new Image("/res/no_art.png");
            }
        }

        return albumImg;
    }


    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;

            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds,
                        durationMinutes, durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d",
                        elapsedMinutes, elapsedSeconds);
            }
        }
    }

}
