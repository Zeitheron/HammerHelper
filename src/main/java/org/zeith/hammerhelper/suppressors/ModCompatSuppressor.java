package org.zeith.hammerhelper.suppressors;

import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerhelper.utils.ModCompatMechanism;

public class ModCompatSuppressor
		implements InspectionSuppressor
{
	@Override
	public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId)
	{
		if(!"unused".equals(toolId) && !"UnusedReturnValue".equals(toolId)) return false;
		return element.getParent() instanceof PsiClass cls && ModCompatMechanism.findLoadCompat(cls) != null;
	}
	
	@Override
	public SuppressQuickFix @NotNull [] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId)
	{
		return SuppressQuickFix.EMPTY_ARRAY;
	}
}
