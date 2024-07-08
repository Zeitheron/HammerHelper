package org.zeith.hammerhelper.contributors.refs;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ToVirtualFilesRefContributor
		extends PsiReferenceContributor
{
	protected abstract void fetchReferences(@NotNull PsiElement element, @NotNull ProcessingContext context, @NotNull Consumer<VirtualFile> fileConsumer);
	
	@Override
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar)
	{
		registrar.registerReferenceProvider(PsiJavaPatterns.literalExpression(), new PsiReferenceProvider()
		{
			@Override
			public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context)
			{
				List<PsiReference> refs = new ArrayList<>();
				fetchReferences(element, context, vf ->
				{
					if(vf == null) return;
					refs.add(new VirtualFilePsiReference(element, vf));
				});
				if(refs.isEmpty()) return PsiReference.EMPTY_ARRAY;
				return refs.toArray(PsiReference[]::new);
			}
		});
	}
	
	public static class VirtualFilePsiReference
			extends PsiReferenceBase<PsiElement>
	{
		private final VirtualFile virtualFile;
		
		public VirtualFilePsiReference(@NotNull PsiElement element, VirtualFile virtualFile)
		{
			super(element);
			this.virtualFile = virtualFile;
		}
		
		@Nullable
		@Override
		public PsiElement resolve()
		{
			return PsiManager.getInstance(myElement.getProject()).findFile(virtualFile);
		}
	}
}