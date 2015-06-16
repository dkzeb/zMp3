package sample;

import com.mpatric.mp3agic.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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

    // fxml elements; tracklist and control buttons!
    @FXML private ListView<String> trackList;
    @FXML private Button stopBtn, startBtn, shuffleBtn, prevBtn, nextBtn;

    // ArrayList for songs, and tmp path for dev purposes! :)
    private ArrayList<Mp3File> soundFiles = new ArrayList<Mp3File>();
    private ArrayList<String> songTitles = new ArrayList<String>();
    private final String musicPath = "D:/MUSIK/";

    private boolean controlsEnabled = false;
    private boolean paused = true;

    private Mp3File initSelect = null;

    private String STATE = "INIT";

    private int trackIndex = 0;

    @FXML void initialize() {

        System.out.println("CONTROLLER INIT!");

        // try/catch for exceptions :) defensive programming FTW!
        try {
            // iterate/walk through all files in our path
            Files.walk(Paths.get(musicPath)).forEach(filePath -> {
                if (Files.isRegularFile(filePath) && filePath.toString().toLowerCase().contains("mp3")) {
                    // for each mp3 file - add to our array
                  try {
                      Mp3File mp3F = new Mp3File(filePath.toFile());
                      soundFiles.add(mp3F);
                      // now we need to get the track name and title!
                      String trackArtist = null;
                      String trackTitle = null;

                      // we need to find out what kind of tag the file has
                      if(mp3F.hasId3v1Tag()) {
                            ID3v1 t = mp3F.getId3v1Tag();
                            trackArtist = t.getArtist();
                            trackTitle = t.getTitle();
                      } else if(mp3F.hasId3v2Tag()) {
                          ID3v2 t = mp3F.getId3v2Tag();
                          trackArtist = t.getArtist();
                          trackTitle = t.getTitle();
                      } else if(mp3F.hasCustomTag()){
                          byte[] t = mp3F.getCustomTag();
                          trackArtist = t.toString();
                      }

                      // basic checks to see that artist and title has a value
                      // or we assign defaults to them

                      if(trackArtist == null || trackArtist.equals("")){
                          trackArtist = "Unknown";
                      }

                      if(trackTitle == null ||trackTitle.equals("")){
                          trackTitle = "Unknown Title";
                      }

                      if(trackArtist.equals("") && trackTitle.equals("")){
                          trackArtist = mp3F.getFilename();
                      }

                      // we add the title to our song titles list!
                      songTitles.add(trackArtist+" - "+trackTitle);
                   // exception catches :) DefProg 4tw;
                  } catch (UnsupportedTagException e){
                    e.printStackTrace();
                 } catch (InvalidDataException e){
                    e.printStackTrace();
                 } catch (IOException e){
                    e.printStackTrace();
                 }
                }
            });

            // populate tracklist, we create an observable list from our ArrayList
            ObservableList<String> oSoundFiles = FXCollections.observableArrayList(songTitles);
            // if the trackList control actually exists and can be found
            if(trackList != null) {
                // we set the items of the trackList to our observable list
                trackList.setItems(oSoundFiles);

                System.out.println(trackList.itemsProperty().toString());

                // setup listview
                trackList.setOnMouseClicked(new EventHandler<MouseEvent>() {

                    @Override
                    public void handle(MouseEvent event) {
                        int selected = trackList.getSelectionModel().getSelectedIndex();
                        Mp3File selectedFile = soundFiles.get(selected);
                        trackIndex = selected;
                        if (event.getClickCount() == 2 && !event.isConsumed()) {
                            if (Main.isPlaying()) {
                                Main.getMP().stop();
                            }
                            event.consume();
                            Main.setSelectedAndPlay(selectedFile);
                        } else {
                            Main.setSelected(selectedFile);
                        }
                    }
                });


            }
        } catch (IOException e){
            e.printStackTrace();
        }

        // setup controls! stopBtn, startBtn, shuffleBtn, prevBtn, nextBtn;
       /* if(stopBtn != null){
            stopBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println("CLICK!");
                }
            });
        }*/
        stopBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Main.getMP().stop();
                STATE = "STOPPED";
                Mp3File f = soundFiles.get(trackIndex);
                if(f == null){
                    initSelect = null;
                } else {
                    Main.setSelected(f);
                }


                paused = true;
            }
        });

        startBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(initSelect == null || STATE.equals("INIT")){
                    initSelect = soundFiles.get(0);
                    STATE = "PLAYING";
                    Main.setSelectedAndPlay(initSelect);
                    paused = false;
                } else if(Main.isPlaying() && !paused){
                    STATE = "PLAYING";
                    Main.pausePlayer();
                    paused = true;
                    System.out.println("PAUSE!");
                } else if(paused) {
                    Main.unpausePlayer();
                    STATE = "PAUSE";
                    paused = false;
                    System.out.println("UNPAUSE!");
                } else if(STATE.equals("STOPPED")){
                    Main.getMP().play();
                    STATE.equals("PLAYING");
                }
            }
        });

        nextBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int nxt = trackIndex+1;
                trackIndex = nxt;
                if(STATE.equals("STOPPED")){
                    Main.setSelected(soundFiles.get(trackIndex));
                } else {
                    Main.getMP().stop();
                    Main.setSelectedAndPlay(soundFiles.get(trackIndex));
                    STATE = "PLAYING";
                }
            }
        });

        prevBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int nxt = trackIndex-1;
                if(nxt < 0)
                    nxt = soundFiles.size()-1;

                trackIndex = nxt;
                if(STATE.equals("STOPPED")){
                    Main.setSelected(soundFiles.get(trackIndex));
                } else {
                    Main.getMP().stop();
                    Main.setSelectedAndPlay(soundFiles.get(trackIndex));
                    STATE = "PLAYING";
                }
            }
        });

    }

    // helper function to id tags in mp3 files!
    private String idTag(Mp3File f){
        String tag = "";
        if(f.hasId3v1Tag()){
            tag = "id3v1";
        } else if(f.hasId3v2Tag()){
            tag = "id3v2";
        } else if(f.hasCustomTag()){
            tag = "custom";
        }

        return tag;

    }

}
