package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    //Objek baru "worldCenter buat nandain titik di tengah2 dan supaya nggak keluar"
    Position centerPosition = new Position(0,0);
    GameObject worldCenter = new GameObject(null, null, null, null, centerPosition, null);

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }


    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);
        System.out.println("----------");
        if (!gameState.getGameObjects().isEmpty()) 
        {
            // List yang isinya game Object "FOOD", terurut dg indeks pertama yg distancenya paling kecil
            var foodList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
            //List yang isinya objek "Player", terurut dg indeks pertama yg distancenya paling kecil
            var nearestPlayer = gameState.getPlayerGameObjects()
                .stream().filter(item -> item.getId() != bot.getId())
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
            //Kondisi ketika menemui player lain yang sizenya lebih kecil
            if (nearestPlayer.get(0).getSize() < bot.getSize())
            {
                playerAction.heading = getHeadingBetween(nearestPlayer.get(0));
                System.out.println("I'll kill You");
            }
            else if (getDistanceBetween(bot, nearestPlayer.get(0))<30)
            {
                //Kondisi ketika sudah di ujung arena
                if (getDistanceBetween(worldCenter, bot)+1.5*bot.getSize()>gameState.world.getRadius())
                {
                    playerAction.heading = getHeadingBetween(worldCenter);
                    System.out.println("Go to save zone");
                }
                else
                //Kondisi ketika mendekat dg musuh
                {
                    playerAction.heading = (getHeadingBetween(nearestPlayer.get(0)) + 180) % 360;
                    System.out.println("Running away");
                }
            }
            //Ini masih repetisi dg yang diatas, jujur bingung buat nambahin supaya ngga ke repeat
            else if (getDistanceBetween(worldCenter, bot)+1.5*bot.getSize()>gameState.world.getRadius())
            {
                playerAction.heading = getHeadingBetween(worldCenter);
            }
            else
            {
                playerAction.heading = getHeadingBetween(foodList.get(0));
            }
        }
        this.playerAction = playerAction;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }


}
