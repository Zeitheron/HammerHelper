package org.zeith.hammerhelper.inspections.annotations.registryname;

import org.zeith.hammerhelper.inspections.annotations.registryname.base.CreatingRegistrar;

import java.util.Map;

public class MissingBlockstateModel
		extends CreatingRegistrar
{
	public MissingBlockstateModel()
	{
		super(Map.of(
				"net.minecraft.world.level.block.Block", withFixedPath("blockstates", (namespace, path, f) -> """
						{
							"variants": {
								"": { "model": "%s:block/%s" }
							}
						}""".formatted(namespace, path)),
				
				"net.minecraft.block.Block", withFixedPath("blockstates", (namespace, path, f) -> """
						{
							"variants": {
								"normal": { "model": "%s:%s" }
							}
						}""".formatted(namespace, path))
		));
	}
	
	@Override
	protected String getProblemMessage(String fieldType, TemplateGenerator generator)
	{
		return "This block is missing a state map.";
	}
	
	@Override
	protected String getQuickFixLabel(String namespace, String registryName, String fieldType, TemplateGenerator generator)
	{
		return "Create block state map for '%s' in '%s'".formatted(registryName, namespace);
	}
}