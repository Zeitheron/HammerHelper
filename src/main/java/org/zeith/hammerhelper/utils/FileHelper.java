package org.zeith.hammerhelper.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerhelper.configs.namespaces.NamespaceConfigsHH;

import java.nio.file.Path;
import java.util.*;

public class FileHelper
{
	@Nullable
	public static VirtualFile findAssetRoot(PsiFile file, ProcessingContext context, KeyedPrefixPath prefixPath)
	{
		var ASSET_ROOT = prefixPath.key();
		var prefix = prefixPath.prefix();
		
		var vfOpt = context.get(ASSET_ROOT);
		if(vfOpt != null)
			return vfOpt.orElse(null);
		
		Stack<String> prefixes = new Stack<>();
		prefixes.addAll(List.of(prefix.split("/")));
		
		VirtualFile res = getRecursive(getResourcesDirectory(file), "assets");
		if(res == null)
		{
			context.put(ASSET_ROOT, Optional.empty());
			return null;
		}
		
		boolean stacking = false;
		boolean stackingComplete = false;
		
		VirtualFile vf = file.getVirtualFile();
		while(vf != null)
		{
			vf = vf.getParent();
			
			if(vf != null && !prefixes.isEmpty() && prefixes.peek().equalsIgnoreCase(vf.getName()))
			{
				stacking = true;
				prefixes.pop();
				if(prefixes.isEmpty())
				{
					stacking = false;
					stackingComplete = true;
				}
				continue;
			}
			
			// Stacking, but the prefix does not match...
			if(stacking && !prefixes.isEmpty() && !prefixes.peek().equalsIgnoreCase(vf.getName()))
				break;
			
			if(vf != null && Objects.equals(res, vf))
			{
				if(!stackingComplete)
					break;
				context.put(ASSET_ROOT, Optional.of(res));
				return res;
			}
		}
		
		context.put(ASSET_ROOT, Optional.empty());
		return null;
	}
	
	public static VirtualFile getResourcesDirectory(PsiFile from)
	{
		VirtualFile baseDir = getModuleDirectory(from);
		return baseDir != null ? baseDir.findChild("resources") : null;
	}
	
	public static VirtualFile getModuleDirectory(PsiFile from)
	{
		Project project = from.getProject();
		var vf = from.getVirtualFile();
		
		if(vf == null)
		{
			from = from.getOriginalFile();
			vf = from.getVirtualFile();
		}
		
		if(vf == null) return null;
		ProjectFileIndex indices = ProjectFileIndex.getInstance(project);
		return indices.getContentRootForFile(vf);
	}
	
	public static List<Namespace> getAllAssetNamespaces(PsiFile file)
	{
		var cfg = new NamespaceConfigsHH(file.getProject());
		
		List<Namespace> namespaces = new ArrayList<>();
		
		if(cfg != null && cfg.getNamespaces() != null)
			for(var namespace : cfg.getNamespaces().entrySet())
			{
				var vf = VirtualFileManager.getInstance().findFileByNioPath(Path.of(namespace.getKey()));
				if(vf == null) continue;
				namespaces.add(new Namespace(namespace.getValue(), vf));
			}
		
		var assets = getRecursive(getResourcesDirectory(file), "assets");
		if(assets != null) for(VirtualFile child : assets.getChildren()) namespaces.add(new Namespace(child.getName(), child));
		
		return namespaces;
	}
	
	public static VirtualFile getRecursive(VirtualFile directory, String... path)
	{
		if(directory == null) return null;
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