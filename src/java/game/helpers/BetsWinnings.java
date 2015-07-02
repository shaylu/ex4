/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.helpers;

import game.Bet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ws.roulette.RouletteType;

/**
 *
 * @author Shay
 */
public class BetsWinnings {

    private static boolean arrayContains(List<Integer> array, int winningNumber) {
        return array.stream().filter(x -> x == winningNumber).findFirst().isPresent();
    }

    public enum ColorOfNumber {

        RED,
        GREEN,
        BLACK;

        public static final List<Integer> reds = Arrays.asList(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36);
        public static final List<Integer> blacks = Arrays.asList(2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 34, 35);
        public static final List<Integer> greens = Arrays.asList(0, 37);

        public static ColorOfNumber get(int number) {
            if (arrayContains(greens, number)) {
                return GREEN;
            } else if (arrayContains(reds, number)) {
                return RED;
            } else {
                return BLACK;
            }
        }

        public static int getNumberOfReds() {
            return reds.size();
        }

        public static int getNumberOfBlacks() {
            return blacks.size();
        }
    }

    public static int calcWinning(RouletteType rouletteType, int winningNumber, ArrayList<Bet> bets) {
        int NUMBERS_ON_WHEEL = 36;
        int winnings = 0;
        for (Bet bet : bets) {
            switch (bet.getType()) {
                case MANQUE:
                    if (winningNumber >= 1 && winningNumber <= 18) {
                        winnings += payout(18, bet.getAmount(), rouletteType);
                    }
                    break;
                case PASSE:
                    if (winningNumber >= 19 && winningNumber <= 36) {
                        winnings += payout(18, bet.getAmount(), rouletteType);
                    }
                    break;
                case ROUGE:
                    if (ColorOfNumber.get(winningNumber) == ColorOfNumber.RED) {
                        winnings += payout(ColorOfNumber.getNumberOfReds(), bet.getAmount(), rouletteType);
                    }
                    break;
                case NOIR:
                    if (ColorOfNumber.get(winningNumber) == ColorOfNumber.BLACK) {
                        winnings += payout(ColorOfNumber.getNumberOfBlacks(), bet.getAmount(), rouletteType);
                    }
                    break;
                case PAIR:
                    if (winningNumber % 2 == 0) {
                        winnings += payout(NUMBERS_ON_WHEEL / 2, bet.getAmount(), rouletteType);
                    }
                    break;
                case IMPAIR:
                    if (winningNumber % 2 == 1) {
                        winnings += payout(NUMBERS_ON_WHEEL / 2, bet.getAmount(), rouletteType);
                    }
                    break;
                case PREMIERE_DOUZAINE:
                    if (winningNumber >= 1 && winningNumber <= 12) {
                        winnings += payout(12, bet.getAmount(), rouletteType);
                    }
                    break;
                case MOYENNE_DOUZAINE:
                    if (winningNumber >= 13 && winningNumber <= 24) {
                        winnings += payout(12, bet.getAmount(), rouletteType);
                    }
                    break;
                case DERNIERE_DOUZAINE:
                    if (winningNumber >= 25 && winningNumber <= 36) {
                        winnings += payout(12, bet.getAmount(), rouletteType);
                    }
                    break;
                case COLUMN_1:
                    if (arrayContains(Arrays.asList(1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34), winningNumber) == true) {
                        winnings += payout(12, bet.getAmount(), rouletteType);
                    }
                    break;
                case COLUMN_2:
                    if (arrayContains(Arrays.asList(2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35), winningNumber) == true) {
                        winnings += payout(12, bet.getAmount(), rouletteType);
                    }
                    break;
                case COLUMN_3:
                    if (arrayContains(Arrays.asList(3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36), winningNumber) == true) {
                        winnings += payout(12, bet.getAmount(), rouletteType);
                    }
                    break;
                case SNAKE:
                    if (arrayContains(Arrays.asList(1, 5, 9, 12, 14, 16, 19, 23, 27, 30, 32, 34), winningNumber) == true) {
                        winnings += payout(12, bet.getAmount(), rouletteType);
                    }
                    break;
                default:
                    if (arrayContains(bet.getNumbers(), winningNumber) == true) {
                        winnings += payout(bet.getNumbers().size(), bet.getAmount(), rouletteType);
                    }
                    break;
            }
        }

        return winnings;
    }

    public static int payout(int numberOfNumbersPlaced, int amountOfMoney, RouletteType rouletteType) {
        int numbersOnRoulette = (rouletteType == RouletteType.FRENCH) ? 36 : 37;
        return amountOfMoney * ((numbersOnRoulette / numberOfNumbersPlaced) - 1);
    }
}
