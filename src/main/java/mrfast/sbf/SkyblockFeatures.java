package mrfast.sbf;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import gg.essential.api.EssentialAPI;
import mrfast.sbf.commands.DungeonsCommand;
import mrfast.sbf.commands.FakePlayerCommand;
import mrfast.sbf.commands.FlipsCommand;
import mrfast.sbf.commands.GetkeyCommand;
import mrfast.sbf.commands.InventoryCommand;
import mrfast.sbf.commands.RepartyCommand;
import mrfast.sbf.commands.ShrugCommand;
import mrfast.sbf.commands.SkyCommand;
import mrfast.sbf.commands.TerminalCommand;
import mrfast.sbf.commands.ViewModelCommand;
import mrfast.sbf.commands.configCommand;
import mrfast.sbf.commands.getNbtCommand;
import mrfast.sbf.commands.pvCommand;
import mrfast.sbf.commands.sidebarCommand;
import mrfast.sbf.core.Config;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.ChatEventListener;
import mrfast.sbf.events.PacketEvent;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.features.actionBar.ActionBarListener;
import mrfast.sbf.features.actionBar.CryptDisplay;
import mrfast.sbf.features.actionBar.DefenceDisplay;
import mrfast.sbf.features.actionBar.EffectiveHealthDisplay;
import mrfast.sbf.features.actionBar.HealthDisplay;
import mrfast.sbf.features.actionBar.ManaDisplay;
import mrfast.sbf.features.actionBar.SecretDisplay;
import mrfast.sbf.features.actionBar.SpeedDisplay;
import mrfast.sbf.features.dungeons.BetterParties;
import mrfast.sbf.features.dungeons.ChestProfit;
import mrfast.sbf.features.dungeons.DungeonMap;
import mrfast.sbf.features.dungeons.DungeonsFeatures;
import mrfast.sbf.features.dungeons.Nametags;
import mrfast.sbf.features.dungeons.Reparty;
import mrfast.sbf.features.dungeons.ShadowAssasinFeatures;
import mrfast.sbf.features.dungeons.solvers.BlazeSolver;
import mrfast.sbf.features.dungeons.solvers.CreeperSolver;
import mrfast.sbf.features.dungeons.solvers.LividFinder;
import mrfast.sbf.features.dungeons.solvers.TeleportPadSolver;
import mrfast.sbf.features.dungeons.solvers.ThreeWeirdosSolver;
import mrfast.sbf.features.events.JerryTimer;
import mrfast.sbf.features.events.MayorJerry;
import mrfast.sbf.features.events.MythologicalEvent;
import mrfast.sbf.features.exoticAuctions.ExoticAuctions;
import mrfast.sbf.features.items.HideGlass;
import mrfast.sbf.features.mining.CommisionsTracker;
import mrfast.sbf.features.mining.HighlightCobblestone;
import mrfast.sbf.features.mining.MetalDetectorSolver;
import mrfast.sbf.features.mining.MiningFeatures;
import mrfast.sbf.features.mining.PathTracer;
import mrfast.sbf.features.misc.AuctionFeatures;
import mrfast.sbf.features.misc.AutoAuctionFlip;
import mrfast.sbf.features.misc.ChronomotronSolver;
import mrfast.sbf.features.misc.ConjuringCooldown;
import mrfast.sbf.features.misc.CropCounter;
import mrfast.sbf.features.misc.FishingHelper;
import mrfast.sbf.features.misc.ItemFeatures;
import mrfast.sbf.features.misc.MiscFeatures;
import mrfast.sbf.features.misc.PlayerDiguiser;
import mrfast.sbf.features.misc.SpamHider;
import mrfast.sbf.features.misc.TreecapCooldown;
import mrfast.sbf.features.misc.UltrasequencerSolver;
import mrfast.sbf.features.overlays.BaitCounterOverlay;
import mrfast.sbf.features.overlays.CollectionOverlay;
import mrfast.sbf.features.overlays.ComposterOverlay;
import mrfast.sbf.features.overlays.CrimsonMap;
import mrfast.sbf.features.overlays.CrystalHollowsMap;
import mrfast.sbf.features.overlays.DamageOverlays;
import mrfast.sbf.features.overlays.DwarvenMap;
import mrfast.sbf.features.overlays.FairySoulWaypoints;
import mrfast.sbf.features.overlays.GardenVisitorOverlay;
import mrfast.sbf.features.overlays.GemstoneMiningOverlay;
import mrfast.sbf.features.overlays.GiftTracker;
import mrfast.sbf.features.overlays.GrandmaWolfTimer;
import mrfast.sbf.features.overlays.MinionOverlay;
import mrfast.sbf.features.overlays.MiscOverlays;
import mrfast.sbf.features.overlays.MissingTalismans;
import mrfast.sbf.features.overlays.RelicFinderWaypoints;
import mrfast.sbf.features.overlays.TradingOverlay;
import mrfast.sbf.features.overlays.ZealotSpawnLocations;
import mrfast.sbf.features.render.DynamicFullbright;
import mrfast.sbf.features.render.HideStuff;
import mrfast.sbf.features.render.HighlightCropArea;
import mrfast.sbf.features.render.RiftFeatures;
import mrfast.sbf.features.render.SlayerFeatures;
import mrfast.sbf.features.trackers.AutomatonTracker;
import mrfast.sbf.features.trackers.EnderNodeTracker;
import mrfast.sbf.features.trackers.GhostTracker;
import mrfast.sbf.features.trackers.IceTreasureTracker;
import mrfast.sbf.features.trackers.PowderTracker;
import mrfast.sbf.features.trackers.TrevorHelper;
import mrfast.sbf.gui.GuiManager;
import mrfast.sbf.gui.ProfileViewerUtils;
import mrfast.sbf.utils.CapeUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ICommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = SkyblockFeatures.MODID, name = SkyblockFeatures.MOD_NAME, version = "1.2.6", acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true)
public class SkyblockFeatures {
    public static final String MODID = "skyblockfeatures";
    public static final String MOD_NAME = "skyblockfeatures";
    // Skyblock Features Produciton API key
    public static String API_KEY = "68f8f3dd-bb03-4ee8-9135-8f6ce023b88a";
    public static String VERSION = "Loading";
    public static final Minecraft mc = Minecraft.getMinecraft();

    public static Config config = new Config();
    public static File modDir = new File(new File(mc.mcDataDir, "config"), "skyblockfeatures");
    public static GuiManager GUIMANAGER;
    public static Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static int ticks = 0;

    public static ArrayDeque<String> sendMessageQueue = new ArrayDeque<>();
    public static boolean usingNEU = false;

    public static File jarFile = null;
    private static long lastChatMessage = 0;

    @Mod.Instance(MODID)
    public static SkyblockFeatures INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!modDir.exists()) modDir.mkdirs();
        GUIMANAGER = new GuiManager();
        jarFile = event.getSourceFile();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Get player uuid
        String playerUUID = Utils.GetMC().getSession().getProfile().getId().toString();

        // Load blacklist
        try {
            URL url = new URL("https://raw.githubusercontent.com/MrFast-js/SBF-Blacklist/main/blacklist.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String s;
            while ((s = reader.readLine()) != null) {
                if(s.equals(playerUUID)) {
                    throw new Error("You're blacklisted from using SBF! If you think this is a mistake contact MrFast#7146 on discord.");
                }
            }
        } catch (Exception ignored) {}

        // Save the config
        config.preload();
        SkyblockFeatures.config.markDirty();
        SkyblockFeatures.config.writeData();

        
        EssentialAPI.getCommandRegistry().registerCommand(new ViewModelCommand());

        // Features to load
        List<Object> features = Arrays.asList(
            this,
            new ChatEventListener(),
            GUIMANAGER,
            SkyblockInfo.getInstance(),
            new SpamHider(),
            new PricingData(),
            new ZealotSpawnLocations(),
            new ChestProfit(),
            new DungeonMap(),
            new DungeonsFeatures(),
            new ItemFeatures(),
            new CrystalHollowsMap(),
            new MayorJerry(),
            new MiningFeatures(),
            new MiscFeatures(),
            new DamageOverlays(),
            new Nametags(),
            new ConjuringCooldown(),
            new SpeedDisplay(),
            new EffectiveHealthDisplay(),
            new ManaDisplay(),
            new HealthDisplay(),
            new SecretDisplay(),
            new CryptDisplay(),
            new DefenceDisplay(),
            new HideStuff(),
            new ActionBarListener(),
            new BetterParties(),
            new CommisionsTracker(),
            new FairySoulWaypoints(),
            new JerryTimer(),
            new GiftTracker(),
            new CropCounter(),
            new HideGlass(),
            new FishingHelper(),
            new AuctionFeatures(),
            new CapeUtils(),
            new MinionOverlay(),
            new AutomatonTracker(),
            new GemstoneMiningOverlay(),
            new TreecapCooldown(),
            new LividFinder(),
            new IceTreasureTracker(),
            new EnderNodeTracker(),
            new HighlightCobblestone(),
            new MissingTalismans(),
            new PlayerDiguiser(),
            new AutoAuctionFlip(),
            new MetalDetectorSolver(),
            new ChronomotronSolver(),
            new UltrasequencerSolver(),
            new TradingOverlay(),
            new MiscOverlays(),
            new TrevorHelper(),
            new PathTracer(),
            new GhostTracker(),
            new CreeperSolver(),
            new PowderTracker(),
            new DwarvenMap(),
            new GrandmaWolfTimer(),
            new RelicFinderWaypoints(),
            new DynamicFullbright(),
            new GardenVisitorOverlay(),
            new BaitCounterOverlay(),
            new HighlightCropArea(),
            new MythologicalEvent(),
            new TeleportPadSolver(),
            new ShadowAssasinFeatures(),
            new ComposterOverlay(),
            new SlayerFeatures(),
            new CrimsonMap(),
            new RiftFeatures(),
            new BlazeSolver(),
            new ThreeWeirdosSolver(),
            new SkyblockInfo(),
            new Reparty(),
            new ProfileViewerUtils(),
            new ExoticAuctions(),
            new CollectionOverlay()
        );
        features.forEach((feature)->{
            MinecraftForge.EVENT_BUS.register(feature);
        });
        // Checks mod folder for version of Skyblock Features your using
        for(String modName:listFilesUsingJavaIO(Minecraft.getMinecraft().mcDataDir.getAbsolutePath()+"/mods")) {
            if(modName.contains("Skyblock-Features")) {
                // Filters out the mod name to just the version
                VERSION = modName.substring(0, modName.length()-4).replaceAll("Skyblock-Features-", "");
                break;
            }
        }
        if(config.apiKey!=API_KEY) config.apiKey = API_KEY;

        SkyblockFeatures.config.timeStartedUp++;
        System.out.println("You have started Skyblock Features up "+SkyblockFeatures.config.timeStartedUp+" times!");
    }
    // List files in a directory (Used only for the mods folder)
    public Set<String> listFilesUsingJavaIO(String dir) {
        return Stream.of(new File(dir).listFiles())
          .filter(file -> !file.isDirectory())
          .map(File::getName)
          .collect(Collectors.toSet());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        usingNEU = Loader.isModLoaded("notenoughupdates");
        ClientCommandHandler commandHandler = ClientCommandHandler.instance;

        List<ICommand> commands = new ArrayList<>();
        commands.add(new getNbtCommand());
        commands.add(new SkyCommand());
        commands.add(new configCommand());
        commands.add(new TerminalCommand());
        commands.add(new ShrugCommand());
        commands.add(new FlipsCommand());
        commands.add(new InventoryCommand());
        commands.add(new GetkeyCommand());
        commands.add(new DungeonsCommand());
        commands.add(new RepartyCommand());
        commands.add(new sidebarCommand());
        commands.add(new FakePlayerCommand());
        commands.add(new pvCommand());

        for (ICommand command : commands) {
            if (!commandHandler.getCommands().containsValue(command)) {
                commandHandler.registerCommand(command);
            }
        }
    }


    public static boolean auctionPricesLoaded = false;
    public static boolean smallItems = false;
    public boolean start = true;
    public boolean loadedBlacklist = false;
    public boolean checkedIfBlacklisted = false;
    ArrayList<String> blacklist = new ArrayList<>();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
       
        SkyblockFeatures.config.autoAuctionFlipMargin = SkyblockFeatures.config.autoAuctionFlipMargin.replaceAll("[^0-9]", "");
        if (event.phase != TickEvent.Phase.START) return;
        // Small items
        if(start) {
            smallItems = SkyblockFeatures.config.smallItems;
            start = false;
        } else {
            if(smallItems && !SkyblockFeatures.config.smallItems) {
                SkyblockFeatures.config.armX = 0;
                SkyblockFeatures.config.armY = 0;
                SkyblockFeatures.config.armZ = 0;
            }
            if(!smallItems && SkyblockFeatures.config.smallItems) {
                SkyblockFeatures.config.armX = 30;
                SkyblockFeatures.config.armY = -5;
                SkyblockFeatures.config.armZ = -60;
            }
            smallItems = SkyblockFeatures.config.smallItems;
        }
        if (mc.thePlayer != null && sendMessageQueue.size() > 0 && System.currentTimeMillis() - lastChatMessage > 200) {
            String msg = sendMessageQueue.pollFirst();
            if (msg != null) {
                mc.thePlayer.sendChatMessage(msg);
            }
        }
        
        if (ticks % 20 == 0) {
            if (mc.thePlayer != null) {
                Utils.checkForSkyblock();
                Utils.checkForDungeons();
            }
            MinecraftForge.EVENT_BUS.post(new SecondPassedEvent());
            ticks = 0;
        }

        ticks++;
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.SendEvent event) {
        if (event.packet instanceof C01PacketChatMessage) {
            lastChatMessage = System.currentTimeMillis();
        }
    }
    GuiScreen lastGui = null;
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !Minecraft.getMinecraft().isSingleplayer() && Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().getNetHandler() != null && EssentialAPI.getMinecraftUtil().isHypixel()) {
            try {
                Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();
                ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(1);
                Collection<Score> collection = scoreboard.getSortedScores(scoreObjective);
                for (Score score1 : collection)
                {
                    ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(score1.getPlayerName());
                    String scoreText = EnumChatFormatting.getTextWithoutFormattingCodes(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score1.getPlayerName()));

                    if (scoreText.contains("⏣")) {
                        locationString = keepLettersAndNumbersOnly(scoreText.replace("⏣", ""));
                    }
                }
            } catch (NullPointerException  e) {
                //TODO: handle exception
            }
        }
    }
    
    public static String locationString = "Unknown";
    private static final Pattern LETTERS_NUMBERS = Pattern.compile("[^a-z A-Z:0-9/'()]");

    private String keepLettersAndNumbersOnly(String text) {
        return LETTERS_NUMBERS.matcher(EnumChatFormatting.getTextWithoutFormattingCodes(text)).replaceAll("");
    }

    private KeyBinding toggleSprint;
    private static boolean toggled = true;

    public final static KeyBinding reloadAH = new KeyBinding("Reload Party Finder/Auction House", Keyboard.KEY_R, "Skyblock Features");
    public final static KeyBinding openBestFlipKeybind = new KeyBinding("Open Best Flip", Keyboard.KEY_J, "Skyblock Features");
    
    @EventHandler
    public void inist(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientRegistry.registerKeyBinding(reloadAH);
        ClientRegistry.registerKeyBinding(openBestFlipKeybind);

        toggleSprint = new KeyBinding("Toggle Sprint", Keyboard.KEY_I, "Skyblock Features");
        ClientRegistry.registerKeyBinding(toggleSprint);
    }

    @SubscribeEvent
    public void onTsick(TickEvent.ClientTickEvent e) {
        if (toggleSprint.isPressed()) {
            if (toggled) {
                Utils.SendMessage(EnumChatFormatting.RED + "Togglesprint disabled.");
            } else {
                Utils.SendMessage(EnumChatFormatting.GREEN + "Togglesprint enabled.");
            }
            toggled = !toggled;
        }
        if (toggled) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }
}
