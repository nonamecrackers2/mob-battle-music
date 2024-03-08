package nonamecrackers2.mobbattlemusic.client.resource;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import nonamecrackers2.mobbattlemusic.client.sound.track.MobSpecificTrack;
import nonamecrackers2.mobbattlemusic.client.sound.track.TrackType;
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
					if (priority >= list.size())
						list.add(parseMobSpecific(object, track));
					else
						list.add(priority, parseMobSpecific(object, track));
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
	
	public List<TrackType> getTracks()
	{
		return this.tracks;
	}
	
	private static MobSpecificTrack parseMobSpecific(JsonObject object, ResourceLocation track) throws JsonSyntaxException, ResourceLocationException, NullPointerException
	{
		int fadeTime = GsonHelper.getAsInt(object, "fade_time");
		String rawId = GsonHelper.getAsString(object, "mob");
		ResourceLocation id = new ResourceLocation(rawId);
		EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
		if (type == null)
			throw new NullPointerException("Unknown entity with id: '" + rawId + "'");
		String selectorRaw = GsonHelper.getAsString(object, "mob_selector");
		MobSelection.Type selector = null;
		for (MobSelection.Type t : MobSelection.Type.values())
		{
			if (selectorRaw.equals(t.getSerializedName()))
			{
				selector = t;
				break;
			}
		}
		if (selector == null)
			throw new NullPointerException("Not a valid mob selector: '" + selectorRaw + "'");
		return new MobSpecificTrack(type, track, fadeTime, selector);
	}

	public static MusicTracksManager getInstance()
	{
		return INSTANCE;
	}
}
