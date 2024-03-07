package nonamecrackers2.mobbattlemusic.client.resource;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import nonamecrackers2.mobbattlemusic.client.sound.track.MobSpecificTrack;
import nonamecrackers2.mobbattlemusic.client.sound.track.TrackType;

public class MusicTracksManager extends SimpleJsonResourceReloadListener
{
	private static final Gson GSON = new GsonBuilder().create();
	private static @Nullable MusicTracksManager instance;
	private List<TrackType> tracks;
	
	public MusicTracksManager()
	{
		super(GSON, "music_tracks");
		if (instance != null)
			throw new IllegalStateException("Music tracks manager has already been initialized!");
		instance = this;
		this.tracks = ImmutableList.copyOf(applyDefaultTrackTypes());
	}
	
	private static ImmutableList.Builder<TrackType> applyDefaultTrackTypes()
	{
		return ImmutableList.builder(TrackType.PLAYER, TrackType.AGGRESSIVE, TrackType.AMBIENT);
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler)
	{
		List<TrackType> list = applyDefaultTrackTypes();
		for (var entry : files.entrySet())
		{
			JsonObject object = GsonHelper.convertToJsonObject(entry.getValue(), "file");
			
			String type = GsonHelper.getAsString(object, "type");
			ResourceLocation track = new ResourceLocation(GsonHelper.getAsString(object, "sound_id"));
			int priority = GsonHelper.getAsInt(object, "priority");
			
			switch (type)
			{
			case "mob_specific":
			{
				this.tracks.add(priority, parseMobSpecific(object, track));
				break;
			}
			default:
				throw new JsonSyntaxException("Unknown type '" + type + "'");
			}
		}
		this.tracks = ImmutableList.copyOf(null)
	}
	
	private static MobSpecificTrack parseMobSpecific(JsonObject object, ResourceLocation track) throws JsonSyntaxException, ResourceLocationException, NullPointerException
	{
		int fadeTime = GsonHelper.getAsInt(object, "fade_time");
		String rawId = GsonHelper.getAsString(object, "mob");
		ResourceLocation id = new ResourceLocation(rawId);
		EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
		if (type == null)
			throw new NullPointerException("Unknown entity with id: '" + rawId + "'");
		return new MobSpecificTrack(type, track, fadeTime);
	}

	public MusicTracksManager getInstance()
	{
		return Objects.requireNonNull(instance, "Music tracks manager has not been initialized yet!");
	}
}
