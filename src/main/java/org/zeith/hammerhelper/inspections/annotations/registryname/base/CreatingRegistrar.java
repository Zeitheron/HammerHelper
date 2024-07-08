package org.zeith.hammerhelper.inspections.annotations.registryname.base;

import com.intellij.codeInspection.*;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.quickfixes.CreateJsonFileQuickFix;
import org.zeith.hammerhelper.utils.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.zeith.hammerhelper.utils.FileHelper.getRecursive;

public abstract class CreatingRegistrar
		extends LocalInspectionTool
{
	protected final Map<String, TemplateGenerator> templateGenerators;
	
	public CreatingRegistrar(Map<String, TemplateGenerator> templateGenerators)
	{
		this.templateGenerators = templateGenerators;
	}
	
	protected abstract String getProblemMessage(String fieldType, TemplateGenerator generator);
	
	protected abstract String getQuickFixLabel(String namespace, String registryName, String fieldType, TemplateGenerator generator);
	
	@Override
	public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session)
	{
		return new JavaElementVisitor()
		{
			@Override
			public void visitField(@NotNull PsiField aField)
			{
				var cls = PsiTypesUtil.getPsiClass(aField.getType());
				if(cls == null) return;
				var qn = PsiHelper.findInstance(cls, templateGenerators.keySet());
				if(qn == null) return;
				var tg = templateGenerators.get(qn);
				if(tg == null) return;
				
				var registryPath = SimplyRegisterMechanism.getRegistryPath(aField);
				if(registryPath == null) return;
				
				var rn = SimplyRegisterMechanism.findRegistryName(aField);
				if(rn == null) return;
				var rnv = rn.findAttributeValue("value");
				if(rnv == null) return;
				
				var namespaces = getAssetNamespaces(aField);
				var fn = (registryPath + ".json").split("/");
				
				var ap = tg.getAssetsPath();
				for(var namespace : namespaces)
				{
					var sub = getRecursive(namespace.file(), ap.split("/"));
					if(sub == null) continue;
					sub = getRecursive(sub, fn);
					if(sub != null) return;
				}
				
				LocalQuickFix[] fixes = new LocalQuickFix[namespaces.size()];
				
				for(int i = 0, namespacesLength = namespaces.size(); i < namespacesLength; i++)
				{
					var ns = namespaces.get(i);
					VirtualFile namespaceVF = ns.file();
					var namespace = ns.name();
					fixes[i] = new CreateJsonFileQuickFix(getQuickFixLabel(namespace, registryPath, qn, tg), tg.createTemplate(namespace, registryPath, aField), 0, (project, requestor) ->
					{
						String directoryPath = namespaceVF.toNioPath().toAbsolutePath().toString().replace(File.separatorChar, '/') + "/" + ap + "/" + registryPath + ".json";
						int lastIdx = directoryPath.lastIndexOf('/');
						FileHelper.getResourcesDirectory(project);
						VirtualFile directory = VfsUtil.createDirectories(directoryPath.substring(0, lastIdx));
						return directory.createChildData(requestor, directoryPath.substring(lastIdx + 1));
					});
				}
				
				holder.registerProblem(rnv, getProblemMessage(qn, tg), ProblemHighlightType.WEAK_WARNING, fixes);
			}
		};
	}
	
	protected List<Namespace> getAssetNamespaces(PsiElement element)
	{
		return FileHelper.getAllAssetNamespaces(element.getProject());
	}
	
	public interface TemplateGenerator
			extends TemplateFactory
	{
		String getAssetsPath();
	}
	
	public static TemplateGenerator withFixedPath(String path, TemplateFactory templateFactory)
	{
		return new TemplateGenerator()
		{
			@Override
			public String getAssetsPath()
			{
				return path;
			}
			
			@Override
			public String createTemplate(String namespace, String registryName, PsiField field)
			{
				return templateFactory.createTemplate(namespace, registryName, field);
			}
		};
	}
	
	public interface TemplateFactory
	{
		String createTemplate(String namespace, String registryName, PsiField field);
	}
}