package com.kaduvill.capnsbeeaddon;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = CapnsBeeAddon.MODID,
        name = CapnsBeeAddon.NAME,
        version = CapnsBeeAddon.VERSION,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = CapnsBeeAddon.DEPENDENCIES
)
public final class CapnsBeeAddon {

    public static final String MODID = "capnsbeeaddon";
    public static final String NAME = "Capn's Bee Addon";
    public static final String VERSION = "GRADLETOKEN_VERSION";

    public static final String DEPENDENCIES =
            "required-after:mixinbooter@[10.7,);" +
            "required-after:forestry@[5.8.2.427];" +
            "required-after:bdlib@[1.14.4.1];" +
            "required-after:gendustry@[1.6.5.8];" +
            "required-after:careerbees";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("{} {} initialized", NAME, VERSION);
    }
}
