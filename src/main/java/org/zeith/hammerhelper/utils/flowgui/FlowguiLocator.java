package org.zeith.hammerhelper.utils.flowgui;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.zeith.hammerhelper.utils.ResourceLocation;
import org.zeith.hammerhelper.utils.resources.ResourceLocator;

public class FlowguiLocator
{
	public static VirtualFile findXmlFile(ResourceLocation id, PsiElement anything, ProcessingContext context)
	{
		return ResourceLocator.findResourceInProject(anything, "assets/" + id.namespace() + "/flowgui/" + id.path() + ".xml", context);
	}
}