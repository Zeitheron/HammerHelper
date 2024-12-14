package org.zeith.hammerhelper.inspections.classes.compat;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.ModCompatMechanism;
import org.zeith.hammerhelper.utils.PsiHelper;

import java.util.Objects;

public class NonMatchingCompatTypes
		extends LocalInspectionTool
{
	@Override
	public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly)
	{
		return new JavaElementVisitor()
		{
			@Override
			public void visitClass(@NotNull PsiClass aClass)
			{
				PsiAnnotation loadCompat = ModCompatMechanism.findLoadCompat(aClass);
				if(loadCompat == null) return;
				
				PsiClass compatType = ModCompatMechanism.getCompatType(loadCompat);
				
				if(!PsiHelper.inHierarchy(aClass, compatType::equals))
				{
					PsiElement pe = aClass.getNameIdentifier();
					if(pe == null) pe = aClass;
					holder.registerProblem(pe, "Non-matching compatibility types between @LoadCompat and extends.", ProblemHighlightType.ERROR);
				}
			}
		};
	}
}