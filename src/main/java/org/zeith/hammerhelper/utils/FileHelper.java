package org.zeith.hammerhelper.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.*;

public class FileHelper
{
	public static VirtualFile getResourcesDirectory(Project project)
	{
		VirtualFile baseDir = project.getBaseDir();
		VirtualFile resourcesDir = baseDir.findFileByRelativePath("src/main/resources");
		if(resourcesDir == null) resourcesDir = baseDir.findChild("resources");
		return resourcesDir;
	}
	
	public static VirtualFile getRecursive(VirtualFile directory, String... path)
	{
		for(var p : path)
		{
			directory = directory.findFileByRelativePath(p);
			if(directory == null) return null;
		}
		return directory;
	}
	
	public static List<String> getRecursiveFilesNamesWithFullPathFromDirectory(VirtualFile directory)
	{
		return getRecursiveFilesNamesWithFullPathFromDirectory(directory, "");
	}
	
	public static List<String> getRecursiveFilesNamesWithFullPathFromDirectory(VirtualFile directory, String path)
	{
		if(directory == null) return new ArrayList<>(0);
		List<String> files = new ArrayList<>();
		for(VirtualFile file : directory.getChildren())
		{
			if(file.isDirectory())
			{
				files.addAll(getRecursiveFilesNamesWithFullPathFromDirectory(file, path + file.getName() + "/"));
			} else
			{
				files.add(path + file.getName());
			}
		}
		return files;
	}
	
	public static Map<String, VirtualFile> getRecursiveFilesNameMapWithFullPathFromDirectory(VirtualFile directory)
	{
		return getRecursiveFilesNameMapWithFullPathFromDirectory(directory, "");
	}
	
	public static Map<String, VirtualFile> getRecursiveFilesNameMapWithFullPathFromDirectory(VirtualFile directory, String path)
	{
		if(directory == null) return new HashMap<>(0);
		Map<String, VirtualFile> files = new HashMap<>();
		for(VirtualFile file : directory.getChildren())
		{
			if(file.isDirectory())
			{
				files.putAll(getRecursiveFilesNameMapWithFullPathFromDirectory(file, path + file.getName() + "/"));
			} else
			{
				files.put(path + file.getName(), file);
			}
		}
		return files;
	}
}