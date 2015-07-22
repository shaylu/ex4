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
import java.util.concurrent.TimeUnit;

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

    protected GamesManager gamesManager;
    protected GameDetails gameDetails;
    protected Players players;
    protected Events events;
    protected HashMap<Integer, Boolean> humanPlayersFinishedBetting;
    protected GameTimer timer;
    protected boolean roundRunning;
    protected BetsValidator betsValidator;

    public Game(GamesManager manager, String XMLData) throws Exception {
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
        int humanPlayers = (int)roulette.getPlayers().getPlayer().stream().filter(x -> x.getType() == game.jaxb.PlayerType.HUMAN).count();
        int computerPlayers = (int)roulette.getPlayers().getPlayer().stream().filter(x -> x.getType() == game.jaxb.PlayerType.COMPUTER).count();

        this.gamesManager = manager;
        this.gameDetails = new GameDetails(name, type, loadedFromXML, initAmountOfMoney, humanPlayers, computerPlayers, minBets, maxBets);
        this.events = new Events();
        this.betsValidator = new BetsValidator(type);

        createComputerPlayers();

        System.out.println("Game(), successfully created a game instance.");
    }

    public Game(GamesManager manger, String name, RouletteType type, int initAmountOfMoney, int numOfHumanPlayers, int numOfComputerPlayers, int minBetsPerPlayer, int maxBetsPerPlayer) throws Exception {
        System.out.println("Game(), creating a game instance from settings.");

        this.gamesManager = manger;
        this.gameDetails = new GameDetails(name, type, false, initAmountOfMoney, numOfHumanPlayers, numOfComputerPlayers, minBetsPerPlayer, maxBetsPerPlayer);
        this.events = new Events();
        this.players = new Players(this);
        this.betsValidator = new BetsValidator(type);

        createComputerPlayers();

        System.out.println("Game(), successfully created a game instance.");
    }

    public int joinGame(String playerName) throws ws.roulette.InvalidParameters_Exception {
        if (getGameDetails().getStatus() != GameStatus.WAITING) {
            throw new InvalidParameters_Exception("game is not waiting to get new players.", new InvalidParameters());
        }

        System.out.println("joinGame(), trying to join game, given name: '" + playerName + "'.");

        int id;

        if (getGameDetails().isLoadedFromXML() == true) {
            Player player = players.getPlayer(playerName);
            if (player != null) {
                if (player.getNameUsed() == false) {
                    id = player.getId();
                    player.setNameUsed(true);
                } else {
                    throw new InvalidParameters_Exception("player already used.", new InvalidParameters());
                }
            } else {
                throw new InvalidParameters_Exception("player doesn't exist.", new InvalidParameters());
            }
        } else {
            try {
                id = players.add(playerName, PlayerType.HUMAN, gameDetails.getInitalSumOfMoney());
            } catch (Exception e) {
                throw new InvalidParameters_Exception("failed to join game, " + e.getMessage(), new InvalidParameters());
            }
        }

        gameDetails.humanPlayerJoined();
        int numberOfHumanPlayersJoined = this.players.getNumberOfHumanPlayers();
        int numberOfNeededHumanPlayers = this.gameDetails.getHumanPlayers();

        if (numberOfHumanPlayersJoined == numberOfNeededHumanPlayers) {
            // can start game
            System.out.println("joinGame(), enought human players joined, can start game");
            Thread t = new Thread(() -> {
                this.play();
            });
            t.start();
        }

        System.out.println("joinGame(), successfully joined game.");
        return id;
    }

    private void createComputerPlayers() throws Exception {
        if (getGameDetails().isLoadedFromXML() == true) {
            return;
        }

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

        synchronized (this) {
            if (this.getGameDetails().getStatus() == GameStatus.ACTIVE) {
                return;
            } else {
                this.getGameDetails().setStatus(GameStatus.ACTIVE);
            }
        }

        setAllPlayersAsActive();
        int roundNumber = 0;

        while (players.getNumberOfActiveHumanPlayers() > 0) {
            roundNumber++;
            messageConsole("play(), round " + roundNumber + " starting.");

            startRound();
            waitFor(10);
        }

        endGame();
    }

    private void startRound() {
        messageConsole("startRound(), round started.");

        resetBets();
        humanPlayersFinishedBetting = new HashMap<>();
        roundRunning = true;

        int secondsPass = 0;
        int secondsToRun = (int) TimeUnit.SECONDS.convert(ROUND_MILLSEC, TimeUnit.MILLISECONDS);

        this.events.gameStarted(ROUND_MILLSEC);

        waitFor(5);
        placeComputerBets();

        while (roundRunning && secondsPass < secondsToRun && players.getNumberOfActiveHumanPlayers() > 0) {
            try {
                Thread.sleep(TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS));
                // sleep one second
            } catch (Exception e) {
            }
            secondsPass++;
        }

        System.out.println("round over after " + secondsPass);

        endRound();
    }

    private void endRound() {
        roundRunning = false;
        messageConsole("endRound(), round ended.");

        resignPlayersThatsDidntPlaceEnoughtBets();

        int winningNumber = turnWheel();
        System.out.println("winning number is: " + winningNumber);

        events.winningNumber(winningNumber);

        giveMoneyToWinners(winningNumber);

        printPlayersStatusToLog();
    }

    private void endGame() {
        messageConsole("endGame(), game ending.");
        gameDetails.setStatus(GameStatus.FINISHED);
        this.events.gameOver();
    }

    private void tryToEndRound() {
        if (roundRunning == false || this.getGameDetails().getStatus() != GameStatus.ACTIVE) {
            return;
        }

        if (humanPlayersFinishedBetting.size() >= this.players.getNumberOfActiveHumanPlayers()) {
            // all the active human players finsihed placing their bets
            // we can end round
            setRoundEnd();
        }
    }

    private void messageConsole(String text) {
        System.out.println(gameDetails.getName() + ", " + text);
    }

    private void placeComputerBets() {
        Random rnd = new Random();
        int min = 0;
        int max = (this.gameDetails.getRouletteType() == RouletteType.FRENCH) ? 36 : 37;

        List<Player> compPlayers = players.players.entrySet().stream().filter(x -> x.getValue().type == PlayerType.COMPUTER && x.getValue().status == PlayerStatus.ACTIVE).map(x -> x.getValue()).collect(Collectors.toList());
        for (Player player : compPlayers) {
            int num = rnd.nextInt((max - min) + 1) + min;
            int money = rnd.nextInt((player.getMoney() - 1) + 1) + 1;

            ArrayList<Integer> numbers = new ArrayList<>();
            numbers.add(num);
            try {
                placeBet(player, money, BetType.STRAIGHT, numbers);
            } catch (Exception e) {
            }

        }
    }

    private void placeBet(Player player, int money, BetType betType, ArrayList<Integer> numbers) throws Exception {
        if (getGameDetails().getStatus() != GameStatus.ACTIVE || roundRunning != true) {
            throw new Exception("game is not running or inactive");
        }

        if (player.money < money) {
            throw new Exception("Not enought money to place bet.");
        }

        if (betsValidator.isBetValid(CastingHelper.cast(betType), numbers) == false) {
            throw new Exception("bet invalid.");
        }

        player.substructMoney(money);
        player.bets.add(new Bet(CastingHelper.cast(betType), numbers, money));

        messageConsole("placeBet(), " + player.name + " placed bet of " + money + " on " + betType.name() + ", and now has $" + player.money + " left.");
        events.playerPlacedABet(player.name, CastingHelper.cast(betType), numbers, money);

        boolean finishedBetting = isPlayerPlacedMaxBets(player);
        if (finishedBetting == true) {
            playerFinishedBetting(player.id);
        }
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
        int min = 0;
        int max = (this.gameDetails.getRouletteType() == RouletteType.FRENCH) ? 36 : 37;
        int res = rnd.nextInt((max - min) + 1) + min;
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
        if (roundRunning == true) {
            Player player = players.getPlayer(id);
            if (player != null && !humanPlayersFinishedBetting.containsKey(id)) {
                humanPlayersFinishedBetting.put(id, true);
                events.playerFinishedBetting(player.getName());
            } else {
                throw new Exception("failed to find player with id " + id + ".");
            }
        } else {
            throw new Exception("round is not running.");
        }

//        if (humanPlayersFinishedBetting.size() == this.players.getNumberOfActiveHumanPlayers())
//        {
//            // all the active human players finsihed placing their bets
//            // we can end round
//            setRoundEnd();
//        }
        tryToEndRound();
    }

    private boolean isPlayerPlacedMaxBets(Player player) {
        int numberOfBetsByPlayer = player.getBets().bets.size();
        int numberOfMaxBetsPerPlayer = this.getGameDetails().getIntMaxWages();

        if (numberOfBetsByPlayer == numberOfMaxBetsPerPlayer) {
            return true;
        } else {
            return false;
        }
    }

    private void setRoundEnd() {
        this.roundRunning = false;
    }

    private void setAllPlayersAsActive() {
        for (Player player : players.getPlayers()) {
            player.setStatus(PlayerStatus.ACTIVE);
        }
    }

    private void printPlayersStatusToLog() {
        System.out.println("== players status:");
        List<Player> playersList = players.getPlayers();
        for (Player player : playersList) {
            System.out.println(player.toString());
        }
        System.out.println("== end players status");
    }

    private void waitFor(int secondsToWait) {
        for (int i = 0; i < secondsToWait; i++) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
    }

    private void resetBets() {
        for (Player player : players.getPlayers()) {
            player.bets.clear();
        }
    }
}
