package org.zeith.hammerhelper.contributors.hammerlib.ref;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.contributors.refs.ToVirtualFilesRefContributor;
import org.zeith.hammerhelper.utils.*;
import org.zeith.hammerhelper.utils.flowgui.FlowguiModel;

import java.util.function.Consumer;

import static org.zeith.hammerhelper.utils.FileHelper.getRecursive;

public class XmlFlowguiRefContrib
		extends ToVirtualFilesRefContributor
{
	@Override
	protected void fetchReferences(@NotNull PsiElement element, @NotNull ProcessingContext context, @NotNull Consumer<VirtualFile> fileConsumer)
	{
		if(!(element instanceof PsiLiteralExpression literal)
		   || !(literal.getParent() instanceof PsiNameValuePair pair)
		   || !"value".equals(pair.getAttributeName())
		) return;
		
		var an = PsiHelper.getContainingAnnotation(element);
		if(an == null || !PsiHelper.isOneOf(an, FlowguiModel.XML_FLOWGUI)) return;
		
		String field = PsiHelper.getAnnotationAttributeValue(an, "value", "");
		if(field.isBlank()) return;
		
		forEachAssetsNamespace(element, namespace ->
				{
					var sub = getRecursive(namespace, "flowgui");
					if(sub == null) return;
					sub = getRecursive(sub, (field + ".xml").split("/"));
					fileConsumer.accept(sub);
				}
		);
	}
	
	protected void forEachAssetsNamespace(PsiElement element, Consumer<VirtualFile> namespaceHandler)
	{
		for(var namespace : FileHelper.getAllAssetNamespaces(element.getContainingFile()))
			namespaceHandler.accept(namespace.file());
	}
	
	protected PsiAnnotation getAnnotationContext(PsiElement position)
	{
		if(!(position.getParent().getParent() instanceof PsiNameValuePair pair)
		   || !"value".equals(pair.getAttributeName())
		) return null;
		
		if(position.getParent().getParent().getParent() instanceof PsiAnnotationParameterList apr
		   && apr.getParent() instanceof PsiAnnotation annotation
		   && PsiHelper.isOneOf(annotation, FlowguiModel.XML_FLOWGUI))
			return annotation;
		
		return null;
	}
}