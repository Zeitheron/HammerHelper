package org.zeith.hammerhelper.inspections.classes.packets;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.PsiHelper;

import java.util.Arrays;
import java.util.function.Predicate;

public class MissingEmptyPacketConstructor
		extends LocalInspectionTool
{
	public static final Predicate<PsiClass> PACKET_TYPE = PsiHelper.oneOf(
			"org.zeith.hammerlib.net.IPacket"
	);
	
	@Override
	public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly)
	{
		return new JavaElementVisitor()
		{
			@Override
			public void visitClass(@NotNull PsiClass aClass)
			{
				super.visitClass(aClass);
				var name = aClass.getNameIdentifier();
				if(name == null) return;
				if(!PsiHelper.inHierarchy(aClass, PACKET_TYPE)) return;
				
				var ctrs = aClass.getConstructors();
				if(Arrays.stream(ctrs).allMatch(PsiMethod::hasParameters))
					holder.registerProblem(name, "Missing empty (no-data) packet constructor.", ProblemHighlightType.ERROR);
			}
		};
	}
}