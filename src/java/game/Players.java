/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import game.helpers.UniqueIDGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import ws.roulette.GameStatus;
import ws.roulette.PlayerStatus;
import ws.roulette.PlayerType;

/**
 *
 * @author Dell
 */
public class Players {

    protected HashMap<Integer, Player> players;
    protected UniqueIDGenerator idsGenerator;
    protected Game game;

    public Players(game.jaxb.Roulette roulette, Game game) throws Exception {
        this(game);
        List<game.jaxb.Players.Player> playersList = roulette.getPlayers().getPlayer();
        for (game.jaxb.Players.Player player : playersList) {
            Player gamePlayer = new Player(player);
            int playerId = gamePlayer.getId();
            this.players.put(playerId, gamePlayer);
        }
    }

    public Players(Game game) {
        players = new HashMap<>();
        idsGenerator = new UniqueIDGenerator();
        this.game = game;
    }

    public Player getPlayer(int id) {
        return players.get(id);
    }

    public Player getPlayer(String playerName) {
        Optional<Player> found = players.values().stream().filter(x -> x.name.equals(playerName)).findFirst();
        return (found.isPresent() ? found.get() : null);
    }

    public int add(String name, PlayerType type, int initAmountMoney) throws Exception {
        Player player = new Player(name, type, initAmountMoney);
        return add(player);
    }

    public int add(Player player) throws Exception {

        if (game.getGameDetails().getStatus() != GameStatus.WAITING) {
            throw new Exception("Game is not waiting for players.");
        }

        if (game.getGameDetails().isLoadedFromXML() == true) {
            // game loaded from xml
            if (isPlayerNameExist(player.getName()) == false) {
                throw new Exception("no player with the given name can be found in players list.");
            }

            if (isPlayerNameExist(player.getName()) == true && getPlayer(player.getName()).nameUsed == true) {
                throw new Exception("player name is already in use.");
            }

            return getPlayerID(player.name);
        } else {
            if (isPlayerNameExist(player.getName()) == true) {
                throw new Exception("player name is already in use.");
            } else {
                // in case of xml game the list of playes is already set
                players.put(player.getId(), player);
                return player.id;
            }
        }
    }

    /**
     *
     * @param playerName
     * @return the player id, 0 if not found
     */
    public int getPlayerID(String playerName) {
        Optional<Map.Entry<Integer, Player>> found = players.entrySet().stream().filter(x -> x.getValue().name.equals(playerName)).findFirst();
        return (found.isPresent() ? found.get().getKey() : 0);
    }

    public List<PlayerDetails> getPlayersDetails() {
        return players.values().stream().map(x -> new PlayerDetails(x)).collect(Collectors.toList());
    }

    int getNumberOfHumanPlayers() {
        if (this.game.getGameDetails().isLoadedFromXML() == true) {
            return (int) players.entrySet().stream().filter(x -> x.getValue().getType() == PlayerType.HUMAN && x.getValue().getNameUsed() == true).count();
        } else {
            return (int) players.entrySet().stream().filter(x -> x.getValue().getType() == PlayerType.HUMAN).count();
        }
    }

    int getNumberOfComputerPlayers() {
        return (int) players.entrySet().stream().filter(x -> x.getValue().getType() == PlayerType.COMPUTER).count();
    }

    boolean isPlayerNameExist(String playerName) {
        return players.values().stream().filter(x -> x.getName().equals(playerName)).count() > 0;
    }

    public boolean isPlayerExist(int playerId) {
        return players.containsKey(playerId);
    }

    public PlayerDetails getPlayersDetails(int playerId) {
        Player player = getPlayer(playerId);
        return new PlayerDetails(player);
    }

    int getNumberOfActiveHumanPlayers() {
        return (int) getActiveHumanPlayers().size();
    }

    List<Player> getActiveHumanPlayers() {
        return players.values().stream().filter(x -> x.getStatus() == PlayerStatus.ACTIVE && x.getType() == PlayerType.HUMAN).collect(Collectors.toList());
    }

    List<Player> getActivePlayers() {
        return players.values().stream().filter(x -> x.getStatus() == PlayerStatus.ACTIVE).collect(Collectors.toList());
    }

    List<Player> getPlayers() {
        return new ArrayList<>(players.values());
    }

    public List<ws.roulette.PlayerDetails> getUnusedHumanPlayers() {
        return players.values().stream().filter(x -> x.nameUsed == false && x.getType() == PlayerType.HUMAN).map(x -> new PlayerDetails(x)).collect(Collectors.toList());
    }
}
