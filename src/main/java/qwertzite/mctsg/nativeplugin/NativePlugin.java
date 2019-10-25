package qwertzite.mctsg.nativeplugin;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import qwertzite.mctsg.api.IBuildingEntry;
import qwertzite.mctsg.api.ICityPlanEntry;
import qwertzite.mctsg.api.IFormatEntry;
import qwertzite.mctsg.api.ITSGPlugin;

public class NativePlugin implements ITSGPlugin {
	public static final String PLUGIN_ID = "native";

	@Override
	public void registerPlans(BiConsumer<String, ICityPlanEntry<?>> registry) {
		registry.accept("mc_village", new ICityPlanEntry<McVillagePlan>() {
			@Override public McVillagePlan get() { return new McVillagePlan(); }

			@Override
			public void cratePlanSetting(JsonObject json) {
				new McVillagePlan().loadJsonSettings(json, new DummyCommandSender());
			}
		});
	}

	@Override
	public void registerFormat(Consumer<IFormatEntry<? extends IBuildingEntry>> registry) {
		// currently there is no native building file format.
	}
	
	public static class DummyCommandSender implements ICommandSender {

		@Override
		public String getName() { return null; }

		@Override
		public boolean canUseCommand(int permLevel, String commandName) { return false; }

		@Override
		public World getEntityWorld() { return null; }

		@Override
		public MinecraftServer getServer() { return null; }
	}
}
