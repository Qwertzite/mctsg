package qwertzite.mctsg;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import qwertzite.mctsg.command.CommandGenerate;
import qwertzite.mctsg.command.CommandResourceRealoading;
import qwertzite.mctsg.nativeplugin.NativePlugin;

/**
 * the main class of Mine Craft Townscape Generator
 * 
 * @author qwertzite (RemiliaMarine)
 * 
 */
@Mod(
		modid = McTownscapeGenCore.MODID,
		name = McTownscapeGenCore.MOD_NAME,
		version = McTownscapeGenCore.VERSION
	)
public class McTownscapeGenCore {
	public static final String MODID = "mctsg";
	public static final String MOD_NAME = "Minecraft Townscape Generator";
	public static final String VERSION = "1.2.0-1.12.2-0015";
	public static final String RESOURCE_DOMAIN = MODID + ":";
	
	@Mod.Instance(MODID)
	public static McTownscapeGenCore INSTANCE;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModLog.onPreInit(event, true);
		MctsgPluginLoader.registerPlugin(new NativePlugin());
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		MctsgPluginLoader.setRegistingFinished();
		BuildingLoader.onInit();
		CityPlanLoader.init();
	}
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandResourceRealoading());
		event.registerServerCommand(new CommandGenerate());
	}
	
	
}
