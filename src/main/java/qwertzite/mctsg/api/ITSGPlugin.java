package qwertzite.mctsg.api;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface ITSGPlugin {
	
	public void registerPlans(BiConsumer<String, ICityPlanEntry<?>> registry);
	public void registerFormat(Consumer<IFormatEntry<? extends IBuildingEntry>> registry);
}
