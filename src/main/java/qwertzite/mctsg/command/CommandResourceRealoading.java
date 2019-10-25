package qwertzite.mctsg.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import qwertzite.mctsg.BuildingLoader;
import qwertzite.mctsg.util.ReloadResult;

public class CommandResourceRealoading extends CommandBase {

	@Override
	public String getName() {
		return "tsgReload";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/tsgReload";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		ReloadResult result = BuildingLoader.reloadResources();
		sender.sendMessage(new TextComponentString("Checked " + result.targetNum + " files."));
		sender.sendMessage(new TextComponentString("Loaded " + result.successed + " buildings."));
		if (result.failed > 0) {
			Style style = new Style().setColor(TextFormatting.RED);
			sender.sendMessage(new TextComponentString("Failed to load " + result.failed + " files.").setStyle(style));
			for (String msg : result.message) {
				sender.sendMessage(new TextComponentString(msg).setStyle(style));
			}
		}
	}

}
