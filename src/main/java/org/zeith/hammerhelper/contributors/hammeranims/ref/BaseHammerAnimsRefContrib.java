package org.zeith.hammerhelper.contributors.hammeranims.ref;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerhelper.contributors.refs.BaseRegisteredObjectRefContributor;

import java.util.function.Consumer;

import static org.zeith.hammerhelper.utils.FileHelper.getRecursive;

public abstract class BaseHammerAnimsRefContrib
		extends BaseRegisteredObjectRefContributor
{
	protected final String bedrockFolder, optSuffix;
	protected final String createNoSuffixMN;
	
	public BaseHammerAnimsRefContrib(String bedrockFolder, String optSuffix, String registrationType)
	{
		super(registrationType);
		this.bedrockFolder = bedrockFolder;
		this.optSuffix = optSuffix;
		
		var simpleName = registrationType.substring(registrationType.lastIndexOf('.') + 1);
		this.createNoSuffixMN = simpleName + ".createNoSuffix";
	}
	
	@Override
	protected void findReference(@NotNull PsiElement element, @NotNull ProcessingContext context, @NotNull PsiField field, @NotNull String registryPath, @NotNull Consumer<@Nullable VirtualFile> fileConsumer)
	{
		boolean createNoSuffix = false;
		var initializer = field.getInitializer();
		if(optSuffix != null && initializer instanceof PsiMethodCallExpression mce)
		{
			var expr = mce.getMethodExpression().getText();
			createNoSuffix = expr.equals(createNoSuffixMN);
		}
		
		var filePath = (registryPath + (createNoSuffix || optSuffix == null ? "" : optSuffix) + ".json").split("/");
		
		forEachAssetsNamespace(element, namespace ->
		{
			var sub = getRecursive(namespace, "bedrock", bedrockFolder);
			if(sub == null) return;
			sub = getRecursive(sub, filePath);
			fileConsumer.accept(sub);
		});
	}
}