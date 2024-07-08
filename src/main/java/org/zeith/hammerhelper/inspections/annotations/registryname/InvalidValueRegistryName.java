package org.zeith.hammerhelper.inspections.annotations.registryname;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.*;

public class InvalidValueRegistryName
		extends LocalInspectionTool
{
	@Override
	public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session)
	{
		return new JavaElementVisitor()
		{
			@Override
			public void visitField(@NotNull PsiField aClass)
			{
				var RegistryName = SimplyRegisterMechanism.findRegistryName(aClass);
				if(RegistryName == null) return;
				var rnPrefix = PsiHelper.getAnnotationAttributeValue(RegistryName, "value", null);
				
				if(rnPrefix != null && !ResourceLocationChecks.isValidPath(rnPrefix))
				{
					holder.registerProblem(RegistryName.findAttributeValue("value"), "Non [a-z0-9/._-] character in registry name.", ProblemHighlightType.ERROR);
				}
			}
		};
	}
}