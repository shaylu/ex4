/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import ws.roulette.GameStatus;
import ws.roulette.RouletteType;

/**
 *
 * @author Dell
 */
public class GameDetails extends ws.roulette.GameDetails {
    @Override
    public void setStatus(GameStatus value) {
        super.setStatus(value); //To change body of generated methods, choose Tools | Templates.
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
            int maxBetsPerPlayer) throws Exception {
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
