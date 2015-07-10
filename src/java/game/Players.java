/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import game.helpers.UniqueIDGenerator;
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
            int id = idsGenerator.getNewId();
            this.players.put(id, new Player(player));
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
        
        if (game.getGameDetails().getStatus() == GameStatus.ACTIVE)
            throw new Exception("Game is running.");

        if (isPlayerNameExist(player.getName()) == false || (isPlayerNameExist(player.getName()) == true && getPlayer(player.getName()).nameUsed == false)) {
            int id = idsGenerator.getNewId();
            players.put(id, player);

            return id;
        }
        else {
            throw new Exception("Player name exist.");
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
        return (int) players.entrySet().stream().filter(x -> x.getValue().getType() == PlayerType.HUMAN).count();
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
}
