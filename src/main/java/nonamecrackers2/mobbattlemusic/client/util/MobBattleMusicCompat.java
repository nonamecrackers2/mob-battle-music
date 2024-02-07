package nonamecrackers2.mobbattlemusic.client.util;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.ModList;

public class MobBattleMusicCompat
{
	private static final Logger LOGGER = LogManager.getLogger("mobbattlemusic/MobBattleMusicCompat");
	private static  @Nullable Class<?> WITHER_STORM_MOD_BOSS_THEME_LOOP;
	
	public static void checkModCompat()
	{
		if (ModList.get().isLoaded("witherstormmod"))
		{
			try
			{
				WITHER_STORM_MOD_BOSS_THEME_LOOP = Class.forName("nonamecrackers2.witherstormmod.client.audio.bosstheme.BossThemeLoop");
				LOGGER.info("witherstormmod detected, enabling compat");
			}
			catch (ClassNotFoundException e)
			{
				LOGGER.warn("Failed to get class for BossThemeLoop from 'witherstormmod'");
				e.printStackTrace();
			}
		}
	}
	
	public static Class<?> getWitherStormModBossThemeLoopClass()
	{
		return WITHER_STORM_MOD_BOSS_THEME_LOOP;
	}
}
