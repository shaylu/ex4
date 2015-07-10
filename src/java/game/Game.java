/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import game.helpers.BetsValidator;
import game.helpers.BetsWinnings;
import game.helpers.CastingHelper;
import game.jaxb.BetType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import ws.roulette.Event;
import ws.roulette.GameStatus;
import ws.roulette.InvalidParameters;
import ws.roulette.InvalidParameters_Exception;
import ws.roulette.PlayerStatus;
import ws.roulette.PlayerType;
import ws.roulette.RouletteType;

/**
 *
 * @author Dell
 */
public class Game implements IChangeGameStatusObserver {

    public static final String[] computerPlayersNames;
    public static final Integer ROUND_MILLSEC = (int) TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);

    static {
        computerPlayersNames = new String[]{"Apollo", "Jupiter", "Neptune", "Pluto", "Gemini", "Luna"};
    }

    protected GameDetails gameDetails;
    protected Players players;
    protected Events events;
    protected HashMap<Integer, Boolean> humanPlayersFinishedBetting;
    protected GameTimer timer;
    protected boolean roundRunning;
    protected BetsValidator betsValidator;

    public Game(String XMLData) throws Exception {
        System.out.println("Game(), creating a game instance from XML data.");

        game.jaxb.Roulette roulette;

        try {
            JAXBContext context = JAXBContext.newInstance(game.jaxb.Roulette.class);
            Unmarshaller unmarshaller = (Unmarshaller) context.createUnmarshaller();
            InputStream stream = new ByteArrayInputStream(XMLData.getBytes());
            roulette = (game.jaxb.Roulette) unmarshaller.unmarshal(stream);
        } catch (Exception e) {
            throw new Exception("Failed to load XML data, " + e.getMessage());
        }

        try {
            this.players = new Players(roulette, this);
        } catch (Exception e) {
            throw new Exception("Failed to load players from xml data, " + e.getMessage());
        }

        boolean loadedFromXML = true;
        String name = roulette.getName();
        RouletteType type = RouletteType.fromValue(roulette.getTableType().value());
        int initAmountOfMoney = roulette.getInitSumOfMoney();
        int minBets = roulette.getMinBetsPerPlayer();
        int maxBets = roulette.getMaxBetsPerPlayer();
        int humanPlayers = this.players.getNumberOfHumanPlayers();
        int computerPlayers = this.players.getNumberOfComputerPlayers();

        this.gameDetails = new GameDetails(name, type, loadedFromXML, initAmountOfMoney, humanPlayers, computerPlayers, minBets, maxBets);
        this.events = new Events();
        this.betsValidator = new BetsValidator(type);

        createComputerPlayers();

        System.out.println("Game(), successfully created a game instance.");
    }

    public Game(String name, RouletteType type, int initAmountOfMoney, int numOfHumanPlayers, int numOfComputerPlayers, int minBetsPerPlayer, int maxBetsPerPlayer) {
        System.out.println("Game(), creating a game instance from settings.");

        this.gameDetails = new GameDetails(name, type, false, initAmountOfMoney, numOfHumanPlayers, numOfComputerPlayers, minBetsPerPlayer, maxBetsPerPlayer);
        this.events = new Events();
        this.players = new Players(this);
        this.betsValidator = new BetsValidator(type);

        System.out.println("Game(), successfully created a game instance.");
    }

    public int joinGame(String playerName) throws ws.roulette.InvalidParameters_Exception {
        System.out.println("joinGame(), trying to join game, given name: '" + playerName + "'.");

        int id;

        try {
            id = players.add(playerName, PlayerType.HUMAN, gameDetails.getInitalSumOfMoney());
            gameDetails.humanPlayerJoined();
        } catch (Exception e) {
            throw new InvalidParameters_Exception("failed to join game, " + e.getMessage(), new InvalidParameters());
        }

        if (gameDetails.isLoadedFromXML() == true) {
            players.getPlayer(playerName).setNameUsed(true);
        }

        System.out.println("joinGame(), successfully joined game.");
        return id;
    }

    protected void createComputerPlayers() throws Exception {
        int numOfComputerPlayers = gameDetails.getComputerizedPlayers();
        for (int i = 0; i < numOfComputerPlayers; i++) {
            players.add(computerPlayersNames[i], PlayerType.COMPUTER, gameDetails.getInitalSumOfMoney());
        }
    }

    public List<Event> getEvents(int eventId) {
        return events.getAfter(eventId);
    }

    @Override
    public void gameStarted() {
        events.gameStarted(ROUND_MILLSEC);
    }

    @Override
    public void gameOvered() {
        events.gameOver();
    }

    public boolean isRunning() {
        return gameDetails.getStatus() == GameStatus.ACTIVE;
    }

    public GameDetails getGameDetails() {
        return gameDetails;
    }

    public Players getPlayers() {
        return players;
    }

    public void resignPlayer(String playerName) {
        Player player = players.getPlayer(playerName);

        if (player != null && player.getStatus() == PlayerStatus.ACTIVE) {
            player.setStatus(PlayerStatus.RETIRED);
            events.playerResigned(playerName);
        }

        if (gameDetails.getStatus() == GameStatus.ACTIVE && roundRunning == true) {
            tryToEndRound();
        }
    }

    public void resignPlayer(int playerID) throws Exception {
        Player player = players.getPlayer(playerID);
        if (player != null) {
            resignPlayer(player.name);
        } else {
            throw new Exception("failed to find player with given id.");
        }
    }

    public void play() {
        int roundNumber = 0;

        while (players.getNumberOfActiveHumanPlayers() > 0) {
            roundNumber++;
            messageConsole("play(), round " + roundNumber + " starting.");

            Thread thread = new Thread(() -> startRound());
            thread.start();

            try {
                thread.join();
            } catch (Exception e) {
                messageConsole("play(), thread failed to join()");
            }
        }

        endGame();
    }

    private void startRound() {
        humanPlayersFinishedBetting = new HashMap<>();
        roundRunning = true;

        if (timer == null) {
            timer = new GameTimer();
            timer.isTimerStopped.addListener((object, oldValue, newValue) -> {
                if (oldValue == false && newValue == true) {
                    endRound();
                }
            });
        }

        timer.start(ROUND_MILLSEC);
        messageConsole("startRound(), timer started, " + ROUND_MILLSEC + " millsec.");

        gameDetails.setStatus(GameStatus.ACTIVE);
        messageConsole("startRound(), round started.");

        placeComputerBets();
    }

    private void endRound() {
        roundRunning = false;
        messageConsole("endRound(), round ended.");

        resignPlayersThatsDidntPlaceEnoughtBets();

        int winningNumber = turnWheel();
        events.winningNumber(winningNumber);

        giveMoneyToWinners(winningNumber);
    }

    private void endGame() {
        messageConsole("endGame(), game ending.");
        gameDetails.setStatus(GameStatus.FINISHED);
    }

    private void tryToEndRound() {
        messageConsole("tryToEndRound(), trying to end round.");

        int numOfHumanPlayersFinishedPlacingBets = humanPlayersFinishedBetting.size();
        int numOfActiveHumanPlayers = players.getNumberOfActiveHumanPlayers();
        messageConsole("tryToEndRound(), active human players finished betting: " + numOfHumanPlayersFinishedPlacingBets);
        messageConsole("tryToEndRound(), active human players: " + numOfActiveHumanPlayers);

        if (numOfHumanPlayersFinishedPlacingBets == numOfActiveHumanPlayers) {
            timer.stop();
        }
    }

    private void messageConsole(String text) {
        System.out.println(gameDetails.getName() + ", " + text);
    }

    private void placeComputerBets() {
        Random rnd = new Random();
        List<Player> compPlayers = players.players.entrySet().stream().filter(x -> x.getValue().type == PlayerType.COMPUTER && x.getValue().status == PlayerStatus.ACTIVE).map(x -> x.getValue()).collect(Collectors.toList());
        for (Player player : compPlayers) {

            int num = rnd.nextInt() + 37;
            int money = rnd.nextInt() + player.money;
            ArrayList<Integer> numbers = new ArrayList<>();
            numbers.add(num);
            try {
                placeBet(player, money, BetType.STRAIGHT, numbers);
            } catch (Exception e) {
            }

        }
    }

    private void placeBet(Player player, int money, BetType betType, ArrayList<Integer> numbers) throws Exception {
        if (player.money < money) {
            throw new Exception("Not enought money to place bet.");
        }

        if (betsValidator.isBetValid(CastingHelper.cast(betType), numbers) == false) {
            throw new Exception("bet invalid.");
        }

        player.substructMoney(money);
        player.bets.add(new Bet(CastingHelper.cast(betType), numbers, money));

        messageConsole("placeBet(), " + player.name + " placed bet of " + money + " on " + betType.name() + ", and now has $" + player.money + " left.");
    }

    public void placeBet(Integer id, int money, BetType betType, ArrayList<Integer> numbers) throws Exception {
        Player player = players.getPlayer(id);
        if (player != null) {
            placeBet(player, money, betType, numbers);
        } else {
            throw new Exception("failed to find player with id " + id + ".");
        }
    }

    private int turnWheel() {
        Random rnd = new Random();
        int max = (this.gameDetails.getRouletteType() == RouletteType.FRENCH) ? 36 : 37;
        int res = rnd.nextInt() + max;
        return res;
    }

    private void resignPlayersThatsDidntPlaceEnoughtBets() {
        for (Player player : players.getActivePlayers()) {
            if (player.bets.bets.size() < this.gameDetails.getMinWages()) {
                resignPlayer(player.name);
            }
        }
    }

    private void giveMoneyToWinners(int winningNumber) {
        for (Player player : players.getActivePlayers()) {
            int winning = BetsWinnings.calcWinning(this.gameDetails.getRouletteType(), winningNumber, player.bets.bets);
            player.addMoney(winning);
            messageConsole("giveMoneyToWinners(), player " + player.name + " won $" + winning);
            events.playerWon(player.name, winning);

            if (player.money == 0) {
                messageConsole("giveMoneyToWinners, player " + player.name + " is left with no money and need to be resigned.");
                resignPlayer(player.name);
            }
        }
    }

    public void playerFinishedBetting(int id) throws Exception {
        Player player = players.getPlayer(id);
        if (player != null) {
            humanPlayersFinishedBetting.put(id, true);
        } else {
            throw new Exception("failed to find player with id " + id + ".");
        }
    }
}
