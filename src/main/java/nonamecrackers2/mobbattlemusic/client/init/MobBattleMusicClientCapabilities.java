package nonamecrackers2.mobbattlemusic.client.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import nonamecrackers2.crackerslib.common.capability.CapUtil;
import nonamecrackers2.mobbattlemusic.MobBattleMusicMod;
import nonamecrackers2.mobbattlemusic.client.manager.BattleMusicManager;

public class MobBattleMusicClientCapabilities
{
	public static Capability<BattleMusicManager> MUSIC_MANAGER = CapabilityManager.get(new CapabilityToken<>() {});
	
	public static void registerCapabilities(RegisterCapabilitiesEvent event)
	{
		event.register(BattleMusicManager.class);
	}
	
	public static void attachLevelCapabilities(AttachCapabilitiesEvent<Level> event)
	{
		Level level = event.getObject();
		if (level instanceof ClientLevel clientLevel)
			CapUtil.registerCap(event, MobBattleMusicMod.id("music_manager"), MUSIC_MANAGER, () -> new BattleMusicManager(Minecraft.getInstance(), clientLevel));
	}
}
