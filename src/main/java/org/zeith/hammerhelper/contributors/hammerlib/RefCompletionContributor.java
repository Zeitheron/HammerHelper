package org.zeith.hammerhelper.contributors.hammerlib;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.PsiHelper;
import org.zeith.hammerhelper.utils.SimplyRegisterMechanism;

public class RefCompletionContributor
		extends CompletionContributor
{
	public RefCompletionContributor()
	{
		extend(CompletionType.BASIC,
				PlatformPatterns.psiElement().inside(PsiLiteralExpression.class),
				new CompletionProvider<>()
				{
					@Override
					protected void addCompletions(@NotNull CompletionParameters parameters,
												  @NotNull ProcessingContext context,
												  @NotNull CompletionResultSet result
					)
					{
						PsiElement element = parameters.getPosition();
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
										for(PsiField f : pc.getFields())
										{
											result.addElement(LookupElementBuilder.create(f));
										}
									}
								}
							}
						});
						
						result.stopHere();
					}
				}
		);
	}
	
	protected PsiAnnotation getAnnotationContext(PsiElement position)
	{
		if(!(position.getParent().getParent() instanceof PsiNameValuePair pair)
		   || !"field".equals(pair.getAttributeName())
		) return null;
		
		if(position.getParent().getParent().getParent() instanceof PsiAnnotationParameterList apr
		   && apr.getParent() instanceof PsiAnnotation annotation
		   && PsiHelper.isOneOf(annotation, SimplyRegisterMechanism.REF))
			return annotation;
		
		return null;
	}
}