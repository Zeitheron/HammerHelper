package org.zeith.hammerhelper.utils;

public record ResourceLocation(String namespace, String path)
{
	@Override
	public String toString()
	{
		return namespace + ":" + path;
	}
}