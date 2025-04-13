package org.zeith.hammerhelper.utils.flowgui;

import com.intellij.psi.*;
import org.zeith.hammerhelper.utils.*;

import java.util.*;

public record FlowguiComponentSpec(PsiClass owner, ResourceLocation id, Map<String, FlowguiPropertySpec> fields)
{
	public static FlowguiComponentSpec fromPsi(ResourceLocation id, PsiClass psi)
	{
		Map<String, FlowguiPropertySpec> fields = new LinkedHashMap<>();
		
		for(PsiField f : psi.getAllFields())
		{
			var mods = f.getModifierList();
			if(mods == null
					|| !mods.hasExplicitModifier(PsiModifier.PUBLIC)
					|| !mods.hasExplicitModifier(PsiModifier.STATIC)
					|| !mods.hasExplicitModifier(PsiModifier.FINAL)
					|| !f.getType().equalsToText("java.lang.String")
					|| !f.getName().startsWith("KEY_")
			) continue;
			
			var xmlName = PsiHelper.getExpressionStringRepresentation(f.getInitializer(), "");
			if(xmlName.isBlank()) continue;
			
			var spec = FlowguiPropertySpec.fromPsi(f);
			if(spec != null) fields.put(xmlName, spec);
		}
		
		return new FlowguiComponentSpec(psi, id, Collections.unmodifiableMap(fields));
	}
}