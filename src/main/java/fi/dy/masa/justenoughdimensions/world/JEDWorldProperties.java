package fi.dy.masa.justenoughdimensions.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.justenoughdimensions.JustEnoughDimensions;
import fi.dy.masa.justenoughdimensions.util.JEDJsonUtils;
import fi.dy.masa.justenoughdimensions.util.JEDStringUtils;
import fi.dy.masa.justenoughdimensions.util.SpawnPointSearch;

public class JEDWorldProperties
{
    private static final Map<Integer, JEDWorldProperties> PROPERTIES = new HashMap<>();
    private static JEDWorldProperties clientProperties = new JEDWorldProperties();

    @Nullable
    private JsonObject fullJEDTag;
    @Nullable
    private JsonObject colorData;
    @Nullable
    private JsonObject perBiomeFogData;

    private boolean disableDawnDuskColors;
    private boolean disableDragon;
    private boolean disableEndSpikes;
    private boolean dontAdvanceWeatherWhenSleeping;
    private boolean forceGameMode;
    private boolean generateFallbackSpawnBlock;
    private boolean useCustomCelestialAngleRange;
    private boolean useCustomDayCycle;
    private boolean useCustomDayTimeRange;
    private boolean worldBorderCenteredOnSpawn;
    private float celestialAngleMin = 0.0f;
    private float celestialAngleMax = 1.0f;
    private int cloudHeight = 128;
    private int dayCycleIncrement = 24000;
    private int dayLength = 12000;
    private int dayTimeMax = 24000 - 1;
    private int dayTimeMin = 0;
    private int nightLength = 12000;
    private int skyDisableFlags;
    private int skyRenderType;
    private int voidTeleportInterval = 40;

    private float[] customLightBrightnessTable = null;
    private boolean hasPerBiomeFog = false;
    private final IdentityHashMap<Biome, Boolean> foggyBiomes = new IdentityHashMap<>();

    private Boolean canDoLightning = null;
    private Boolean canDoRainSnowIce = null;
    private Boolean canRespawnHere = null;
    private WorldProvider.WorldSleepResult canSleepHere = null;
    private Boolean canSpawnHostiles = null;
    private Boolean canSpawnPeacefulMobs = null;
    private Boolean hasSkyLight = null;
    private Boolean hasXZFog = null;
    private Boolean ignoreSpawnSuitability = null;
    private Boolean isSurfaceWorld = null;
    private Boolean shouldClientCheckLight = null;

    private Integer averageGroundLevel = null;
    private Double horizon = null;
    private Double movementFactor = null;
    private Integer respawnDimension = null;
    private Float sunBrightnessFactor = null;
    private Float sunBrightness = null;

    private Vec3d cloudColor = null;
    private Vec3d fogColor = null;
    private Vec3d moonColor = null;
    private Vec3d skyColor = null;
    private Vec3d sunColor = null;

    private Float skyBlendRatio = null;
    private Float moonScale = null;
    private Float sunScale = null;
    private Float skyColorLightBlendRatio = null;

    private String cloudRenderer = null;
    private String skyRenderer = null;
    private String weatherRenderer = null;

    private String musicType = null;
    private String worldProviderOverrideClass = null;

    private SpawnPointSearch spawnPointSearchType = null;

    @Nullable
    public static JEDWorldProperties getPropertiesIfExists(World world)
    {
        JEDWorldProperties props = getPropertiesIfExists(world.provider.getDimension());

        if (props == null && world.isRemote)
        {
            props = clientProperties;
        }

        return props;
    }

    @Nullable
    public static JEDWorldProperties getPropertiesIfExists(int dimension)
    {
        return PROPERTIES.get(dimension);
    }

    /**
     * Gets the properties for the requested dimension.
     * If there are no properties for that dimension, then a new default instance is returned.
     * NOTE: If the default instance is created and returned, it is NOT added to the map.
     * @param dimension
     * @return the properties for the requested dimension, or a new default instance (which is NOT added to the map!)
     */
    public static JEDWorldProperties getOrCreateProperties(int dimension)
    {
        JEDWorldProperties props = PROPERTIES.get(dimension);

        if (props == null)
        {
            props = new JEDWorldProperties();
        }

        return props;
    }

    public static JEDWorldProperties createAndSetPropertiesForDimension(int dimension, @Nullable JsonObject obj)
    {
        if (obj != null)
        {
            JEDWorldProperties props = new JEDWorldProperties(obj);
            PROPERTIES.put(dimension, props);

            return props;
        }

        return null;
    }

    public static void setClientProperties(@Nullable JsonObject obj)
    {
        clientProperties = obj != null ? new JEDWorldProperties(obj) : new JEDWorldProperties();
    }

    public static JEDWorldProperties getClientProperties()
    {
        return clientProperties;
    }

    public static void removePropertiesFrom(int dimension)
    {
        PROPERTIES.remove(dimension);
    }

    public static void clearWorldProperties()
    {
        PROPERTIES.clear();
    }

    private JEDWorldProperties()
    {
    }

    private JEDWorldProperties(@Nonnull JsonObject obj)
    {
        this.fullJEDTag = JEDJsonUtils.deepCopy(obj);
        this.colorData = JEDJsonUtils.getNestedObject(obj, "Colors", false);
        this.perBiomeFogData = JEDJsonUtils.getNestedObject(obj, "FoggyBiomes", false);

        if (JEDJsonUtils.hasBoolean(obj, "ForceGameMode"))          { this.forceGameMode            = JEDJsonUtils.getBoolean(obj, "ForceGameMode"); }
        if (JEDJsonUtils.hasBoolean(obj, "CustomDayCycle"))         { this.useCustomDayCycle        = JEDJsonUtils.getBoolean(obj, "CustomDayCycle"); }
        if (JEDJsonUtils.hasBoolean(obj, "CanRespawnHere"))         { this.canRespawnHere           = JEDJsonUtils.getBoolean(obj, "CanRespawnHere"); }
        if (JEDJsonUtils.hasBoolean(obj, "CanSpawnPeacefulMobs"))   { this.canSpawnPeacefulMobs     = JEDJsonUtils.getBoolean(obj, "CanSpawnPeacefulMobs"); }
        if (JEDJsonUtils.hasBoolean(obj, "CanSpawnHostiles"))       { this.canSpawnHostiles         = JEDJsonUtils.getBoolean(obj, "CanSpawnHostiles"); }
        if (JEDJsonUtils.hasBoolean(obj, "DisableDawnDuskColors"))  { this.disableDawnDuskColors    = JEDJsonUtils.getBoolean(obj, "DisableDawnDuskColors"); }
        if (JEDJsonUtils.hasBoolean(obj, "DisableEnderDragon"))     { this.disableDragon            = JEDJsonUtils.getBoolean(obj, "DisableEnderDragon"); }
        if (JEDJsonUtils.hasBoolean(obj, "DisableEndSpikes"))       { this.disableEndSpikes         = JEDJsonUtils.getBoolean(obj, "DisableEndSpikes"); }
        if (JEDJsonUtils.hasBoolean(obj, "HasSkyLight"))            { this.hasSkyLight              = JEDJsonUtils.getBoolean(obj, "HasSkyLight"); }
        if (JEDJsonUtils.hasBoolean(obj, "IsSurfaceWorld"))         { this.isSurfaceWorld           = JEDJsonUtils.getBoolean(obj, "IsSurfaceWorld"); }
        if (JEDJsonUtils.hasBoolean(obj, "IgnoreSpawnSuitability")) { this.ignoreSpawnSuitability   = JEDJsonUtils.getBoolean(obj, "IgnoreSpawnSuitability"); }
        if (JEDJsonUtils.hasBoolean(obj, "HasXZFog"))               { this.hasXZFog                 = JEDJsonUtils.getBoolean(obj, "HasXZFog"); }
        if (JEDJsonUtils.hasBoolean(obj, "CanDoLightning"))         { this.canDoLightning           = JEDJsonUtils.getBoolean(obj, "CanDoLightning"); }
        if (JEDJsonUtils.hasBoolean(obj, "CanDoRainSnowIce"))       { this.canDoRainSnowIce         = JEDJsonUtils.getBoolean(obj, "CanDoRainSnowIce"); }
        if (JEDJsonUtils.hasBoolean(obj, "ShouldClientCheckLight")) { this.shouldClientCheckLight   = JEDJsonUtils.getBoolean(obj, "ShouldClientCheckLight"); }
        if (JEDJsonUtils.hasBoolean(obj, "WorldBorderCenteredOnSpawn")) { this.worldBorderCenteredOnSpawn = JEDJsonUtils.getBoolean(obj, "WorldBorderCenteredOnSpawn"); }
        if (JEDJsonUtils.hasBoolean(obj, "GenerateFallbackSpawnBlock")) { this.generateFallbackSpawnBlock = JEDJsonUtils.getBoolean(obj, "GenerateFallbackSpawnBlock"); }
        if (JEDJsonUtils.hasBoolean(obj, "DontAdvanceWeatherWhenSleeping")) { this.dontAdvanceWeatherWhenSleeping = JEDJsonUtils.getBoolean(obj, "DontAdvanceWeatherWhenSleeping"); }
        
        if (JEDJsonUtils.hasInteger(obj, "DayLength"))          { this.dayLength            = JEDJsonUtils.getInteger(obj, "DayLength"); }
        if (JEDJsonUtils.hasInteger(obj, "NightLength"))        { this.nightLength          = JEDJsonUtils.getInteger(obj, "NightLength"); }
        if (JEDJsonUtils.hasInteger(obj, "DayCycleIncrement"))  { this.dayCycleIncrement    = JEDJsonUtils.getInteger(obj, "DayCycleIncrement"); }
        if (JEDJsonUtils.hasInteger(obj, "CloudHeight"))        { this.cloudHeight          = JEDJsonUtils.getInteger(obj, "CloudHeight"); }
        if (JEDJsonUtils.hasInteger(obj, "SkyRenderType"))      { this.skyRenderType        = JEDJsonUtils.getInteger(obj, "SkyRenderType"); }
        if (JEDJsonUtils.hasInteger(obj, "SkyDisableFlags"))    { this.skyDisableFlags      = JEDJsonUtils.getInteger(obj, "SkyDisableFlags"); }
        if (JEDJsonUtils.hasInteger(obj, "RespawnDimension"))   { this.respawnDimension     = JEDJsonUtils.getInteger(obj, "RespawnDimension"); }
        if (JEDJsonUtils.hasInteger(obj, "AverageGroundLevel")) { this.averageGroundLevel   = JEDJsonUtils.getInteger(obj, "AverageGroundLevel"); }
        if (JEDJsonUtils.hasInteger(obj, "VoidTeleportInterval")) { this.voidTeleportInterval = JEDJsonUtils.getInteger(obj, "VoidTeleportInterval"); }

        if (JEDJsonUtils.hasInteger(obj, "SunBrightnessFactor")){ this.sunBrightnessFactor  = JEDJsonUtils.getFloat(obj, "SunBrightnessFactor"); }
        if (JEDJsonUtils.hasInteger(obj, "SunBrightness"))      { this.sunBrightness        = JEDJsonUtils.getFloat(obj, "SunBrightness"); }

        if (JEDJsonUtils.hasInteger(obj, "Horizon"))            { this.horizon              = JEDJsonUtils.getDouble(obj, "Horizon"); }
        if (JEDJsonUtils.hasInteger(obj, "MovementFactor"))     { this.movementFactor       = JEDJsonUtils.getDouble(obj, "MovementFactor"); }

        if (JEDJsonUtils.hasString(obj, "SkyRenderer"))         { this.skyRenderer          = JEDJsonUtils.getString(obj, "SkyRenderer"); }
        if (JEDJsonUtils.hasString(obj, "CloudRenderer"))       { this.cloudRenderer        = JEDJsonUtils.getString(obj, "CloudRenderer"); }
        if (JEDJsonUtils.hasString(obj, "WeatherRenderer"))     { this.weatherRenderer      = JEDJsonUtils.getString(obj, "WeatherRenderer"); }
        if (JEDJsonUtils.hasString(obj, "MusicType"))           { this.musicType            = JEDJsonUtils.getString(obj, "MusicType"); }
        if (JEDJsonUtils.hasString(obj, "WorldProviderOverride")) { this.worldProviderOverrideClass = JEDJsonUtils.getString(obj, "WorldProviderOverride"); }

        if (JEDJsonUtils.hasString(obj, "CloudColor"))  { this.cloudColor   = JEDStringUtils.hexStringToColor(JEDJsonUtils.getString(obj, "CloudColor")); }
        if (JEDJsonUtils.hasString(obj, "FogColor"))    { this.fogColor     = JEDStringUtils.hexStringToColor(JEDJsonUtils.getString(obj, "FogColor")); }
        if (JEDJsonUtils.hasString(obj, "MoonColor"))   { this.moonColor    = JEDStringUtils.hexStringToColor(JEDJsonUtils.getString(obj, "MoonColor")); }
        if (JEDJsonUtils.hasString(obj, "SkyColor"))    { this.skyColor     = JEDStringUtils.hexStringToColor(JEDJsonUtils.getString(obj, "SkyColor")); }
        if (JEDJsonUtils.hasString(obj, "SunColor"))    { this.sunColor     = JEDStringUtils.hexStringToColor(JEDJsonUtils.getString(obj, "SunColor")); }

        if (JEDJsonUtils.hasFloat(obj, "SkyBlend"))     { this.skyBlendRatio    = JEDJsonUtils.getFloat(obj, "SkyBlend"); }
        if (JEDJsonUtils.hasFloat(obj, "MoonScale"))    { this.moonScale        = JEDJsonUtils.getFloat(obj, "MoonScale"); }
        if (JEDJsonUtils.hasFloat(obj, "SunScale"))     { this.sunScale         = JEDJsonUtils.getFloat(obj, "SunScale"); }
        if (JEDJsonUtils.hasFloat(obj, "SkyLightBlend")){ this.skyColorLightBlendRatio = JEDJsonUtils.getFloat(obj, "SkyLightBlend"); }

        if (JEDJsonUtils.hasString(obj, "CanSleepHere"))
        {
            String value = JEDJsonUtils.getString(obj, "CanSleepHere");
            if (value.equalsIgnoreCase("allow"))                { this.canSleepHere = WorldProvider.WorldSleepResult.ALLOW; }
            else if (value.equalsIgnoreCase("deny"))            { this.canSleepHere = WorldProvider.WorldSleepResult.DENY; }
            else if (value.equalsIgnoreCase("bed_explodes"))    { this.canSleepHere = WorldProvider.WorldSleepResult.BED_EXPLODES; }
            else { JustEnoughDimensions.logger.warn("Invalid 'CanSleepHere' value: '{}'", value); }
        }

        if (JEDJsonUtils.hasObject(obj, "FoggyBiomes"))
        {
            JsonObject fogObj = obj.getAsJsonObject("FoggyBiomes");
            boolean listedHaveFog = JEDJsonUtils.getBoolean(fogObj, "listed_have_fog");
            Set<Biome> biomes = new HashSet<>();

            if (JEDJsonUtils.hasArray(fogObj, "biomes"))
            {
                JsonArray arr = fogObj.getAsJsonArray("biomes");
                final int size = arr.size();

                for (int i = 0; i < size; ++i)
                {
                    String name = arr.get(i).getAsString();
                    ResourceLocation rl = new ResourceLocation(name);
                    Biome biome = ForgeRegistries.BIOMES.getValue(rl);

                    if (biome != null)
                    {
                        biomes.add(biome);
                    }
                }
            }

            this.foggyBiomes.clear();

            if (listedHaveFog == false)
            {
                Set<Biome> allBiomes = new HashSet<>(ForgeRegistries.BIOMES.getValuesCollection());
                allBiomes.removeAll(biomes);
                biomes = allBiomes;
            }

            for (Biome biome : biomes)
            {
                this.foggyBiomes.put(biome, true);
            }

            this.hasPerBiomeFog = true;
        }

        if (JEDJsonUtils.hasDouble(obj, "CelestialAngleMin") && JEDJsonUtils.hasDouble(obj, "CelestialAngleMax"))
        {
            this.celestialAngleMin = MathHelper.clamp(JEDJsonUtils.getFloat(obj, "CelestialAngleMin"), 0f, 1f);
            this.celestialAngleMax = MathHelper.clamp(JEDJsonUtils.getFloat(obj, "CelestialAngleMax"), 0f, 1f);
            this.useCustomCelestialAngleRange = true;
        }

        if (JEDJsonUtils.hasInteger(obj, "DayTimeMin") && JEDJsonUtils.hasInteger(obj, "DayTimeMax"))
        {
            this.dayTimeMin = MathHelper.clamp(JEDJsonUtils.getInteger(obj, "DayTimeMin"), 0, 24000 - 1);
            this.dayTimeMax = MathHelper.clamp(JEDJsonUtils.getInteger(obj, "DayTimeMax"), 0, 24000 - 1);
            this.useCustomDayTimeRange = true;
        }

        if (JEDJsonUtils.hasObject(obj, "SpawnPointSearchType"))
        {
            this.spawnPointSearchType = SpawnPointSearch.fromJson(obj.get("SpawnPointSearchType").getAsJsonObject());
        }

        JsonElement el = obj.get("LightBrightness");

        if (el != null && el.isJsonArray())
        {
            JsonArray arr = el.getAsJsonArray();

            if (arr.size() == 16)
            {
                try
                {
                    float[] light = new float[16];

                    for (int i = 0; i < 16; i++)
                    {
                        light[i] = arr.get(i).getAsFloat();
                    }

                    this.customLightBrightnessTable = light;
                }
                catch (Exception e)
                {
                    JustEnoughDimensions.logger.warn("Failed to read a light brightness table from JSON", e);
                }
            }
        }

        if (this.dayLength   <= 0) { this.dayLength = 1; }
        if (this.nightLength <= 0) { this.nightLength = 1; }
    }

    public JsonObject getJEDPropsForClientSync()
    {
        JsonObject obj = new JsonObject();

        if (this.dayLength != 12000)        { obj.add("DayLength",          new JsonPrimitive(this.dayLength)); }
        if (this.nightLength != 12000)      { obj.add("NightLength",        new JsonPrimitive(this.nightLength)); }
        if (this.dayCycleIncrement != 24000){ obj.add("DayCycleIncrement",  new JsonPrimitive(this.dayCycleIncrement)); }
        if (this.cloudHeight != 128)        { obj.add("CloudHeight",        new JsonPrimitive(this.cloudHeight)); }
        if (this.skyRenderer != null)       { obj.add("SkyRenderer",        new JsonPrimitive(this.skyRenderer)); }
        if (this.cloudRenderer != null)     { obj.add("CloudRenderer",      new JsonPrimitive(this.cloudRenderer)); }
        if (this.weatherRenderer != null)   { obj.add("WeatherRenderer",    new JsonPrimitive(this.weatherRenderer)); }
        if (this.skyRenderType != 0)        { obj.add("SkyRenderType",      new JsonPrimitive(this.skyRenderType)); }
        if (this.skyDisableFlags != 0)      { obj.add("SkyDisableFlags",    new JsonPrimitive(this.skyDisableFlags)); }
        if (this.disableDawnDuskColors)     { obj.add("DisableDawnDuskColors", new JsonPrimitive(this.disableDawnDuskColors)); }
        if (this.useCustomDayCycle)         { obj.add("CustomDayCycle",     new JsonPrimitive(this.useCustomDayCycle)); }
        if (this.hasSkyLight != null)       { obj.add("HasSkyLight",        new JsonPrimitive(this.hasSkyLight)); }
        if (this.hasXZFog != null)          { obj.add("HasXZFog",           new JsonPrimitive(this.hasXZFog)); }
        if (this.horizon != null)           { obj.add("Horizon",            new JsonPrimitive(this.horizon)); }
        if (this.musicType != null)         { obj.add("MusicType",          new JsonPrimitive(this.musicType)); }

        if (this.cloudColor != null)        { obj.add("CloudColor",         new JsonPrimitive(JEDStringUtils.colorToHexString(this.cloudColor))); }
        if (this.fogColor != null)          { obj.add("FogColor",           new JsonPrimitive(JEDStringUtils.colorToHexString(this.fogColor))); }
        if (this.moonColor != null)         { obj.add("MoonColor",          new JsonPrimitive(JEDStringUtils.colorToHexString(this.moonColor))); }
        if (this.skyColor != null)          { obj.add("SkyColor",           new JsonPrimitive(JEDStringUtils.colorToHexString(this.skyColor))); }
        if (this.sunColor != null)          { obj.add("SunColor",           new JsonPrimitive(JEDStringUtils.colorToHexString(this.sunColor))); }

        if (this.skyBlendRatio != null)     { obj.add("SkyBlend",           new JsonPrimitive(this.skyBlendRatio)); }
        if (this.moonScale != null)         { obj.add("MoonScale",          new JsonPrimitive(this.moonScale)); }
        if (this.sunScale != null)          { obj.add("SunScale",           new JsonPrimitive(this.sunScale)); }
        if (this.skyColorLightBlendRatio != null) { obj.add("SkyLightBlend", new JsonPrimitive(this.skyColorLightBlendRatio)); }

        if (this.isSurfaceWorld != null)        { obj.add("IsSurfaceWorld",         new JsonPrimitive(this.isSurfaceWorld)); }
        if (this.shouldClientCheckLight != null){ obj.add("ShouldClientCheckLight", new JsonPrimitive(this.shouldClientCheckLight)); }
        if (this.sunBrightnessFactor != null)   { obj.add("SunBrightnessFactor",    new JsonPrimitive(this.sunBrightnessFactor)); }
        if (this.sunBrightness != null)         { obj.add("SunBrightness",          new JsonPrimitive(this.sunBrightness)); }
        if (this.worldProviderOverrideClass != null) { obj.add("WorldProviderOverride", new JsonPrimitive(this.worldProviderOverrideClass)); }

        if (this.useCustomCelestialAngleRange)
        {
            obj.add("CelestialAngleMin", new JsonPrimitive(this.celestialAngleMin));
            obj.add("CelestialAngleMax", new JsonPrimitive(this.celestialAngleMax));
        }

        if (this.useCustomDayTimeRange)
        {
            obj.add("DayTimeMin", new JsonPrimitive(this.dayTimeMin));
            obj.add("DayTimeMax", new JsonPrimitive(this.dayTimeMax));
        }

        if (this.colorData != null)
        {
            obj.add("Colors", JEDJsonUtils.deepCopy(this.colorData));
        }

        if (this.perBiomeFogData != null)
        {
            obj.add("FoggyBiomes", JEDJsonUtils.deepCopy(this.perBiomeFogData));
        }

        if (this.customLightBrightnessTable != null)
        {
            JsonArray arr = new JsonArray();

            for (int i = 0; i < this.customLightBrightnessTable.length; i++)
            {
                arr.add(this.customLightBrightnessTable[i]);
            }

            obj.add("LightBrightness", arr);
        }

        return obj;
    }

    @Nullable
    public JsonObject getFullJEDProperties()
    {
        return this.fullJEDTag;
    }

    @Nullable
    public JsonObject getNestedObject(String name)
    {
        if (this.fullJEDTag != null)
        {
            JsonObject obj = JEDJsonUtils.getNestedObject(this.fullJEDTag, name, false);
            return obj != null ? JEDJsonUtils.deepCopy(obj) : null;
        }

        return null;
    }

    @Nonnull
    private static NBTTagList writeFloats(float... values)
    {
        NBTTagList tagList = new NBTTagList();

        for (float f : values)
        {
            tagList.appendTag(new NBTTagFloat(f));
        }

        return tagList;
    }

    public boolean getDisableDawnDuskColors()
    {
        return this.disableDawnDuskColors;
    }

    public boolean getDisableDragon()
    {
        return this.disableDragon;
    }

    public boolean getDisableEndSpikes()
    {
        return this.disableEndSpikes;
    }

    public boolean getForceGameMode()
    {
        return this.forceGameMode;
    }

    public boolean getUseCustomDayCycle()
    {
        return this.useCustomDayCycle;
    }

    public boolean getUseCustomCelestialAngleRange()
    {
        return this.useCustomCelestialAngleRange;
    }

    public boolean getUseCustomDayTimeRange()
    {
        return this.useCustomDayTimeRange;
    }

    public boolean shouldWorldBorderBeCenteredOnSpawn()
    {
        return this.worldBorderCenteredOnSpawn;
    }

    public boolean getDontAdvanceWeatherWhenSleeping()
    {
        return this.dontAdvanceWeatherWhenSleeping;
    }

    public int getDayLength()
    {
        return this.dayLength;
    }

    public int getNightLength()
    {
        return this.nightLength;
    }

    public int getDayCycleIncrement()
    {
        return this.dayCycleIncrement;
    }

    public int getDayTimeMin()
    {
        return this.dayTimeMin;
    }

    public int getDayTimeMax()
    {
        return this.dayTimeMax;
    }

    public float getCelestialAngleMin()
    {
        return this.celestialAngleMin;
    }

    public float getCelestialAngleMax()
    {
        return this.celestialAngleMax;
    }

    public float[] getCustomLightBrightnessTable()
    {
        return this.customLightBrightnessTable;
    }

    public int getSkyRenderType()
    {
        return this.skyRenderType;
    }

    public int getSkyDisableFlags()
    {
        return this.skyDisableFlags;
    }

    public int getCloudHeight()
    {
        return this.cloudHeight;
    }

    public int getVoidTeleportInterval()
    {
        return this.voidTeleportInterval;
    }

    @Nullable
    public Vec3d getSkyColor()
    {
        return this.skyColor;
    }

    @Nullable
    public Vec3d getFogColor()
    {
        return this.fogColor;
    }

    @Nullable
    public Vec3d getCloudColor()
    {
        return this.cloudColor;
    }

    @Nullable
    public Float getSkyBlendRatio()
    {
        return this.skyBlendRatio;
    }

    @Nullable
    public Float getSkyColorLightBlendRatio()
    {
        return this.skyColorLightBlendRatio;
    }

    @Nullable
    public Vec3d getSunColor()
    {
        return this.sunColor;
    }

    @Nullable
    public Vec3d getMoonColor()
    {
        return this.moonColor;
    }

    @Nullable
    public Float getSunScale()
    {
        return this.sunScale;
    }

    @Nullable
    public Float getMoonScale()
    {
        return this.moonScale;
    }

    @Nullable
    public Boolean canRespawnHere()
    {
        return this.canRespawnHere;
    }

    @Nullable
    public WorldProvider.WorldSleepResult canSleepHere()
    {
        return this.canSleepHere;
    }

    @Nullable
    public Boolean canSpawnPeacefulMobs()
    {
        return this.canSpawnPeacefulMobs;
    }

    @Nullable
    public Boolean canSpawnHostiles()
    {
        return this.canSpawnHostiles;
    }

    @Nullable
    public Integer getRespawnDimension()
    {
        return this.respawnDimension;
    }

    @Nullable
    public Float getSunBrightnessFactor()
    {
        return this.sunBrightnessFactor;
    }

    @Nullable
    public Float getSunBrightness()
    {
        return this.sunBrightness;
    }

    @Nullable
    public Double getHorizon()
    {
        return this.horizon;
    }

    @Nullable
    public Double getMovementFactor()
    {
        return this.movementFactor;
    }

    @Nullable
    public Integer getAverageGroundLevel()
    {
        return this.averageGroundLevel;
    }

    @Nullable
    public Boolean isSurfaceWorld()
    {
        return this.isSurfaceWorld;
    }

    @Nullable
    public Boolean getHasSkyLight()
    {
        return this.hasSkyLight;
    }

    @Nullable
    public Boolean ignoreSpawnSuitability()
    {
        return this.ignoreSpawnSuitability;
    }

    @Nullable
    public Boolean getHasXZFog()
    {
        return this.hasXZFog;
    }

    public boolean getHasPerBiomeFog()
    {
        return this.hasPerBiomeFog;
    }

    public boolean doesBiomeHaveFog(Biome biome)
    {
        return this.foggyBiomes.containsKey(biome);
    }

    @Nullable
    public Boolean canDoLightning()
    {
        return this.canDoLightning;
    }

    @Nullable
    public Boolean canDoRainSnowIce()
    {
        return this.canDoRainSnowIce;
    }

    @Nullable
    public Boolean shouldClientCheckLight()
    {
        return this.shouldClientCheckLight;
    }

    public boolean generateFallbackSpawnBlock()
    {
        return this.generateFallbackSpawnBlock;
    }

    @Nullable
    public String getMusicType()
    {
        return this.musicType;
    }

    @Nullable
    public SpawnPointSearch getSpawnPointSearchType()
    {
        return this.spawnPointSearchType;
    }

    public boolean overrideWorldProvider()
    {
        return this.worldProviderOverrideClass != null;
    }

    @Nullable
    public String getWorldProviderOverrideClassName()
    {
        return this.worldProviderOverrideClass;
    }
}
