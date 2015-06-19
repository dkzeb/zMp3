package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sthaz_000 on 18-06-2015.
 */
public class DisplayController {
    @FXML private Label artistTrackTxt, albumTxt, timeText;

    private String t;

    @FXML void initialize(){
        //timeText.setText(time);

        final Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                t = Main.getTime();
                timeText.setText("Time: "+t);
                //System.out.println(t);
            }
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

    }
}
