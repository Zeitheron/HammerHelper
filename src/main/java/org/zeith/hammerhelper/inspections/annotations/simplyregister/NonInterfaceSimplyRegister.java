package org.zeith.hammerhelper.inspections.annotations.simplyregister;

import com.intellij.codeInspection.*;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.QuickFixHelper;

import static org.zeith.hammerhelper.utils.SimplyRegisterMechanism.findSimplyRegister;

public class NonInterfaceSimplyRegister
		extends SimplyRegisterInspector
{
	@Override
	public @NotNull JavaElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session)
	{
		return new JavaElementVisitor()
		{
			@Override
			public void visitClass(@NotNull PsiClass aClass)
			{
				if(findSimplyRegister(aClass) == null) return;
				
				if(!aClass.isInterface())
				{
					holder.registerProblem(aClass.getNameIdentifier(), "@SimplyRegister-annotated classes should be interfaces.", ProblemHighlightType.WEAK_WARNING,
							QuickFixHelper.convertClassToInterface()
					);
				}
			}
		};
	}
}