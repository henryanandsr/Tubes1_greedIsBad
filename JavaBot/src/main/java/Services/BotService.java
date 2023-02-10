package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    boolean valid = true;
    int ctick = 0;
    static int wt = -1;
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

    public void computeNextPlayerAction(PlayerAction playerAction, abc) {
        if (gameState.getWorld().getCurrentTick()!=null){
            if (gameState.getWorld().getCurrentTick()!=wt){
                System.out.println(gameState.getWorld().getCurrentTick());
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = new Random().nextInt(360);
                boolean fireTeleport = false;
                boolean supernova = false;
                UUID tId;
                double tTime = -99999;
                double tDist = -99999;
                if (!gameState.getGameObjects().isEmpty()){
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


                    if (getDistanceBetween(bot, nearestPlayer.get(0)) < (7*bot.getSize()+nearestPlayer.get(0).getSize())){
                        if (nearestTorpedos.size() != 0){
                            System.out.println("MASUK SHIELD COMBAT MODE");
                            if ((bot.shield > 0) && (bot.getSize()>26) && ((getDistanceBetween(nearestTorpedos.get(0), bot) < (bot.getSize()+85)))){
                                System.out.println("testtt");
                                if (!valid && (gameState.getWorld().getCurrentTick() - ctick >= 20)){
                                    valid = true;
                                }
                                if (valid){
                                    playerAction.action = PlayerActions.ACTIVATESHIELD;
                                    playerAction.heading = getHeadingBetween(worldCenter);
                                    System.out.println("SHIELDDDD");
                                    ctick = gameState.getWorld().getCurrentTick();
                                    valid = false;
                                }
                            }else{
                                playerAction.action = PlayerActions.FORWARD;
                                playerAction.heading = getHeadingBetween(nearestTorpedos.get(0)) + 450 % 360;
                            }
                        }
                        else if (getDistanceBetween(bot, nearestPlayer.get(0)) < (5*bot.getSize()+nearestPlayer.get(0).getSize()) && (bot.getSize() < nearestPlayer.get(0).getSize())){
                            if (bot.getTorpedoSalvo() > 0 && bot.getSize() > 30){
                                playerAction.heading = getHeadingBetween(nearestPlayer.get(0));
                                playerAction.action = PlayerActions.FIRETORPEDOES;
                                System.out.println("SMALLER, SHOOOT!");
                            }
                            else{
                                playerAction.action = PlayerActions.FORWARD;
                                playerAction.heading = getHeadingBetween(nearestPlayer.get(0)) + 540 % 360;
                                if (getDistanceBetween(worldCenter, bot)+2*bot.getSize()>gameState.world.getRadius()){
                                    int tempHeading;
                                    if (getHeadingBetween(nearestPlayer.get(0))>=270 || (getHeadingBetween(nearestPlayer.get(0))>=90 && getHeadingBetween(nearestPlayer.get(0)) < 180))
                                    {
                                        tempHeading = getHeadingBetween(nearestPlayer.get(0)) + 90 % 360;
                                    }else{
                                        tempHeading = getHeadingBetween(nearestPlayer.get(0)) - 90 % 360;
                                    }
                                    playerAction.heading = tempHeading;
                                    playerAction.action = PlayerActions.FORWARD;
                                }
                                
                                else if (getDistanceBetween(nearestGasCloud.get(0), bot) < (bot.getSize()+nearestGasCloud.get(0).getSize()+60))
                                {
                                    playerAction.heading = (getHeadingBetween(nearestGasCloud.get(0))+90) %360;
                                }
                                else if (getDistanceBetween(nearestAsteroid.get(0), bot)<30+bot.getSize()+nearestAsteroid.get(0).getSize())
                                {
                                    playerAction.heading += 90 %360;
                                }
                            }
                        }
                        else if (getDistanceBetween(bot, nearestPlayer.get(0)) < (7*bot.getSize()+nearestPlayer.get(0).getSize())){
                            if (bot.getSize() > nearestPlayer.get(0).getSize()){
                                playerAction.heading = getHeadingBetween(nearestPlayer.get(0));
                                if (bot.getTorpedoSalvo() > 0 && bot.getSize() > 30){
                                    playerAction.action = PlayerActions.FIRETORPEDOES;
                                    System.out.println("BIGGER, SHOOOT!");
                                }else{
                                    playerAction.action = PlayerActions.FORWARD;
                                    System.out.println("CHARGE");
                                }
                            }else{
                                idle(nearestTorpedos, nearestGasCloud, foodList, superFoodList);
                            }       
                        }else{
                            idle(nearestTorpedos, nearestGasCloud, foodList, superFoodList);
                        }
                    }else{
                        System.out.println("MASUK IDLE MODE");
                        idle(nearestTorpedos, nearestGasCloud, foodList, superFoodList);
                        // System.out.println("MASUK IDLE MODE");
                        // if (nearestTorpedos.size() != 0){
                        //     // System.out.println("MASUK SHIELD IDLE MODE");
                        //     if (bot.getSize()>26 && (getDistanceBetween(nearestTorpedos.get(0), bot) < (85+bot.getSize()))){
                        //         // System.out.println("testtt");
                        //         if (gameState.getWorld().getCurrentTick() - ctick > 20){
                        //             valid = true;
                        //         }
                        //         if (valid && bot.shield > 0){
                        //             playerAction.action = PlayerActions.ACTIVATESHIELD;
                        //             playerAction.heading = getHeadingBetween(worldCenter);
                        //             // System.out.println("SHIELDDDD");
                        //             ctick = gameState.getWorld().getCurrentTick();
                        //             valid = false;
                        //         }
                        //         // else{
                        //         //     playerAction.action = PlayerActions.STARTAFTERBURNER;
                        //         //     playerAction.heading = getHeadingBetween(nearestTorpedos.get(0)) + 450 % 360;
                        //         // }
                        //     }
                        // }
                        // else if (getDistanceBetween(worldCenter, bot)+2.5*bot.getSize()>gameState.world.getRadius()){
                        //     playerAction.heading = getHeadingBetween(worldCenter);
                        // }
                        // else if (getDistanceBetween(nearestGasCloud.get(0), bot) < (80+bot.getSize()+nearestGasCloud.get(0).getSize())){
                        //     if (getDistanceBetween(nearestGasCloud.get(0), bot)<=(1+bot.getSize()+nearestGasCloud.get(0).getSize())){
                        //         playerAction.heading = (getHeadingBetween(nearestGasCloud.get(0))+180) %360;
                        //     }else{
                        //         playerAction.heading = (getHeadingBetween(nearestGasCloud.get(0))+90) %360;
                        //     }
                        // }
                        // else if (getDistanceBetween(superFoodList.get(0),bot)<getDistanceBetween(foodList.get(0), bot)){
                        //     playerAction.heading = getHeadingBetween(superFoodList.get(0));
                        // }
                        // else{
                        //     playerAction.heading = getHeadingBetween(foodList.get(0));
                        // }
                    }   
                }
                wt = gameState.getWorld().getCurrentTick();
                this.playerAction = playerAction;
            }
        }
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
    public void idle(List<GameObject> nearestTorpedos, List<GameObject> nearestGasCloud, List<GameObject> foodList, List<GameObject> superFoodList){
        playerAction.action = PlayerActions.FORWARD;
        System.out.println("MASUK FUNC IDLE MODE");
        
        if (nearestTorpedos.size() != 0){
            System.out.println("MASUK SHIELD IDLE MODE");
            if (getDistanceBetween(nearestTorpedos.get(0), bot) < (85+bot.getSize())){
                System.out.println("TORPEDOS COMING");
                if (bot.getSize()>26){
                    if (gameState.getWorld().getCurrentTick() - ctick > 20){
                        valid = true;
                    }
                    if (valid && bot.shield > 0){
                        playerAction.action = PlayerActions.ACTIVATESHIELD;
                        playerAction.heading = getHeadingBetween(worldCenter);
                        System.out.println("SHIELDDDD");
                        ctick = gameState.getWorld().getCurrentTick();
                        valid = false;
                    }else{
                        playerAction.action = PlayerActions.STARTAFTERBURNER;
                        playerAction.heading = getHeadingBetween(nearestTorpedos.get(0)) + 450 % 360;    
                    }
                }else{
                    playerAction.heading = getHeadingBetween(nearestTorpedos.get(0)) + 450 % 360;
                }
            }
        }
        else if (getDistanceBetween(worldCenter, bot)+2.5*bot.getSize()>gameState.world.getRadius()){
            playerAction.heading = getHeadingBetween(worldCenter);
        }
        else if (getDistanceBetween(nearestGasCloud.get(0), bot) < (80+bot.getSize()+nearestGasCloud.get(0).getSize())){
            if (getDistanceBetween(nearestGasCloud.get(0), bot)<=(1+bot.getSize()+nearestGasCloud.get(0).getSize())){
                playerAction.heading = (getHeadingBetween(nearestGasCloud.get(0))+180) %360;
            }else{
                playerAction.heading = (getHeadingBetween(nearestGasCloud.get(0))+90) %360;
            }
        }
        else if (getDistanceBetween(superFoodList.get(0),bot)<getDistanceBetween(foodList.get(0), bot)){
            playerAction.heading = getHeadingBetween(superFoodList.get(0));
        }
        else{
            playerAction.heading = getHeadingBetween(foodList.get(0));
        }
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