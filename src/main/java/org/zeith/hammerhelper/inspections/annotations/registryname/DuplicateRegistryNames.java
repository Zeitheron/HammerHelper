package org.zeith.hammerhelper.inspections.annotations.registryname;

import com.google.common.collect.ArrayListMultimap;
import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.PsiHelper;
import org.zeith.hammerhelper.utils.SimplyRegisterMechanism;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DuplicateRegistryNames
		extends LocalInspectionTool
{
	@Override
	public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session)
	{
		return new JavaElementVisitor()
		{
			@Override
			public void visitField(@NotNull PsiField aClass)
			{
				var RegistryName = SimplyRegisterMechanism.findRegistryName(aClass);
				if(RegistryName == null) return;
				
				ArrayListMultimap<String, PsiField> fields = null;
				if(aClass.getParent() instanceof PsiClass ownerClass)
				{
					var psiFields = ownerClass.getFields();
					fields = ArrayListMultimap.create(psiFields.length, 1);
					for(PsiField psiField : psiFields)
					{
						var subPath = SimplyRegisterMechanism.getRegistryPath(psiField);
						if(subPath == null) continue;
						fields.put(subPath, psiField);
					}
				}
				
				if(fields == null) return;
				var path = SimplyRegisterMechanism.getRegistryPath(aClass);
				if(path == null) return;
				
				var allFields = fields.get(path);
				
				if(allFields.size() > 1)
				{
					Set<String> typesB = new HashSet<>();
					PsiHelper.visitPossibleTypes(PsiTypesUtil.getPsiClass(aClass.getType()), typesB);
					
					String allDupes = allFields.stream()
							.filter(psf -> !psf.getName().equals(aClass.getName()))
							.filter(psf -> !PsiHelper.sharingSameRegistryInheritance(PsiTypesUtil.getPsiClass(psf.getType()), typesB).isEmpty())
							.map(PsiField::getName)
							.collect(Collectors.joining(", "));
					
					if(!allDupes.isBlank())
						holder.registerProblem(RegistryName.findAttributeValue("value"),
								"This registry name is already in use in %s field%s.".formatted(allDupes, allDupes.contains(", ") ? "s" : ""),
								ProblemHighlightType.ERROR
						);
				}
			}
		};
	}
}