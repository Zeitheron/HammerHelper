package org.zeith.hammerhelper.contributors.refs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerhelper.utils.*;

import java.util.Set;
import java.util.function.Consumer;

import static org.zeith.hammerhelper.utils.FileHelper.getRecursive;

public abstract class BaseRegisteredObjectRefContributor
		extends ToVirtualFilesRefContributor
{
	protected final Set<String> registrationTypes;
	
	public BaseRegisteredObjectRefContributor(String... registrationTypes)
	{
		this.registrationTypes = Set.of(registrationTypes);
	}
	
	protected abstract void findReference(@NotNull PsiElement element, @NotNull ProcessingContext context, @NotNull PsiField registerField, @NotNull String registryPath, @NotNull Consumer<@Nullable VirtualFile> fileConsumer);
	
	@Override
	protected void fetchReferences(@NotNull PsiElement element, @NotNull ProcessingContext context, @NotNull Consumer<@Nullable VirtualFile> fileConsumer)
	{
		var field = getAnnotationContext(element);
		if(field == null) return;
		Project project = element.getProject();
		
		var registryPath = SimplyRegisterMechanism.getRegistryPath(field);
		if(registryPath == null) return;
		
		findReference(element, context, field, registryPath, fileConsumer);
	}
	
	protected PsiField getAnnotationContext(PsiElement position)
	{
		if(position.getParent().getParent() instanceof PsiAnnotationParameterList apr
		   && apr.getParent() instanceof PsiAnnotation annotation
		   && PsiHelper.isOneOf(annotation, SimplyRegisterMechanism.REGISTRY_NAME)
		   && annotation.getOwner() instanceof PsiModifierList modList
		   && modList.getParent() instanceof PsiField field)
		{
			var cls = PsiTypesUtil.getPsiClass(field.getType());
			return PsiHelper.instanceOf(cls, registrationTypes) ? field : null;
		}
		
		return null;
	}
	
	protected void forEachAssetsNamespace(PsiElement element, Consumer<VirtualFile> namespaceHandler)
	{
		VirtualFile assets = getRecursive(FileHelper.getResourcesDirectory(element.getProject()), "assets");
		if(assets == null) return;
		for(var namespace : assets.getChildren()) namespaceHandler.accept(namespace);
	}
}