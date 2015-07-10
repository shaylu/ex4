/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import ws.roulette.GameStatus;
import ws.roulette.RouletteType;

/**
 *
 * @author Dell
 */
public class GameDetails extends ws.roulette.GameDetails {

    IChangeGameStatusObserver statusChangesListener;

    @Override
    public void setStatus(GameStatus value) {
//        GameStatus oldStatus = this.getStatus();
        super.setStatus(value); //To change body of generated methods, choose Tools | Templates.
//        
//        if (oldStatus == null)
//            return;
//        
//        if (!oldStatus.equals(value)) {
//            if (oldStatus == GameStatus.WAITING && value == GameStatus.ACTIVE) {
//                if (statusChangesListener != null) {
//                    statusChangesListener.gameStarted();
//                }
//            } else if (oldStatus == GameStatus.ACTIVE && value == GameStatus.FINISHED) {
//                if (statusChangesListener != null) {
//                    statusChangesListener.gameOvered();
//                }
//            }
//        }
    }

    private GameDetails() {
        super();
        this.setStatus(GameStatus.WAITING);
        this.joinedHumanPlayers = 0;
    }

    public GameDetails(
            String gameName,
            RouletteType type,
            boolean loadedFromXML,
            int initialAmountOfMoneyPerPlayer,
            int numOfHumanPlayers,
            int numOfComputerPlayers,
            int minBetsPerPlayer,
            int maxBetsPerPlayer) {
        this();
        this.setName(gameName);
        this.setRouletteType(type);
        this.setLoadedFromXML(loadedFromXML);
        this.setInitalSumOfMoney(initialAmountOfMoneyPerPlayer);
        this.setHumanPlayers(numOfHumanPlayers);
        this.setComputerizedPlayers(numOfComputerPlayers);
        this.setMinWages(minBetsPerPlayer);
        this.setIntMaxWages(maxBetsPerPlayer);
    }

    void humanPlayerJoined() {
        joinedHumanPlayers++;
    }
}
