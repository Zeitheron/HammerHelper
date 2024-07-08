package org.zeith.hammerhelper.inspections.annotations.simplyregister;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.*;

public class InvalidPrefixSimplyRegister
		extends LocalInspectionTool
{
	@Override
	public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session)
	{
		return new JavaElementVisitor()
		{
			@Override
			public void visitClass(@NotNull PsiClass aClass)
			{
				var SimplyRegister = SimplyRegisterMechanism.findSimplyRegister(aClass);
				if(SimplyRegister == null) return;
				var srPrefix = PsiHelper.getAnnotationAttributeValue(SimplyRegister, "prefix", null);
				
				if(srPrefix != null && !ResourceLocationChecks.isValidPath(srPrefix))
				{
					holder.registerProblem(SimplyRegister.findAttributeValue("prefix"), "Non [a-z0-9/._-] character in prefix.", ProblemHighlightType.ERROR);
				}
			}
		};
	}
}