package net.dehydration.mod;

import java.util.Collection;
import java.util.Iterator;

import net.dehydration.access.HydrationManagerAccess;
import net.dehydration.network.HydrationServerPacket;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ModCommands {

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
			dispatcher.register((CommandManager.literal("thirst").requires((serverCommandSource) -> {
				return serverCommandSource.hasPermissionLevel(3);
			})).then((CommandManager.argument("targets", EntityArgumentType.players())
					.then(CommandManager.literal("true").executes((commandContext) -> {
						return executeThirstCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"),
								true);
					})).then(CommandManager.literal("false").executes((commandContext) -> {
						return executeThirstCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"),
								false);
					})))));
		});
	}

	private static int executeThirstCommand(ServerCommandSource source, Collection<ServerPlayerEntity> targets, boolean setThirst) {
		Iterator<ServerPlayerEntity> var3 = targets.iterator();

		while (var3.hasNext()) {
			ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) var3.next();
			((HydrationManagerAccess) serverPlayerEntity).getHydrationManager().setThirst(setThirst);
			HydrationServerPacket.writeS2CExcludedSyncPacket(serverPlayerEntity, setThirst);
		}
		source.sendFeedback(() -> Text.translatable("commands.dehydration.changed"), true);

		return targets.size();
	}

}