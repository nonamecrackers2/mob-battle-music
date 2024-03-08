package nonamecrackers2.mobbattlemusic.client.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import nonamecrackers2.mobbattlemusic.client.config.MobBattleMusicConfig;

public class MobSelection
{
	private final Map<MobSelection.Type, List<Mob>> mobs;
	private final @Nullable LivingEntity panicTarget;
	
	private MobSelection(Map<MobSelection.Type, List<Mob>> mobs, @Nullable LivingEntity panicTarget)
	{
		this.mobs = mobs;
		this.panicTarget = panicTarget;
	}
	
	public List<Mob> forSelection(MobSelection.Type sel)
	{
		return this.mobs.get(sel);
	}
	
	public int count(MobSelection.Type sel)
	{
		return this.forSelection(sel).size();
	}
	
	public @Nullable LivingEntity panicTarget()
	{
		return this.panicTarget;
	}
	
	public static MobSelection.Builder builder()
	{
		return new MobSelection.Builder();
	}
	
	public static MobSelection.Type enemies()
	{
		return MobBattleMusicConfig.CLIENT.onlyCountVisibleMobs.get() ? MobSelection.Type.ENEMIES : MobSelection.Type.VIEWABLE_ENEMIES;
	}
	
	public static MobSelection.Type attacking()
	{
		return MobBattleMusicConfig.CLIENT.onlyCountVisibleMobs.get() ? MobSelection.Type.ATTACKING : MobSelection.Type.VIEWABLE_ATTACKING;
	}
	
	public static class Builder
	{
		private final Map<MobSelection.Type, List<Mob>> mobs = Maps.newEnumMap(MobSelection.Type.class);
		private @Nullable LivingEntity panicTarget;
		
		private Builder()
		{
			for (MobSelection.Type sel : MobSelection.Type.values())
				this.mobs.put(sel, Lists.newArrayList());
		}
		
		public Builder add(MobSelection.Type sel, Mob mob)
		{
			this.mobs.get(sel).add(mob);
			return this;
		}
		
		public Builder setPanicTarget(@Nullable LivingEntity panicTarget)
		{
			this.panicTarget = panicTarget;
			return this;
		}
		
		public MobSelection build()
		{
			return new MobSelection(this.mobs.entrySet().stream().map(e -> Map.entry(e.getKey(), ImmutableList.copyOf(e.getValue()))).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)), this.panicTarget);
		}
	}
	
	public static enum Type implements StringRepresentable
	{
		ENEMIES("enemies"),
		ATTACKING("attacking"),
		VIEWABLE_ENEMIES("viewable_enemies"),
		VIEWABLE_ATTACKING("viewable_attacking");

		private final String id;
		
		private Type(String id)
		{
			this.id = id;
		}
		
		@Override
		public String getSerializedName()
		{
			return this.id;
		}
	}
}
