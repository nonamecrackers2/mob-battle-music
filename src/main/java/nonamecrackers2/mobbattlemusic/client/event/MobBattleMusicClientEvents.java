package nonamecrackers2.mobbattlemusic.client.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nonamecrackers2.mobbattlemusic.client.init.MobBattleMusicClientCapabilities;
import nonamecrackers2.mobbattlemusic.client.manager.BattleMusicManager;

public class MobBattleMusicClientEvents
{
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		if (event.phase == TickEvent.Phase.END && mc.level != null && !mc.isPaused())
			mc.level.getCapability(MobBattleMusicClientCapabilities.MUSIC_MANAGER).ifPresent(BattleMusicManager::tick);
	}
	
	public static void onSoundEngineLoad(SoundEngineLoadEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null)
			mc.level.getCapability(MobBattleMusicClientCapabilities.MUSIC_MANAGER).ifPresent(BattleMusicManager::reload);
	}
}
