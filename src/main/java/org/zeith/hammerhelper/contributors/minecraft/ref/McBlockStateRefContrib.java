package org.zeith.hammerhelper.contributors.minecraft.ref;

import com.intellij.json.psi.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.configs.general.GeneralConfigsHH;
import org.zeith.hammerhelper.contributors.refs.ToVirtualFilesRefContributor;
import org.zeith.hammerhelper.utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class McBlockStateRefContrib
		extends PsiReferenceContributor
{
	public static Predicate<String> MODEL_PATH = Pattern.compile("variants/[^/]*/model").asPredicate();
	public static Predicate<String> TEXTURE_PATH = Pattern.compile("variants/[^/]*/textures/[^/]+").asPredicate();
	public static final KeyedPrefixPath BLOCKSTATES = KeyedPrefixPath.of("blockstates");
	
	@Override
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar)
	{
		var jsonPattern = PlatformPatterns.psiElement(JsonStringLiteral.class)
				.inFile(PlatformPatterns.psiFile().with(new PsiFilePatternCondition()));
		
		registrar.registerReferenceProvider(jsonPattern, new PsiReferenceProvider()
				{
					@Override
					public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context)
					{
						if(!GeneralConfigsHH.get(context).enableMCJsonReferences())
							return new PsiReference[0];
						
						List<PsiReference> refs = new ArrayList<>();
						
						VirtualFile assets = FileHelper.findAssetRoot(element.getContainingFile(), context, BLOCKSTATES);
						
						if(element instanceof JsonStringLiteral literal
						   && literal.getParent() instanceof JsonProperty prop
						   && prop.getParent() instanceof JsonObject obj
						)
						{
							JsonProperty jsp = obj.findProperty(prop.getName());
							
							if(jsp == null || jsp.getValue() != element)
								return new PsiReference[0];
							
							String jsonPath = PsiJsonHelper.reconstructPathFromRoot(literal);
							
							if(MODEL_PATH.test(jsonPath))
							{
								String[] path = ResourceLocationChecks.split(literal.getValue());
								
								var s = (path[0] + "/models/" + path[1] + ".json").split("/");
								VirtualFile modelFile = FileHelper.getRecursive(assets, s);
								
								if(modelFile != null)
									refs.add(new ToVirtualFilesRefContributor.VirtualFilePsiReference(element, modelFile));
							} else if(TEXTURE_PATH.test(jsonPath))
							{
								String[] path = ResourceLocationChecks.split(literal.getValue());
								
								var s = (path[0] + "/textures/" + path[1] + ".png").split("/");
								VirtualFile modelFile = FileHelper.getRecursive(assets, s);
								
								if(modelFile != null)
									refs.add(new ToVirtualFilesRefContributor.VirtualFilePsiReference(element, modelFile));
							}
						}
						
						return refs.toArray(PsiReference[]::new);
					}
				}
		);
	}
	
	private static class PsiFilePatternCondition
			extends PatternCondition<PsiFile>
	{
		public PsiFilePatternCondition()
		{
			super("PsiFilePatternCondition");
		}
		
		@Override
		public boolean accepts(@NotNull PsiFile file, ProcessingContext context)
		{
			return FileHelper.findAssetRoot(file, context, BLOCKSTATES) != null;
		}
	}
}