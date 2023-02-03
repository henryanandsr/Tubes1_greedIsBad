package Enums;

public enum PlayerActions {
  FORWARD(1),
  STOP(2),
  START_AFTERBURNER(3),
  STOP_AFTERBURNER(4),
  FIRE_TORPEDOS(5),
  FIRE_SUPERNOVA(6),
  DETONATE_SUPERNOVA(7),
  FIRE_TELEPORT(8),
  TELEPORT(9),
  ACTIVE_SHIELD(10);

  public final Integer value;

  private PlayerActions(Integer value) {
    this.value = value;
  }
}