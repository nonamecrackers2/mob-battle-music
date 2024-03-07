package nonamecrackers2.mobbattlemusic.client.config;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import nonamecrackers2.crackerslib.common.config.ConfigHelper;
import nonamecrackers2.crackerslib.common.config.ReloadType;
import nonamecrackers2.mobbattlemusic.MobBattleMusicMod;

public class MobBattleMusicConfig
{
	public static final ClientConfig CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;
	
	static
	{
		var clientPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
		CLIENT = clientPair.getLeft();
		CLIENT_SPEC = clientPair.getRight();
	}
	
	public static class ClientConfig extends ConfigHelper
	{
		public final ForgeConfigSpec.ConfigValue<Integer> maxMobsForMaxVolume;
		public final ForgeConfigSpec.ConfigValue<Integer> maxMobSearchRadius;
		public final ForgeConfigSpec.ConfigValue<Integer> threatRadius;
		public final ForgeConfigSpec.ConfigValue<Integer> threatReevaluationCooldown;
		public final ForgeConfigSpec.ConfigValue<Integer> playerReevaluationCooldown;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> ignoredMobs;
		public final ForgeConfigSpec.ConfigValue<Boolean> onlyCountVisibleMobs;
		public final ForgeConfigSpec.ConfigValue<Integer> musicTrackEmptyTime;
		public final ForgeConfigSpec.ConfigValue<Double> nonAggressiveFadeTime;
		public final ForgeConfigSpec.ConfigValue<Boolean> nonAggressiveTrackEnabled;
		public final ForgeConfigSpec.ConfigValue<Double> aggressiveFadeTime;
		public final ForgeConfigSpec.ConfigValue<Boolean> aggressiveTrackEnabled;
		public final ForgeConfigSpec.ConfigValue<Double> playerFadeTime;
		public final ForgeConfigSpec.ConfigValue<Boolean> playerTrackEnabled;
		public final ForgeConfigSpec.ConfigValue<Integer> calmDownTime;
		public final ForgeConfigSpec.ConfigValue<Boolean> punchingCountsAsViolence;
		
		public ClientConfig(ForgeConfigSpec.Builder builder)
		{
			super(MobBattleMusicMod.MODID);
			
			this.maxMobSearchRadius = this.createRangedIntValue(builder, 64, 4, 256, "maxMobSearchRadius", ReloadType.GAME, "The radius mobs must be in in order to count towards music tracks playing");

			this.threatRadius = this.createRangedIntValue(builder, 24, 4, 256, "threatRadius", ReloadType.GAME, "Specifies the distance the nearest enemy threat must be in in order for the aggressive/player music tracks to continue to play");
			
			this.ignoredMobs = this.createListValue(builder, String.class, () -> {
				return Lists.newArrayList("minecraft:creeper");
			}, v -> {
				return ResourceLocation.isValidResourceLocation(v);
			}, "ignoredMobs", ReloadType.NONE, "A list of mobs that should NOT be considered towards music tracks playing");
			
			this.musicTrackEmptyTime = this.createRangedIntValue(builder, 15, 1, 300, "musicTrackEmptyTime", ReloadType.NONE, "Specifies the time (in seconds) that a music track must not be playing for it to stop completely. Higher time will make it so music tracks won't restart as much if they fade back in for whatever reason");
			
			this.calmDownTime = this.createRangedIntValue(builder, 10, 1, 300, "calmDownTime", ReloadType.NONE, "Specifies the time (in seconds) after there is no longer a nearby threat for the aggressive/player music tracks to stop playing. Effectively acts as the player 'calming down' after being attacked");
			
			builder.comment("Non-aggressive music track").push("non_aggro");
			
			this.nonAggressiveTrackEnabled = this.createValue(builder, true, "nonAggressiveTrackEnabled", ReloadType.NONE, "Specifies if the non-aggressive music track should play at all");
			
			this.nonAggressiveFadeTime = this.createRangedDoubleValue(builder, 6.0D, 0.05D, 60.0D, "nonAggressiveFadeTime", ReloadType.NONE, "The time (in seconds) for the non-aggressive music track to fade in/out");
			
			this.maxMobsForMaxVolume = this.createRangedIntValue(builder, 5, 1, 100, "maxMobsForMaxVolume", ReloadType.NONE, "The maximum amount of mobs required for the non-aggressive background music track to reach full volume. Set to '1' for this track type to play at full volume constantly");
			
			builder.pop();
			
			builder.comment("Aggressive music track").push("aggro");
			
			this.aggressiveTrackEnabled = this.createValue(builder, true, "aggressiveTrackEnabled", ReloadType.NONE, "Specifies if the aggressive music track should play at all");
			
			this.aggressiveFadeTime = this.createRangedDoubleValue(builder, 2.0D, 0.05D, 60.0D, "aggressiveFadeTime", ReloadType.NONE, "The time (in seconds) for the aggressive music track to fade in/out");
			
			this.threatReevaluationCooldown = this.createRangedIntValue(builder, 5, 1, 300, "threatReevaluationCooldown", ReloadType.NONE, "Specifies the time (in seconds) for the current threat that causes the aggressive music track to play to be reevaluated");
			
			this.onlyCountVisibleMobs = this.createValue(builder, true, "onlyCountVisibleMobs", ReloadType.NONE, "If enabled, the aggressive track will only play for threats that the player can see on screen");
			
			builder.pop();
			
			builder.comment("Player music track").push("player");
			
			this.playerTrackEnabled = this.createValue(builder, true, "playerTrackEnabled", ReloadType.NONE, "Specifies if the player music track should play at all");
			
			this.playerFadeTime = this.createRangedDoubleValue(builder, 1.0D, 0.05D, 60.0D, "playerFadeTime", ReloadType.NONE, "The time (in seconds) for the player music track to fade in/out");
			
			this.playerReevaluationCooldown = this.createRangedIntValue(builder, 20, 1, 300, "playerReevaluationCooldown", ReloadType.NONE, "Specifies the time (in seconds) for the current player threat that is causing the player music track to play to be reevaluated. Will cause the player track to stop playing if the player is no longer attacking or being a threat");
			
			this.punchingCountsAsViolence = this.createValue(builder, false, "punchingCountsAsViolence", ReloadType.NONE, "Specifies if punching a player should count towards the player track playing");
			
			builder.pop();
		}
	}
}
