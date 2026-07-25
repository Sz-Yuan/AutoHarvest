package kite.autoharvest.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import kite.autoharvest.config.AutoHarvestConfig;
import me.shedaniel.autoconfig.AutoConfigClient;

public class AutoHarvestModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfigClient.getConfigScreen(AutoHarvestConfig.class, parent).get();
    }
}
