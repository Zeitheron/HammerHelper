package org.zeith.hammerhelper.inspections.xml;

import com.intellij.codeInspection.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SharedProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.contributors.hammerlib.flowgui.FlowguiXmlFileContributor;
import org.zeith.hammerhelper.contributors.refs.ToVirtualFilesRefContributor;
import org.zeith.hammerhelper.quickfixes.AddXmlAttributeQuickFix;
import org.zeith.hammerhelper.utils.FileHelper;
import org.zeith.hammerhelper.utils.ResourceLocation;
import org.zeith.hammerhelper.utils.flowgui.*;

import java.util.*;
import java.util.regex.Pattern;

import static org.zeith.hammerhelper.contributors.hammerlib.flowgui.FlowguiXmlFileContributor.FLOWGUI;
import static org.zeith.hammerhelper.utils.FileHelper.getRecursive;

public class FlowguiInspection
		extends LocalInspectionTool
{
	@Override
	public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session)
	{
		return new XmlElementVisitor()
		{
			@Override
			public void visitXmlTag(@NotNull XmlTag tag)
			{
				ProcessingContext ctx = new ProcessingContext(new SharedProcessingContext());
				
				if(FileHelper.findAssetRoot(tag.getContainingFile(), ctx, FLOWGUI) == null) return;
				
				FlowguiModel model = FlowguiModel.fromProject(tag, ctx);
				
				final var HL_EMPTY = model.findComponentIdFromTag("empty");
				
				switch(tag.getName())
				{
					case "root" ->
					{
						inspectTag(tag, model, model.findSpec(HL_EMPTY), HL_EMPTY, holder);
						return;
					}
					case "import" ->
					{
						XmlAttribute from = tag.getAttribute("from");
						if(from == null)
						{
							holder.registerProblem(tag, "Missing required attribute: %s".formatted("from"), ProblemHighlightType.ERROR, new AddXmlAttributeQuickFix("from"));
						} else
						{
							var id = ResourceLocation.parse(from.getValue());
							var path = ("assets/" + id.namespace() + "/flowgui/" + id.path() + ".xml").split("/");
							
							VirtualFile module = FileHelper.getResourcesDirectory(from.getContainingFile().getOriginalFile());
							
							var dst = getRecursive(module, path);
							if(dst == null || !dst.exists())
							{
								PsiElement el = from.getValueElement();
								if(el == null) el = from;
								holder.registerProblem(el, "The import could not be resolved.", ProblemHighlightType.ERROR);
							}
						}
						
						inspectTag(tag, model, model.findSpec(HL_EMPTY), HL_EMPTY, holder);
						return;
					}
				}
				
				var clazz = FlowguiXmlFileContributor.componentClass(tag, ctx).orElse(null);
				if(clazz == null) return;
				
				var spec = model.findSpec(clazz);
				if(spec == null) return;
				
				inspectTag(tag, model, spec, clazz, holder);
			}
		};
	}
	
	protected void inspectTag(XmlTag tag, FlowguiModel model, FlowguiComponentSpec spec, String clazz, ProblemsHolder holder)
	{
		Set<String> missingFields = new LinkedHashSet<>();
		
		for(Map.Entry<String, FlowguiPropertySpec> entry : spec.fields().entrySet())
		{
			String name = entry.getKey();
			var valueAttrib = tag.getAttribute(name);
			FlowguiPropertySpec propSpec = entry.getValue();
			
			if(valueAttrib == null && propSpec.required() && !"class".equals(name))
				missingFields.add(name);
			
			if(valueAttrib != null && !propSpec.treatAsJs(valueAttrib.getValue()))
			{
				var val = valueAttrib.getValue();
				
				var allowed = propSpec.allowedValues();
				if(!allowed.isEmpty())
				{
					boolean matched = false;
					for(Pattern pattern : allowed)
					{
						if(pattern.asMatchPredicate().test(val))
						{
							matched = true;
						}
					}
					if(!matched)
					{
						PsiElement el = valueAttrib.getValueElement();
						if(el == null) el = valueAttrib;
						holder.registerProblem(el, "Value is not allowed.", ProblemHighlightType.ERROR);
					}
				}
			}
		}
		
		var qf = new AddXmlAttributeQuickFix(missingFields.toArray(String[]::new));
		for(String name : missingFields)
			holder.registerProblem(tag, "Missing required attribute: %s".formatted(name), ProblemHighlightType.ERROR, qf);
	}
}