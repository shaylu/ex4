/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ws.roulette.BetType;

/**
 *
 * @author Dell
 */
public class Bet {
    protected BetType type;
    protected ArrayList<Integer> numbers;
    protected int amount;
    
    public Bet(){
        numbers = new ArrayList<>();
    }
    
    public Bet(BetType type, List<Integer> numbers, int amount){
        this();
        this.type = type;
        this.numbers.addAll(numbers);
        this.amount = amount;
    }
    
    public Bet(game.jaxb.Bet bet){
        this(BetType.valueOf(bet.getType().value()), bet.getNumber(), Integer.valueOf(bet.getAmount().toString()));
    }

    public BetType getType() {
        return type;
    }

    public void setType(BetType type) {
        this.type = type;
    }

    public ArrayList<Integer> getNumbers() {
        return numbers;
    }

    public void setNumbers(ArrayList<Integer> numbers) {
        this.numbers = numbers;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.type);
        hash = 41 * hash + Objects.hashCode(this.numbers);
        hash = 41 * hash + this.amount;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Bet other = (Bet) obj;
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.numbers, other.numbers)) {
            return false;
        }
        if (this.amount != other.amount) {
            return false;
        }
        return true;
    }
    
    
}
