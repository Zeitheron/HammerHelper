package org.zeith.hammerhelper.multihost.flowgui;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SharedProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.FileHelper;
import org.zeith.hammerhelper.utils.flowgui.*;

import java.util.List;

import static org.zeith.hammerhelper.contributors.hammerlib.flowgui.FlowguiXmlFileRefContributor.FLOWGUI;

// Sadly this is for ultimate version only, but for those who have it -- enjoy!
public class JsFieldHostInjector
		implements MultiHostInjector
{
	private Language language;
	
	public JsFieldHostInjector()
	{
		language = Language.findInstancesByMimeType("application/javascript").stream().findFirst().orElse(null);
	}
	
	@Override
	public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context)
	{
		if(language == null
		   || !(context instanceof XmlAttributeValue val)
		   || !(val.getParent() instanceof XmlAttribute attrib)
		   || !(val instanceof PsiLanguageInjectionHost host)) return;
		
		var tag = attrib.getParent();
		
		ProcessingContext ctx = new ProcessingContext(new SharedProcessingContext());
		if(FileHelper.findAssetRoot(context.getContainingFile(), ctx, FLOWGUI) == null) return;
		var clazz = FlowguiModel.componentClass(tag, ctx).orElse(null);
		if(clazz == null) return;
		
		var model = FlowguiModel.fromProject(context, ctx);
		FlowguiComponentSpec spec = model.findSpec(clazz);
		FlowguiPropertySpec prop;
		if(spec == null || (prop = spec.fields().get(attrib.getName())) == null || !prop.treatAsJs(val.getValue())) return;
		
		// Adjust range inside host to exclude quotes
		TextRange hostRange = host.getTextRange();
		int startOffset = hostRange.getStartOffset(); // Host's starting offset in the file
		int contentStart = startOffset + 1; // Skip the opening quote
		int contentEnd = hostRange.getEndOffset() - 1; // Skip the closing quote
		
		if(contentStart < contentEnd)
		{ // Ensure valid range
			TextRange rangeInsideHost = new TextRange(1, contentEnd - contentStart + 1);
			
			registrar.startInjecting(language)
					.addPlace(null, null, host, rangeInsideHost)
					.doneInjecting();
		}
	}
	
	
	@Override
	public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn()
	{
		return List.of(XmlAttributeValue.class);
	}
}