package org.zeith.modid;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.zeith.hammerlib.api.items.CreativeTab;
import org.zeith.hammerlib.compat.base.CompatContext;
import org.zeith.hammerlib.compat.base.CompatList;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;
import org.zeith.hammerlib.core.init.ItemsHL;
import org.zeith.hammerlib.proxy.HLConstants;
import org.zeith.hammerlib.util.mcf.Resources;
import org.zeith.modid.compat.BaseCompatMI;

@Mod(ModId.MOD_ID)
public class ModId
{
	public static final String MOD_ID = "modid";

	public final CompatList<BaseCompatMI> COMPATS;

	@CreativeTab.RegisterTab
	public static final CreativeTab MOD_TAB = new CreativeTab(id("root"),
			builder -> builder
					.icon(() -> ItemsHL.COPPER_GEAR.getDefaultInstance())
					.withTabsBefore(HLConstants.HL_TAB.id())
	);
	
	public ModId(IEventBus bus)
	{
		LanguageAdapter.registerMod(MOD_ID);
		COMPATS = CompatList.gather(BaseCompatMI.class, CompatContext.builder(bus)
				.build()
		);
	}
	
	public static ResourceLocation id(String path)
	{
		return Resources.location(MOD_ID, path);
	}
}