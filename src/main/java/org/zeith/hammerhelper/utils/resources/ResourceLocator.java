package org.zeith.hammerhelper.utils.resources;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.zeith.hammerhelper.utils.FileHelper;

import java.util.ArrayList;
import java.util.List;

public class ResourceLocator
{
	private static final Key<List<VirtualFile>> ASSET_DIRECTORIES = Key.create("asset directories");
	
	// Path example: hammerlib/logo.png
	public static VirtualFile findAssetResourceInProject(PsiElement anything, String path, ProcessingContext context)
	{
		return findResourceInProject(anything, "assets/" + path, context);
	}
	
	// Path example: assets/hammerlib/logo.png
	public static VirtualFile findResourceInProject(PsiElement anything, String path, ProcessingContext context)
	{
		List<VirtualFile> dirs = findAllRoots(anything, context);
		String[] fp = path.split("/");
		for(VirtualFile dir : dirs)
		{
			VirtualFile rec = FileHelper.getRecursive(dir, fp);
			if(rec != null) return rec;
		}
		return null;
	}
	
	public static List<VirtualFile> findAllRoots(PsiElement anything, ProcessingContext ctx)
	{
		var m = ctx.get(ASSET_DIRECTORIES);
		if(m != null) return m;
		ctx.put(ASSET_DIRECTORIES, m = findAllRoots(anything));
		return m;
	}
	
	public static List<VirtualFile> findAllRoots(PsiElement anything)
	{
		var project = anything.getProject();
		
		List<VirtualFile> roots = new ArrayList<>();
		
		for(var module : ModuleManager.getInstance(project).getModules())
		{
			ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
			for(ContentEntry contentEntry : moduleRootManager.getContentEntries())
				for(VirtualFile resourceRoot : contentEntry.getSourceFolderFiles())
					if(resourceRoot != null && resourceRoot.isDirectory())
						roots.add(resourceRoot);
		}
		
		OrderEnumerator.orderEntries(project).forEachLibrary(library ->
		{
			VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
			for(VirtualFile file : files)
				if(file != null && file.isDirectory())
					roots.add(file);
			return true;
		});
		
		return roots;
	}
}