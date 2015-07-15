/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import ws.roulette.BetType;
import ws.roulette.RouletteType;

/**
 *
 * @author Dell
 */
public class BetsValidator {

    public static final int ZEROS_ROW = 0;
    public static final int LOWER_ROW = 1;
    public static final int MIDDLE_ROW = 2;
    public static final int TOP_ROW = 3;

    public RouletteType rouletteType;

    public BetsValidator(RouletteType rouletteType) {
        this.rouletteType = rouletteType;
    }

    public boolean isBetValid(BetType type, List<Integer> numbers) {
        switch (type) {
            case STRAIGHT:
                return validateStraight(numbers);
            case SPLIT:
                return validateSplit(numbers);
            case STREET:
                return validateStreet(numbers);
            case CORNER:
                return validateCorner(numbers);
            case SIX_LINE:
                return validateSixLine(numbers);
            case TRIO:
                return validateTrio(numbers);
            case BASKET:
                return validateBasket(numbers);
            case TOP_LINE:
                return validateTopLine(numbers);
            default:
                return true;
        }
    }

    protected int maxValue() {
        return (rouletteType == RouletteType.AMERICAN) ? 37 : 36;
    }

    protected int minValue() {
        return 0;
    }

    protected boolean validateStraight(List<Integer> numbers) {
        boolean res = false;
        if (numbers.size() == 1) {
            int numberSelected = numbers.get(0);
            if (numberSelected >= minValue() && numberSelected <= maxValue()) {
                res = true;
            }
        }

        return res;
    }

    protected boolean validateSplit(List<Integer> numbers) {
        boolean res = false;
        if (numbers.size() == 2) {

            int firstNumber = numbers.get(0);
            int secondNumber = numbers.get(1);
            if (isNextToEachOther(firstNumber, secondNumber) == true) {
                res = true;
            }
        }

        return res;
    }

    protected boolean validateStreet(List<Integer> numbers) {
        boolean res = false;
        if (numbers.size() == 3) {

            ArrayList<Integer> sorted = createSortedList(numbers.toArray(new Integer[numbers.size()]));
            if (getNumberRow(sorted.get(0)) == LOWER_ROW && getNumberRow(sorted.get(1)) == MIDDLE_ROW && getNumberRow(sorted.get(2)) == TOP_ROW) {
                res = true;
            }
        }
        return res;
    }

    protected boolean validateCorner(List<Integer> numbers) {
        boolean res = false;
        if (numbers.size() == 4) {
            ArrayList<Integer> sorted = createSortedList(numbers.toArray(new Integer[numbers.size()]));

            int first = sorted.get(0);
            int firstRow = getNumberRow(first);
            int second = sorted.get(1);
            int third = sorted.get(2);
            int thirdRow = getNumberRow(third);
            int forth = sorted.get(3);

            if ((thirdRow == firstRow) && (third == first + 3) && (first + 1 == second) && (third + 1 == forth)) {
                res = true;
            }
        }
        return res;
    }

    protected boolean validateSixLine(List<Integer> numbers) {
        boolean res = false;
        if (numbers.size() == 6) {
            ArrayList<Integer> sorted = createSortedList(numbers.toArray(new Integer[numbers.size()]));
            int first = sorted.get(0);
            if (getNumberRow(first) == LOWER_ROW) {
                boolean flag = true;
                for (int i = 0; i < 6; i++) {
                    int sortedNumber = sorted.get(i);
                    if (sortedNumber != first + i) {
                        flag = false;
                    }
                }

                if (flag == true) {
                    // only if the first number is in the lower row and the numbers following then the bet is ok
                    res = true;
                }
            }
        }
        return res;
    }

    protected boolean validateTrio(List<Integer> numbers) {
        boolean res = false;
        if (numbers.size() == 3) {
            ArrayList<Integer> sorted = createSortedList(numbers.toArray(new Integer[numbers.size()]));
            int first = sorted.get(0), second = sorted.get(1), third = sorted.get(2);

            if (first == 0 && second == 1 && third == 2) {
                res = true;
            } else if (first == 0 && second == 2 && third == 3) {
                res = true;
            }
        }
        return res;
    }

    protected boolean validateBasket(List<Integer> numbers) {
        boolean res = false;
        if (numbers.size() == 3) {
            ArrayList<Integer> sorted = createSortedList(numbers.toArray(new Integer[numbers.size()]));
            int first = sorted.get(0), second = sorted.get(1), third = sorted.get(2);

            if (first == 0 && second == 1 && third == 2) {
                res = true;
            } else if (first == 0 && second == 2 && third == 37) {
                res = true;
            } else if (first == 2 && second == 3 && third == 37) {
                res = true;
            }
        }
        return res;
    }

    protected boolean validateTopLine(List<Integer> numbers) {
        boolean res = false;
        if (numbers.size() == 5) {
            ArrayList<Integer> sorted = createSortedList(numbers.toArray(new Integer[numbers.size()]));
            int first = sorted.get(0), second = sorted.get(1), third = sorted.get(2), forth = sorted.get(3), fifth = sorted.get(4);

            if (first == 0 && second == 1 && third == 2 && forth == 3 && fifth == 37) {
                res = true;
            }
        }
        return res;
    }

    private int getNumberRow(int number) {
        if (number == 0 || number == 37) {
            return ZEROS_ROW;
        } else if (number % 3 == 1) {
            return LOWER_ROW;
        } else if (number % 3 == 2) {
            return MIDDLE_ROW;
        } else {
            return TOP_ROW;
        }
    }

    private int getNumberCol(int firstNumber) {
        return (firstNumber / 3 + 1);
    }

    private boolean isNextToEachOther(int first, int second) {
        ArrayList<Integer> sorted = createSortedList(first, second);
        if (first == 0 && rouletteType == RouletteType.FRENCH) {
            return (second == 1 || second == 2 | second == 3);
        } else if (first == 0 && rouletteType == rouletteType.AMERICAN) {
            return (second == 1 || second == 2 | second == 37);
        } else if (second == 37) {
            return (second == 0 || second == 2 | second == 3);
        } else {
            int firstRow = getNumberRow(first);
            int firstCol = getNumberCol(first);
            int secondRow = getNumberRow(second);
            int secondCol = getNumberCol(second);

            if (firstRow == secondRow) // same row
            {
                return (secondCol == firstCol + 1);
            } else { // diffrent rows
                return (secondRow == firstRow + 1);
            }
        }
    }

    private ArrayList<Integer> createSortedList(Integer... numbers) {
        if (numbers.length == 0) {
            return new ArrayList<>();
        }

        ArrayList<Integer> res = new ArrayList<>(Arrays.asList(numbers));
        res.sort((o1, o2) -> {
            return o1 - o2;
        });
        return res;
    }
}
