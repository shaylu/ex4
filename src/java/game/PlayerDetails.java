/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

/**
 *
 * @author Dell
 */
public class PlayerDetails extends ws.roulette.PlayerDetails{

    public PlayerDetails(Player player) {
        super();
        this.setName(player.getName());
        this.setType(ws.roulette.PlayerType.valueOf(player.getStatus().value()));
        this.setStatus(ws.roulette.PlayerStatus.valueOf(player.getStatus().value()));
        this.setMoney(player.money);
    }
    
}
