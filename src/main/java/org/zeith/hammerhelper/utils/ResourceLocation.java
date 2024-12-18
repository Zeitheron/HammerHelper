package org.zeith.hammerhelper.utils;

public record ResourceLocation(String namespace, String path)
{
	@Override
	public String toString()
	{
		return namespace + ":" + path;
	}
	
	public static ResourceLocation parse(String value)
	{
		String[] split = ResourceLocationChecks.split(value);
		return new ResourceLocation(split[0], split[1]);
	}
}