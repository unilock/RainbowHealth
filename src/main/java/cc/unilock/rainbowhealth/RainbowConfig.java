package cc.unilock.rainbowhealth;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class RainbowConfig {
	public static final RainbowConfig CONFIG;
	public static final ModConfigSpec CONFIG_SPEC;

	static {
		Pair<RainbowConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(RainbowConfig::new);

		CONFIG = pair.getLeft();
		CONFIG_SPEC = pair.getRight();
	}

	public final ModConfigSpec.BooleanValue enableBar;
	public final ModConfigSpec.BooleanValue enableText;
	public final ModConfigSpec.BooleanValue divideByTwo;
	public final ModConfigSpec.ConfigValue<Integer> minMaxHealth;

	private RainbowConfig(ModConfigSpec.Builder builder) {
		this.enableBar = builder.define("enable_bar", true);
		this.enableText = builder.define("enable_text", true);
		this.divideByTwo = builder.define("divide_by_two", false);
		this.minMaxHealth = builder.define("min_max_health", 40);
	}
}