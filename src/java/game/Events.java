/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import ws.roulette.BetType;
import ws.roulette.EventType;

/**
 *
 * @author Dell
 */
public class Events {
    protected HashMap<Integer, Event> events;

    public Events() {
        events = new HashMap<>();
    }
    
    public void gameStarted(int timeout){
        Event event = new Event(EventType.GAME_START);
        event.setTimeout(timeout);
        events.put(event.getId(), event);
    }
    
    public void gameOver(){
        Event event = new Event(EventType.GAME_OVER);
        events.put(event.getId(), event);
    }
    
    public void playerFinishedBetting(String playerName){
        Event event = new Event(EventType.PLAYER_FINISHED_BETTING);
        event.setPlayerName(playerName);
        events.put(event.getId(), event);
    }
    
    public void playerResigned(String playerName){
        Event event = new Event(EventType.PLAYER_RESIGNED);
        event.setPlayerName(playerName);
        events.put(event.getId(), event);
    }
    
    public void playerPlacedABet(String playerName, BetType type, List<Integer> numbers, int amount){
        Event event = new Event(EventType.PLAYER_BET);
        event.setPlayerName(playerName);
        event.setBetType(type);
        event.setAmount(amount);
        event.setNumbers(numbers);
        events.put(event.getId(), event);
    }
    
    public void winningNumber(int number){
        Event event = new Event(EventType.WINNING_NUMBER);
        event.setWinningNumber(number);
        events.put(event.getId(), event);
    }
    
    public void playerWon(String playerName, int winningAmount){
        Event event = new Event(EventType.WINNING_NUMBER);
        event.setPlayerName(playerName);
        event.setAmount(winningAmount);
        events.put(event.getId(), event);
    }

    protected List<ws.roulette.Event> getAfter(int eventId) {
        return events.entrySet().stream().filter(x -> x.getKey() > eventId).map(x -> x.getValue()).collect(Collectors.toList());
    }
}
