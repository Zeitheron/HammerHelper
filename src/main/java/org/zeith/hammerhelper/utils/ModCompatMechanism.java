package org.zeith.hammerhelper.utils;

import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiClassUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Query;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ModCompatMechanism
{
	public static final String[] LOAD_COMPAT = {
			"org.zeith.hammerlib.compat.base.BaseCompat.LoadCompat",
			"org.zeith.hammerlib.compat.base.ModCompat"
	};
	
	public static @Nullable PsiAnnotation findLoadCompat(PsiModifierListOwner element)
	{
		return PsiHelper.findFirstAnnotation(element, LOAD_COMPAT);
	}
	
	public static PsiClass getCompatType(PsiAnnotation loadCompat)
	{
		PsiClass compatType = PsiHelper.getClassFromPsiAnnotation(loadCompat, "compatType");
		if(compatType == null) compatType = PsiHelper.getClassFromPsiAnnotation(loadCompat, "type");
		return compatType;
	}
	
	private static final Key<Map<String, PsiClass>> MOD_CLASSES_RESULT = Key.create("hammerhelper mod map cache");
	
	public static Map<String, PsiClass> gatherModClasses(PsiElement anything, ProcessingContext ctx)
	{
		var m = ctx.get(MOD_CLASSES_RESULT);
		if(m != null) return m;
		ctx.put(MOD_CLASSES_RESULT, m = gatherModClasses(anything));
		return m;
	}
	
	public static Map<String, PsiClass> gatherModClasses(PsiElement anything)
	{
		JavaPsiFacade facade = JavaPsiFacade.getInstance(anything.getProject());
		
		GlobalSearchScope scope = GlobalSearchScope.everythingScope(anything.getProject());
		
		PsiClass mod = facade.findClass("net.neoforged.fml.common.Mod", scope);
		if(mod == null) mod = facade.findClass("net.minecraftforge.fml.common.Mod", scope);
		if(mod == null) return Map.of();
		
		Map<String, PsiClass> register = new HashMap<>();
		
		for(PsiReference pr : ReferencesSearch.search(mod).allowParallelProcessing())
		{
			if(!(pr.getElement() instanceof PsiJavaCodeReferenceElement jcre)) continue;
			if(!(jcre.getParent() instanceof PsiAnnotation annotation)) continue;
			String modid = PsiHelper.getAnnotationAttributeValue(annotation, "value", "");
			if(modid == null || modid.isBlank()) continue;
			PsiClass pc = PsiUtil.getContainingClass(annotation);
			if(pc == null) continue;
			register.put(modid, pc);
		}
		
		return Map.copyOf(register);
	}
}