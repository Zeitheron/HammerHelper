package org.zeith.hammerhelper.contributors.hammerlib.flowgui;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.FileHelper;
import org.zeith.hammerhelper.utils.flowgui.*;

import java.util.*;

import static org.zeith.hammerhelper.contributors.hammerlib.flowgui.FlowguiXmlFileRefContributor.FLOWGUI;
import static org.zeith.hammerhelper.utils.FileHelper.getRecursive;

public class FlowguiXmlFileContributor
		extends CompletionContributor
{
	public FlowguiXmlFileContributor()
	{
		extend(CompletionType.BASIC,
				PlatformPatterns.psiElement(XmlToken.class),
				new CompletionProvider<>()
				{
					@Override
					protected void addCompletions(@NotNull CompletionParameters parameters,
												  @NotNull ProcessingContext context,
												  @NotNull CompletionResultSet result
					)
					{
						PsiElement element = parameters.getPosition();
						if(!(element instanceof XmlToken token))
							return;
						
						if(FileHelper.findAssetRoot(parameters.getOriginalFile(), context, FLOWGUI) == null)
							return;
						
						var par = token.getParent();
						
						FlowguiModel model = FlowguiModel.fromProject(par, context);
						if(model.getSpecs().isEmpty())
							return;
						
						if(par instanceof XmlAttributeValue val && val.getParent() instanceof XmlAttribute attrib)
						{
							var tag = attrib.getParent();
							
							var clazz = FlowguiModel.componentClass(attrib.getParent(), context).orElse(null);
							
							if(tag.getName().equals("import"))
								importProcessing(val, attrib, context, result, model, clazz);
							else
								componentProcessing(val, attrib, context, result, model, clazz);
							
							result.stopHere();
						} else if(par instanceof XmlAttribute attrib)
						{
							var tag = attrib.getParent();
							var clazz = FlowguiModel.componentClass(tag, context).orElse(null);
							
							clazz = switch(tag.getName())
							{
								case "import", "root" -> model.findComponentIdFromTag("empty");
								default -> clazz;
							};
							
							comKeyProcessing(attrib.getParent(), result, model, clazz);
						}
					}
				}
		);
	}
	
	public void comKeyProcessing(
			XmlTag tag,
			@NotNull CompletionResultSet result,
			@NotNull FlowguiModel model,
			String clazz
	)
	{
		FlowguiComponentSpec spec = model.findSpec(clazz);
		if(spec == null) return;
		List<LookupElementBuilder> list = new ArrayList<>();
		Set<String> suggested = new HashSet<>();
		
		var keysSorted = spec.fields().entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.toList();
		
		for(var e : keysSorted)
		{
			var s = e.getKey();
			if("class".equals(s) || tag.getAttribute(s) != null || !suggested.add(s)) continue;
			list.add(LookupElementBuilder.create(s));
		}
		
		result.addAllElements(list);
	}
	
	public void componentProcessing(XmlAttributeValue cursor, XmlAttribute attrib,
									@NotNull ProcessingContext context,
									@NotNull CompletionResultSet result,
									@NotNull FlowguiModel model,
									String clazz
	)
	{
		var an = attrib.getName();
		var tag = attrib.getParent();
		if("class".equals(an) && "com".equals(tag.getName()))
		{
			result.addAllElements(model.getSpecs().stream().map(LookupElementBuilder::create).toList());
			return;
		}
		
		var spec = model.findSpec(clazz);
		FlowguiPropertySpec field;
		if(spec != null && (field = spec.fields().get(an)) != null)
			result.addAllElements(field.suggestions().stream().map(LookupElementBuilder::create).toList());
	}
	
	public void importProcessing(XmlAttributeValue cursor, XmlAttribute attrib,
								 @NotNull ProcessingContext context,
								 @NotNull CompletionResultSet result,
								 @NotNull FlowguiModel model,
								 String clazz
	)
	{
		var tag = attrib.getParent();
		if("from".equals(attrib.getName()))
		{
			result.addAllElements(
					getXmlFiles(cursor.getContainingFile())
							.stream()
							.map(LookupElementBuilder::create)
							.toList()
			);
			return;
		}
	}
	
	
	private List<String> getXmlFiles(PsiFile file)
	{
		List<String> names = new ArrayList<>();
		
		for(var namespace : FileHelper.getAllAssetNamespaces(file))
		{
			var sub = getRecursive(namespace.file(), "flowgui");
			names.addAll(FileHelper.getRecursiveFilesNamesWithFullPathFromDirectory(sub)
					.stream()
					.filter(s -> s.endsWith(".xml"))
					.map(s -> namespace.name() + ":" + s.substring(0, s.lastIndexOf(".")))
					.toList()
			);
		}
		
		return names;
	}
}