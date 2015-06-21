package code;

import com.mpatric.mp3agic.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class UIControllerPlaylist {

    // fxml elements; tracklist and control buttons!
    @FXML private ListView<String> libList, playList;
    @FXML private Button stopBtn, startBtn, prevBtn, shuffleBtn, nextBtn;
    @FXML private Label artistTitleText, albumText;
    @FXML private ImageView albumArt;
    @FXML private Slider volumeSlider;

    // ArrayList for songs, and tmp path for dev purposes! :)
    private ArrayList<String> libSongTitles = new ArrayList<String>();
    private static ArrayList<Mp3File> libSoundFiles = new ArrayList<Mp3File>();

    // playlist arrays, for titles and corresponding index
    private ArrayList<String> playListTitles = new ArrayList<String>();
    private ArrayList<Integer> playListTrackIndex = new ArrayList<Integer>();


    private final String musicPath = "D:/MUSIK/";

    private boolean paused = true;

    private Mp3File initSelect = null;

    private String STATE = "INIT";

    private static int trackIndex = 0;
    private static int totalNrOfTracks = 0;
    private static boolean shuffleMode = false;


    private Integer draggedIndex = null;

    private final SimpleObjectProperty<ListCell<String>> dragSource = new SimpleObjectProperty<>();

    // runnable for the endOfMedia event
    private Runnable endOfMedia = new Runnable(){
        @Override
        public void run(){
            playNxtTrack(); //DONE: implement the playNextTrack method
            setupTrackInfo();
        }
    };

    @FXML void initialize() {

        System.out.println("CONTROLLER INIT!");

        artistTitleText.setText("No Track Selected");
        albumText.setText("");

        // button graphics and events
        if(prevBtn != null && startBtn != null && nextBtn != null && stopBtn != null && shuffleBtn != null) {
            // graphics bindings TODO: move all of this to CSS
            prevBtn.setStyle("-fx-background-image: url('/res/prev_icon.png'); -fx-padding: 5px; -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-size: 65% auto");
            startBtn.setStyle("-fx-background-image: url('/res/playpause_icon.png'); -fx-padding: 5px; -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-size: 65% auto");
            nextBtn.setStyle("-fx-background-image: url('/res/next_icon.png'); -fx-padding: 5px; -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-size: 65% auto");
            stopBtn.setStyle("-fx-background-image: url('/res/stop_icon.png'); -fx-padding: 5px; -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-size: 65% auto");
            shuffleBtn.setStyle("-fx-background-image: url('/res/shuffle_icon.png');  -fx-padding: 5px; -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-background-size: 65% auto");


            // the next button is the simplest of our button events!
            nextBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    playNxtTrack();
                }
            });

            prevBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    playPrevTrack();
                }
            });

            startBtn.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event){
                    // the start button acts as a play/pause button
                    // is we are playing - pause
                    if(Main.isPlaying())
                        Main.getMP().pause();

                    if(Main.isPaused())
                        Main.getMP().play();

                    if(Main.isStopped()){
                        // TODO: this should def. be fixed
                        Main.setSelectedAndPlay(libSoundFiles.get(trackIndex));
                    }
                }
            });

            stopBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if(Main.isPlaying())
                        Main.getMP().stop();
                }
            });

        }

        // CellFactory for drag and drop
        libList.setCellFactory(lv -> {
                    ListCell<String> cell = new ListCell<String>() {
                        @Override
                        public void updateItem(String item, boolean empty) {
                            if(item != null) {
                                super.updateItem(item, empty);
                                setText(item);
                                //System.out.println("Added " + item);
                            }
                        }
                    };

                    // Start the drag events and setup mode of transfer and clipboards!
                    // we use a semi dirty hack with dragSource to ensure that the source is
                    // kept across multiple panes/windows

                    cell.setOnDragDetected(event -> {
                        if(!cell.isEmpty()){
                            libList.getSelectionModel().select(cell.getIndex());
                            // the cell is not empty - start a drag event
                            Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
                            ClipboardContent cc = new ClipboardContent();
                            cc.putString(cell.getItem());
                            draggedIndex = cell.getIndex();
                            db.setContent(cc);
                            dragSource.set(cell);
                        }
                    });

                    // return the newly made cell from the factory (this means add it to the library!
                    return cell;
                }
        );

        // starts out like the cell factory above but uses more drag events to enable the drag and drop
        playList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    if(item != null) {
                        super.updateItem(item, empty);
                        setText(item);
                    }
                }
            };

            cell.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    event.acceptTransferModes(TransferMode.COPY);
                }
            });

            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                System.out.println(db.getString()+" dsource: "+dragSource.get());
                if (db.hasString() && dragSource.get() != null) {
                    // in this example you could just do
                    // listView.getItems().add(db.getString());
                    // but more generally:

                    //System.out.println(db.getString());
                    System.out.println("DRAG DROPPED");
                    ListCell<String> dragSourceCell = dragSource.get();

                    if(playList.getItems().get(0).equals("No Tracks Loaded")){
                        playList.getItems().remove(0);
                        Main.setSelectedAndPlay(libSoundFiles.get(draggedIndex));
                        setupTrackInfo();
                    }

                    playList.getItems().add(dragSourceCell.getItem());
                    // add our dragged index and the nullify
                    playListTrackIndex.add(draggedIndex);
                    draggedIndex = null;

                    event.setDropCompleted(true);
                    dragSource.set(null);
                } else {
                    event.setDropCompleted(false);
                    draggedIndex = null;
                }
            });

            return cell;
        });
        // END OF DRAG AND DROP

        // since our playlist is empty, we'll create a dummy entry with the text "no tracks loaded"
        playList.getItems().add("No Tracks Loaded");

        // LIBRARY LOAD
        // we get the libDir from main (where we will later load and save to an XML file or similar settings file.
        // we then walk through each entry in the libDir array and load the files into our libSongList array
        for(String path : Main.libDir){
            try {
                Files.walk(Paths.get(path)).forEach(filePath -> {
                    // if the file we have walked to is an Mp3 file
                    if (Files.isRegularFile(filePath) && filePath.toString().toLowerCase().contains("mp3")) {
                        try {
                            // create a new file from the path we have walked
                            Mp3File f = new Mp3File(filePath.toFile());
                            // add the file to our libSoundFiles
                            libSoundFiles.add(f);
                            // give it a name to correspond with our libSongTitle

                            // we make null temp strings
                            String trackArtist = null;
                            String trackTitle = null;
                            // check if the file has one of three types of tags
                            // and assign artist and title strings
                            if(f.hasId3v1Tag()) {
                                ID3v1 t = f.getId3v1Tag();
                                trackArtist = t.getArtist();
                                trackTitle = t.getTitle();
                            } else if(f.hasId3v2Tag()) {
                                ID3v2 t = f.getId3v2Tag();
                                trackArtist = t.getArtist();
                                trackTitle = t.getTitle();
                            } else if(f.hasCustomTag()){
                                byte[] t = f.getCustomTag();
                                trackArtist = t.toString();
                            }

                            // in the event that both artist and title is empty
                            // assign the filename as name for the file
                            if(trackArtist.equals("") && trackTitle.equals("")){
                                trackArtist = f.getFilename();
                            }

                            // now we check if either string is null, if so assign a default value
                            if(trackArtist == null || trackArtist.equals(""))
                                trackArtist = "Unknown Artist";
                            if(trackTitle == null || trackTitle.equals(""))
                                trackTitle = "Unknown Title";

                            // we then add the artist and title to the libSongTitles array
                            // and count up the number of tracks!

                            libSongTitles.add(trackArtist+" - "+trackTitle);

                        // exceptions that could be thrown by the files
                        } catch (UnsupportedTagException e){
                            e.printStackTrace();
                        } catch (InvalidDataException e){
                            e.printStackTrace();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        // the library lists have been loaded with files
        // now we load these into our library list view
        // we begin by createing an observable arraylist from the libSongTitles
        ObservableList<String> oLibSongTitles = FXCollections.observableArrayList(libSongTitles);

        // does the libList actually exist?
        if(libList != null) {
            // add the song titles
            libList.setItems(oLibSongTitles);

            // now we set up mouse events for the library list
            // when we double click we want to add a song to our playlist
            // if the playlist is empty - then we remove the stub item on the playlist,
            // add the selected song, and begin playing it.


            libList.setOnTouchPressed(new EventHandler<TouchEvent>() {
                @Override
                public void handle(TouchEvent event) {
                    System.out.println("YOU TOUCHED ME!");
                    if(!event.isConsumed()){

                       System.out.println("You touched "+libList.getSelectionModel().getSelectedIndex());
                    }
                }
            });

            libList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {

                    // we double clicked an item
                    if(event.getClickCount() == 2 && !event.isConsumed()){
                        // get the index of track in our library
                        int selectedIndex = libList.getSelectionModel().getSelectedIndex();
                        // get the artist and title from our library titles list
                        String selectedTitle = libSongTitles.get(selectedIndex);

                        // this if statement checks if the default stub (no tracks loaded) is still
                        // present in the list - if it is - remove it and auto play since this is
                        // the first track on the list available.
                        if(playList.getItems().get(0).equals("No Tracks Loaded")){
                            playList.getItems().remove(0);
                            Main.setSelectedAndPlay(libSoundFiles.get(selectedIndex));
                        }

                        // add the track to the playList
                        playList.getItems().add(selectedTitle);
                        playListTrackIndex.add(selectedIndex);

                        // set title and album texts
                        setupTrackInfo();

                        // for end of media we use our default endOfMedia runnable that we created
                        // in the top of the class - listed in the fieldVars
                        Main.getMP().setOnEndOfMedia(endOfMedia);

                        // count up our total number of tracks
                        totalNrOfTracks = totalNrOfTracks+1;
                        event.consume();

                    }
                }
            });



        }

        // setup click events on the playlist aswell

        playList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() == 2 && !event.isConsumed()){
                    if(Main.isPlaying())
                        Main.getMP().stop();

                    trackIndex = playList.getSelectionModel().getSelectedIndex();
                    System.out.println("Index: "+trackIndex);
                    Main.setSelectedAndPlay(libSoundFiles.get(playListTrackIndex.get(playList.getSelectionModel().getSelectedIndex())));
                    setupTrackInfo();
                }
            }
        });

        // create an instance of the player so we can get our initial volume
        Main.createPlayer(libSoundFiles.get(0));

        // then use this info to setup and create our volumeslider
        if(Main.getMP() != null && volumeSlider != null) {
            System.out.println("VOL: " + Main.getMP().getVolume());
            volumeSlider.setMax(1.0);
            volumeSlider.setMin(0.0);
            volumeSlider.setValue(Main.getMP().getVolume());

            volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    Main.getMP().setVolume(volumeSlider.getValue());
                    Main.setVolumeVal(volumeSlider.getValue());
                }
            });
        }


        // END OF INIT
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

    // playNextTrack Function! this is used for both the nextButton and
    // in the onEndOfMedia runnable
    public void playNxtTrack(){

        // if we are already playing - we should prob. stop it :)
        if(Main.isPlaying())
            Main.getMP().stop();

        // stub integer for nxt index
        int nxt;
        // if we are in shuffle mode, we should get a random integer between 0 and our playlist size
        if (shuffleMode) {
            nxt = randInt(0, playListTrackIndex.size() - 1);
        } else {
            // else, just increment our index with one :)
            nxt = trackIndex + 1;
        }

        System.out.println(nxt+" totalTracks "+playListTrackIndex.size());

        // if next index is larger than the size of our playlist - go back to the beginning (e.g. 0)
        if(nxt > playListTrackIndex.size()-1){
            nxt = 0;
        }
        trackIndex = nxt;
        // set the next track to selected and play it
        Main.setSelectedAndPlay(libSoundFiles.get(playListTrackIndex.get(nxt)));

        // now that our track has been selected, update track artist, title and album on display
        setupTrackInfo();

    }

    public void playPrevTrack(){

        // if we are already playing - we should prob. stop it :)
        if(Main.isPlaying())
            Main.getMP().stop();

        // stub integer for nxt index
        int nxt;
        // if we are in shuffle mode, we should get a random integer between 0 and our playlist size
        if (shuffleMode) {
            nxt = randInt(0, playListTrackIndex.size() - 1);
        } else {
            // else, just increment our index with one :)
            nxt = trackIndex - 1;
        }

        System.out.println(nxt+" totalTracks "+playListTrackIndex.size());

        // if next index is larger than the size of our playlist - go back to the beginning (e.g. 0)
        if(nxt < 0){
            nxt = playListTrackIndex.size()-1;
        }
        trackIndex = nxt;
        // set the next track to selected and play it
        Main.setSelectedAndPlay(libSoundFiles.get(playListTrackIndex.get(nxt)));

        // now that our track has been selected, update track artist, title and album on display
        setupTrackInfo();

    }

    private void setupTrackInfo(){
        artistTitleText.setText(Main.getMediaInfo("artTrk"));
        albumText.setText(Main.getMediaInfo("album"));
        albumArt.setImage(Main.getAlbumArt());
    }

    // random integer function - which returns a random int
    // inside of the specified range
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

}
