package cat.szyuan.autoharvest;


import java.util.ArrayList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class TaskManager {
    private final ArrayList<Line> taskList = new ArrayList<>();

    enum Commands {
        MOVEITEM,
        SKIPTICK
    }

    static class Line {
        public Line(Commands command, Object... args) {
            Command = command;
            Args = args;
        }

        Commands Command;
        Object[] Args;
    }

    public void Add_MoveItem(int slotNumber, int currentHotbarSlot) {
        taskList.add(new Line(Commands.MOVEITEM, slotNumber, currentHotbarSlot));
    }

    public void Add_TickSkip(int skipTick) {
        for (int i = 0; i < skipTick; i++)
            taskList.add(new Line(Commands.SKIPTICK));
    }

    public int Count() {
        return taskList.size();
    }

    public void RunATask() {
        if (taskList.isEmpty())
            return;
        Line line = taskList.get(0);
        switch (line.Command) {
            case MOVEITEM:
                MinecraftClient mc = MinecraftClient.getInstance();
                assert mc.player != null;
                PlayerScreenHandler container = mc.player.playerScreenHandler;
                if ((int) line.Args[0] < 9) {
                    MinecraftClient.getInstance().player.getInventory().selectedSlot = (int) line.Args[0];
                } else {
                    assert mc.interactionManager != null;
                    mc.interactionManager.clickSlot(container.syncId, (int) line.Args[0], (int) line.Args[1], SlotActionType.SWAP, mc.player);
                }
                break;
            case SKIPTICK:
                break;
        }
        taskList.remove(0);
    }
}
