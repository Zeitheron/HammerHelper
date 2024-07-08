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
		
		var simpleName = registrationType.substring(registrationType.lastIndexOf('.') + 1);
		var createNoSuffixMN = simpleName + ".createNoSuffix";
		
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
						Project project = position.getProject();
						
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
							createNoSuffix = expr.equals(createNoSuffixMN);
						}
						
						List<String> animationFiles = getAnimationFiles(project);
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
	
	private List<String> getAnimationFiles(Project project)
	{
		List<String> names = new ArrayList<>();
		
		VirtualFile file = getRecursive(project.getBaseDir(), "src", "main", "resources", "assets");
		if(file == null) return names;
		
		for(var namespace : file.getChildren())
		{
			var sub = getRecursive(namespace, "bedrock", bedrockFolder);
			names.addAll(FileHelper.getRecursiveFilesNamesWithFullPathFromDirectory(sub).stream().map(s -> s.substring(0, s.lastIndexOf("."))).toList());
		}
		
		return names;
	}
}