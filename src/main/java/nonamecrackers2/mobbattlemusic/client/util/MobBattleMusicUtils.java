package nonamecrackers2.mobbattlemusic.client.util;

import com.mojang.serialization.DataResult;

import net.minecraft.util.StringRepresentable;

public class MobBattleMusicUtils
{
	public static <T extends Enum<T> & StringRepresentable> DataResult<T> parseEnum(Class<T> clazz, String id)
	{
		for (T constant : clazz.getEnumConstants())
		{
			if (constant.getSerializedName().equals(id))
				return DataResult.success(constant);
		}
		return DataResult.error(() -> "Unknown " + clazz.getSimpleName().toUpperCase() + " '" + id + "'");
	}
}
