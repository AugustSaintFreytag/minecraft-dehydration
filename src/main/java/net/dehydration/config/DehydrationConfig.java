package net.dehydration.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "dehydration")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class DehydrationConfig implements ConfigData {

	// Player

	@ConfigEntry.Category("player_settings")
	@Comment("Defines speed of dehydration per player exhaustion. Bigger value = slower depletion.")
	public float dehydrationExhaustionRate = 0.85F;

	@ConfigEntry.Category("player_settings")
	@Comment("Defines the damage a player takes every 4 seconds when dying of thirst.")
	public float dehydrationDepletionDamage = 0.5F;

	@ConfigEntry.Category("player_settings")
	@Comment("Defines the amount of hydration a player respawns with after death.")
	@ConfigEntry.BoundedDiscrete(min = 0, max = 20)
	public int hydrationOnRespawn = 10;

	@ConfigEntry.Category("player_settings")
	@Comment("Defines if Haste and Fatique effects are applied when hydration is low.")
	public boolean extraDehydrationEffects = true;

	@ConfigEntry.Category("player_settings")
	@Comment("Defines if the player has increased rate of dehydration in hot dimensions.")
	public boolean increasedDehydrationInHotBiomes = true;

	@ConfigEntry.Category("player_settings")
	@Comment("Defines by how much dehydration rate increases in hot dimensions. 1.25 = 25% more thirst")
	public float increasedDehydrationFactor = 1.25F;

	// Effects

	@ConfigEntry.Category("effect_settings")
	@Comment("Defines the intensity of a default applied thirst effect (e.g. from contaminated water). Smaller value = less drain.")
	public float thirstEffectAmplitude = 0.1F;

	@ConfigEntry.Category("effect_settings")
	@Comment("Defines the duration of a default applied thirst effect (e.g. from contaminated water) in ticks. 20 ticks = 1 second.")
	public int thirstEffectDuration = 200;

	// Source

	@ConfigEntry.Category("water_source_settings")
	@Comment("Defines if water blocks should give a thirst effect")
	public boolean waterBlocksAreContaminated = true;

	@ConfigEntry.Category("water_source_settings")
	@ConfigEntry.BoundedDiscrete(min = 0, max = 20)
	@Comment("Defines the hydration value of a water source block or flowing water block.")
	public int waterBlockHydrationValue = 10;

	@ConfigEntry.Category("water_source_settings")
	@Comment("Defines if the player can drink flowing water instead of only still water blocks.")
	public boolean allowDrinkingFlowingWaterBlocks = false;

	// Potions

	@ConfigEntry.Category("potion_settings")
	@Comment("Defines if bad potions should give a thirst effect when consumed. Uses defined thirst effect chance.")
	public boolean badPotionsAreContaminated = true;

	// Flask

	@ConfigEntry.Category("flask_settings")
	@ConfigEntry.BoundedDiscrete(min = 0, max = 20)
	@Comment("Defines the hydration value of one charge of a filled water flask.")
	public int flaskHydrationValue = 12;

	// Food & Drinks

	@ConfigEntry.Category("consumable_settings")
	public boolean enableAutoInferredHydrationValues = true;

	@ConfigEntry.Category("consumable_settings")
	@ConfigEntry.BoundedDiscrete(min = 0, max = 20)
	public int drinksWeakHydrationValue = 2;

	@ConfigEntry.Category("consumable_settings")
	@ConfigEntry.BoundedDiscrete(min = 0, max = 20)
	public int drinksRegularHydrationValue = 4;

	@ConfigEntry.Category("consumable_settings")
	@ConfigEntry.BoundedDiscrete(min = 0, max = 20)
	public int drinksStrongHydrationValue = 8;

	@ConfigEntry.Category("consumable_settings")
	@ConfigEntry.BoundedDiscrete(min = 0, max = 20)
	public int foodWeakHydrationValue = 1;

	@ConfigEntry.Category("consumable_settings")
	@ConfigEntry.BoundedDiscrete(min = 0, max = 20)
	public int foodRegularHydrationValue = 2;

	@ConfigEntry.Category("consumable_settings")
	@ConfigEntry.BoundedDiscrete(min = 0, max = 20)
	public int foodStrongHydrationValue = 4;

	// Sleep

	@ConfigEntry.Category("sleep_settings")
	@ConfigEntry.BoundedDiscrete(min = 0, max = 20)
	public int dehydrationAfterSleeping = 4;

	@ConfigEntry.Category("sleep_settings")
	@ConfigEntry.BoundedDiscrete(min = 0, max = 20)
	public int hungerWhenSleeping = 2;

	// Production

	@ConfigEntry.Category("production_settings")
	@Comment("Defines the time in ticks it takes to boil water in a cauldron.")
	public int waterBoilingTime = 400;

	@ConfigEntry.Category("production_settings")
	@Comment("Defines the time in ticks before a pump can produce water again.")
	public int pumpCooldown = 1200;

	@ConfigEntry.Category("production_settings")
	@Comment("Defines if a pump requires a water source between 10-50 blocks underground to produce water.")
	public boolean pumpRequiresUndergroundWaterSource = false;

	@ConfigEntry.Category("production_settings")
	@Comment("Defines if filling a bottle consumes the water source block.")
	public boolean fillingBottleConsumesWaterSource = false;

	// Advanced

	@ConfigEntry.Category("advanced_settings")
	@Comment("Enables alternate textures by the lead texture artist (original mod setting).")
	public boolean useAlternateHUDTextures = false;

	@ConfigEntry.Category("advanced_settings")
	@Comment("Defines the horizontal offset to render the hydration HUD above the hotbar.")
	public int horizontalHUDOffset = 0;

	@ConfigEntry.Category("advanced_settings")
	@Comment("Defines the vertical offset to render the hydration HUD above the hotbar.")
	public int verticalHUDOffset = 0;

	@ConfigEntry.Category("advanced_settings")
	@Comment("Defines if the hydration HUD should show how much hydration would be gained when holding a usable item.")
	public boolean previewHydrationWhenHoldingItem = true;

	@ConfigEntry.Category("advanced_settings")
	@Comment("Defines if an item's hydration value should be rendered in the item tooltip. Disable when using other mods that display hydration values (e.g. Saint's Consumable Tooltips).")
	public boolean renderHydrationValueInItemTooltip = true;
}
