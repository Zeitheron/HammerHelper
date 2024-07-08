package org.zeith.hammerhelper;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

public class HammerHelper
{
	public static Project currentProject()
	{
		for(Project project : ProjectManager.getInstance().getOpenProjects())
		{
			return project;
		}
		
		return null;
	}
	
	public static ConfigsHH cfg(Project project)
	{
		return new ConfigsHH(project);
	}
}