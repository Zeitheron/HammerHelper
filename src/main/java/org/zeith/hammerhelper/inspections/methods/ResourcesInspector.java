package org.zeith.hammerhelper.inspections.methods;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.PsiHelper;
import org.zeith.hammerhelper.utils.ResourceLocationChecks;

public class ResourcesInspector
		extends LocalInspectionTool
{
	@Override
	public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session)
	{
		return new JavaElementVisitor()
		{
			@Override
			public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression)
			{
				PsiMethod method = expression.resolveMethod();
				if(method == null || !"location".equals(method.getName())) return;
				var owner = method.getContainingClass();
				if(owner == null || owner.getName() == null) return;
				if(!"org.zeith.hammerlib.util.mcf.Resources".equals(owner.getQualifiedName())) return;
				
				var pars = expression.getArgumentList().getExpressions();
				
				if(pars.length == 1)
				{
					var composed = pars[0];
					var val = PsiHelper.getExpressionStringRepresentation(composed, null);
					if(val == null) return;
					
					if(val.contains(":"))
					{
						var rl = val.split(":", 2);
						if(!ResourceLocationChecks.isValidNamespace(rl[0]))
							holder.registerProblem(composed, "Non [a-z0-9._-] character in namespace.", ProblemHighlightType.ERROR);
						if(!ResourceLocationChecks.isValidPath(rl[1]))
							holder.registerProblem(composed, "Non [a-z0-9/._-] character in path.", ProblemHighlightType.ERROR);
					} else
					{
						if(!ResourceLocationChecks.isValidPath(val))
							holder.registerProblem(composed, "Non [a-z0-9/._-] character in path.", ProblemHighlightType.ERROR);
					}
				}
				
				if(pars.length == 2)
				{
					var val = PsiHelper.getExpressionStringRepresentation(pars[0], null);
					if(val != null && !ResourceLocationChecks.isValidNamespace(val))
						holder.registerProblem(pars[0], "Non [a-z0-9._-] character in namespace.", ProblemHighlightType.ERROR);
					
					val = PsiHelper.getExpressionStringRepresentation(pars[1], null);
					if(val != null && !ResourceLocationChecks.isValidPath(val))
						holder.registerProblem(pars[1], "Non [a-z0-9/._-] character in path.", ProblemHighlightType.ERROR);
				}
			}
		};
	}
}