/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import ws.roulette.GameStatus;
import ws.roulette.RouletteType;

/**
 *
 * @author Dell
 */
public class GamesManager {

    private HashMap<String, Game> games;

    public GamesManager() {
        games = new HashMap<>();
    }
    
    

    public String add(String name, RouletteType type, int initAmountOfMoney, int numOfHumanPlayers, int numOfComputerPlayers, int minBetsPerPlayer, int maxBetsPerPlayer) throws Exception {
        Game game = new Game(this, name, type, initAmountOfMoney, numOfHumanPlayers, numOfComputerPlayers, minBetsPerPlayer, maxBetsPerPlayer);
        return add(game);
    }

    public String add(String xmldata) throws Exception {
        Game game = new Game(this, xmldata);
        return add(game);
    }

    private String add(Game game) throws Exception {
        String gameName = game.getGameDetails().getName();
        synchronized (this) {
            if (isExist(gameName) == false) {
                games.put(gameName, game);
                return gameName;
            } else {
                throw new Exception("Game with the same name already exist.");
            }
        }
    }

    public boolean isExist(String name) {
        return games.containsKey(name);
    }

    public GameStatus getStatus(String name) throws Exception {
        Game game = get(name);
        return game.getGameDetails().getStatus();
    }

    public Game get(String name) throws Exception {
        if (isExist(name) == true) {
            return games.get(name);
        } else {
            throw new Exception("game with given name doesn't exist");
        }
    }
    
    public Game getByPlayerID(int id) throws Exception{
        Optional<Game> game = games.values().stream().filter(x -> x.players.getPlayer(id) != null).findFirst();
        if (game.isPresent() == true){
            return game.get();
        }
        else {
            throw new Exception("player doesn't exist");
        }
    }
    
    public List<Game> getWaitingGames(){
        return games.values().stream().filter(x -> x.getGameDetails().getStatus() == GameStatus.WAITING).collect(Collectors.toList());
    }
}
