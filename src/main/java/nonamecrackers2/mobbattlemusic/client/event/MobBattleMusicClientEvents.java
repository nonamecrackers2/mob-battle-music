package nonamecrackers2.mobbattlemusic.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import nonamecrackers2.crackerslib.client.event.impl.ConfigMenuButtonEvent;
import nonamecrackers2.crackerslib.client.event.impl.RegisterConfigScreensEvent;
import nonamecrackers2.crackerslib.client.gui.ConfigHomeScreen;
import nonamecrackers2.crackerslib.client.gui.title.TextTitle;
import nonamecrackers2.mobbattlemusic.MobBattleMusicMod;
import nonamecrackers2.mobbattlemusic.client.config.MobBattleMusicConfig;
import nonamecrackers2.mobbattlemusic.client.init.MobBattleMusicClientCapabilities;
import nonamecrackers2.mobbattlemusic.client.manager.BattleMusicManager;

public class MobBattleMusicClientEvents
{
	public static void registerConfigScreen(RegisterConfigScreensEvent event)
	{
		event.builder(ConfigHomeScreen.builder(TextTitle.ofModDisplayName(MobBattleMusicMod.MODID))
				.crackersDefault("https://github.com/nonamecrackers2/mob-battle-music/issues").build()
		).addSpec(ModConfig.Type.CLIENT, MobBattleMusicConfig.CLIENT_SPEC).register();
	}
	
	public static void registerConfigMenuButton(ConfigMenuButtonEvent event)
	{
		event.defaultButtonWithSingleCharacter('M', 0xFF85FF75);
	}
	
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		if (event.phase == TickEvent.Phase.END && mc.level != null && !mc.isPaused())
			mc.level.getCapability(MobBattleMusicClientCapabilities.MUSIC_MANAGER).ifPresent(BattleMusicManager::tick);
	}
	
	@SubscribeEvent
	public static void onPlayerAttack(AttackEntityEvent event)
	{
		Player player = event.getEntity();
		if (player.level().isClientSide())
		{
			player.level().getCapability(MobBattleMusicClientCapabilities.MUSIC_MANAGER).ifPresent(manager -> {
				manager.onAttack(event.getTarget());
			});
		}
	}
	
	public static void onSoundEngineLoad(SoundEngineLoadEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null)
			mc.level.getCapability(MobBattleMusicClientCapabilities.MUSIC_MANAGER).ifPresent(BattleMusicManager::reload);
	}
}
