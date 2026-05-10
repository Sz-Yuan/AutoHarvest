package kite.autoharvest.mode;

import java.util.List;
import java.util.stream.Collectors;

public class CompositeMode implements AutoMode {
    private final List<AutoMode> modes;

    public CompositeMode(AutoMode... modes) {
        this.modes = List.of(modes);
    }

    public static CompositeMode farmer() {
        return new CompositeMode(new PlantMode(), new HarvestMode());
    }

    @Override
    public void tick() {
        modes.forEach(AutoMode::tick);
    }

    @Override
    public String getName() {
        return modes.stream()
                .map(AutoMode::getName)
                .collect(Collectors.joining(" + "));
    }

    @Override
    public void onDisable() {
        modes.forEach(AutoMode::onDisable);
    }
}