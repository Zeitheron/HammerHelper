package org.zeith.hammerhelper.utils.flowgui;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.zeith.hammerhelper.utils.*;

import java.util.List;

public class FlowguiLocator
{
	public static List<VirtualFile> findXmlFile(ResourceLocation id, PsiElement anything)
	{
		var libs = FileHelper.listFiles(anything.getProject(), "assets/" + id.namespace() + "/flowgui/" + id.path() + ".xml");
		for(Namespace namespace : FileHelper.getAllAssetNamespaces(anything.getContainingFile().getOriginalFile()))
		{
			VirtualFile vf = FileHelper.getRecursive(namespace.file(), ("flowgui/" + id.path() + ".xml").split("/"));
			if(vf != null && vf.exists())
				libs.add(vf);
		}
		return libs;
	}
}