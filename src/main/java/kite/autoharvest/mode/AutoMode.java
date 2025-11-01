package kite.autoharvest.mode;

public interface AutoMode {
    void tick();

    String getName();

    default void onDisable() {
    }
}
