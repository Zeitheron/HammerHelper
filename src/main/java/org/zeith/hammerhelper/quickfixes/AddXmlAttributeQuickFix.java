package org.zeith.hammerhelper.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

public class AddXmlAttributeQuickFix
		implements LocalQuickFix
{
	private final String[] attributes;
	
	public AddXmlAttributeQuickFix(String... attributes)
	{
		this.attributes = attributes;
	}
	
	@Override
	public @NotNull String getName()
	{
		return "Add %s misssing attributes".formatted(attributes.length);
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
			for(String s : attributes)
			{
				if(xmlTag.getAttribute(s) == null)
					xmlTag.setAttribute(s, "");
			}
		}
	}
}