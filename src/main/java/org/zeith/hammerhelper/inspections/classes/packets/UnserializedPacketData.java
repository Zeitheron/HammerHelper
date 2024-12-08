package org.zeith.hammerhelper.inspections.classes.packets;

import com.intellij.codeInspection.*;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.PsiHelper;

import java.util.*;

import static org.zeith.hammerhelper.inspections.classes.packets.MissingEmptyPacketConstructor.PACKET_TYPE;

public class UnserializedPacketData
		extends LocalInspectionTool
{
	public static final Set<String> IGNORES = Set.of("ignore", "skip");
	
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
				
				Map<String, PsiField> lookup = new HashMap<>();
				for(PsiField field : aClass.getFields())
				{
					if(field.hasModifier(JvmModifier.FINAL) || field.hasModifier(JvmModifier.STATIC) || Arrays.stream(field.getAnnotations()).anyMatch(p ->
							p.getQualifiedName() != null
							&& IGNORES.stream().anyMatch(p.getQualifiedName().toLowerCase(Locale.ROOT)::contains)
					)) continue;
					lookup.put(field.getName(), field);
				}
				
				if(lookup.isEmpty()) return;
				
				Set<String> allReadFields = new HashSet<>(lookup.keySet());
				Set<String> allWriteFields = new HashSet<>(lookup.keySet());
				
				for(PsiMethod write : aClass.findMethodsByName("write", false))
					allWriteFields.removeAll(getFieldReferences(holder, aClass, write));
				
				for(PsiMethod read : aClass.findMethodsByName("read", false))
					allReadFields.removeAll(getFieldAssignments(aClass, read));
				
				for(String read : allReadFields)
					holder.registerProblem(lookup.get(read), "Field is not read from network.", ProblemHighlightType.WARNING);
				
				for(String write : allWriteFields)
					holder.registerProblem(lookup.get(write), "Field is not written to network.", ProblemHighlightType.WARNING);
			}
		};
	}
	
	public Set<String> getFieldReferences(@NotNull ProblemsHolder holder, PsiClass owner, PsiMethod method)
	{
		Set<String> references = new HashSet<>();
		
		method.getBody().accept(new JavaRecursiveElementVisitor()
		{
			@Override
			public void visitReferenceExpression(@NotNull PsiReferenceExpression expression)
			{
				super.visitReferenceExpression(expression);
				if(expression.resolve() instanceof PsiField pf && Objects.equals(owner, pf.getParent()))
				{
					if(expression.getParent() instanceof PsiAssignmentExpression)
						holder.registerProblem(expression, "Attempting to assign field while writing to network.", ProblemHighlightType.ERROR);
					else
						references.add(pf.getName());
				}
			}
		});
		
		return references;
	}
	
	public Set<String> getFieldAssignments(PsiClass owner, PsiMethod method)
	{
		Set<String> references = new HashSet<>();
		
		method.getBody().accept(new JavaRecursiveElementVisitor()
		{
			@Override
			public void visitReferenceExpression(@NotNull PsiReferenceExpression expression)
			{
				super.visitReferenceExpression(expression);
				if(expression.resolve() instanceof PsiField pf && Objects.equals(owner, pf.getParent()))
				{
					references.add(pf.getName());
				}
			}
		});
		
		return references;
	}
}