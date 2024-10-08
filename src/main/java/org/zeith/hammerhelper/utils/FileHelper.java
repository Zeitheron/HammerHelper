package org.zeith.hammerhelper.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.zeith.hammerhelper.HammerHelper;

import java.nio.file.Path;
import java.util.*;

public class FileHelper
{
	public static String getSrcMainResourcesChild(Project project)
	{
		return "src/main/resources";
	}
	
	public static VirtualFile getResourcesDirectory(Project project)
	{
		VirtualFile baseDir = project.getBaseDir();
		VirtualFile resourcesDir = baseDir.findFileByRelativePath(getSrcMainResourcesChild(project));
		if(resourcesDir == null) resourcesDir = baseDir.findChild("resources");
		return resourcesDir;
	}
	
	public static List<Namespace> getAllAssetNamespaces(Project project)
	{
		var cfg = HammerHelper.cfg(project);
		
		List<Namespace> namespaces = new ArrayList<>();
		
		if(cfg != null && cfg.getNamespaces() != null)
			for(var namespace : cfg.getNamespaces().entrySet())
			{
				var vf = VirtualFileManager.getInstance().findFileByNioPath(Path.of(namespace.getKey()));
				if(vf == null) continue;
				namespaces.add(new Namespace(namespace.getValue(), vf));
			}
		
		var assets = getRecursive(getResourcesDirectory(project), "assets");
		if(assets != null) for(VirtualFile child : assets.getChildren()) namespaces.add(new Namespace(child.getName(), child));
		
		return namespaces;
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