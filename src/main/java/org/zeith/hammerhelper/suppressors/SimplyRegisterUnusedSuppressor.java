package org.zeith.hammerhelper.suppressors;

import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerhelper.utils.SimplyRegisterMechanism;

import java.util.Arrays;

public class SimplyRegisterUnusedSuppressor
		implements InspectionSuppressor
{
	@Override
	public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId)
	{
		if(!"unused".equals(toolId) && !"UnusedReturnValue".equals(toolId)) return false;
		
		var com = element.getParent();
		
		if(com instanceof PsiClass cls && SimplyRegisterMechanism.findSimplyRegister(cls) != null)
			return Arrays.stream(cls.getFields())
					.anyMatch(psf -> SimplyRegisterMechanism.findRegistryName(psf) != null);
		
		return com instanceof PsiField cls && SimplyRegisterMechanism.findRegistryName(cls) != null;
	}
	
	@Override
	public SuppressQuickFix @NotNull [] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId)
	{
		return SuppressQuickFix.EMPTY_ARRAY;
	}
}
