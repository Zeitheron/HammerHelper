package org.zeith.hammerhelper.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public abstract class NonExistingBinding
		extends LocalInspectionTool
{
	@Override
	public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session)
	{
		return new JavaElementVisitor()
		{
			@Override
			public void visitField(@NotNull PsiField field)
			{
			
			}
		};
	}
}