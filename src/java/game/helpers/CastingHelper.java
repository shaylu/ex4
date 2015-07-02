/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.helpers;

/**
 *
 * @author Shay
 */
public class CastingHelper {
    public static ws.roulette.BetType cast(game.jaxb.BetType givenBetType){
        return ws.roulette.BetType.valueOf(givenBetType.name());
    }
}
