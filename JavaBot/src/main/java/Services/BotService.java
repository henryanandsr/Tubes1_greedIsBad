package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    boolean valid = true;
    int ctick = 0;
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    //Objek baru "worldCenter buat nandain titik di tengah2 dan supaya nggak keluar"
    Position centerPosition = new Position(0,0);
    GameObject tempTorpedo = new GameObject(null, null, null, null, null, null,null,null,null,null,null);
    GameObject worldCenter = new GameObject(null, null, null, null, centerPosition, null,null,null,null,null,null);
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
        boolean fireTeleport = false;
        boolean supernova = false;
        UUID tId;
        double tTime = -99999;
        double tDist = -99999;
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
            var nearestGasCloud = gameState.getGameObjects()
            .stream().filter(item-> item.getGameObjectType() == ObjectTypes.GAS_CLOUD)
            .sorted(Comparator
                    .comparing(item->getDistanceBetween(bot,item)))
                .collect(Collectors.toList());
            var nearestAsteroid = gameState.getGameObjects()
            .stream().filter(item->item.getGameObjectType() == ObjectTypes.ASTEROID_FIELD)
            .sorted(Comparator
                    .comparing(item->getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
            var superFoodList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPER_FOOD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot,item)))
                    .collect(Collectors.toList());
            var nearestTorpedos = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO &&  (Math.abs(getHeadingBetween(item) - item.currentHeading + 180) +360 % 360)  < 60)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot,item)))
                    .collect(Collectors.toList());
            System.out.println(nearestTorpedos);
            //Kondisi ketika menemui player lain yang sizenya lebih kecil
            // System.out.println("PLAYER 1 = " + nearestPlayer.get(0).getId());
            // System.out.println("PLAYER 2 = " + nearestPlayer.get(1).getId());
            // System.out.println("PLAYER 3 = " + nearestPlayer.get(2).getId());
            // if (nearestPlayer.get(0).getSize() < bot.getSize() && getDistanceBetween(nearestPlayer.get(0), bot) < 100)
            // {
            //     playerAction.heading = getHeadingBetween(nearestPlayer.get(0));
            //     System.out.println("I'll kill You");
            // }
            // System.out.println(gameState.getWorld().getCurrentTick());
            // System.out.println("torpedo " + bot.getTorpedoSalvo());
            // System.out.println("Shield "  + bot.shield);
            // if (fireTeleport==true)
            // {
            //     playerAction.action = PlayerActions.TELEPORT;
            //     fireTeleport = false;
            // }
            if (nearestTorpedos.size() != 0)
            {
                // // tId = nearestTorpedos.get(0).getId();
                // if (!valid)
                // {
                //     tTime = gameState.getWorld().getCurrentTick() + 2;
                //     tDist = getDistanceBetween(nearestTorpedos.get(0), bot);
                //     tempTorpedo = nearestTorpedos.get(0);
                //     valid = true;
                // }
                if (bot.getSize()>26 && getDistanceBetween(nearestTorpedos.get(0), bot) < 4*bot.getSize())
                {
                    System.out.println("testtt");
                    if (gameState.getWorld().getCurrentTick() - ctick > 20)
                    {
                        valid = true;
                    }
                    if (valid)
                    {
                        playerAction.action = PlayerActions.ACTIVATESHIELD;
                        System.out.println("SHIELDDDD");
                        ctick = gameState.getWorld().getCurrentTick();
                        valid = false;
                    }
                }
            }
            else if (getDistanceBetween(bot, nearestPlayer.get(0)) < 4*(bot.getSize()+nearestPlayer.get(0).getSize()) && bot.getTorpedoSalvo() > 0 && bot.getSize() > 30)
            {
                /*
                //Kondisi ketika sudah di ujung arena
                if (getDistanceBetween(worldCenter, bot)+1.5*bot.getSize()>gameState.world.getRadius())
                {
                    playerAction.heading = (getHeadingBetween(worldCenter) + 90 ) % 360;
                    // System.out.println("Go to save zone");
                }
                else
                //Kondisi ketika mendekat dg musuh
                {
                    */
                if (bot.getSize() > 40+nearestPlayer.get(0).getSize()){
                    playerAction.heading = getHeadingBetween(nearestPlayer.get(0));
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                    supernova = true;
                    // this.playerAction = playerAction;
                    // playerAction.heading = (getHeadingBetween(nearestPlayer.get(0)) + 180) % 360;
                    // System.out.println("Running away");
                }else{
                    playerAction.heading = getHeadingBetween(nearestPlayer.get(0)) + 540 % 360;
                    // playerAction.action = PlayerActions.FIRETORPEDOES;
                    // supernova = true;
                    // this.playerAction = playerAction;
                    // playerAction.heading = (getHeadingBetween(nearestPlayer.get(0)) + 180) % 360;
                    // System.out.println("Running away");
                    if (getDistanceBetween(nearestAsteroid.get(0), bot)<30)
                    {
                        playerAction.heading += 90 %360;
                    }
                }
                // }
            }
            //Ini masih repetisi dg yang diatas, jujur bingung buat nambahin supaya ngga ke repeat
            else if (getDistanceBetween(worldCenter, bot)+2*bot.getSize()>gameState.world.getRadius() && fireTeleport == false)
            {
                playerAction.heading = getHeadingBetween(worldCenter);
                playerAction.action = PlayerActions.FORWARD;
                // System.out.println("Go to save zone");
                // playerAction.action = PlayerActions.FIRETELEPORT;
                fireTeleport = true;
                ctick = gameState.getWorld().getCurrentTick();
            }
            else
            {
                if (getDistanceBetween(nearestGasCloud.get(0), bot) < 2*bot.getSize())
                {
                    playerAction.heading = (getHeadingBetween(nearestGasCloud.get(0))+90) %360;
                    // System.out.println("Get out from gas Cloud");
                }
                else if (getDistanceBetween(foodList.get(0),bot)<getDistanceBetween(superFoodList.get(0), bot))
                {
                    playerAction.heading = getHeadingBetween(superFoodList.get(0));
                    // System.out.println("Superfood");
                }
                else
                {
                    playerAction.heading = getHeadingBetween(foodList.get(0));
                    System.out.println("FOOOOOOOD");
                }
            }
            }
        // }
        this.playerAction = playerAction;
    }

    public GameState getGameState() {
        return this.gameState;
    }
    public int ResolveNewTarget()
    {
        var directionToFood = gameState.getGameObjects().stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD).sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        int heading = getHeadingBetween(directionToFood.get(0));
        return heading;
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
