package org.zeith.hammerhelper.inspections.classes.packets;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.quickfixes.AddPacketExecuteQuickFix;
import org.zeith.hammerhelper.utils.PsiHelper;

import java.util.function.Predicate;

import static org.zeith.hammerhelper.inspections.classes.packets.MissingEmptyPacketConstructor.PACKET_TYPE;

public class MissingPacketExecutes
		extends LocalInspectionTool
{
	public static final Predicate<PsiClass> EXECUTION = PsiHelper.oneOfImplementedMethods(
			"org.zeith.hammerlib.net.IPacket",
			"execute", "clientExecute", "serverExecute"
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
				
				if(!PsiHelper.inHierarchy(aClass, EXECUTION))
					holder.registerProblem(name, "Packet class does nothing.", ProblemHighlightType.WARNING,
							new AddPacketExecuteQuickFix(aClass)
					);
			}
		};
	}
}