package nonamecrackers2.mobbattlemusic.client.resource;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import nonamecrackers2.mobbattlemusic.client.sound.track.MobListTrack;
import nonamecrackers2.mobbattlemusic.client.sound.track.MobSpecificTrack;
import nonamecrackers2.mobbattlemusic.client.sound.track.MobTagTrack;
import nonamecrackers2.mobbattlemusic.client.sound.track.MobTrack;
import nonamecrackers2.mobbattlemusic.client.sound.track.TrackType;
import nonamecrackers2.mobbattlemusic.client.util.MobBattleMusicUtils;
import nonamecrackers2.mobbattlemusic.client.util.MobSelection;

public class MusicTracksManager extends SimpleJsonResourceReloadListener
{
	private static final Gson GSON = new GsonBuilder().create();
	private static final MusicTracksManager INSTANCE = new MusicTracksManager();
	private static final Logger LOGGER = LogManager.getLogger("mobbattlemusic/MusicTracksManager");
	private List<TrackType> tracks;
	
	private MusicTracksManager()
	{
		super(GSON, "music_tracks");
		this.tracks = ImmutableList.copyOf(applyDefaultTrackTypes());
	}
	
	private static List<TrackType> applyDefaultTrackTypes()
	{
		return Lists.newArrayList(TrackType.PLAYER, TrackType.AGGRESSIVE, TrackType.AMBIENT);
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler)
	{
		Minecraft mc = Minecraft.getInstance();
		List<TrackType> list = applyDefaultTrackTypes();
		for (var entry : files.entrySet())
		{
			try
			{
				JsonObject object = GsonHelper.convertToJsonObject(entry.getValue(), "file");
				
				String type = GsonHelper.getAsString(object, "type");
				ResourceLocation track = new ResourceLocation(GsonHelper.getAsString(object, "sound"));
				if (mc.getSoundManager().getSoundEvent(track) == null)
					throw new NullPointerException("Unknown sound with id '" + track + "'");
				int priority = GsonHelper.getAsInt(object, "priority");
				
				switch (type)
				{
				case "mob_specific":
				{
					insert(list, parseMobTrack(object, track, (t, f, g, s) -> {
						return new MobSpecificTrack(parseEntityType(GsonHelper.getAsString(object, "mob")), t, f, g, s);
					}), priority);
					break;
				}
				case "mob_list":
				{
					insert(list, parseMobTrack(object, track, (t, f, g, s) -> 
					{
						JsonArray array = GsonHelper.getAsJsonArray(object, "mobs");
						List<EntityType<?>> entityTypes = Lists.newArrayList();
						for (JsonElement element : array)
							entityTypes.add(parseEntityType(GsonHelper.convertToString(element, "entity type")));
						return new MobListTrack(entityTypes, t, f, g, s);
					}), priority);
					break;
				}
				case "mob_tag":
				{
					insert(list, parseMobTrack(object, track, (t, f, g, s) -> 
					{
						TagKey<EntityType<?>> tag = TagKey.codec(Registries.ENTITY_TYPE).parse(JsonOps.INSTANCE, object.get("tag")).resultOrPartial(m -> {
							throw new JsonSyntaxException(m);
						}).get();
						return new MobTagTrack(tag, t, f, g, s);
					}), priority);
					break;
				}
				default:
					throw new JsonSyntaxException("Unknown type '" + type + "'");
				}
			}
			catch (Exception e)
			{
				LOGGER.debug("Failed to generate track for file '" + entry.getKey() + "': ", e);
			}
		}
		this.tracks = ImmutableList.copyOf(list);
	}
	
	private static void insert(List<TrackType> list, TrackType track, int index)
	{
		if (index >= list.size())
			list.add(track);
		else
			list.add(index, track);
	}
	
	public List<TrackType> getTracks()
	{
		return this.tracks;
	}
	
	private static <T extends MobTrack> T parseMobTrack(JsonObject object, ResourceLocation track, MusicTracksManager.MobTrackBuilder<T> builder)
	{
		int fadeTime = GsonHelper.getAsInt(object, "fade_time");
		MobSelection.GroupType group = MobBattleMusicUtils.parseEnum(MobSelection.GroupType.class, GsonHelper.getAsString(object, "group")).resultOrPartial(m -> {
			throw new NoSuchElementException(m);
		}).get();
		MobSelection.Selector selector = MobBattleMusicUtils.parseEnum(MobSelection.Selector.class, GsonHelper.getAsString(object, "selector")).resultOrPartial(m -> {
			throw new NoSuchElementException(m);
		}).get();
		return builder.make(track, fadeTime, group, selector);
	}
	
	private static EntityType<?> parseEntityType(String rawId)
	{
		ResourceLocation id = new ResourceLocation(rawId);
		EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(id);
		if (entityType == null)
			throw new NullPointerException("Unknown entity with id: '" + id + "'");
		return entityType;
	}
	
	public static MusicTracksManager getInstance()
	{
		return INSTANCE;
	}
	
	@FunctionalInterface
	public static interface MobTrackBuilder<T extends MobTrack>
	{
		T make(ResourceLocation track, int fadeTime, MobSelection.GroupType group, MobSelection.Selector selector);
	}
}
