package org.zeith.hammerhelper.contributors.hammerlib;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.ModCompatMechanism;
import org.zeith.hammerhelper.utils.PsiHelper;

import java.util.Map;

public class ModCompatContributor
		extends CompletionContributor
{
	public ModCompatContributor()
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
						
						Map<String, PsiClass> mods = ModCompatMechanism.gatherModClasses(element);
						for(String modid : mods.keySet())
							result.addElement(LookupElementBuilder.create(modid));
						
						result.stopHere();
					}
				}
		);
	}
	
	protected PsiAnnotation getAnnotationContext(PsiElement position)
	{
		if(!(position.getParent().getParent() instanceof PsiNameValuePair pair)
		   || !"modid".equals(pair.getAttributeName())
		) return null;
		
		if(position.getParent().getParent().getParent() instanceof PsiAnnotationParameterList apr
		   && apr.getParent() instanceof PsiAnnotation annotation
		   && PsiHelper.isOneOf(annotation, ModCompatMechanism.LOAD_COMPAT))
			return annotation;
		
		return null;
	}
}