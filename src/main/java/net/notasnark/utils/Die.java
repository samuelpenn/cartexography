/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.utils;

public class Die {

    /**
     * Generate a random integer between 1 and the value given.
     *
     * @param size  Size of die to roll.
     * @return      Random integer between 1 and the size given.
     */
    private static int roll(int size) {
        return (int)(Math.random() * size) + 1;
    }

    /**
     * Generate a random long value between 1 and the value given. This is for use when the size of
     * the number needed is bigger than that allowed by an integer.
     *
     * @param size  Size of die to roll.
     * @return      Random long between 1 and the size given.
     */
    private static long roll(long size) {
        return (long)(Math.random() * size) + 1;
    }

    /**
     * Generate the result of a single d20.
     *
     * @return      A random number.
     */
    public static int die() {
        return roll(20);
    }

    /**
     * Generate result of a variance using two rolls, one positive and
     * one negative.
     *
     * @param size      Size of the die to be rolled.
     * @return          Random result, between -(Size-1) and +(Size-1)
     */
    public static int dieV(int size) {
        return roll(size) - roll(size);
    }

    public static long dieV(long size) {
        return roll(size) - roll(size);
    }

    /**
     * Gets the result of rolling one or more dice of the specified size.
     * If a number of dice is not specified, then it is assumed to be 1.
     * The result returned is the total of all rolls.
     *
     * @param size      Size of the dice to be rolled.
     * @param number    Optional number of dice to roll.
     *
     * @return          Random result.
     */
    public static int die(int size, int... number) {
        if (number == null || number.length == 0) {
            return roll(size);
        } else {
            int total = 0;

            for (int i=0; i < number[0]; i++) {
                total += roll(size);
            }

            return total;
        }
    }

    public static long die(long size, int... number) {
        if (number == null || number.length == 0) {
            return roll(size);
        } else {
            long total = 0;

            for (int i=0; i < number[0]; i++) {
                total += roll(size);
            }

            return total;
        }
    }

    /**
     * Roll a number of 2 sided dice, and total the rolls together.
     *
     * @param number    Number of dice to roll, or 1 if not specified.
     * @return          Sum of all the dice rolled.
     */
    public static int d2(int... number) {
        return die(2, number);
    }

    /**
     * Roll a number of 3 sided dice, and total the rolls together.
     *
     * @param number    Number of dice to roll, or 1 if not specified.
     * @return          Sum of all the dice rolled.
     */
    public static int d3(int... number) {
        return die(3, number);
    }

    /**
     * Roll a number of 4 sided dice, and total the rolls together.
     *
     * @param number    Number of dice to roll, or 1 if not specified.
     * @return          Sum of all the dice rolled.
     */
    public static int d4(int... number) {
        return die(4, number);
    }

    /**
     * Roll a number of 6 sided dice, and total the rolls together.
     *
     * @param number    Number of dice to roll, or 1 if not specified.
     * @return          Sum of all the dice rolled.
     */
    public static int d6(int... number) {
        return die(6, number);
    }

    /**
     * Roll a number of 8 sided dice, and total the rolls together.
     *
     * @param number    Number of dice to roll, or 1 if not specified.
     * @return          Sum of all the dice rolled.
     */
    public static int d8(int... number) {
        return die(8, number);
    }

    /**
     * Roll a number of 10 sided dice, and total the rolls together.
     *
     * @param number    Number of dice to roll, or 1 if not specified.
     * @return          Sum of all the dice rolled.
     */
    public static int d10(int... number) {
        return die(10, number);
    }

    /**
     * Roll a number of 12 sided dice, and total the rolls together.
     *
     * @param number    Number of dice to roll, or 1 if not specified.
     * @return          Sum of all the dice rolled.
     */
    public static int d12(int... number) {
        return die(12, number);
    }

    /**
     * Roll a number of 20 sided dice, and total the rolls together.
     *
     * @param number    Number of dice to roll, or 1 if not specified.
     * @return          Sum of all the dice rolled.
     */
    public static int d20(int... number) {
        return die(20, number);
    }

    /**
     * Roll a number of 100 sided dice, and total the rolls together.
     *
     * @param number    Number of dice to roll, or 1 if not specified.
     * @return          Sum of all the dice rolled.
     */
    public static int d100(int... number) {
        return die(100, number);
    }

    public static int rollZero(int size) {
        return (int)(Math.random()*size);
    }


    public static boolean isRoll(String dice) {
        return dice.matches("[0-9]+D[0-9]+([-+][0-9]+)?");
    }

    public static int roll(String dice) {
        int number = 0;
        int type = 0;
        int modifier = 0;

        if (isRoll(dice)) {
            number = Integer.parseInt(dice.replaceAll("([0-9]+)D.*", "$1"));
            type = Integer.parseInt(dice.replaceAll("[0-9]+D([0-9]+).*", "$1"));
            if (dice.matches(".*[-+].*")) {
                modifier = Integer.parseInt(dice.replaceAll("[0-9]+D[0-9]+(.*)", "$1"));
            }
            return die(type, number) + modifier;
        }

        return 0;
    }

    /**
     * Gets a random number between 0.000 and 0.999. Number will be to three decimal
     * places.
     *
     * @return  Random value between 0.000 and 0.999.
     */
    public static double milli() {
        return rollZero(1000) / 1000.0;
    }
}
