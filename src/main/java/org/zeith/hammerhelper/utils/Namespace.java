package org.zeith.hammerhelper.utils;

import com.intellij.openapi.vfs.VirtualFile;

public record Namespace(String name, VirtualFile file)
{
}