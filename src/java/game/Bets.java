/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.util.ArrayList;
import java.util.List;
import ws.roulette.BetType;
import ws.roulette.RouletteType;

/**
 *
 * @author Dell
 */
public class Bets {
    
    protected ArrayList<Bet> bets;

    public Bets() {
        this.bets = new ArrayList<>();
    }

    public Bets(game.jaxb.Bets bets) throws Exception {
        this();
        List<game.jaxb.Bet> jaxbBets = bets.getBet();
        for (game.jaxb.Bet jaxbBet : jaxbBets) {
            Bet bet = new Bet(jaxbBet);
            add(bet);
        }
    }

    public void add(Bet bet) throws Exception {
        bets.add(bet);
    }

    public void add(BetType type, List<Integer> numbers, int amount) throws Exception {
        Bet bet = new Bet(type, numbers, amount);
        add(bet);
    }
    
    public void clear(){
        bets.clear();
    }
    
}
