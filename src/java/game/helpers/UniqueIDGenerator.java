/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.helpers;

/**
 *
 * @author Dell
 */
public class UniqueIDGenerator {

    int lastGivenId;

    public UniqueIDGenerator() {
        lastGivenId = 0;
    }

    public int getNewId() {
        lastGivenId++;
        return lastGivenId;
    }
}
