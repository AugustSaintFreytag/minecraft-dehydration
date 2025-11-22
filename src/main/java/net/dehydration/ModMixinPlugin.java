package net.dehydration;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

public class ModMixinPlugin implements IMixinConfigPlugin {

	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		var loader = FabricLoader.getInstance();

		// Disable Dehydration -> Origins compatibility if Origins is not loaded
		if (!loader.isModLoaded("origins") && (mixinClassName.equals("net.dehydration.mixin.compat.OriginCommandMixin")
				|| mixinClassName.equals("net.dehydration.mixin.compat.PlayerOriginComponentMixin"))) {
			return false;
		}

		if (!loader.isModLoaded("autohud") && (mixinClassName.equals("compat.autohud.AutoHudDehydrationCompatMixin"))) {
			return false;
		}

		if (!loader.isModLoaded("farmersdelight") && (mixinClassName.equals("compat.farmersdelight.FarmersDelightConsumableItemMixin"))) {
			return false;
		}

		// Disable AutoHUD -> Dehydration compatibility, handled Dehydration -> AutoHUD
		if (mixinClassName.equals("mod.crend.autohud.compat.mixin.dehydration.ThirstManagerMixin")) {
			return false;
		}

		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

}
