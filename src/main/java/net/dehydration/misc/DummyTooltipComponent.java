package net.dehydration.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;

@Environment(EnvType.CLIENT)
public class DummyTooltipComponent implements TooltipComponent {

	public DummyTooltipComponent() {
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		return 0;
	}

	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
	}

}
