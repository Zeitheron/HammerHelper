package org.zeith.hammerhelper.contributors.hammerlib.flowgui;

import com.intellij.patterns.*;
import com.intellij.psi.*;
import com.intellij.psi.xml.*;
import com.intellij.util.Consumer;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.contributors.refs.ToVirtualFilesRefContributor;
import org.zeith.hammerhelper.utils.*;
import org.zeith.hammerhelper.utils.flowgui.*;
import org.zeith.hammerhelper.utils.hlide.FileRefByRegex;
import org.zeith.hammerhelper.utils.resources.ResourceLocator;

import java.util.ArrayList;
import java.util.List;

public class FlowguiXmlFileRefContributor
		extends PsiReferenceContributor
{
	public static final KeyedPrefixPath FLOWGUI = KeyedPrefixPath.of("flowgui");
	
	public static PsiFilePattern.Capture<PsiFile> flowguiXmlSource()
	{
		return PlatformPatterns.psiFile().with(new PsiFilePatternCondition());
	}
	
	@Override
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar reg)
	{
		var flowguiXmlFile = flowguiXmlSource();
		
		var xmlAttribValPattern = PlatformPatterns.psiElement(XmlAttributeValue.class).inFile(flowguiXmlFile);
		
		reg.registerReferenceProvider(xmlAttribValPattern, new PsiReferenceProvider()
				{
					@Override
					public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psi, @NotNull ProcessingContext context)
					{
						if(!(psi instanceof XmlAttributeValue val)) return PsiReference.EMPTY_ARRAY;
						if(!(val.getParent() instanceof XmlAttribute attrib)) return PsiReference.EMPTY_ARRAY;
						if(!(attrib.getParent() instanceof XmlTag tag)) return PsiReference.EMPTY_ARRAY;
						
						List<PsiReference> refs = new ArrayList<>();
						
						FlowguiModel model = FlowguiModel.fromProject(tag, context);
						if(model.getSpecs().isEmpty())
							return PsiReference.EMPTY_ARRAY;
						
						FlowguiModel.componentClass(tag, context).ifPresent(id ->
								contributeComponentString(id, attrib, val, context, refs::add)
						);
						
						String empty = model.findComponentIdFromTag("empty");
						
						switch(tag.getName())
						{
							case "root" ->
							{
								if(empty != null)
									contributeComponentString(empty, attrib, val, context, refs::add);
							}
							case "import" -> contributeImport(attrib, val, context, refs::add);
						}
						
						return refs.toArray(PsiReference[]::new);
					}
				}
		);
	}
	
	protected void contributeImport(XmlAttribute attrib, XmlAttributeValue cursor, @NotNull ProcessingContext context, Consumer<PsiReference> ref)
	{
		var field = attrib.getName();
		
		if("from".equals(field))
		{
			var vf = FlowguiLocator.findXmlFile(ResourceLocation.parse(attrib.getValue()), attrib, context);
			if(vf != null) ref.accept(new ToVirtualFilesRefContributor.VirtualFilePsiReference(cursor, vf));
		}
		
	}
	
	protected void contributeComponentString(String comId, XmlAttribute attrib, XmlAttributeValue cursor, @NotNull ProcessingContext context, Consumer<PsiReference> ref)
	{
		var field = attrib.getName();
		
		FlowguiModel model = FlowguiModel.fromProject(cursor, context);
		FlowguiComponentSpec spec = model.findSpec(comId);
		if(spec == null) return;
		
		if("class".equals(field) && "com".equalsIgnoreCase(attrib.getParent().getName()))
		{
			ref.accept(PsiReferenceBase.createSelfReference(cursor, spec.owner()));
			return;
		}
		
		FlowguiPropertySpec fieldSpec = spec.fields().get(field);
		if(fieldSpec != null)
		{
			List<FileRefByRegex> refs = fieldSpec.fileReferences();
			for(FileRefByRegex fr : refs)
			{
				var file = fr.resolve(attrib.getValue()).orElse(null);
				if(file == null) continue;
				var dst = ResourceLocator.findResourceInProject(attrib, file, context);
				if(dst != null)
					ref.accept(new ToVirtualFilesRefContributor.VirtualFilePsiReference(cursor, dst));
			}
		}
	}
	
	protected void contributeComponentName(XmlAttribute attrib, XmlAttributeValue cursor, @NotNull ProcessingContext context, Consumer<PsiReference> ref)
	{
		var field = attrib.getName();
		
		FlowguiModel model = FlowguiModel.fromProject(cursor, context);
		FlowguiComponentSpec spec = FlowguiModel.componentClass(attrib.getParent(), context).map(model::findSpec).orElse(null);
		if(spec == null) return;
		
		FlowguiPropertySpec fieldSpec = spec.fields().get(field);
		if(fieldSpec != null)
			ref.accept(PsiReferenceBase.createSelfReference(attrib, fieldSpec.owner()));
	}
	
	protected void refToComponentType(PsiElement cursor, String id, @NotNull ProcessingContext context, Consumer<PsiReference> ref)
	{
		FlowguiModel model = FlowguiModel.fromProject(cursor, context);
		FlowguiComponentSpec spec = model.findSpec(id);
		if(spec != null)
			ref.accept(PsiReferenceBase.createSelfReference(cursor, spec.owner()));
	}
	
	private static class PsiFilePatternCondition
			extends PatternCondition<PsiFile>
	{
		public PsiFilePatternCondition()
		{
			super("PsiFilePatternCondition");
		}
		
		@Override
		public boolean accepts(@NotNull PsiFile file, ProcessingContext context)
		{
			return FileHelper.findAssetRoot(file, context, FLOWGUI) != null;
		}
	}
}