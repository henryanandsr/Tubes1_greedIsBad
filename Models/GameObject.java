package Models;

import Enums.*;
import java.util.*;

public class GameObject {
  public UUID id;
  public Integer size;
  public Integer speed;
  public Integer currentHeading;
  public Position position;
  public ObjectTypes gameObjectType;
  public Integer torpedoSalvo;
  public Integer shield;

  public GameObject(UUID id, Integer size, Integer speed, Integer currentHeading, Position position, ObjectTypes gameObjectType, Integer torpedoSalvo, Integer Shield) {
    this.id = id;
    this.size = size;
    this.speed = speed;
    this.currentHeading = currentHeading;
    this.position = position;
    this.gameObjectType = gameObjectType;
    this.torpedoSalvo = torpedoSalvo;
    this.shield = Shield;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getSpeed() {
    return speed;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
  }

  public Position getPosition() {
    return position;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public ObjectTypes getGameObjectType() {
    return gameObjectType;
  }

  public void setGameObjectType(ObjectTypes gameObjectType) {
    this.gameObjectType = gameObjectType;
  }

  public static GameObject FromStateList(UUID id, List<Integer> stateList)
  {
    Position position = new Position(stateList.get(4), stateList.get(5));
    System.out.println(stateList);
    if (stateList.size() != 11)
    {
      return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position, ObjectTypes.valueOf(stateList.get(3)), null,null);
    }
    else
    {
      return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position, ObjectTypes.valueOf(stateList.get(3)), stateList.get(6), stateList.get(7));
    }
  }
}
