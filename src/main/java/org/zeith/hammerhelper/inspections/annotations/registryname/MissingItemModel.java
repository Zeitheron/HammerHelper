package org.zeith.hammerhelper.inspections.annotations.registryname;

import org.zeith.hammerhelper.inspections.annotations.registryname.base.CreatingRegistrar;

import java.util.Map;

public class MissingItemModel
		extends CreatingRegistrar
{
	public MissingItemModel()
	{
		super(Map.of(
				"net.minecraft.world.item.Item", withFixedPath("models/item", (namespace, path, f) -> """
						{
						  "parent": "item/generated",
						  "textures": {
						    "layer0": "%s:item/%s"
						  }
						}""".formatted(namespace, path)),
				
				"net.minecraft.item.Item", withFixedPath("models/item", (namespace, path, f) -> """
						{
						  "parent": "item/generated",
						  "textures": {
						    "layer0": "%s:items/%s"
						  }
						}""".formatted(namespace, path))
		));
	}
	
	@Override
	protected String getProblemMessage(String fieldType, TemplateGenerator generator)
	{
		return "This item is missing a model.";
	}
	
	@Override
	protected String getQuickFixLabel(String namespace, String registryName, String fieldType, TemplateGenerator generator)
	{
		return "Create item model for '%s' in '%s'".formatted(registryName, namespace);
	}
}