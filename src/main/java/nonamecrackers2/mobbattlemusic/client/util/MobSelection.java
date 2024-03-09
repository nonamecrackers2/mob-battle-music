package nonamecrackers2.mobbattlemusic.client.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.Maps;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import nonamecrackers2.mobbattlemusic.client.config.MobBattleMusicConfig;

public class MobSelection
{
	private static final MobSelection.EmptyGroup EMPTY = new MobSelection.EmptyGroup();
	private final Map<MobSelection.GroupType, MobSelection.Group> groups;
	private final @Nullable LivingEntity panicTarget;
	
	private MobSelection(Map<MobSelection.GroupType, MobSelection.Group> groups, @Nullable LivingEntity panicTarget)
	{
		this.groups = groups;
		this.panicTarget = panicTarget;
	}
	
	public MobSelection.Group group(MobSelection.GroupType type)
	{
		return this.groups.getOrDefault(type, EMPTY);
	}
	
	public @Nullable LivingEntity panicTarget()
	{
		return this.panicTarget;
	}
	
	public static MobSelection.Builder builder()
	{
		return new MobSelection.Builder();
	}
	
	public static MobSelection.Selector defaultSelector()
	{
		return MobBattleMusicConfig.CLIENT.onlyCountVisibleMobs.get() ? MobSelection.Selector.ON_SCREEN : MobSelection.Selector.LINE_OF_SIGHT;
	}
	
	public static class Group
	{
		private final Map<MobSelection.Selector, List<Mob>> all;
		
		Group(Map<MobSelection.Selector, List<Mob>> all)
		{
			this.all = all;
		}
		
		public List<Mob> forSelector(MobSelection.Selector selector)
		{
			if (this.all.containsKey(selector))
				return this.all.get(selector);
			else
				return Collections.emptyList();
		}
		
		public int count(MobSelection.Selector selector)
		{
			return this.forSelector(selector).size();
		}
	}
	
	static class EmptyGroup extends MobSelection.Group
	{
		EmptyGroup()
		{
			super(null);
		}
		
		@Override
		public List<Mob> forSelector(Selector selector)
		{
			return Collections.emptyList();
		}
		
		@Override
		public int count(Selector selector)
		{
			return 0;
		}
	}
	
	public static class Builder
	{
		private final Map<MobSelection.GroupType, MobSelection.Group> mobs = Maps.newEnumMap(MobSelection.GroupType.class);
		private @Nullable LivingEntity panicTarget;
		
		private Builder() {}
		
		public Builder addToGroup(MobSelection.GroupType type, MobSelection.Selector selector, Mob mob)
		{
			MobSelection.Group group = this.mobs.computeIfAbsent(type, t -> new MobSelection.Group(Maps.newEnumMap(MobSelection.Selector.class)));
			group.all.computeIfAbsent(selector, s -> Lists.newArrayList()).add(mob);
			return this;
		}
		
		public Builder setPanicTarget(@Nullable LivingEntity panicTarget)
		{
			this.panicTarget = panicTarget;
			return this;
		}
		
		public MobSelection build()
		{
			return new MobSelection(this.mobs, this.panicTarget);
		}
	}
	
	public static enum GroupType implements StringRepresentable
	{
		ENEMIES("enemies"),
		ATTACKING("attacking");

		private final String id;
		
		private GroupType(String id)
		{
			this.id = id;
		}
		
		@Override
		public String getSerializedName()
		{
			return this.id;
		}
	}
	
	public static enum Selector implements StringRepresentable
	{
		ANY("any"),
		LINE_OF_SIGHT("line_of_sight"),
		ON_SCREEN("on_screen");

		private final String id;
		
		private Selector(String id)
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
