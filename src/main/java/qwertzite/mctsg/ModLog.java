package qwertzite.mctsg;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModLog {
    private static Logger LOGGER;
    private static boolean isDebug = true;
    
    /** must be loaded after Config */
    public static void onPreInit(FMLPreInitializationEvent event, boolean isDebug) {
    	ModLog.LOGGER = event.getModLog();
    	ModLog.isDebug = isDebug;
    }
    
    public static void log(Level level, Throwable e, String format, Object... data)
    {
    	LOGGER.log(level, String.format(format, data), e);
    }
    
    public static void log(Level level, Throwable e, String message)
    {
        LOGGER.log(level, message, e);
    }
    
    public static void info(String format, Object... data)
    {
    	LOGGER.log(Level.INFO, format, data);
    }
    
    public static void warn(String format, Object... data)
    {
    	LOGGER.log(Level.WARN, format, data);
    }
    
    public static void warn(String format, Throwable t) {
    	LOGGER.error(format, t);
    }
    
    public static void debug(Object format, Object... data)
    {
        if (isDebug)
        {
        	LOGGER.log(Level.INFO, "(DEBUG) " + format, data);
        }
    }
    
}
