package Services;

import Enums.*;
import Models.*;

import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.*;

import org.w3c.dom.html.HTMLHeadingElement;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    boolean valid = true; //check shield
    boolean evade = false; //check afterburner
    boolean fireTele = true; //check fire teleport
    boolean alrdFire = false; //check teleport
    double getTime = 999; //teleport tick
    int ctick = 0; // turn off shield
    int wt = -1; //1 tick - 1 action
    int evadetick = 0; //turn off afterburner
    GameObject biggestPlayer;
    int detonatetick=0;
    boolean alrdFireSupernova = false;
    //Objek baru "worldCenter buat nandain titik di tengah2 dan supaya nggak keluar"
    Position centerPosition = new Position(0,0);
    GameObject teleTarget;
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

        if (gameState.getWorld().getCurrentTick()!=null){
            if (gameState.getWorld().getCurrentTick()!=wt){
                
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = getHeadingBetween(worldCenter);
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
                    var supernova_list = gameState.getGameObjects()
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP)
                        .sorted(Comparator 
                                .comparing(item ->getDistanceBetween(item, bot)))
                            .collect(Collectors.toList());
                    int a = 5;
                    for (int i = 0 ; i<nearestPlayer.size();i++)
                    {
                        if (a<nearestPlayer.get(0).getSize())
                        {
                            a=nearestPlayer.get(0).getSize();
                            biggestPlayer = nearestPlayer.get(i);
                        }
                    }
                    // var nearestTorpedos = gameState.getGameObjects()
                    //     .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO)
                    //     .sorted(Comparator
                    //             .comparing(item -> getDistanceBetween(bot,item)))
                    //         .collect(Collectors.toList());
                    var nearestTorpedos = gameState.getGameObjects()
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO &&  (Math.abs(getHeadingBetween(item) - item.currentHeading + 180) +360 % 360)  < 60)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot,item)))
                            .collect(Collectors.toList());
                    GameObject scanMusuh = nearestPlayer.get(0); //musuh terdekat kita (untuk menentukan attack/defense)
                    boolean Terkepung = terkepung(nearestPlayer, bot, scanMusuh);
                    int curRad = gameState.getWorld().getRadius();
                    System.out.println("\n");
                    System.out.println("---------------------------------------------------------");
                    System.out.println("Torpedo Count : " + bot.getTorpedoSalvo());
                    System.out.println("Shield Count : " + bot.shield);
                    System.out.println("Teleport Count : " + bot.fireTeleport);
                    System.out.println("Current Radius : " + curRad);
                    System.out.println("Current Size : " + bot.getSize());
                    System.out.println("Current Tick : " + (gameState.getWorld().getCurrentTick()));
                    System.out.println("---------------------------------------------------------");
                    System.out.println("\n");
                    if (gameState.getWorld().getCurrentTick() - ctick > 20) //penanda supaya nyalain shield biar ngga terus2an nembak shield
                    {
                        valid = true;
                    }
                    if (getDistanceBetween(bot, nearestPlayer.get(0)) < (curRad+bot.getSize()+nearestPlayer.get(0).getSize())){
                        System.out.println("COMBAT");
                        if(alrdFire && gameState.getWorld().getCurrentTick()==getTime && teleTarget != null)
                        {
                            if (bot.getSize()>teleTarget.getSize())
                            {    
                                playerAction.action = PlayerActions.TELEPORT;
                                System.out.println("tele");
                                alrdFire = false;
                                fireTele = true;
                                teleTarget = null;
                            }
                            else
                            {
                                System.out.println("cancel tele");
                                alrdFire = false;
                                fireTele = true;
                                teleTarget = null;
                            }
                        }
                        else if (alrdFire && gameState.getWorld().getCurrentTick()>getTime)
                        {
                            playerAction.action = PlayerActions.TELEPORT;
                            System.out.println("tele");
                            alrdFire = false;
                            fireTele = true;
                        }
                        else if ((gameState.getWorld().getCurrentTick() > evadetick + 5) && evade)
                        {
                            System.out.println("STOP AFTERBURNER");
                            playerAction.action = PlayerActions.STOPAFTERBURNER;
                            evade = false;
                        }
                        else if (alrdFireSupernova && detonatetick == gameState.getWorld().getCurrentTick())
                        {
                            playerAction.action = PlayerActions.DETONATESUPERNOVA;
                            alrdFireSupernova = false;
                        }
                        else if (bot.superNovaAvailable==1)
                        {
                            playerAction.heading = getHeadingBetween(biggestPlayer);
                            playerAction.action = PlayerActions.FIRESUPERNOVA;
                            detonatetick = gameState.getWorld().getCurrentTick() + (int)(getDistanceBetween(biggestPlayer, bot))/20;
                            alrdFireSupernova = true;
                        }
                        else if(supernova_list.size()!=0 && getDistanceBetween(supernova_list.get(0),bot)<50)
                        {
                            playerAction.heading = getHeadingBetween(supernova_list.get(0));
                            playerAction.action = PlayerActions.FORWARD;
                        }
                        else if (scanMusuh != null && getDistanceBetween(bot, scanMusuh) <= 7*bot.getSize() + scanMusuh.getSize())
                        {
                            System.out.println("Combat activated, defaulting to defense mode");
                            if (Terkepung)
                            {
                                System.out.println("Terkepung!");
                                if (bot.fireTeleport > 0 && fireTele && bot.getSize() > 30){
                                    System.out.println("Firing teleport");
                                    playerAction.heading = getHeadingBetween(worldCenter);
                                    playerAction.action = PlayerActions.FIRETELEPORT;           //terkepung dan teleport maneuver
                                    alrdFire = true;
                                    fireTele = false;

                                    getTime = getDistanceBetween(bot, worldCenter)/20 + gameState.getWorld().getCurrentTick();
                                } else if (bot.getSize()>26 && bot.torpedoSalvo > 0){
                                    System.out.println("GA PUNYA TELEPORT, TEMBAAK");
                                    playerAction.heading = getHeadingBetween(scanMusuh);
                                    playerAction.action = PlayerActions.FIRETORPEDOES;
                                }else{
                                    int heading1 = getHeadingBetween(nearestPlayer.get(0));
                                    int heading2 = getHeadingBetween(nearestPlayer.get(1));
                                    int tempHeading1 = Math.abs(heading1-heading2);
                                    int tempHeading2 = 360-tempHeading1;
                                    int refHeading = Math.min(tempHeading1, tempHeading2) < 180? tempHeading1 : tempHeading2;
                                    playerAction.heading = (Math.max(tempHeading1, tempHeading2)/2 + refHeading)%360;
                                }
                            } 
                            else if (nearestTorpedos.size() != 0 && valid){
                                System.out.println("Torpedo Detected");
                                if (bot.getSize()>25 && getDistanceBetween(nearestTorpedos.get(0), bot) < 65+bot.getSize() && bot.shield > 0)
                                {
                                    System.out.println("Able to shield");
                                    playerAction.action = PlayerActions.ACTIVATESHIELD;
                                    System.out.println("Shield Engaged!");
                                    ctick = gameState.getWorld().getCurrentTick();
                                    valid = false;
        
                                } else if (bot.getSize() >=15 && bot.getSize() <= 25 && bot.fireTeleport == 0){
                                        System.out.println("Evasive maneuvers!");
                                        int temp1 = getHeadingBetween(nearestTorpedos.get(0)) - getHeadingBetween(nearestTorpedos.get(1));
                                        playerAction.heading = getHeadingBetween(nearestTorpedos.get(0)) + (Math.max(Math.abs(temp1),Math.abs(360-temp1))/2);
                                        playerAction.action = PlayerActions.STARTAFTERBURNER;
                                        evadetick = gameState.getWorld().getCurrentTick();
                                        evade = true;
                                }else if(nearestTorpedos.size()>1){
                                    System.out.println("Evasive maneuvers! -Tanpa afterburner");
                                    int temp1 = getHeadingBetween(nearestTorpedos.get(0)) - getHeadingBetween(nearestTorpedos.get(1));
                                    playerAction.heading = getHeadingBetween(nearestTorpedos.get(0)) + (Math.max(Math.abs(temp1),Math.abs(360-temp1))/2);     
                                }else{
                                    System.out.println("Evasive maneuvers! -Tanpa afterburner 2");
                                    playerAction.heading = getHeadingBetween(nearestTorpedos.get(0)) + 90;
                                }
                            } 
                            else if (scanMusuh != null && scanMusuh.getSize() > bot.getSize()){
                                System.out.println("HES BIGGER");
                                if (bot.torpedoSalvo > 0 && bot.getSize()>26){
                                    System.out.println("Enemy is bigger, shoot anyway");
                                    playerAction.heading = getHeadingBetween(scanMusuh);
                                    playerAction.action = PlayerActions.FIRETORPEDOES;
                                }
                                else if (scanMusuh != null &&getDistanceBetween(bot, scanMusuh) < 2*bot.getSize()+scanMusuh.getSize() && bot.getSize()>25)
                                {
                                    System.out.println("RUNNING");
                                    playerAction.action = PlayerActions.STARTAFTERBURNER;
                                    playerAction.heading = getHeadingBetween(nearestPlayer.get(0)) + 540 % 360;
                                    evadetick = gameState.getWorld().getCurrentTick() + 5;
                                    evade = true;
                                    if (getDistanceBetween(worldCenter, bot)+2*bot.getSize()>gameState.world.getRadius()){
                                        System.out.println("IM NEAR THE BORDER");
                                        int tempHeading;
                                        if (getHeadingBetween(nearestPlayer.get(0))>=270 || (getHeadingBetween(nearestPlayer.get(0))>=90 && getHeadingBetween(nearestPlayer.get(0)) < 180))
                                        {
                                            tempHeading = getHeadingBetween(nearestPlayer.get(0)) + 90 % 360;
                                        }else{
                                            tempHeading = getHeadingBetween(nearestPlayer.get(0)) - 90 % 360;
                                        }
                                        playerAction.heading = tempHeading;
                                        playerAction.action = PlayerActions.STARTAFTERBURNER;
                                        evadetick = gameState.getWorld().getCurrentTick();
                                        evade = true;
                                    }
                                    
                                    else if (nearestGasCloud.size() != 0 && getDistanceBetween(nearestGasCloud.get(0), bot) < (bot.getSize()+nearestGasCloud.get(0).getSize()+60))
                                    {
                                        int tempHeading;
                                        if (getHeadingBetween(nearestGasCloud.get(0))>=270 || (getHeadingBetween(nearestGasCloud.get(0))>=90 && getHeadingBetween(nearestGasCloud.get(0)) < 180))
                                        {
                                            tempHeading = getHeadingBetween(nearestGasCloud.get(0)) + 135 % 360;
                                        }else{
                                            tempHeading = getHeadingBetween(nearestGasCloud.get(0)) - 135 % 360;
                                        }
                                        playerAction.heading = tempHeading;
                                        playerAction.action = PlayerActions.FORWARD;
                                    }
                                    else if (nearestAsteroid.size() != 0 && getDistanceBetween(nearestAsteroid.get(0), bot)<30+bot.getSize()+nearestAsteroid.get(0).getSize())
                                    {
                                        int tempHeading;
                                        if (getHeadingBetween(nearestAsteroid.get(0))>=270 || (getHeadingBetween(nearestAsteroid.get(0))>=90 && getHeadingBetween(nearestAsteroid.get(0)) < 180))
                                        {
                                            tempHeading = getHeadingBetween(nearestAsteroid.get(0)) + 135 % 360;
                                        }else{
                                            tempHeading = getHeadingBetween(nearestAsteroid.get(0)) - 135 % 360;
                                        }
                                        playerAction.heading = tempHeading;
                                        playerAction.action = PlayerActions.FORWARD;
                                    }
                                }
                                else if (scanMusuh != null && getDistanceBetween(bot, scanMusuh) < 4*bot.getSize()+scanMusuh.getSize())
                                {
                                    System.out.println("RUNNING");
                                    playerAction.action = PlayerActions.FORWARD;
                                    playerAction.heading = getHeadingBetween(nearestPlayer.get(0)) + 540 % 360;
                                    if (getDistanceBetween(worldCenter, bot)+2*bot.getSize()>gameState.world.getRadius()){
                                        System.out.println("IM NEAR THE BORDER");
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
                                    
                                    else if (nearestGasCloud.size() != 0 && getDistanceBetween(nearestGasCloud.get(0), bot) < (bot.getSize()+nearestGasCloud.get(0).getSize()+60))
                                    {
                                        int tempHeading;
                                        if (getHeadingBetween(nearestGasCloud.get(0))>=270 || (getHeadingBetween(nearestGasCloud.get(0))>=90 && getHeadingBetween(nearestGasCloud.get(0)) < 180))
                                        {
                                            tempHeading = getHeadingBetween(nearestGasCloud.get(0)) + 135 % 360;
                                        }else{
                                            tempHeading = getHeadingBetween(nearestGasCloud.get(0)) - 135 % 360;
                                        }
                                        playerAction.heading = tempHeading;
                                        playerAction.action = PlayerActions.FORWARD;
                                    }
                                    else if (nearestAsteroid.size() != 0 && getDistanceBetween(nearestAsteroid.get(0), bot)<30+bot.getSize()+nearestAsteroid.get(0).getSize())
                                    {
                                        int tempHeading;
                                        if (getHeadingBetween(nearestAsteroid.get(0))>=270 || (getHeadingBetween(nearestAsteroid.get(0))>=90 && getHeadingBetween(nearestAsteroid.get(0)) < 180))
                                        {
                                            tempHeading = getHeadingBetween(nearestAsteroid.get(0)) + 135 % 360;
                                        }else{
                                            tempHeading = getHeadingBetween(nearestAsteroid.get(0)) - 135 % 360;
                                        }
                                        playerAction.heading = tempHeading;
                                        playerAction.action = PlayerActions.FORWARD;
                                    }
                                }
                                else{
                                    System.out.println("MASUK IDLE MODE4");
                                    idle(nearestTorpedos, nearestGasCloud, foodList, superFoodList, nearestPlayer,supernova_list);
                                } 
                            } 
                            else if (scanMusuh != null && scanMusuh.getSize()<=bot.getSize())
                            {
                                System.out.println("Attack mode");
                                if (nearestPlayer.size() != 0 && nearestPlayer.get(0).getSize() < bot.getSize()-40 && fireTele)
                                {
                                    playerAction.heading = getHeadingBetween(nearestPlayer.get(0));
                                    playerAction.action = PlayerActions.FIRETELEPORT;
                                    teleTarget = nearestPlayer.get(0);
                                    alrdFire = true;
                                    fireTele = false;
                                    getTime = (getDistanceBetween(nearestPlayer.get(0), bot)-bot.getSize() - nearestPlayer.get(0).getSize() + 20)/20 + gameState.getWorld().getCurrentTick();
                                    System.out.println("fire tele");
                                }
                                else if (bot.torpedoSalvo > 0 && bot.getSize()>26 && scanMusuh != null){
                                    System.out.println("fire torpedo");
                                    playerAction.heading = getHeadingBetween(scanMusuh);
                                    playerAction.action = PlayerActions.FIRETORPEDOES;
                                }else{
                                    System.out.println("KEJAR");
                                    playerAction.heading = getHeadingBetween(scanMusuh);
                                    playerAction.action = PlayerActions.FORWARD;
                                }
                            }else
                            {
                            System.out.println("MASUK IDLE MODE1");
                            idle(nearestTorpedos, nearestGasCloud, foodList, superFoodList, nearestPlayer,supernova_list);
                            }
                        }
                        else{
                            System.out.println("MASUK IDLE MODE2");
                            idle(nearestTorpedos, nearestGasCloud, foodList, superFoodList, nearestPlayer,supernova_list);
                        }          
                    } 
                    else{
                        System.out.println("MASUK IDLE MODE3");
                        idle(nearestTorpedos, nearestGasCloud, foodList, superFoodList, nearestPlayer,supernova_list);
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
    //fungsi ini cuman dipanggil sekali?
    public int ResolveNewTarget()
    {
        var directionToFood = gameState.getGameObjects()
            .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
            .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        int heading = getHeadingBetween(directionToFood.get(0));
        return heading;
    }

    public boolean terkepung (List<GameObject> nearestPlayer, GameObject bot, GameObject scanMusuh){
        if (nearestPlayer.size()>3)
        {
            if (getDistanceBetween(bot, nearestPlayer.get(2)) <= 4*bot.getSize()
            && getHeadingBetween(scanMusuh) - getHeadingBetween(nearestPlayer.get(1)) <= 270 && getHeadingBetween(scanMusuh) - getHeadingBetween(nearestPlayer.get(1)) >= 90 
            && getHeadingBetween(nearestPlayer.get(1)) - getHeadingBetween(nearestPlayer.get(2)) <= 270 && getHeadingBetween(nearestPlayer.get(1)) - getHeadingBetween(nearestPlayer.get(2)) >= 90
            && getHeadingBetween(scanMusuh) - getHeadingBetween(nearestPlayer.get(2)) <= 270 && getHeadingBetween(scanMusuh) - getHeadingBetween(nearestPlayer.get(2)) >= 90)
            {
                return true;
            }
        }
        return false;
    }

    public void idle(List<GameObject> nearestTorpedos, List<GameObject> nearestGasCloud, List<GameObject> foodList, List<GameObject> superFoodList, List<GameObject> nearestPlayer, List<GameObject> supernova_list){
        playerAction.action = PlayerActions.FORWARD;
        System.out.println("MASUK FUNC IDLE MODE");
        if(alrdFire && gameState.getWorld().getCurrentTick()==getTime && teleTarget != null)
        {
            if (bot.getSize()>teleTarget.getSize())
            {    
                playerAction.action = PlayerActions.TELEPORT;
                System.out.println("tele");
                alrdFire = false;
                fireTele = true;
                teleTarget = null;
            }
            else
            {
                System.out.println("cancel tele");
                alrdFire = false;
                fireTele = true;
                teleTarget = null;
            }
        }
        else if (alrdFire && gameState.getWorld().getCurrentTick()==getTime)
        {
            playerAction.action = PlayerActions.TELEPORT;
            System.out.println("tele");
            alrdFire = false;
            fireTele = true;
        }
        else if ((gameState.getWorld().getCurrentTick() > evadetick + 5) && evade)
        {
            System.out.println("STOP AFTERBURNER");
            playerAction.action = PlayerActions.STOPAFTERBURNER;
            evade = false;
        }
        else if (nearestTorpedos.size() != 0 && valid){
            System.out.println("Torpedo Detected");
            if (bot.getSize()>25 && getDistanceBetween(nearestTorpedos.get(0), bot) < 65+bot.getSize() && bot.shield > 0)
            {
                System.out.println("Able to shield");
                playerAction.action = PlayerActions.ACTIVATESHIELD;
                System.out.println("Shield Engaged!");
                ctick = gameState.getWorld().getCurrentTick();
                valid = false;

            } else if (bot.getSize() >=15 && bot.getSize() <= 25 && bot.fireTeleport == 0){
                    System.out.println("Evasive maneuvers!");
                    int temp1 = getHeadingBetween(nearestTorpedos.get(0)) - getHeadingBetween(nearestTorpedos.get(1));
                    playerAction.heading = getHeadingBetween(nearestTorpedos.get(0)) + (Math.max(Math.abs(temp1),Math.abs(360-temp1))/2);
                    playerAction.action = PlayerActions.STARTAFTERBURNER;
                    evadetick = gameState.getWorld().getCurrentTick();
                    evade = true;
            }else if(nearestTorpedos.size()>1){
                System.out.println("Evasive maneuvers! -Tanpa afterburner");
                int temp1 = getHeadingBetween(nearestTorpedos.get(0)) - getHeadingBetween(nearestTorpedos.get(1));
                playerAction.heading = getHeadingBetween(nearestTorpedos.get(0)) + (Math.max(Math.abs(temp1),Math.abs(360-temp1))/2);     
            }else{
                System.out.println("Evasive maneuvers! -Tanpa afterburner 2");
                playerAction.heading = getHeadingBetween(nearestTorpedos.get(0)) + 90;
            }
        }
        else if(nearestGasCloud.size() != 0 &&  getDistanceBetween(worldCenter, bot)+2.5*bot.getSize()>gameState.world.getRadius() && getDistanceBetween(nearestGasCloud.get(0), bot) < (80+bot.getSize()+nearestGasCloud.get(0).getSize()))
        {
            if (bot.getSize()>40 && bot.fireTeleport>0 && fireTele)
            {
                playerAction.heading = getHeadingBetween(worldCenter);
                playerAction.action = PlayerActions.FIRETELEPORT;
                alrdFire = true;
                fireTele = false;
                getTime = getDistanceBetween(worldCenter, bot)/20 + gameState.getWorld().getCurrentTick();
                System.out.println("Waktu tick buat tele" + getTime);
            }
            else
            {
                if(nearestGasCloud.size() != 0 &&  getHeadingBetween(nearestGasCloud.get(0))>=270 || (getHeadingBetween(nearestGasCloud.get(0))>=90 && getHeadingBetween(nearestGasCloud.get(0))<=180))
                {
                    playerAction.heading = getHeadingBetween(nearestGasCloud.get(0))+135%360;
                }
                else
                {
                    playerAction.heading = getHeadingBetween(nearestGasCloud.get(0))-135%360;
                }
            }
        }
        else if (getDistanceBetween(worldCenter, bot)+2.5*bot.getSize()>gameState.world.getRadius()){
            playerAction.heading = getHeadingBetween(worldCenter);
        }
        else if (nearestGasCloud.size()!=0 &&  getDistanceBetween(nearestGasCloud.get(0), bot) < (80+bot.getSize()+nearestGasCloud.get(0).getSize())){
            if (getDistanceBetween(nearestGasCloud.get(0), bot)<=(1+bot.getSize()+nearestGasCloud.get(0).getSize())){
                playerAction.heading = (getHeadingBetween(nearestGasCloud.get(0))+180) %360;
            }else{
                if(getHeadingBetween(nearestGasCloud.get(0))>=270 || (getHeadingBetween(nearestGasCloud.get(0))>=90 && getHeadingBetween(nearestGasCloud.get(0))<=180))
                {
                    playerAction.heading = getHeadingBetween(nearestGasCloud.get(0))+135%360;
                }
                else
                {
                    playerAction.heading = getHeadingBetween(nearestGasCloud.get(0))-135%360;
                }
            }
        }
        else if (superFoodList.size() != 0 && foodList.size() != 0 &&  getDistanceBetween(superFoodList.get(0),bot)<getDistanceBetween(foodList.get(0), bot)){
            playerAction.heading = getHeadingBetween(superFoodList.get(0));
        }
        else{
            if(foodList.size()!=0)
            {
                playerAction.heading = getHeadingBetween(foodList.get(0));
            }
            
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