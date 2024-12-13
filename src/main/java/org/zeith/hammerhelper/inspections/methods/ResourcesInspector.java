package org.zeith.hammerhelper.inspections.methods;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.PsiHelper;
import org.zeith.hammerhelper.utils.ResourceLocationChecks;

import java.util.concurrent.atomic.AtomicBoolean;

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
					String str = PsiHelper.getExpressionStringRepresentation(pars[0], "");
					
					String path;
					String namespace;
					if(str.contains(":"))
					{
						var split = str.split(":", 2);
						namespace = split[0];
						path = split[1];
					} else
					{
						namespace = "minecraft";
						path = str;
					}
					
					if(!namespace.isEmpty() && !ResourceLocationChecks.isValidNamespace(namespace))
						holder.registerProblem(pars[0], "Non [a-z0-9._-] character in namespace.", ProblemHighlightType.ERROR);
					
					if(!path.isEmpty() && !ResourceLocationChecks.isValidPath(path))
						holder.registerProblem(pars[0], "Non [a-z0-9/._-] character in path.", ProblemHighlightType.ERROR);
				}
				
				if(pars.length == 2)
				{
					PsiHelper.visitExpressionStringRepresentation(pars[0], (psi, str) ->
							{
								if(str != null && !ResourceLocationChecks.isValidNamespace(str))
									holder.registerProblem(psi, "Non [a-z0-9._-] character in namespace.", ProblemHighlightType.ERROR);
							}
					);
					
					PsiHelper.visitExpressionStringRepresentation(pars[1], (psi, str) ->
							{
								if(str != null && !ResourceLocationChecks.isValidPath(str))
									holder.registerProblem(psi, "Non [a-z0-9/._-] character in path.", ProblemHighlightType.ERROR);
							}
					);
				}
			}
		};
	}
}