package net.creativious.starwarsreborn.config;

import net.creativious.starwarsreborn.util.config.ConfigComment;
import net.creativious.starwarsreborn.util.config.ConfigCustomName;
import net.creativious.starwarsreborn.util.config.NeptuneConfig;
import net.creativious.starwarsreborn.util.config.NeptuneSubConfig;

public class StarWarsRebornConfig extends NeptuneConfig {

    public JediConfig jedi = new JediConfig();
    public LightsaberConfig lightsaber = new LightsaberConfig();

    public static class LightsaberConfig implements NeptuneSubConfig {
        @ConfigComment("Damage dealt by the lightsaber (1.0 = half a heart)")
        @ConfigCustomName("damage")
        public Double lightsaber_damage = 10.0D;
        @ConfigComment("Should the lightsaber have an attack cooldown?")
        @ConfigCustomName("do cooldown")
        public Boolean lightsaber_attack_cooldown = true;
        @ConfigComment("Should the lightsaber do fire damage?")
        @ConfigCustomName("do fire damage")
        public Boolean lightsaber_fire_damage = false;
    }

    public static class JediDoubleJumpConfig implements NeptuneSubConfig {
        @ConfigComment("Should the player be able to double jump?")
        public Boolean allow = true;
        @ConfigComment("Should their be a cooldown on double jumping?")
        @ConfigCustomName("has cooldown")
        public Boolean have_cooldown = true;
        @ConfigComment("Cooldown time in milliseconds")
        @ConfigCustomName("cooldown time")
        public Long cooldown_time_in_ms = 2000L;
    }

    public static class JediConfig implements NeptuneSubConfig {

        public JediDoubleJumpConfig double_jump = new JediDoubleJumpConfig();
    }
}
