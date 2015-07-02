/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import game.helpers.UniqueIDGenerator;
import ws.roulette.EventType;

/**
 *
 * @author Dell
 */
public class Event extends ws.roulette.Event{
    public static final UniqueIDGenerator idsGenerator;
    
    static {
        idsGenerator = new UniqueIDGenerator();
    }
    
    public Event(EventType type) {
        super();
        id = idsGenerator.getNewId();
        this.type = type;
    }
}
