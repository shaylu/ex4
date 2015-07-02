/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.ws;

import game.Game;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.jws.WebService;
import ws.roulette.GameDoesNotExists;
import ws.roulette.GameDoesNotExists_Exception;
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

    protected void preformGameInitializeCheck() throws ws.roulette.InvalidParameters_Exception {
        if (isGameInitialized() == false) {
            throw new InvalidParameters_Exception("game isn't initialized", new InvalidParameters());
        }
    }

    public java.util.List<ws.roulette.Event> getEvents(int eventId, int playerId) throws ws.roulette.InvalidParameters_Exception {
        preformGameInitializeCheck();
        return game.getEvents(eventId);
    }

    public void createGame(int computerizedPlayers, int humanPlayers, int initalSumOfMoney, int intMaxWages, int minWages, java.lang.String name, ws.roulette.RouletteType rouletteType) throws ws.roulette.InvalidParameters_Exception, ws.roulette.DuplicateGameName_Exception {
        if (isGameInitialized() == true && game.isRunning()) {
            throw new InvalidParameters_Exception("game already running.", new InvalidParameters());
        }

        game = new Game(name, rouletteType, initalSumOfMoney, humanPlayers, computerizedPlayers, minWages, intMaxWages);
    }

    public ws.roulette.GameDetails getGameDetails(java.lang.String gameName) throws ws.roulette.GameDoesNotExists_Exception {
        if (isGameInitialized() == false) {
            throw new GameDoesNotExists_Exception("Game is not running.", new GameDoesNotExists());
        }

        return game.getGameDetails();
    }

    public java.util.List<java.lang.String> getWaitingGames() {
        if (isGameInitialized() == true && game.isRunning() == false) {
            return Arrays.asList(new String[]{game.getGameDetails().getName()});
        } else {
            return new ArrayList<>();
        }
    }

    public int joinGame(java.lang.String gameName, java.lang.String playerName) throws ws.roulette.InvalidParameters_Exception, ws.roulette.GameDoesNotExists_Exception {
        preformGameInitializeCheck();
        return game.joinGame(playerName);
    }

    public ws.roulette.PlayerDetails getPlayerDetails(int playerId) throws ws.roulette.GameDoesNotExists_Exception, ws.roulette.InvalidParameters_Exception {
        preformGameInitializeCheck();

        if (game.getPlayers().isPlayerExist(playerId) == true) {
            return game.getPlayers().getPlayersDetails(playerId);
        } else {
            throw new InvalidParameters_Exception("Player id invalid.", new InvalidParameters());
        }
    }

    public void makeBet(int betMoney, ws.roulette.BetType betType, java.util.List<java.lang.Integer> numbers, int playerId) throws ws.roulette.InvalidParameters_Exception {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void finishBetting(int playerId) throws ws.roulette.InvalidParameters_Exception {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void resign(int playerId) throws ws.roulette.InvalidParameters_Exception {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public java.lang.String createGameFromXML(java.lang.String xmlData) throws ws.roulette.DuplicateGameName_Exception, ws.roulette.InvalidParameters_Exception, ws.roulette.InvalidXML_Exception {
        if (isGameInitialized() == true && game.isRunning() == true) {
            try {
                game = new Game(xmlData);
            } catch (Exception e) {
                throw new InvalidParameters_Exception("failed to create game from XML, " + e.getMessage(), new InvalidParameters());
            }

            return game.getGameDetails().getName();
        } else {
            throw new InvalidParameters_Exception("game is already running.", new InvalidParameters());
        }
    }

    public java.util.List<ws.roulette.PlayerDetails> getPlayersDetails(java.lang.String gameName) throws ws.roulette.GameDoesNotExists_Exception {
        try {
            preformGameInitializeCheck();
        } catch (Exception e) {
            throw new GameDoesNotExists_Exception(e.getMessage(), new GameDoesNotExists());
        }

        return new ArrayList<>(game.getPlayers().getPlayersDetails());
    }

}
