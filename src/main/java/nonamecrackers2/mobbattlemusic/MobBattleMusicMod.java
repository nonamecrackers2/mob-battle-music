package nonamecrackers2.mobbattlemusic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nonamecrackers2.mobbattlemusic.client.event.MobBattleMusicClientEvents;
import nonamecrackers2.mobbattlemusic.client.init.MobBattleMusicClientCapabilities;

@Mod(MobBattleMusicMod.MODID)
public class MobBattleMusicMod
{
	public static final String MODID = "mobbattlemusic";
	
	public MobBattleMusicMod()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::clientSetup);
	}
	
	public void clientSetup(FMLClientSetupEvent event)
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(MobBattleMusicClientCapabilities::registerCapabilities);
		modEventBus.addListener(MobBattleMusicClientEvents::onSoundEngineLoad);
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.addGenericListener(Level.class, MobBattleMusicClientCapabilities::attachLevelCapabilities);
		forgeBus.register(MobBattleMusicClientEvents.class);
	}
	
	public static ResourceLocation id(String path)
	{
		return new ResourceLocation(MODID, path);
	}
}
