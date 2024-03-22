package cat.szyuan.autoharvest;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;

public class Configure {
    private File configFile;

    public FlowerISseed flowerISseed = new FlowerISseed();

    public static class FlowerISseed {
        public boolean value = false;
        private String name = "flowerISseed";
    }

    public Effect_radius effect_radius = new Effect_radius();

    public static class Effect_radius {
        public int value = 3;
        private String name = "effect_radius";
        public static final int Max = 3;
        public static final int Min = 0;
    }

    public TickSkip tickSkip = new TickSkip();

    public static class TickSkip {
        public int value = 0;
        private String name = "tick_skip";
        public static final int Max = 100;
        public static final int Min = 0;
    }

    public KeepFishingRodAlive keepFishingRodAlive = new KeepFishingRodAlive();

    public static class KeepFishingRodAlive {
        public boolean value = true;
        String name = "keepFishingRodAlive";
    }


    public Configure() {
        this.configFile = FabricLoader
                .getInstance()
                .getConfigDir()
                .resolve("AutoHarvest.json")
                .toFile();
        flowerISseed.value = false;
    }

    public Configure load() {
        try {
            if (!Files.exists(this.configFile.toPath()))
                return this;
            String jsonStr = new String(Files.readAllBytes(this.configFile.toPath()));
            if (!jsonStr.equals("")) {
                JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();

                if (jsonObject.has(flowerISseed.name)) {
                    try {
                        this.flowerISseed.value = jsonObject.getAsJsonPrimitive(flowerISseed.name).getAsBoolean();
                    } catch (Exception e) {
                    }
                }
                if (jsonObject.has(effect_radius.name)) {
                    try {
                        this.effect_radius.value = jsonObject.getAsJsonPrimitive(effect_radius.name).getAsInt();
                        if (effect_radius.value < Effect_radius.Min || effect_radius.value > Effect_radius.Max)
                            effect_radius.value = Effect_radius.Max;
                    } catch (Exception e) {
                    }
                }
                if (jsonObject.has(tickSkip.name)) {
                    try {
                        this.tickSkip.value = jsonObject.getAsJsonPrimitive(tickSkip.name).getAsInt();
                        if (tickSkip.value < TickSkip.Min || tickSkip.value > TickSkip.Max)
                            tickSkip.value = TickSkip.Min;
                    } catch (Exception e) {
                    }
                }
                if (jsonObject.has(keepFishingRodAlive.name)) {
                    try {
                        this.keepFishingRodAlive.value = jsonObject.getAsJsonPrimitive(keepFishingRodAlive.name).getAsBoolean();
                    } catch (Exception e) {
                    }
                }
                return this;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Configure save() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(flowerISseed.name, this.flowerISseed.value);
        jsonObject.addProperty(effect_radius.name, this.effect_radius.value);
        jsonObject.addProperty(tickSkip.name, this.tickSkip.value);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement el = JsonParser.parseString(jsonObject.toString());
        try (PrintWriter out = new PrintWriter(configFile)) {
            out.println(gson.toJson(el));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return this;
    }
}
