package org.zeith.hammerhelper.utils.flowgui;

import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerhelper.utils.PsiHelper;
import org.zeith.hammerhelper.utils.hlide.FileRefByRegex;
import org.zeith.hammerhelper.utils.hlide.HammerLibIDE;

import java.util.List;
import java.util.regex.Pattern;

public record FlowguiPropertySpec(
		PsiField owner,
		@Nullable String defaultValue,
		boolean required, boolean allowJs,
		@NotNull List<FileRefByRegex> fileReferences,
		@NotNull List<Pattern> allowedValues
)
{
	public static final Pattern JS_START = Pattern.compile("^\\s*\\([^)]*\\)\\s*=>\\s*");
	
	public static boolean isJSCode(String input)
	{
		return JS_START.matcher(input).find();
	}
	
	public boolean treatAsJs(String input)
	{
		return input != null && allowJs() && isJSCode(input);
	}
	
	public static FlowguiPropertySpec fromPsi(PsiField field)
	{
		var def = HammerLibIDE.getDefault(field);
		if(def.isBlank()) def = null;
		
		var required = PsiHelper.findFirstAnnotation(field, HammerLibIDE.Required);
		if(def == null || def.isBlank()) def = PsiHelper.getAnnotationAttributeValue(
				required,
				"value",
				""
		);
		
		return new FlowguiPropertySpec(field,
				def,
				required != null,
				HammerLibIDE.isJsAllowed(field),
				HammerLibIDE.getFileReferences(field),
				HammerLibIDE.getAllowedValues(field)
		);
	}
}