package org.zeith.hammerhelper.utils.flowgui;

import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.*;
import org.zeith.hammerhelper.utils.hlide.HammerLibIDE;

import java.util.*;

public class FlowguiModel
{
	private static final Key<FlowguiModel> MOD_CLASSES_RESULT = Key.create("hammerhelper flowgui model cache");
	
	private final Map<String, FlowguiComponentSpec> specs = new HashMap<>();
	private final Map<String, String> predefinedTagComponents = new HashMap<>();
	
	public static final String[] XML_FLOWGUI = {
			"org.zeith.hammerlib.client.flowgui.reader.XmlFlowgui"
	};
	
	public static Optional<String> componentClass(XmlTag tag, @NotNull ProcessingContext context)
	{
		FlowguiModel model = fromProject(tag, context);
		String classValue = tag.getAttributeValue("class");
		if(classValue == null || classValue.isBlank())
		{
			String comType = model.findComponentIdFromTag(tag.getName());
			if(comType != null) classValue = comType;
		}
		return Optional.ofNullable(classValue);
	}
	
	public Set<String> getSpecs()
	{
		return specs.keySet();
	}
	
	public void register(ResourceLocation id, FlowguiComponentSpec spec)
	{
		specs.putIfAbsent(id.toString(), spec);
	}
	
	public void registerTagComponent(String tag, String id)
	{
		predefinedTagComponents.putIfAbsent(tag, id);
	}
	
	public String findComponentIdFromTag(String tag)
	{
		return predefinedTagComponents.get(tag);
	}
	
	public FlowguiComponentSpec findSpec(String id)
	{
		return specs.get(id);
	}
	
	public static FlowguiModel fromProject(PsiElement anything, ProcessingContext ctx)
	{
		var m = ctx.get(MOD_CLASSES_RESULT);
		if(m != null) return m;
		ctx.put(MOD_CLASSES_RESULT, m = fromProject(anything));
		return m;
	}
	
	public static FlowguiModel fromProject(PsiElement anything)
	{
		JavaPsiFacade facade = JavaPsiFacade.getInstance(anything.getProject());
		
		GlobalSearchScope scope = GlobalSearchScope.everythingScope(anything.getProject());
		
		FlowguiModel model = new FlowguiModel();
		
		PsiClass flowguiTags = facade.findClass("org.zeith.hammerlib.client.flowgui.reader.FlowguiTags", scope);
		if(flowguiTags != null)
			for(PsiField field : flowguiTags.getFields())
			{
				var mods = field.getModifierList();
				if(mods != null
						&& field.getName().startsWith("COM_")
						&& mods.hasExplicitModifier(PsiModifier.STATIC)
						&& mods.hasExplicitModifier(PsiModifier.FINAL)
						&& field.getType().equalsToText("java.lang.String")
				)
				{
					String tagType = field.getName().substring(4);
					String value = PsiHelper.getExpressionStringRepresentation(field.getInitializer(), "");
					model.registerTagComponent(tagType, value);
				}
			}
		
		PsiClass readerAnnotation = facade.findClass("org.zeith.hammerlib.client.flowgui.reader.FlowguiReader", scope);
		if(readerAnnotation == null) return model;
		
		for(PsiReference pr : ReferencesSearch.search(readerAnnotation).findAll())
		{
			if(pr == null) continue;
			if(!(pr.getElement() instanceof PsiJavaCodeReferenceElement jcre)) continue;
			if(!(jcre.getParent() instanceof PsiAnnotation annotation)) continue;
			String identifier = PsiHelper.getAnnotationAttributeValue(annotation, "value", "");
			if(identifier == null || identifier.isBlank()) continue;
			PsiClass pc = PsiUtil.getContainingClass(annotation);
			if(pc == null) continue;
			var id = new ResourceLocation(HammerLibIDE.getNamespace(pc), identifier);
			model.register(id, FlowguiComponentSpec.fromPsi(id, pc));
		}
		
		return model;
	}
}