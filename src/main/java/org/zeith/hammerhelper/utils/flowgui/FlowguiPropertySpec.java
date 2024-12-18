package org.zeith.hammerhelper.utils.flowgui;

import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerhelper.utils.hlide.FileRefByRegex;
import org.zeith.hammerhelper.utils.hlide.HammerLibIDE;

import java.util.List;
import java.util.regex.Pattern;

public record FlowguiPropertySpec(
		@Nullable String defaultValue,
		boolean required, boolean allowJs,
		@NotNull List<FileRefByRegex> fileReferences,
		@NotNull List<Pattern> allowedValues
)
{
	public static FlowguiPropertySpec fromPsi(PsiField field)
	{
		var def = HammerLibIDE.getDefault(field);
		if(def.isBlank()) def = null;
		return new FlowguiPropertySpec(
				def,
				HammerLibIDE.isRequired(field),
				HammerLibIDE.isJsAllowed(field),
				HammerLibIDE.getFileReferences(field),
				HammerLibIDE.getAllowedValues(field)
		);
	}
}