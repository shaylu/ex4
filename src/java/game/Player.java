/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import game.helpers.UniqueIDGenerator;
import java.util.Objects;
import ws.roulette.PlayerStatus;
import ws.roulette.PlayerType;

/**
 *
 * @author Dell
 */
public class Player {
    public static final game.helpers.UniqueIDGenerator idsGenerator;
    static {
        idsGenerator = new UniqueIDGenerator();
    }
    
    protected int id;
    protected String name;
    protected PlayerType type;
    protected ws.roulette.PlayerStatus status;
    protected int money;
    protected Bets bets;
    protected boolean finishedPlacingBets;
    protected boolean nameUsed; // on xml loaded games this will be true if someone took this name

    public Player() {
        id = idsGenerator.getNewId();
        name = "Undefined";
        type = PlayerType.COMPUTER;
        status = PlayerStatus.JOINED;
        money = 0;
        bets = new Bets();
        
        reset();
    }
    
    public Player(String name, PlayerType type, int money){
        this();
        this.name = name;
        this.type = type;
        this.money = money;
    }
    
    public Player(game.jaxb.Players.Player player) throws Exception{
        this(player.getName(), PlayerType.valueOf(player.getType().value()), Integer.valueOf(player.getMoney().toString()));
        //bets = new Bets(player.getBets());
        this.nameUsed = false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayerType getType() {
        return type;
    }

    public void setType(PlayerType type) {
        this.type = type;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public Bets getBets() {
        return bets;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.id;
        hash = 23 * hash + Objects.hashCode(this.name);
        hash = 23 * hash + Objects.hashCode(this.type);
        hash = 23 * hash + Objects.hashCode(this.status);
        hash = 23 * hash + this.money;
        hash = 23 * hash + Objects.hashCode(this.bets);
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
        final Player other = (Player) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (this.status != other.status) {
            return false;
        }
        if (this.money != other.money) {
            return false;
        }
        if (!Objects.equals(this.bets, other.bets)) {
            return false;
        }
        return true;
    }
    
    public void reset(){
        bets.clear();
        finishedPlacingBets = false;
    }
    
    public PlayerDetails getPlayerDetails(){
        return new PlayerDetails(this);
    }

    public void setNameUsed(boolean nameUsed) {
        this.nameUsed = nameUsed;
    }
    
    
}
