package org.zeith.hammerhelper.contributors.hammeranims;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.*;

import java.util.*;

import static org.zeith.hammerhelper.utils.FileHelper.getRecursive;

public abstract class BaseHammerAnimsContrib
		extends CompletionContributor
{
	protected final String bedrockFolder, optSuffix, registrationType;
	
	public BaseHammerAnimsContrib(String bedrockFolder, String optSuffix, String registrationType)
	{
		this.bedrockFolder = bedrockFolder;
		this.optSuffix = optSuffix;
		this.registrationType = registrationType;
		
		var createNoSuffixMN = "createNoSuffix";
		
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
						PsiElement position = parameters.getPosition();
						var field = getAnnotationContext(position);
						if(field == null) return;
						
						var prefix = "";
						if(field.getParent() instanceof PsiClass psiClass)
						{
							var sr = SimplyRegisterMechanism.findSimplyRegister(psiClass);
							var v = PsiHelper.getAnnotationAttributeValue(sr, "prefix", "");
							if(v != null) prefix = v;
						}
						
						boolean createNoSuffix = false;
						var initializer = field.getInitializer();
						if(optSuffix != null && initializer instanceof PsiMethodCallExpression mce)
						{
							var expr = mce.getMethodExpression().getText();
							createNoSuffix = expr.contains(createNoSuffixMN);
						}
						
						List<String> animationFiles = getAnimationFiles(position.getContainingFile());
						if(optSuffix == null || createNoSuffix)
						{
							for(String file : animationFiles)
								if(file.startsWith(prefix))
									result.addElement(LookupElementBuilder.create(file.substring(prefix.length())));
						} else // with suffix
						{
							var l = optSuffix.length();
							for(String file : animationFiles)
								if(file.startsWith(prefix) && file.endsWith(optSuffix))
									result.addElement(LookupElementBuilder.create(file.substring(prefix.length(), file.length() - l)));
						}
						result.stopHere();
					}
				}
		);
	}
	
	private PsiField getAnnotationContext(PsiElement position)
	{
		if(position.getParent().getParent().getParent() instanceof PsiAnnotationParameterList apr
		   && apr.getParent() instanceof PsiAnnotation annotation
		   && PsiHelper.isOneOf(annotation, SimplyRegisterMechanism.REGISTRY_NAME)
		   && annotation.getOwner() instanceof PsiModifierList modList
		   && modList.getParent() instanceof PsiField field)
		{
			var cls = PsiTypesUtil.getPsiClass(field.getType());
			return PsiHelper.instanceOf(cls, Set.of(registrationType)) ? field : null;
		}
		
		return null;
	}
	
	private List<String> getAnimationFiles(PsiFile file)
	{
		List<String> names = new ArrayList<>();
		
		for(var namespace : FileHelper.getAllAssetNamespaces(file))
		{
			var sub = getRecursive(namespace.file(), "bedrock", bedrockFolder);
			names.addAll(FileHelper.getRecursiveFilesNamesWithFullPathFromDirectory(sub).stream().map(s -> s.substring(0, s.lastIndexOf("."))).toList());
		}
		
		return names;
	}
}