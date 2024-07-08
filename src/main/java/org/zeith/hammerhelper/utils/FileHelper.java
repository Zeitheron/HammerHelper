package org.zeith.hammerhelper.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class FileHelper
{
	public static VirtualFile getResourcesDirectory(Project project)
	{
		VirtualFile baseDir = project.getBaseDir();
		VirtualFile resourcesDir = baseDir.findFileByRelativePath("src/main/resources");
		if(resourcesDir == null)
		{
			resourcesDir = baseDir.findChild("resources");
		}
		return resourcesDir;
	}
}