/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.ws;

import game.Game;
import game.GamesManager;
import game.helpers.CastingHelper;
import game.jaxb.BetType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.jws.WebService;
import ws.roulette.DuplicateGameName;
import ws.roulette.DuplicateGameName_Exception;
import ws.roulette.GameDoesNotExists;
import ws.roulette.GameDoesNotExists_Exception;
import ws.roulette.GameStatus;
import ws.roulette.InvalidParameters;
import ws.roulette.InvalidParameters_Exception;

/**
 *
 * @author Dell
 */
@WebService(serviceName = "RouletteWebServiceService", portName = "RouletteWebServicePort", endpointInterface = "ws.roulette.RouletteWebService", targetNamespace = "http://roulette.ws/", wsdlLocation = "WEB-INF/wsdl/RouletteWebServiceFromWSDL/RouletteWebServiceService.wsdl")
public class RouletteWebServiceFromWSDL {

    public static GamesManager games;

    static {
        games = new GamesManager();
    }

    public java.util.List<ws.roulette.Event> getEvents(int eventId, int playerId) throws ws.roulette.InvalidParameters_Exception {
        Game game;

        try {
            game = games.getByPlayerID(playerId);
        } catch (Exception e) {
            throw new InvalidParameters_Exception(e.getMessage(), new InvalidParameters());
        }

        return game.getEvents(eventId);
    }

    public void createGame(int computerizedPlayers, int humanPlayers, int initalSumOfMoney, int intMaxWages, int minWages, java.lang.String name, ws.roulette.RouletteType rouletteType) throws ws.roulette.InvalidParameters_Exception, ws.roulette.DuplicateGameName_Exception {
        if (games.isExist(name)) {
            throw new DuplicateGameName_Exception("game with given name already exist.", new DuplicateGameName());
        }

        try {
            games.add(name, rouletteType, initalSumOfMoney, humanPlayers, computerizedPlayers, minWages, intMaxWages);
        } catch (Exception e) {
            throw new InvalidParameters_Exception(e.getMessage(), new InvalidParameters());
        }
    }

    public ws.roulette.GameDetails getGameDetails(java.lang.String gameName) throws ws.roulette.GameDoesNotExists_Exception {
        try {
            return games.get(gameName).getGameDetails();
        } catch (Exception e) {
            throw new GameDoesNotExists_Exception(e.getMessage(), new GameDoesNotExists());
        }
    }

    public java.util.List<java.lang.String> getWaitingGames() {
        return games.getWaitingGames().stream().map(x -> x.getGameDetails().getName()).collect(Collectors.toList());
    }

    public int joinGame(java.lang.String gameName, java.lang.String playerName) throws ws.roulette.InvalidParameters_Exception, ws.roulette.GameDoesNotExists_Exception {
        if (games.isExist(gameName) == false) {
            throw new GameDoesNotExists_Exception("Game doesn't exist.", new GameDoesNotExists());
        }
        try {
            return games.get(gameName).joinGame(playerName);
        } catch (Exception e) {
            throw new InvalidParameters_Exception(e.getMessage(), new InvalidParameters());
        }
    }

    public ws.roulette.PlayerDetails getPlayerDetails(int playerId) throws ws.roulette.GameDoesNotExists_Exception, ws.roulette.InvalidParameters_Exception {
        Game game;
        try {
            game = games.getByPlayerID(playerId);
        } catch (Exception e) {
            throw new GameDoesNotExists_Exception(e.getMessage(), new GameDoesNotExists());
        }

        if (game.getPlayers().isPlayerExist(playerId) == true) {
            return game.getPlayers().getPlayersDetails(playerId);
        } else {
            throw new InvalidParameters_Exception("Player id invalid.", new InvalidParameters());
        }
    }

    public void makeBet(int betMoney, ws.roulette.BetType betType, java.util.List<java.lang.Integer> numbers, int playerId) throws ws.roulette.InvalidParameters_Exception {
        try {
            Game game = games.getByPlayerID(playerId);
            game.placeBet(playerId, betMoney, BetType.valueOf(betType.name()), new ArrayList<>(numbers));
        } catch (Exception e) {
            throw new InvalidParameters_Exception(e.getMessage(), new InvalidParameters());
        }
    }

    public void finishBetting(int playerId) throws ws.roulette.InvalidParameters_Exception {
        try {
            Game game = games.getByPlayerID(playerId);
            game.playerFinishedBetting(playerId);
        } catch (Exception e) {
            throw new InvalidParameters_Exception(e.getMessage(), new InvalidParameters());
        }
    }

    public void resign(int playerId) throws ws.roulette.InvalidParameters_Exception {
        try {
            Game game = games.getByPlayerID(playerId);
            game.resignPlayer(playerId);
        } catch (Exception e) {
            throw new InvalidParameters_Exception(e.getMessage(), new InvalidParameters());
        }
    }

    public java.lang.String createGameFromXML(java.lang.String xmlData) throws ws.roulette.DuplicateGameName_Exception, ws.roulette.InvalidParameters_Exception, ws.roulette.InvalidXML_Exception {
        try {
            return games.add(xmlData);
        } catch (Exception e) {
            throw new InvalidParameters_Exception(e.getMessage(), new InvalidParameters());
        }
    }

    public java.util.List<ws.roulette.PlayerDetails> getPlayersDetails(java.lang.String gameName) throws ws.roulette.GameDoesNotExists_Exception {
        try {
            Game game = games.get(gameName);
            if (game.getGameDetails().getStatus() == GameStatus.WAITING && game.getGameDetails().isLoadedFromXML() == true) {
                // return the list of unused human players
                return game.getPlayers().getUnusedHumanPlayers();
            } else {
                return new ArrayList<>(game.getPlayers().getPlayersDetails());
            }
        } catch (Exception e) {
            throw new GameDoesNotExists_Exception(e.getMessage(), new GameDoesNotExists());
        }
    }
}
