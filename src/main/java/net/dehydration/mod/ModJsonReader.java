package net.dehydration.mod;

import net.dehydration.Mod;
import net.dehydration.data.DataLoader;
import net.dehydration.network.HydrationServerPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class ModJsonReader {

	public static void init() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DataLoader());
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
			if (success) {
				for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++)
					HydrationServerPacket.writeS2CHydrationTemplateSyncPacket(server.getPlayerManager().getPlayerList().get(i));
				Mod.LOGGER.info("Finished reload on {}", Thread.currentThread());
			} else
				Mod.LOGGER.error("Failed to reload on {}", Thread.currentThread());
		});
	}
}
