package org.zeith.hammerhelper.inspections.annotations.registryname;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.configs.mcver.MinecraftVersion;
import org.zeith.hammerhelper.configs.mcver.MinecraftVersionConfigs;
import org.zeith.hammerhelper.inspections.annotations.registryname.base.CreatingRegistrar;

import java.util.Map;

public class MissingItemProperty
		extends CreatingRegistrar
{
	public MissingItemProperty()
	{
		super(Map.of(
				"net.minecraft.world.item.Item", withFixedPath("items", (namespace, path, f) -> """
						{
						  "model": {
						    "type": "minecraft:model",
						    "model": "%s:item/%s"
						  }
						}""".formatted(namespace, path)
				)
		));
	}
	
	@Override
	protected boolean isEnabled(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session)
	{
		MinecraftVersion mcv = MinecraftVersionConfigs.getMinecraftVersion(holder.getProject());
		return mcv != null && mcv.pack_version != null && mcv.pack_version.resource() >= 34;
	}
	
	@Override
	protected String getProblemMessage(String fieldType, TemplateGenerator generator)
	{
		return "This item is missing a properties file.";
	}
	
	@Override
	protected String getQuickFixLabel(String namespace, String registryName, String fieldType, TemplateGenerator generator)
	{
		return "Create item properties for '%s' in '%s'".formatted(registryName, namespace);
	}
}