package org.zeith.hammerhelper.contributors.hammerlib.flowgui;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.xml.*;
import com.intellij.util.Consumer;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.contributors.refs.ToVirtualFilesRefContributor;
import org.zeith.hammerhelper.utils.*;
import org.zeith.hammerhelper.utils.flowgui.*;
import org.zeith.hammerhelper.utils.hlide.FileRefByRegex;

import java.util.*;

import static org.zeith.hammerhelper.utils.FileHelper.getRecursive;

public class FlowguiXmlFileContributor
		extends PsiReferenceContributor
{
	public static final KeyedPrefixPath FLOWGUI = KeyedPrefixPath.of("flowgui");
	
	@Override
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar reg)
	{
		var flowguiXmlFile = PlatformPatterns.psiFile().with(new PsiFilePatternCondition());
		
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
						
						componentClass(tag, context).ifPresent(id ->
								contributeComponentString(id, attrib, val, context, refs::add)
						);
						
						switch(tag.getName())
						{
							case "import" -> contributeImport(attrib, val, context, refs::add);
						}
						
						return refs.toArray(PsiReference[]::new);
					}
				}
		);
	}
	
	public static Optional<String> componentClass(XmlTag tag, @NotNull ProcessingContext context)
	{
		FlowguiModel model = FlowguiModel.fromProject(tag, context);
		String classValue = tag.getAttributeValue("class");
		if(classValue == null || classValue.isBlank())
		{
			String comType = model.findComponentIdFromTag(tag.getName());
			if(comType != null) classValue = comType;
		}
		return Optional.ofNullable(classValue);
	}
	
	protected void contributeImport(XmlAttribute attrib, XmlAttributeValue cursor, @NotNull ProcessingContext context, Consumer<PsiReference> ref)
	{
		var field = attrib.getName();
		
		if("from".equals(field))
		{
			var id = ResourceLocation.parse(attrib.getValue());
			var path = ("assets/" + id.namespace() + "/flowgui/" + id.path() + ".xml").split("/");
			
			VirtualFile module = FileHelper.getResourcesDirectory(attrib.getContainingFile().getOriginalFile());
			
			var dst = getRecursive(module, path);
			if(dst != null)
				ref.accept(new ToVirtualFilesRefContributor.VirtualFilePsiReference(cursor, dst));
			
			return;
		}
		
	}
	
	protected void contributeComponentString(String comId, XmlAttribute attrib, XmlAttributeValue cursor, @NotNull ProcessingContext context, Consumer<PsiReference> ref)
	{
		var field = attrib.getName();
		
		FlowguiModel model = FlowguiModel.fromProject(cursor, context);
		FlowguiComponentSpec spec = model.findSpec(comId);
		if(spec == null) return;
		
		if("class".equals(field))
		{
			ref.accept(PsiReferenceBase.createSelfReference(cursor, spec.owner()));
			return;
		}
		
		VirtualFile module = FileHelper.getModuleDirectory(attrib.getContainingFile().getOriginalFile());
		
		FlowguiPropertySpec fieldSpec = spec.fields().get(field);
		if(fieldSpec != null)
		{
			List<FileRefByRegex> refs = fieldSpec.fileReferences();
			for(FileRefByRegex fr : refs)
			{
				var file = fr.resolve(attrib.getValue()).orElse(null);
				if(file == null) continue;
				var dst = FileHelper.getRecursive(module, file.split("/"));
				if(dst != null)
					ref.accept(new ToVirtualFilesRefContributor.VirtualFilePsiReference(cursor, dst));
			}
		}
	}
	
	protected void contributeComponentName(XmlAttribute attrib, XmlAttributeValue cursor, @NotNull ProcessingContext context, Consumer<PsiReference> ref)
	{
		var field = attrib.getName();
		
		FlowguiModel model = FlowguiModel.fromProject(cursor, context);
		FlowguiComponentSpec spec = componentClass(attrib.getParent(), context).map(model::findSpec).orElse(null);
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