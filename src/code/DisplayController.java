package code;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 * Created by sthaz_000 on 18-06-2015.
 */
public class DisplayController {
    @FXML private Label timeText;
    @FXML private Slider timeSlider;

    private double trackTime;

    @FXML void initialize(){

        if(timeSlider != null) {
            timeSlider.setMin(0.0);
            timeSlider.setMax(1.0);

            timeSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> obs, Boolean wasChanging, Boolean isNowChanging) {

                    if(! isNowChanging) {
                        Main.getMP().seek(Duration.seconds(timeSlider.getValue() * trackTime));
                        Main.getMP().play();
                    } else {
                        Main.getMP().pause();
                    }
                }
            });

        }

        final Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(Main.isPlaying()) {

                    if(timeText != null)
                        timeText.setText(Main.getTime());

                        String[] timeSplit = Main.getTime().split("/");

                        // time passed split up in total seconds
                        String[] passed = timeSplit[0].split(":");
                        double passedSeconds = (Integer.parseInt(passed[0]) * 60) + Integer.parseInt(passed[1]);

                        String[] left = timeSplit[1].split(":");
                        double secondsLeft = (Integer.parseInt(left[0]) * 60) + Integer.parseInt(left[1]);

                        trackTime = secondsLeft;
                        double sliderVal = passedSeconds / secondsLeft;

                        //System.out.println(sliderVal);

                        if (timeSlider != null)
                            timeSlider.setValue(sliderVal);

                } else {
                    if(timeText != null)
                        timeText.setText("");
                }
            }
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

    }
}
