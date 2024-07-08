package org.zeith.hammerhelper.utils;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifierListOwner;
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
	
	public static @Nullable PsiAnnotation findSimplyRegister(PsiModifierListOwner element)
	{
		return PsiHelper.findFirstAnnotation(element, SIMPLY_REGISTER);
	}
	
	public static @Nullable PsiAnnotation findRegistryName(PsiModifierListOwner element)
	{
		return PsiHelper.findFirstAnnotation(element, REGISTRY_NAME);
	}
}