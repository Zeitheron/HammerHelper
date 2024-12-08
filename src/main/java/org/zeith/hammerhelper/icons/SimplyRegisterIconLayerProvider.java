package org.zeith.hammerhelper.icons;

import com.intellij.ide.IconLayerProvider;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerhelper.utils.SimplyRegisterMechanism;

import javax.swing.*;

public class SimplyRegisterIconLayerProvider
		implements IconLayerProvider
{
	@Override
	public @Nullable Icon getLayerIcon(@NotNull Iconable element, boolean isLocked)
	{
		if(element instanceof PsiClass pc && SimplyRegisterMechanism.findSimplyRegister(pc) != null)
			return HLIcons.SIMPLY_REGISTER;
		return null;
	}
	
	@Override
	public @NotNull String getLayerDescription()
	{
		return "Simply Register Class";
	}
}