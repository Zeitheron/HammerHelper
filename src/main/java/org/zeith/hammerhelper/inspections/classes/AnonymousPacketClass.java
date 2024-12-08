package org.zeith.hammerhelper.inspections.classes;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.PsiHelper;

import static org.zeith.hammerhelper.inspections.classes.MissingEmptyPacketConstructor.PACKET_TYPE;

public class AnonymousPacketClass
		extends LocalInspectionTool
{
	@Override
	public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly)
	{
		return new JavaElementVisitor()
		{
			@Override
			public void visitAnonymousClass(@NotNull PsiAnonymousClass aClass)
			{
				super.visitAnonymousClass(aClass);
				if(!PsiHelper.inHierarchy(aClass, PACKET_TYPE)) return;
				
				holder.registerProblem(aClass, "Anonymous packet class is not allowed.", ProblemHighlightType.ERROR);
			}
		};
	}
}