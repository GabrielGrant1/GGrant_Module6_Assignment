package edu.wctc;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiceGame {
    private final List<Player> players;
    private final List<Die> dice;
    private final int maxRolls;
    private Player currentPlayer;

    public DiceGame(int countPlayers, int countDice, int maxRolls) throws IllegalArgumentException{
        players = new ArrayList<>();
        dice = new ArrayList<>();

        if(countPlayers > 2) {
            for (int i = 0; i < countPlayers; i++){
                players.add(new Player());
            }
            for (int i = 0; i < countDice; i++){
                dice.add(new Die(6));
            }
            this.maxRolls = maxRolls;
        }else{
            throw new IllegalArgumentException();
        }

    }

    private boolean allDiceHeld() {
        return dice.stream().allMatch(Die::isBeingHeld);
    }

    public boolean autoHold(int faceValue){
        //search for held dice first
        Optional<Die> dval = dice.stream().filter(Die::isBeingHeld).filter(die -> die.getFaceValue() == faceValue).findFirst();
        int value = dval.isPresent() ? faceValue : 0;
        if(value == faceValue){
            return true;
        }else if(value == 0){
            //search for unheld dice second
            //This seems to ignore dice that should be held in some cases (for example, 6 is not present so 4 and 5 are ignored)
            Optional<Die> dval2 = dice.stream().filter(die -> die.getFaceValue() == faceValue).findFirst();
            int value2 = dval2.isPresent() ? faceValue : 0;
            if(value2 == faceValue) {
                dval2.get().holdDie();
                return true;
            }
        }
        return false;
    }

    public boolean currentPlayerCanRoll(){
        boolean rollsRemain = true;
        if(currentPlayer.getRollsUsed() >= maxRolls){
            rollsRemain = false;
        }

        if(rollsRemain && !allDiceHeld()){
            return true;
        }else{
            return false;
        }
    }

    public int getCurrentPlayerNumber(){
        return currentPlayer.getPlayerNumber();
    }

    public int getCurrentPlayerScore(){
        return currentPlayer.getScore();
    }

    public String getDiceResults(){
        return dice.stream().map(Die::toString).collect(Collectors.joining());
    }

    public String getFinalWinner(){
        Optional<Player> winner = players.stream().max(Comparator.comparingInt(Player::getWins));
        return winner.toString();
    }

    public String getGameResults(){
        Stream<Player> playerList = players.stream().sorted(Comparator.comparingInt(Player::getWins).reversed());
        int highscore = players.stream().mapToInt(Player::getScore).max().getAsInt();
        Stream<Player> winners = players.stream().filter(player -> player.getScore() == highscore);
        winners.forEach(Player::addWin);
        Stream<Player> losers = players.stream().filter(player -> player.getScore() != highscore);
        losers.forEach(Player::addLoss);
        String gameResults = playerList.map(Player::toString).collect(Collectors.joining());
        return gameResults;
    }

    private boolean isHoldingDie(int faceValue){
        Optional<Die> dval = dice.stream().findFirst().filter(die -> die.getFaceValue() == faceValue);
        int value = dval.isPresent() ? faceValue : 0;
        if(value != 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean nextPlayer(){
        if(players.size() > currentPlayer.getPlayerNumber()){
            currentPlayer = players.get(currentPlayer.getPlayerNumber());
            return true;
        }else{
            return false;
        }
    }

    public void playerHold(char dieNum){
        Optional<Die> dnum = dice.stream().filter(die -> die.getDieNum() == dieNum).findFirst();
        int value = dnum.isPresent() ? dieNum : 0;
        if(value != 0){
            dnum.get().holdDie();
        }
    }

    public void resetDice(){
        dice.stream().forEach(Die::resetDie);
    }

    public void resetPlayers(){
        players.stream().forEach(Player::resetPlayer);
    }

    public void rollDice(){
        currentPlayer.roll();
        dice.stream().forEach(Die::rollDie);
    }

    public void scoreCurrentPlayer(){

        if(isHoldingDie(6) && isHoldingDie(5) && isHoldingDie(4)){
            int total = 0;
            for(Die d : dice){
                total += d.getFaceValue();
            }
            currentPlayer.setScore(currentPlayer.getScore()+total-15);
        }else{
            currentPlayer.setScore(currentPlayer.getScore());
        }
    }

    public void startNewGame(){
        currentPlayer = players.get(0);
        resetPlayers();
    }
}
