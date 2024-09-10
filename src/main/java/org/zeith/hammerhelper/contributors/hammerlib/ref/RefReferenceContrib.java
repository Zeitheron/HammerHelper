package org.zeith.hammerhelper.contributors.hammerlib.ref;

import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.PsiHelper;
import org.zeith.hammerhelper.utils.SimplyRegisterMechanism;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RefReferenceContrib
		extends PsiReferenceContributor
{
	protected void fetchReferences(@NotNull PsiElement element, @NotNull ProcessingContext context, @NotNull Consumer<PsiElement> fileConsumer)
	{
		if(!(element instanceof PsiLiteralExpression literal)
		   || !(literal.getParent() instanceof PsiNameValuePair pair)
		   || !"field".equals(pair.getAttributeName())
		) return;
		var an = getAnnotationContext(element);
		if(an == null) return;
		
		PsiAnnotationMemberValue value = an.findAttributeValue("value");
		String field = PsiHelper.getAnnotationAttributeValue(an, "field", "");
		
		value.acceptChildren(new PsiElementVisitor()
		{
			@Override
			public void visitElement(@NotNull PsiElement element)
			{
				if(element instanceof PsiTypeElement pt)
				{
					var pc = PsiTypesUtil.getPsiClass(pt.getType());
					if(pc != null)
					{
						fileConsumer.accept(pc.findFieldByName(field, false));
					}
				}
			}
		});
	}
	
	protected PsiAnnotation getAnnotationContext(PsiElement position)
	{
		if(position.getParent().getParent() instanceof PsiAnnotationParameterList apr
		   && apr.getParent() instanceof PsiAnnotation annotation
		   && PsiHelper.isOneOf(annotation, SimplyRegisterMechanism.REF))
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
				});
				if(refs.isEmpty()) return PsiReference.EMPTY_ARRAY;
				return refs.toArray(PsiReference[]::new);
			}
		});
	}
}