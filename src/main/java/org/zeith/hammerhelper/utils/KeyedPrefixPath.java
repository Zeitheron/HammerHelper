package org.zeith.hammerhelper.utils;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.*;

public record KeyedPrefixPath(Key<Optional<VirtualFile>> key, String prefix)
{
	private static final Map<String, KeyedPrefixPath> REGISTRY = new HashMap<>();
	
	public static KeyedPrefixPath of(String prefix)
	{
		return REGISTRY.computeIfAbsent(prefix, p -> new KeyedPrefixPath(Key.create("hammerhelper.asset.root.vf." + p), p));
	}
}