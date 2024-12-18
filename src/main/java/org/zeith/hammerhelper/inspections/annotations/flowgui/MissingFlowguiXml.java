package org.zeith.hammerhelper.inspections.annotations.flowgui;

import com.intellij.codeInspection.*;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.quickfixes.CreateJsonFileQuickFix;
import org.zeith.hammerhelper.utils.*;
import org.zeith.hammerhelper.utils.flowgui.FlowguiModel;

import java.io.File;
import java.util.List;

import static org.zeith.hammerhelper.utils.FileHelper.getRecursive;

public class MissingFlowguiXml
		extends LocalInspectionTool
{
	@Override
	public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly)
	{
		return new JavaElementVisitor()
		{
			@Override
			public void visitClass(@NotNull PsiClass aClass)
			{
				var flowgui = PsiHelper.findFirstAnnotation(aClass, FlowguiModel.XML_FLOWGUI);
				if(flowgui == null) return;
				
				var value = PsiHelper.getAnnotationAttributeValue(flowgui, "value", "");
				if(value == null || value.isBlank()) return;
				
				var namespaces = getAssetNamespaces(aClass);
				var fn = ("flowgui/" + value + ".xml").split("/");
				
				for(var namespace : namespaces)
				{
					var sub = getRecursive(namespace.file(), fn);
					if(sub != null) return;
				}
				
				LocalQuickFix[] fixes = new LocalQuickFix[namespaces.size()];
				
				for(int i = 0, namespacesLength = namespaces.size(); i < namespacesLength; i++)
				{
					var ns = namespaces.get(i);
					VirtualFile namespaceVF = ns.file();
					var namespace = ns.name();
					fixes[i] = new CreateJsonFileQuickFix(getQuickFixLabel(namespace, value), createTemplate(), 0, (project, requestor) ->
					{
						String directoryPath =
								namespaceVF.toNioPath()
										.toAbsolutePath()
										.toString()
										.replace(File.separatorChar, '/')
								+ "/flowgui/" + value + ".xml";
						
						int lastIdx = directoryPath.lastIndexOf('/');
						VirtualFile directory = VfsUtil.createDirectories(directoryPath.substring(0, lastIdx));
						return directory.createChildData(requestor, directoryPath.substring(lastIdx + 1));
					}
					);
				}
				
				var obj = flowgui.findAttributeValue("value");
				if(obj == null) obj = flowgui;
				holder.registerProblem(obj, getProblemMessage(), ProblemHighlightType.ERROR, fixes);
			}
		};
	}
	
	protected String getProblemMessage()
	{
		return "Missing flowgui XML file.";
	}
	
	protected String createTemplate()
	{
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<root width="176" height="166" centered="int">
					<img id="background" src="hammerlib:textures/gui/test_machine.png"
				         u-coord="0" v-coord="0"
				         image-width="176" image-height="166"
				         file-width="256" file-height="256"
				    >
				        <import from="c:player_hotbar" x="7" y="25" align-y="bottom"/>
				        <import from="c:player_inventory" x="7" y="83" align-y="bottom"/>
				    </img>
				</root>""";
	}
	
	protected String getQuickFixLabel(String namespace, String id)
	{
		return "Create flowgui XML file for '%s' in '%s'".formatted(id, namespace);
	}
	
	protected List<Namespace> getAssetNamespaces(PsiElement element)
	{
		return FileHelper.getAllAssetNamespaces(element.getContainingFile());
	}
}