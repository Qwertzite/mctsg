package qwertzite.mctsg.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class StringParseHelper {
	
	public static int toInt(String str, ICommandSender sender, String name) {
		try {
			return Integer.parseInt(str);
		} catch(Exception e) {
			sender.sendMessage(new TextComponentString("Failed to parse " + str + " to int for " + name).setStyle((new Style()).setColor(TextFormatting.RED)));
			throw e;
		}
	}
	
}
