package Services;

import Enums.*;
import Models.*;

import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.*;

import org.w3c.dom.html.HTMLHeadingElement;

public class BotService {
    boolean valid = true;
    int ctick = 0;
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    boolean evade = false;
    int evadetick = 0;
    boolean fireTele = true;
    int tempTick = 0;
    double getTime = 999; 
    boolean alrdFire = false;
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
        boolean fireTeleport = false;
        boolean supernova = false;
        UUID tId;
        double tTime = -99999;
        double tDist = -99999;
        if (!gameState.getGameObjects().isEmpty()) 
        {
            if(gameState.getWorld().getCurrentTick()==tempTick)
            {
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = new Random().nextInt(360);
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
                GameObject scanMusuh = nearestPlayer.get(0); //musuh terdekat kita (untuk menentukan attack/defense)
                if (gameState.getWorld().getCurrentTick() != null)
                {
                    if(alrdFire && gameState.getWorld().getCurrentTick()>getTime)
                    {
                        
                        // tempTele = tempListTele.get(0);
                        playerAction.action = PlayerActions.TELEPORT;
                        System.out.println("tele");
                        alrdFire = false;
                    }
                        if ((gameState.getWorld().getCurrentTick() > evadetick + 5) && evade)
                        {
                            playerAction.action = PlayerActions.STOPAFTERBURNER;
                            evade = false;
                        }
                    else if (getDistanceBetween(nearestPlayer.get(0), bot)<100)
                    {
                        playerAction.heading = getHeadingBetween(nearestPlayer.get(0));
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        //kurang defense disini
                    }
                    else if (nearestPlayer.get(0).getSize() < bot.getSize()-40 && fireTele && bot.getSize()>26)
                    {
                        playerAction.heading = getHeadingBetween(nearestPlayer.get(0));
                        playerAction.action = PlayerActions.FIRETELEPORT;
                        alrdFire = true;
                        fireTele = false;
                        getTime = (getDistanceBetween(nearestPlayer.get(0), bot)-bot.getSize() - nearestPlayer.get(0).getSize())/20 + gameState.getWorld().getCurrentTick();
                        System.out.println("fire tele");
                    }
                    else if (getDistanceBetween(bot, scanMusuh) <= 1.5*(bot.getSize() + scanMusuh.getSize()))
                    {
                        System.out.println("Combat activated, defaulting to defense mode");
                        if (getDistanceBetween(bot, nearestPlayer.get(2)) <= 2*bot.getSize()
                        && getHeadingBetween(scanMusuh) - getHeadingBetween(nearestPlayer.get(1)) <= 270 && getHeadingBetween(scanMusuh) - getHeadingBetween(nearestPlayer.get(1)) >= 90 
                        && getHeadingBetween(nearestPlayer.get(1)) - getHeadingBetween(nearestPlayer.get(2)) <= 270 && getHeadingBetween(nearestPlayer.get(1)) - getHeadingBetween(nearestPlayer.get(2)) >= 90
                        && getHeadingBetween(scanMusuh) - getHeadingBetween(nearestPlayer.get(2)) <= 270 && getHeadingBetween(scanMusuh) - getHeadingBetween(nearestPlayer.get(2)) >= 90
                        ){
                            System.out.println("Terkepung!");
                            if (bot.fireTeleport > 0){
                                System.out.println("Firing teleport");
                                playerAction.heading = getHeadingBetween(worldCenter);
                                playerAction.action = PlayerActions.FIRETELEPORT;           //terkepung dan teleport maneuver
                            } else {
                                System.out.println("Tidak punya teleport");
                            }
                        } else if (nearestTorpedos.size() != 0){
                            System.out.println("Torpedo Detected");
                            if (bot.getSize()>26 && getDistanceBetween(nearestTorpedos.get(0), bot) < 4*bot.getSize() && bot.fireTeleport > 0)
                            {
                                System.out.println("Able to shield");
                                if (gameState.getWorld().getCurrentTick() - ctick > 20) //penanda supaya nyalain shield biar ngga terus2an nembak shield
                                {
                                    valid = true;
                                }
                                if (valid)
                                {
                                    playerAction.action = PlayerActions.ACTIVATESHIELD;
                                    System.out.println("Shield Engaged!");
                                    ctick = gameState.getWorld().getCurrentTick();
                                    valid = false;
                                }
                            } else if (bot.getSize() >=15 && bot.getSize() <= 25 && bot.fireTeleport == 0){
                                    System.out.println("Evasive maneuvers!");
                                    int temp1 = getHeadingBetween(nearestTorpedos.get(0)) - getHeadingBetween(nearestTorpedos.get(1));
                                    playerAction.heading = getHeadingBetween(nearestTorpedos.get(0)) + (Math.max(Math.abs(temp1),Math.abs(360-temp1))/2);
                                    playerAction.action = PlayerActions.STARTAFTERBURNER;
                                    evadetick = gameState.getWorld().getCurrentTick();
                                    evade = true;
                            }
                        } 
                        else if (scanMusuh.getSize() > bot.getSize()){
                            System.out.println("OH SHIT HES BIG");
                            if (getDistanceBetween(worldCenter, bot)+   bot.getSize()-50>gameState.world.getRadius())
                            {
                                //ketika dikejar tapi kita ngarah ke world border
                                System.out.println("OH FCK IM AT THE BORDER");
                                int tempHeading;
                                if (getHeadingBetween(nearestPlayer.get(0))>=270 || (getHeadingBetween(nearestPlayer.get(0))>=90 && getHeadingBetween(nearestPlayer.get(0)) < 180))
                                {
                                    tempHeading = getHeadingBetween(nearestPlayer.get(0))+90;
                                }else
                                {
                                    tempHeading = getHeadingBetween(nearestPlayer.get(0))-90;
                                }
                                playerAction.heading = tempHeading;
                                playerAction.action = PlayerActions.FORWARD;
                            } 
                            else 
                            {
                                System.out.println("RUNNING");
                                playerAction.heading = 180 + getHeadingBetween(scanMusuh);
                                playerAction.action = PlayerActions.FORWARD;
                                //buat afterburner + navigasi gas cloud asteroid! (dibikinin bintang?)
                            } 
                        } 
                        else if (scanMusuh.getSize()<=bot.getSize())
                        {
                            System.out.println("Attack mode");
                            if (scanMusuh.getSize() < bot.getSize()){
                                System.out.println("Enemy is smaller, in pursuit");
                                playerAction.heading = getHeadingBetween(scanMusuh);
                                playerAction.action = PlayerActions.FORWARD;
                                //afterburner pursue? teleport jump?
                            } else if (scanMusuh.getSize() == bot.getSize()){
                                System.out.println("Enemy is equal size, firing torpedo");
                                playerAction.heading = getHeadingBetween(scanMusuh);
                                playerAction.action = PlayerActions.FIRETORPEDOES;
                                //nembak torpedo lalu gerak ke samping
                            }
                        }
                    } 
                    // else 
                    // {
                    //     System.out.println("Non-Combat");
                    //     playerAction.heading = getHeadingBetween(foodList.get(0));
                    //     playerAction.action = PlayerActions.FORWARD;
                    // }
        /////////////////////////OLD/////////////////////////////////
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
            }
        }
        this.playerAction = playerAction;
    }
    

    public GameState getGameState() {
        return this.gameState;
    }
    //fungsi ini cuman dipanggil sekali?
    public int ResolveNewTarget()
    {
        var directionToFood = gameState.getGameObjects()
            .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
            .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
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
