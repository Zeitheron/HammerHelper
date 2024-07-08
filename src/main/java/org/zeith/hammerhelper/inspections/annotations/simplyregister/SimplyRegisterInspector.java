package org.zeith.hammerhelper.inspections.annotations.simplyregister;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SimplyRegisterInspector
		extends LocalInspectionTool
{
	@Override
	public abstract @NotNull JavaElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session);
}