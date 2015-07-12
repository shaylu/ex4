/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.ws;

import game.Game;
import game.helpers.CastingHelper;
import game.jaxb.BetType;
import java.util.ArrayList;
import java.util.Arrays;
import javax.jws.WebService;
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

    Game game;

    public RouletteWebServiceFromWSDL() {
        game = null;
    }

    protected boolean isGameInitialized() {
        return (game != null);
    }

    public java.util.List<ws.roulette.Event> getEvents(int eventId, int playerId) throws ws.roulette.InvalidParameters_Exception {
        if (game != null) {
            return game.getEvents(eventId);
        } else {
            throw new InvalidParameters_Exception("game is not initialized yet.", new InvalidParameters());
        }
    }

    public void createGame(int computerizedPlayers, int humanPlayers, int initalSumOfMoney, int intMaxWages, int minWages, java.lang.String name, ws.roulette.RouletteType rouletteType) throws ws.roulette.InvalidParameters_Exception, ws.roulette.DuplicateGameName_Exception {
        if (game != null && game.getGameDetails().getStatus() != GameStatus.FINISHED) {
            throw new InvalidParameters_Exception("game already running.", new InvalidParameters());
        }

        try {
            game = new Game(name, rouletteType, initalSumOfMoney, humanPlayers, computerizedPlayers, minWages, intMaxWages);
        } catch (Exception e) {
            throw new InvalidParameters_Exception(e.getMessage(), new InvalidParameters());
        }
    }

    public ws.roulette.GameDetails getGameDetails(java.lang.String gameName) throws ws.roulette.GameDoesNotExists_Exception {
        if (game == null) {
            throw new GameDoesNotExists_Exception("Game is not running.", new GameDoesNotExists());
        }

        return game.getGameDetails();
    }

    public java.util.List<java.lang.String> getWaitingGames() {
        if (game != null && game.getGameDetails().getStatus() == GameStatus.WAITING) {
            ArrayList<String> result = new ArrayList<>();
            result.add(game.getGameDetails().getName());
            return result;
        } else {
            return new ArrayList<>();
        }
    }

    public int joinGame(java.lang.String gameName, java.lang.String playerName) throws ws.roulette.InvalidParameters_Exception, ws.roulette.GameDoesNotExists_Exception {
        if (game == null) {
            throw new GameDoesNotExists_Exception("Game not initialized.", new GameDoesNotExists());
        }

        if (game.getGameDetails().getStatus() != GameStatus.WAITING) {
            throw new InvalidParameters_Exception("Can't join game because of it's state.", new InvalidParameters());
        }

        return game.joinGame(playerName);
    }

    public ws.roulette.PlayerDetails getPlayerDetails(int playerId) throws ws.roulette.GameDoesNotExists_Exception, ws.roulette.InvalidParameters_Exception {
        if (game == null) {
            throw new GameDoesNotExists_Exception("game is not initialized yet.", new GameDoesNotExists());
        }

        if (game.getPlayers().isPlayerExist(playerId) == true) {
            return game.getPlayers().getPlayersDetails(playerId);
        } else {
            throw new InvalidParameters_Exception("Player id invalid.", new InvalidParameters());
        }
    }

    public void makeBet(int betMoney, ws.roulette.BetType betType, java.util.List<java.lang.Integer> numbers, int playerId) throws ws.roulette.InvalidParameters_Exception {
        if (game == null) {
            throw new InvalidParameters_Exception("game is not initialized yet.", new InvalidParameters());
        }
        if (game != null && game.getGameDetails().getStatus() != GameStatus.ACTIVE) {
            throw new InvalidParameters_Exception("game is inactive.", new InvalidParameters());
        }

        try {
            game.placeBet(playerId, betMoney, BetType.valueOf(betType.name()), new ArrayList<>(numbers));
        } catch (Exception e) {
            throw new InvalidParameters_Exception("failed to make bet, " + e.getMessage(), new InvalidParameters());
        }
    }

    public void finishBetting(int playerId) throws ws.roulette.InvalidParameters_Exception {
        if (game == null) {
            throw new InvalidParameters_Exception("game is not initialized yet.", new InvalidParameters());
        }

        try {
            game.playerFinishedBetting(playerId);
        } catch (Exception e) {
            throw new InvalidParameters_Exception("failed to finish betting, " + e.getMessage(), new InvalidParameters());
        }
    }

    public void resign(int playerId) throws ws.roulette.InvalidParameters_Exception {
        if (game == null) {
            throw new InvalidParameters_Exception("game is not initialized yet.", new InvalidParameters());
        }

        try {
            game.resignPlayer(playerId);
        } catch (Exception e) {
            throw new InvalidParameters_Exception("failed to finish betting, " + e.getMessage(), new InvalidParameters());
        }
    }

    public java.lang.String createGameFromXML(java.lang.String xmlData) throws ws.roulette.DuplicateGameName_Exception, ws.roulette.InvalidParameters_Exception, ws.roulette.InvalidXML_Exception {
        if (game == null || game.getGameDetails().getStatus() == GameStatus.FINISHED) {
            try {
                game = new Game(xmlData);
                return game.getGameDetails().getName();
            } catch (Exception e) {
                throw new ws.roulette.InvalidParameters_Exception(e.getMessage(), new InvalidParameters());
            }
        }else{
            throw new InvalidParameters_Exception("game is already running.", new InvalidParameters());
        }
    }

    public java.util.List<ws.roulette.PlayerDetails> getPlayersDetails(java.lang.String gameName) throws ws.roulette.GameDoesNotExists_Exception {
        if (game == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(game.getPlayers().getPlayersDetails());
    }
}
