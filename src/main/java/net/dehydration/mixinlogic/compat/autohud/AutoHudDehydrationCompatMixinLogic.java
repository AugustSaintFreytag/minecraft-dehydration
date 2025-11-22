package net.dehydration.mixinlogic.compat.autohud;

import java.util.Objects;

import mod.crend.autohud.AutoHud;
import mod.crend.autohud.compat.DehydrationCompat;
import mod.crend.autohud.component.Component;
import mod.crend.autohud.component.Components;
import mod.crend.autohud.component.state.PolicyComponentState;
import mod.crend.autohud.render.ComponentRenderer;
import net.dehydration.access.HydrationManagerAccess;

public interface AutoHudDehydrationCompatMixinLogic {

	static void mixinStatic() {
		// Overwrite the static block's behavior
		DehydrationCompat.Thirst = Component.builder("dehydration", "thirst").config(AutoHud.config.hunger())
				.stackComponents(new Component[] { Components.Air }).inMainHud().state((player) -> {
					var hydrationManager = ((HydrationManagerAccess) player).getHydrationManager();

					if (hydrationManager.hasThirst()) {
						Component thirstComponent = DehydrationCompat.Thirst;
						Objects.requireNonNull(hydrationManager);

						return new PolicyComponentState(thirstComponent, hydrationManager::getHydrationLevel, 20);
					} else {
						return null;
					}
				}).build();
		DehydrationCompat.THIRST_WRAPPER = ComponentRenderer.of(DehydrationCompat.Thirst);
		AutoHud.addApi(new DehydrationCompat());
	}
}