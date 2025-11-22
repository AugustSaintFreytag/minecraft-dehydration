package net.dehydration;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.dehydration.api.DehydrationAPI;
import net.dehydration.api.HydrationTemplate;
import net.dehydration.mod.ModBlocks;
import net.dehydration.mod.ModCommands;
import net.dehydration.mod.ModCompat;
import net.dehydration.mod.ModConfig;
import net.dehydration.mod.ModEffects;
import net.dehydration.mod.ModEvents;
import net.dehydration.mod.ModFluids;
import net.dehydration.mod.ModItems;
import net.dehydration.mod.ModJsonReader;
import net.dehydration.mod.ModSounds;
import net.dehydration.mod.ModTags;
import net.dehydration.network.HydrationServerPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;

public class Mod implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("Dehydration");

	public static final List<HydrationTemplate> HYDRATION_TEMPLATES = new ArrayList<HydrationTemplate>();

	@Override
	public void onInitialize() {
		ModBlocks.init();
		ModCommands.init();
		ModCompat.init();
		ModConfig.init();
		ModEffects.init();
		ModItems.init();
		ModEvents.init();
		ModFluids.init();
		ModSounds.init();
		ModTags.init();
		HydrationServerPacket.init();
		ModJsonReader.init();

		FabricLoader.getInstance().getEntrypointContainers("dehydration", DehydrationAPI.class).forEach((entrypoint) -> {
			ModMetadata metadata = entrypoint.getProvider().getMetadata();
			String id = metadata.getId();

			try {
				DehydrationAPI api = entrypoint.getEntrypoint();
				api.registerDrinkEvent();
			} catch (Throwable exception) {
				LOGGER.log(Level.ERROR, "Mod {} is providing a broken DehydrationAPI implementation", id, exception);
			}
		});
	}

}
