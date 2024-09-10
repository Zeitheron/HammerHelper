package org.zeith.hammerhelper.utils;

import com.intellij.psi.*;
import org.jetbrains.annotations.Nullable;

public class SimplyRegisterMechanism
{
	public static final String[] SIMPLY_REGISTER = {
			"org.zeith.hammerlib.annotations.SimplyRegister",
			"com.zeitheron.hammercore.annotations.SimplyRegister"
	};
	
	public static final String[] REGISTRY_NAME = {
			"org.zeith.hammerlib.annotations.RegistryName",
			"com.zeitheron.hammercore.annotations.RegistryName"
	};
	
	public static final String[] REF = {
			"org.zeith.hammerlib.annotations.Ref"
	};
	
	public static @Nullable PsiAnnotation findSimplyRegister(PsiModifierListOwner element)
	{
		return PsiHelper.findFirstAnnotation(element, SIMPLY_REGISTER);
	}
	
	public static @Nullable PsiAnnotation findRegistryName(PsiModifierListOwner element)
	{
		return PsiHelper.findFirstAnnotation(element, REGISTRY_NAME);
	}
	
	public static @Nullable String getRegistryPath(PsiField field)
	{
		var prefix = "";
		if(field.getParent() instanceof PsiClass psiClass)
		{
			var sr = SimplyRegisterMechanism.findSimplyRegister(psiClass);
			var v = PsiHelper.getAnnotationAttributeValue(sr, "prefix", "");
			if(v != null) prefix = v;
		}
		
		var rn = SimplyRegisterMechanism.findRegistryName(field);
		var rnStr = PsiHelper.getAnnotationAttributeValue(rn, "value", "");
		if(rnStr == null || rnStr.isBlank()) return null;
		
		return prefix + rnStr;
	}
}