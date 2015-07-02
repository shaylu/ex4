/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Dell
 */
public class GameTimer extends Timer{
    public SimpleBooleanProperty isTimerStopped; 

    public GameTimer() {
        super();
        isTimerStopped = new SimpleBooleanProperty(true);
    }

    public void start(int timeout){
        stop();
        
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                stop();
            }
        };
        
        this.schedule(task, timeout);
    }
    
    public void stop(){
        isTimerStopped.set(true);
        this.cancel();
        this.purge();
    }
}
