package org.zeith.hammerhelper.utils.flowgui;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import org.zeith.hammerhelper.utils.PsiHelper;
import org.zeith.hammerhelper.utils.ResourceLocation;

import java.util.*;

public record FlowguiComponentSpec(PsiClass owner, ResourceLocation id, Map<String, FlowguiPropertySpec> fields)
{
	public static FlowguiComponentSpec fromPsi(ResourceLocation id, PsiClass psi)
	{
		Map<String, FlowguiPropertySpec> fields = new LinkedHashMap<>();
		
		for(PsiField f : psi.getAllFields())
		{
			if(!f.hasModifier(JvmModifier.PUBLIC)
			   || !f.hasModifier(JvmModifier.STATIC)
			   || !f.hasModifier(JvmModifier.FINAL)
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