package eab.locatel;

import eab.locatel.command.CommandLocateL;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
    modid = LocateLMod.MODID,
    name = LocateLMod.NAME,
    version = LocateLMod.VERSION,
    acceptedMinecraftVersions = "[1.12.2]"
)
public class LocateLMod {
    public static final String MODID = "locatel";
    public static final String NAME = "LocateL";
    public static final String VERSION = "1.0.0";
    
    @Mod.Instance(MODID)
    public static LocateLMod instance;
    
    public static Logger logger;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("LocateL Mod is loading!");
    }
    
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandLocateL());
    }
}