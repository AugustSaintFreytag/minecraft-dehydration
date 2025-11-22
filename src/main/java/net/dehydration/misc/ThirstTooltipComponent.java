package net.dehydration.misc;

import net.dehydration.mod.ModRendering;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;

@Environment(EnvType.CLIENT)
public class ThirstTooltipComponent implements TooltipComponent {

	private final int thirstQuench;
	private final int quality;

	public ThirstTooltipComponent(ThirstTooltipData data) {
		this.thirstQuench = data.getThirstQuench();
		this.quality = data.getDrinkQuality();
	}

	@Override
	public int getHeight() {
		return 11;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		if (this.thirstQuench > 20) {
			return 18 + textRenderer.getWidth("x10") * 3 / 4;
		}

		return this.thirstQuench * 9 / 2 + (this.thirstQuench % 2 != 0 ? 9 : 0);
	}

	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
		var matrixStack = context.getMatrices();
		var thirstQuench = this.thirstQuench;

		if (thirstQuench > 20) {
			// Draw only single full thirst element.
			context.drawTexture(ModRendering.THIRST_ICON, x, y, 0, 0, 9, 9, 256, 256); // Background
			context.drawTexture(ModRendering.THIRST_ICON, x, y, this.quality * 18, 9, 9, 9, 256, 256);

			// Draw "x10" multiplier indicator after element.
			var thirstQuenchValue = (int) Math.ceil(Math.abs(thirstQuench) / 2f);
			var thirstQuenchText = "x" + thirstQuenchValue;

			matrixStack.push();
			matrixStack.translate(x + 9, y, 0);
			matrixStack.scale(0.75f, 0.75f, 0.75f);

			context.drawTextWithShadow(textRenderer, thirstQuenchText, 1, 3, 0xFFAAAAAA);

			matrixStack.pop();
			return;
		}

		// Draw full thirst elements
		for (int i = 0; i < thirstQuench / 2; i++) {
			var elementX = x + i * 9 - 1;
			var elementY = y;

			context.drawTexture(ModRendering.THIRST_ICON, elementX, elementY, 0, 0, 9, 9, 256, 256); // Background
			context.drawTexture(ModRendering.THIRST_ICON, elementX, elementY, this.quality * 18, 9, 9, 9, 256, 256);
		}

		// Draw single half thirst element
		if (thirstQuench % 2 != 0) {
			context.drawTexture(ModRendering.THIRST_ICON, x + thirstQuench / 2 * 9 - 1, y, 0, 0, 9, 9, 256, 256); // Background
			context.drawTexture(ModRendering.THIRST_ICON, x + thirstQuench / 2 * 9 - 1, y, this.quality * 18 + 9, 9, 9, 9, 256, 256);
		}
	}

}
