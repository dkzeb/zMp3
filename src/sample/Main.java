package sample;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;


import java.io.File;
import java.util.Timer;
import java.util.TimerTask;


public class Main extends Application {

    private static boolean bIsPlaying = false;
    private static MediaPlayer mediaPlayer;

    private static Mp3File selected;

    private static Duration currDurr;


    private static Timer timeTracker;

    @Override
    public void start(Stage stage) throws Exception{

        System.out.println("MAIN INIT!");

        timeTracker = new Timer();

        // Load our Layout, create the scene and set titles and visibility
        Parent root = FXMLLoader.load(getClass().getResource("layout.fxml"));
        Label timerLabel = (Label) root.lookup("timeText");
        if(timerLabel != null){
            timerLabel.setText("ITS TIME BABY!");
        }
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("zMp3");
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
            Media media = new Media(f.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnEndOfMedia(new Runnable(){
                @Override
                public void run(){
                    UIController.playNxtTrack();
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
        if(mediaPlayer != null && isPlaying()) {
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

    public static MediaPlayer getMP(){
        return mediaPlayer;
    }

    public static String getMediaInfo(String type){
        String toReturn = "";
        if(type.equals("artTrk")){
            ID3v2 tag = selected.getId3v2Tag();
            if(tag != null){
                toReturn = tag.getArtist()+" - "+tag.getTitle();
            }
        } else if(type.equals("album")){
            ID3v2 tag = selected.getId3v2Tag();
            if(tag != null){
                toReturn = tag.getAlbum();
            }
        } else if(type.equals("time")){
            toReturn = mediaPlayer.getCurrentTime().toString();
        }
        return toReturn;
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
