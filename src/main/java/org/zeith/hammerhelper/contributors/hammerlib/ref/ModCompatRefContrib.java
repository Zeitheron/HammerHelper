package org.zeith.hammerhelper.contributors.hammerlib.ref;

import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModCompatRefContrib
		extends PsiReferenceContributor
{
	protected void fetchReferences(@NotNull PsiElement element, @NotNull ProcessingContext context, @NotNull Consumer<PsiElement> fileConsumer)
	{
		if(!(element instanceof PsiLiteralExpression literal)
		   || !(literal.getParent() instanceof PsiNameValuePair pair)
		   || !"modid".equals(pair.getAttributeName())
		) return;
		
		var an = getAnnotationContext(element);
		if(an == null) return;
		
		fileConsumer.accept(ModCompatMechanism.gatherModClasses(element, context)
				.get(PsiHelper.getAnnotationAttributeValue(an, "modid", ""))
		);
	}
	
	protected PsiAnnotation getAnnotationContext(PsiElement position)
	{
		if(position.getParent().getParent() instanceof PsiAnnotationParameterList apr
		   && apr.getParent() instanceof PsiAnnotation annotation
		   && PsiHelper.isOneOf(annotation, ModCompatMechanism.LOAD_COMPAT))
			return annotation;
		return null;
	}
	
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
									refs.add(new PsiReferenceBase.Immediate<>(element, vf));
								}
						);
						if(refs.isEmpty()) return PsiReference.EMPTY_ARRAY;
						return refs.toArray(PsiReference[]::new);
					}
				}
		);
	}
}
