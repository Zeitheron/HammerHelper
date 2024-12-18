package org.zeith.hammerhelper.contributors.hammerlib.flowgui;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.*;
import org.zeith.hammerhelper.utils.flowgui.FlowguiModel;

import java.util.*;

import static org.zeith.hammerhelper.utils.FileHelper.getRecursive;

public class FlowguiXmlAnnotationContributor
		extends CompletionContributor
{
	public FlowguiXmlAnnotationContributor()
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
						
						for(String fileRef : getXmlFiles(element.getContainingFile()))
							result.addElement(LookupElementBuilder.create(fileRef));
						
						result.stopHere();
					}
				}
		);
	}
	
	
	private List<String> getXmlFiles(PsiFile file)
	{
		List<String> names = new ArrayList<>();
		
		for(var namespace : FileHelper.getAllAssetNamespaces(file))
		{
			var sub = getRecursive(namespace.file(), "flowgui");
			names.addAll(FileHelper.getRecursiveFilesNamesWithFullPathFromDirectory(sub)
					.stream()
					.filter(s -> s.endsWith(".xml"))
					.map(s -> s.substring(0, s.lastIndexOf(".")))
					.toList()
			);
		}
		
		return names;
	}
	
	protected PsiAnnotation getAnnotationContext(PsiElement position)
	{
		if(!(position.getParent().getParent() instanceof PsiNameValuePair pair)
		   || !"value".equals(pair.getAttributeName())
		) return null;
		
		if(position.getParent().getParent().getParent() instanceof PsiAnnotationParameterList apr
		   && apr.getParent() instanceof PsiAnnotation annotation
		   && PsiHelper.isOneOf(annotation, FlowguiModel.XML_FLOWGUI))
			return annotation;
		
		return null;
	}
}