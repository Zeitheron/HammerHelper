package org.zeith.hammerhelper.inspections.annotations.ref;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.PsiHelper;

public class InvalidRefTarget
		extends LocalInspectionTool
{
	@Override
	public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly)
	{
		return new JavaElementVisitor()
		{
			@Override
			public void visitAnnotation(@NotNull PsiAnnotation annotation)
			{
				PsiAnnotationMemberValue fieldAttr = annotation.findAttributeValue("field");
				if(fieldAttr == null) return;
				String field = PsiHelper.getAnnotationAttributeValue(annotation, "field", "");
				if(field == null || field.isBlank()) return;
				
				PsiClass pc = PsiHelper.getClassFromPsiAnnotation(annotation, "value");
				boolean found = pc != null && pc.findFieldByName(field, false) != null;
				
				if(!found)
				{
					holder.registerProblem(fieldAttr, "@Ref points to non-existent field.",
							ProblemHighlightType.ERROR
					);
				}
			}
		};
	}
}