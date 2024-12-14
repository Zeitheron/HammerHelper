package org.zeith.hammerhelper.configs.general;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.util.Key;
import com.intellij.util.ProcessingContext;
import lombok.With;
import org.zeith.hammerhelper.configs.HHConfigHelper;

@With
public record GeneralConfigsHH(
		boolean enableMCJsonReferences
)
{
	private static final Key<GeneralConfigsHH> GENERAL_CONFIGS_HH_KEY = Key.create("hammerhelper general configs");
	
	public static final String ENABLE_MC_JSON_REFS_KEY = HHConfigHelper.getConfigGlobalName("general.mcjsonrefs");
	
	public GeneralConfigsHH(PropertiesComponent instance)
	{
		this(
				instance.getBoolean(ENABLE_MC_JSON_REFS_KEY, true)
		);
	}
	
	public static GeneralConfigsHH pull()
	{
		return new GeneralConfigsHH(PropertiesComponent.getInstance());
	}
	
	public void push()
	{
		PropertiesComponent props = PropertiesComponent.getInstance();
		props.setValue(ENABLE_MC_JSON_REFS_KEY, enableMCJsonReferences, true);
	}
	
	public static GeneralConfigsHH get(ProcessingContext ctx)
	{
		GeneralConfigsHH c = ctx.get(GENERAL_CONFIGS_HH_KEY);
		if(c == null) ctx.put(GENERAL_CONFIGS_HH_KEY, c = pull());
		return c;
	}
}