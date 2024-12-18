package org.zeith.hammerhelper.utils.flowgui;

import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.ProcessingContext;
import org.zeith.hammerhelper.utils.PsiHelper;
import org.zeith.hammerhelper.utils.ResourceLocation;
import org.zeith.hammerhelper.utils.hlide.HammerLibIDE;

import java.util.HashMap;
import java.util.Map;

public class FlowguiModel
{
	private static final Key<FlowguiModel> MOD_CLASSES_RESULT = Key.create("hammerhelper flowgui model cache");
	
	private final Map<String, FlowguiComponentSpec> specs = new HashMap<>();
	
	public static final String[] XML_FLOWGUI = {
			"org.zeith.hammerlib.client.flowgui.reader.XmlFlowgui"
	};
	
	public void register(ResourceLocation id, FlowguiComponentSpec spec)
	{
		specs.putIfAbsent(id.toString(), spec);
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
		
		PsiClass readerAnnotation = facade.findClass("org.zeith.hammerlib.client.flowgui.reader.FlowguiReader", scope);
		if(readerAnnotation == null) return model;
		
		for(PsiReference pr : ReferencesSearch.search(readerAnnotation).allowParallelProcessing())
		{
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