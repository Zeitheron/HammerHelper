package org.zeith.hammerhelper.icons;

import com.intellij.ide.IconLayerProvider;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerhelper.utils.ModCompatMechanism;
import org.zeith.hammerhelper.utils.PsiHelper;

import javax.swing.*;
import java.util.function.Predicate;

public class ModCompatIconLayerProvider
		implements IconLayerProvider
{
	public static final Predicate<PsiClass> COMPAT_TYPE = PsiHelper.oneOf(
			"org.zeith.hammerlib.compat.base.BaseCompat"
	);
	
	@Override
	public @Nullable Icon getLayerIcon(@NotNull Iconable element, boolean isLocked)
	{
		if(element instanceof PsiClass pc
		   && PsiHelper.inHierarchy(pc, COMPAT_TYPE)
		   && ModCompatMechanism.findLoadCompat(pc) != null)
			return HLIcons.COMPAT;
		return null;
	}
	
	@Override
	public @NotNull String getLayerDescription()
	{
		return "Mod Compatibility Class";
	}
}