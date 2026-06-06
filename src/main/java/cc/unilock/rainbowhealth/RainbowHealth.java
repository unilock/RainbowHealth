package cc.unilock.rainbowhealth;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = RainbowHealth.MOD_ID, dist = Dist.CLIENT)
public class RainbowHealth {
	public static final String MOD_ID = "rainbowhealth";

	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(RainbowHealth.MOD_ID, "textures/gui/health.png");

	public RainbowHealth(ModContainer modContainer, IEventBus modEventBus) {
		modContainer.registerConfig(ModConfig.Type.CLIENT, RainbowConfig.CONFIG_SPEC);
		modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

		NeoForge.EVENT_BUS.addListener(this::renderGuiLayerPre);
		modEventBus.addListener(this::registerGuiLayers);
	}

	private void renderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
		if (RainbowConfig.CONFIG.enableBar.getAsBoolean()) {
			if (VanillaGuiLayers.PLAYER_HEALTH.equals(event.getName())) {
				var player = Minecraft.getInstance().getCameraEntity() instanceof Player p ? p : null;
				if (player != null) {
					if (getMaxHealth(player) > RainbowConfig.CONFIG.minMaxHealth.get()) {
						event.setCanceled(true);
					}
				}
			}
		}
	}

	private void registerGuiLayers(RegisterGuiLayersEvent event) {
		final int width = 4;
		final int halfWidth = width / 2;
		final int height = 9;

		event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH, ResourceLocation.fromNamespaceAndPath(RainbowHealth.MOD_ID, "bar"), (guiGraphics, deltaTracker) -> {
			if (RainbowConfig.CONFIG.enableBar.getAsBoolean()) {
				var client = Minecraft.getInstance();
				if (client.gameMode == null || !client.gameMode.canHurtPlayer()) return;

				var player = client.getCameraEntity() instanceof Player p ? p : null;
				if (player != null) {
					if (getMaxHealth(player) > RainbowConfig.CONFIG.minMaxHealth.get()) {
						final int x = guiGraphics.guiWidth() / 2 - 91;
						final int y = guiGraphics.guiHeight() - client.gui.leftHeight;

						boolean blinking = player.hurtTime > 0 && player.hurtTime / 3 % 2 == 1;

						boolean regeneration = player.hasEffect(MobEffects.REGENERATION);
						int rOffset = regeneration ? Math.abs((player.getEffect(MobEffects.REGENERATION).getDuration() % 40) - 40) - 5 : 0; // TODO: I don't like this math

						boolean poison = player.hasEffect(MobEffects.POISON);
						boolean wither = player.hasEffect(MobEffects.WITHER);
						boolean frozen = player.isFullyFrozen();

						int bars = getHealth(player) * 60 / getMaxHealth(player);

						for (int i = 0; i < 60; i++) {
							int j = i / 2;

							int u = j * (width - 1) + (i % 2 * halfWidth);
							int v = 0;

							int offset = 0;
							if (regeneration) {
								if (rOffset == j - 2) {
									offset = -1;
								}
								if (rOffset == j - 1) {
									offset = -2;
								}
								if (rOffset == j + 1) {
									offset = 2;
								}
								if (rOffset == j + 2) {
									offset = 1;
								}
							}

							if (poison) {
								v = height * 3;
							}
							if (wither) {
								v = height * 4;
							}
							if (frozen) {
								v = height * 5;
							}
							if (i >= bars) {
								v = height;

								if (blinking) {
									v = height * 2;
								}
							}

							guiGraphics.blit(TEXTURE, x + u, y + offset, u, v, halfWidth, height, 91, 54);
						}

						client.gui.leftHeight += height + 1;
					}
				}
			}
		});

		event.registerAbove(VanillaGuiLayers.ARMOR_LEVEL, ResourceLocation.fromNamespaceAndPath(RainbowHealth.MOD_ID, "text"), (guiGraphics, deltaTracker) -> {
			if (RainbowConfig.CONFIG.enableText.getAsBoolean()) {
				var client = Minecraft.getInstance();
				if (client.gameMode == null || !client.gameMode.canHurtPlayer()) return;

				var player = client.getCameraEntity() instanceof Player p ? p : null;
				if (player != null) {
					int x = guiGraphics.guiWidth() / 2 - 91;
					int y = guiGraphics.guiHeight() - client.gui.leftHeight;

					if (RainbowConfig.CONFIG.divideByTwo.getAsBoolean()) {
						guiGraphics.drawString(client.font, String.format("Health: %d/%d", getHealth(player)/2, getMaxHealth(player)/2), x, y, 0xFFFFFF);
					} else {
						guiGraphics.drawString(client.font, String.format("HP: %d/%d", getHealth(player), getMaxHealth(player)), x, y, 0xFFFFFF);
					}

					client.gui.leftHeight += client.font.lineHeight + 1;
				}
			}
		});
	}

	private static int getHealth(Player player) {
		return Math.round(player.getHealth() + player.getAbsorptionAmount());
	}

	private static int getMaxHealth(Player player) {
		return Math.round(player.getMaxHealth() + player.getMaxAbsorption());
	}
}