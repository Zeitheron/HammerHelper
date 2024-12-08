package org.zeith.hammerhelper.quickfixes;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.daemon.QuickFixBundle;
import com.intellij.codeInsight.generation.*;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.featureStatistics.ProductivityFeatureNames;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.infos.CandidateInfo;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AddPacketExecuteQuickFix
		extends LocalQuickFixAndIntentionActionOnPsiElement
{
	public AddPacketExecuteQuickFix(@Nullable PsiElement aClass)
	{
		super(aClass);
	}
	
	@Override
	public boolean isAvailable(@NotNull Project project,
							   @NotNull PsiFile file,
							   @NotNull PsiElement startElement,
							   @NotNull PsiElement endElement)
	{
		return BaseIntentionAction.canModify(startElement);
	}
	
	@Override
	public @NotNull String getText()
	{
		return QuickFixBundle.message("implement.methods.fix");
	}
	
	@Override
	public @NotNull String getFamilyName()
	{
		return getText();
	}
	
	@Override
	public void invoke(@NotNull Project project,
					   @NotNull PsiFile file,
					   final @Nullable Editor editor,
					   @NotNull PsiElement startElement,
					   @NotNull PsiElement endElement)
	{
		final PsiElement myPsiElement = startElement;
		
		if(editor == null || !FileModificationService.getInstance().prepareFileForWrite(myPsiElement.getContainingFile())) return;
		
		boolean skipImplemented = true;
		PsiClass owner = myPsiElement instanceof PsiClass pc ? pc : null;
		
		if(owner == null && myPsiElement instanceof PsiEnumConstant)
		{
			owner = ((PsiEnumConstant) myPsiElement).getContainingClass();
			skipImplemented = ((PsiEnumConstant) myPsiElement).getInitializingClass() != null;
		}
		
		if(owner == null) return;
		
		final PsiClass psiClass = owner;
		
		chooseMethodsToImplement(editor, startElement,
				owner,
				skipImplemented, chooser ->
				{
					if(chooser == null) return;
					
					final List<PsiMethodMember> selectedElements = chooser.getSelectedElements();
					if(selectedElements == null || selectedElements.isEmpty()) return;
					
					final var o = chooser.getOptions();
					
					WriteCommandAction.writeCommandAction(project, file).run(() ->
							OverrideImplementUtil.overrideOrImplementMethods(psiClass, selectedElements, o.isCopyJavaDoc(), o.isInsertOverrideWherePossible())
									.forEach(gi ->
									{
										PsiElement anchor = gi.findInsertionAnchor(psiClass, startElement);
										gi.insert(psiClass, anchor, false);
									})
					);
				}
		);
	}
	
	@Override
	public boolean startInWriteAction()
	{
		return false;
	}
	
	protected static void chooseMethodsToImplement(
			Editor editor,
			PsiElement startElement,
			PsiClass aClass,
			boolean skipImplemented,
			Consumer<JavaOverrideImplementMemberChooser> callback)
	{
		FeatureUsageTracker.getInstance().triggerFeatureUsed(ProductivityFeatureNames.CODEASSISTS_OVERRIDE_IMPLEMENT);
		
		final Collection<CandidateInfo> overrideImplement =
				OverrideImplementExploreUtil.getMapToOverrideImplement(aClass, true, skipImplemented)
						.values()
						.stream()
						.filter(ci ->
								ci.getElement() instanceof PsiMethod m
								&& m.hasParameters()
								&& m.getName().toLowerCase(Locale.ROOT).contains("execute")
						)
						.collect(Collectors.toCollection(ArrayList::new));
		
		OverrideImplementUtil.showJavaOverrideImplementChooser(editor, startElement, true, overrideImplement, new ArrayList<>(), callback);
	}
	
	@Override
	public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file)
	{
		final PsiElement startElement = getStartElement();
		final PsiElement copy = PsiTreeUtil.findSameElementInCopy(startElement, file);
		final OverrideOrImplementOptions options = new OverrideOrImplementOptions()
		{
			@Override
			public boolean isInsertOverrideWherePossible()
			{
				return true;
			}
		};
		final Collection<CandidateInfo> overrideImplement;
		final PsiClass aClass;
		if(copy instanceof PsiEnumConstant enumConstant)
		{
			final PsiClass containingClass = enumConstant.getContainingClass();
			if(containingClass == null) return IntentionPreviewInfo.EMPTY;
			aClass = enumConstant.getOrCreateInitializingClass();
			overrideImplement = OverrideImplementExploreUtil.getMapToOverrideImplement(containingClass, true, false).values();
		} else
		{
			if(!(copy instanceof PsiClass psiClass)) return IntentionPreviewInfo.EMPTY;
			aClass = psiClass;
			overrideImplement = OverrideImplementExploreUtil.getMethodsToOverrideImplement(psiClass, true);
		}
		final List<PsiMethodMember> members = filterNonDefaultMethodMembers(overrideImplement);
		OverrideImplementUtil.overrideOrImplementMethodsInRightPlace(editor, aClass, members, options);
		return IntentionPreviewInfo.DIFF;
	}
	
	public static @NotNull List<PsiMethodMember> filterNonDefaultMethodMembers(Collection<CandidateInfo> overrideImplement)
	{
		return ContainerUtil.map(
				ContainerUtil.filter(overrideImplement,
						t -> t.getElement() instanceof PsiMethod method && !method.hasModifierProperty(PsiModifier.DEFAULT)
				),
				PsiMethodMember::new
		);
	}
}