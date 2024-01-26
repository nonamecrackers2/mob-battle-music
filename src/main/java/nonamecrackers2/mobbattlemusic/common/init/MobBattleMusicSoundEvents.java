package nonamecrackers2.mobbattlemusic.common.init;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nonamecrackers2.mobbattlemusic.MobBattleMusicMod;

public class MobBattleMusicSoundEvents
{
	private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MobBattleMusicMod.MODID);
	
	public static final RegistryObject<SoundEvent> NON_AGGRO_TRACK = createSoundEvent("non_aggro_track");
	public static final RegistryObject<SoundEvent> AGGRO_TRACK = createSoundEvent("aggro_track");
	public static final RegistryObject<SoundEvent> PLAYER_TRACK = createSoundEvent("player_track");
	
	private static RegistryObject<SoundEvent> createSoundEvent(String name)
	{
		return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(MobBattleMusicMod.id(name)));
	}
			
	public static void register(IEventBus modBus)
	{
		SOUND_EVENTS.register(modBus);
	}
}
