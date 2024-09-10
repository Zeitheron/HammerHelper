package org.zeith.hammerhelper.inspections.annotations.ref;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.PsiHelper;

import java.util.concurrent.atomic.AtomicBoolean;

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
				PsiAnnotationMemberValue value = annotation.findAttributeValue("value");
				PsiAnnotationMemberValue fieldAttr = annotation.findAttributeValue("field");
				if(fieldAttr == null) return;
				String field = PsiHelper.getAnnotationAttributeValue(annotation, "field", "");
				if(field == null || field.isBlank()) return;
				
				AtomicBoolean found = new AtomicBoolean(false);
				value.acceptChildren(new PsiElementVisitor()
				{
					@Override
					public void visitElement(@NotNull PsiElement element)
					{
						if(element instanceof PsiTypeElement pt)
						{
							var pc = PsiTypesUtil.getPsiClass(pt.getType());
							if(pc != null)
							{
								if(pc.findFieldByName(field, false) != null)
									found.set(true);
							}
						}
					}
				});
				if(!found.get())
				{
					holder.registerProblem(fieldAttr, "@Ref points to non-existent field.",
							ProblemHighlightType.ERROR
					);
				}
			}
		};
	}
}