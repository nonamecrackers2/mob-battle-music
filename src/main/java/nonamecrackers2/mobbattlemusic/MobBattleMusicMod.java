package nonamecrackers2.mobbattlemusic;

import org.apache.maven.artifact.versioning.ArtifactVersion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nonamecrackers2.mobbattlemusic.client.config.MobBattleMusicConfig;
import nonamecrackers2.mobbattlemusic.client.event.MobBattleMusicClientEvents;
import nonamecrackers2.mobbattlemusic.client.init.MobBattleMusicClientCapabilities;
import nonamecrackers2.mobbattlemusic.client.util.MobBattleMusicCompat;

@Mod(MobBattleMusicMod.MODID)
public class MobBattleMusicMod
{
	public static final String MODID = "mobbattlemusic";
	private static ArtifactVersion version;
	
	public MobBattleMusicMod()
	{
		version = ModLoadingContext.get().getActiveContainer().getModInfo().getVersion();
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(MobBattleMusicClientEvents::registerReloadListeners);
		modEventBus.addListener(this::clientSetup);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, MobBattleMusicConfig.CLIENT_SPEC);
	}
	
	public void clientSetup(FMLClientSetupEvent event)
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(MobBattleMusicClientCapabilities::registerCapabilities);
		modEventBus.addListener(MobBattleMusicClientEvents::onSoundEngineLoad);
		modEventBus.addListener(MobBattleMusicClientEvents::registerConfigScreen);
		modEventBus.addListener(MobBattleMusicClientEvents::registerConfigMenuButton);
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.addGenericListener(Level.class, MobBattleMusicClientCapabilities::attachLevelCapabilities);
		forgeBus.register(MobBattleMusicClientEvents.class);
		event.enqueueWork(() -> {
			MobBattleMusicCompat.checkModCompat();
		});
	}
	
	public static ResourceLocation id(String path)
	{
		return new ResourceLocation(MODID, path);
	}
	
	public static ArtifactVersion getModVersion()
	{
		return version;
	}
}
