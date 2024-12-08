package org.zeith.hammerhelper.inspections.annotations.simplyregister;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.PsiHelper;
import org.zeith.hammerhelper.utils.SimplyRegisterMechanism;

public class AnonymousClassesInsideSimplyRegisters
		extends SimplyRegisterInspector
{
	@Override
	public @NotNull JavaElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session)
	{
		return new JavaElementVisitor()
		{
			@Override
			public void visitAnonymousClass(@NotNull PsiAnonymousClass aClass)
			{
				super.visitAnonymousClass(aClass);
				PsiClass owner = PsiHelper.findClassParent(aClass.getParent());
				if(owner == null) return;
				if(!owner.isInterface() || SimplyRegisterMechanism.findSimplyRegister(owner) == null) return;
				holder.registerProblem(aClass, "Anonymous class inside @SimplyRegister-annotated class.", ProblemHighlightType.ERROR);
			}
		};
	}
}