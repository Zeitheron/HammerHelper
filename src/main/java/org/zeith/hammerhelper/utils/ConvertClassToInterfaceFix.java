package org.zeith.hammerhelper.utils;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.modcommand.ModCommand;
import com.intellij.modcommand.ModCommandQuickFix;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.containers.ContainerUtil;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.psiutils.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConvertClassToInterfaceFix
{
	public static ClassMayBeInterfaceFix create()
	{
		return new ClassMayBeInterfaceFix();
	}
	
	static boolean isEmptyConstructor(@NotNull PsiMethod method)
	{
		return method.isConstructor() && MethodUtils.isTrivial(method);
	}
	
	public static class ClassMayBeInterfaceFix
			extends ModCommandQuickFix
	{
		@Override
		@NotNull
		public String getFamilyName()
		{
			return InspectionGadgetsBundle.message("class.may.be.interface.convert.quickfix");
		}
		
		@Override
		public @NotNull ModCommand perform(@NotNull Project project, @NotNull ProblemDescriptor descriptor)
		{
			final PsiIdentifier classNameIdentifier = (PsiIdentifier) descriptor.getPsiElement();
			final PsiClass interfaceClass = (PsiClass) classNameIdentifier.getParent();
			final SearchScope searchScope = interfaceClass.getUseScope();
			final List<PsiClass> elements = new ArrayList<>();
			elements.add(interfaceClass);
			for(final PsiClass inheritor : ClassInheritorsSearch.search(interfaceClass, searchScope, false))
			{
				elements.add(inheritor);
			}
			return ModCommand.psiUpdate(interfaceClass, (cls, updater) ->
					{
						moveSubClassExtendsToImplements(ContainerUtil.map(elements, updater::getWritable));
						changeClassToInterface(cls);
						moveImplementsToExtends(cls);
					}
			);
		}
		
		private static void changeClassToInterface(PsiClass aClass)
		{
			for(PsiMethod method : aClass.getMethods())
			{
				if(isEmptyConstructor(method))
				{
					method.delete();
					continue;
				}
				PsiUtil.setModifierProperty(method, PsiModifier.PUBLIC, false);
				if(method.hasModifierProperty(PsiModifier.STATIC))
				{
					continue;
				} else if(method.hasModifierProperty(PsiModifier.ABSTRACT))
				{
					PsiUtil.setModifierProperty(method, PsiModifier.ABSTRACT, false); // redundant modifier
					continue;
				}
				PsiUtil.setModifierProperty(method, PsiModifier.DEFAULT, true);
			}
			for(PsiField field : aClass.getFields())
			{
				PsiUtil.setModifierProperty(field, PsiModifier.PUBLIC, false);
				PsiUtil.setModifierProperty(field, PsiModifier.STATIC, false);
				PsiUtil.setModifierProperty(field, PsiModifier.FINAL, false);
			}
			for(PsiClass innerClass : aClass.getInnerClasses())
			{
				PsiUtil.setModifierProperty(innerClass, PsiModifier.PUBLIC, false);
			}
			final PsiIdentifier nameIdentifier = aClass.getNameIdentifier();
			if(nameIdentifier == null)
			{
				return;
			}
			final PsiKeyword classKeyword = PsiTreeUtil.getPrevSiblingOfType(nameIdentifier, PsiKeyword.class);
			final PsiElementFactory factory = JavaPsiFacade.getElementFactory(aClass.getProject());
			final PsiKeyword interfaceKeyword = factory.createKeyword(PsiKeyword.INTERFACE);
			if(classKeyword == null)
			{
				return;
			}
			PsiUtil.setModifierProperty(aClass, PsiModifier.ABSTRACT, false);
			PsiUtil.setModifierProperty(aClass, PsiModifier.FINAL, false);
			classKeyword.replace(interfaceKeyword);
		}
		
		private static void moveImplementsToExtends(PsiClass anInterface)
		{
			final PsiReferenceList extendsList = anInterface.getExtendsList();
			if(extendsList == null)
			{
				return;
			}
			final PsiReferenceList implementsList = anInterface.getImplementsList();
			if(implementsList == null)
			{
				return;
			}
			final PsiJavaCodeReferenceElement[] referenceElements = implementsList.getReferenceElements();
			for(final PsiJavaCodeReferenceElement referenceElement : referenceElements)
			{
				extendsList.add(referenceElement);
				referenceElement.delete();
			}
		}
		
		private static void moveSubClassExtendsToImplements(List<PsiClass> inheritors)
		{
			final PsiClass oldClass = inheritors.get(0);
			for(int i = 1; i < inheritors.size(); i++)
			{
				final PsiClass inheritor = inheritors.get(i);
				final PsiReferenceList extendsList = inheritor.getExtendsList();
				if(extendsList == null)
				{
					continue;
				}
				final PsiReferenceList implementsList = inheritor.getImplementsList();
				moveReference(extendsList, implementsList, oldClass);
			}
		}
		
		private static void moveReference(@NotNull PsiReferenceList source, @Nullable PsiReferenceList target,
										  @NotNull PsiClass oldClass)
		{
			final PsiJavaCodeReferenceElement[] sourceReferences = source.getReferenceElements();
			for(final PsiJavaCodeReferenceElement sourceReference : sourceReferences)
			{
				if(sourceReference.isReferenceTo(oldClass))
				{
					if(target != null)
					{
						target.add(sourceReference);
					}
					sourceReference.delete();
				}
			}
		}
	}
}