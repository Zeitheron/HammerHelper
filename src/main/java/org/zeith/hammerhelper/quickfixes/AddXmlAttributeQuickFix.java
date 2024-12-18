package org.zeith.hammerhelper.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class AddXmlAttributeQuickFix
		implements LocalQuickFix
{
	private final Map<String, String> attributes;
	
	public AddXmlAttributeQuickFix(String... attributes)
	{
		this.attributes = Arrays.stream(attributes).collect(Collectors.toMap(UnaryOperator.identity(), s -> ""));
	}
	
	public AddXmlAttributeQuickFix(Map<String, String> attributes)
	{
		this.attributes = Map.copyOf(attributes);
	}
	
	@Override
	public @NotNull String getName()
	{
		return "Add %s misssing attributes".formatted(attributes.size());
	}
	
	@Override
	public @NotNull String getFamilyName()
	{
		return "XML Attribute Fixes";
	}
	
	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor)
	{
		if(descriptor.getPsiElement() instanceof XmlTag xmlTag)
		{
			for(var e : attributes.entrySet())
			{
				var key = e.getKey();
				if(xmlTag.getAttribute(key) == null)
					xmlTag.setAttribute(key, e.getValue());
			}
		}
	}
}