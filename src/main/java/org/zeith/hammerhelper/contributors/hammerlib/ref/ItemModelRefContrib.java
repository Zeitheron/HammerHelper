package org.zeith.hammerhelper.contributors.hammerlib.ref;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerhelper.contributors.refs.BaseRegisteredObjectRefContributor;

import java.util.function.Consumer;

import static org.zeith.hammerhelper.utils.FileHelper.getRecursive;

public class ItemModelRefContrib
		extends BaseRegisteredObjectRefContributor
{
	public ItemModelRefContrib()
	{
		super(
				"net.minecraft.world.item.Item",
				"net.minecraft.item.Item" // 1.12.2
		);
	}
	
	@Override
	protected void findReference(@NotNull PsiElement element, @NotNull ProcessingContext context, @NotNull PsiField registerField, @NotNull String registryPath, @NotNull Consumer<@Nullable VirtualFile> fileConsumer)
	{
		var filePath = (registryPath + ".json").split("/");
		
		forEachAssetsNamespace(element, namespace ->
		{
			var sub = getRecursive(namespace, "models", "item");
			if(sub == null) return;
			sub = getRecursive(sub, filePath);
			fileConsumer.accept(sub);
		});
	}
}