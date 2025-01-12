package org.zeith.hammerhelper.utils.hlide;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifierListOwner;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.zeith.hammerhelper.utils.PsiHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class HammerLibIDE
{
	public static final String PACKAGE = "org.zeith.hammerlib.annotations.ide.";
	
	public static final String FileReference = PACKAGE + "FileReference";
	public static final String AllowedValues = PACKAGE + "AllowedValues";
	public static final String Required = PACKAGE + "Required";
	public static final String AllowJS = PACKAGE + "AllowJS";
	public static final String Suggestions = PACKAGE + "Suggestions";
	
	public static List<FileRefByRegex> getFileReferences(PsiModifierListOwner element)
	{
		PsiAnnotation ref = PsiHelper.findFirstAnnotation(element, FileReference);
		if(ref == null) return List.of();
		
		List<String> regex = PsiHelper.getAnnotationAttributeValueList(ref, "regex", List.of());
		List<String> value = PsiHelper.getAnnotationAttributeValueList(ref, "value", List.of());
		
		List<FileRefByRegex> refs = new ArrayList<>();
		
		for(int i = 0, len = Math.min(regex.size(), value.size()); i < len; i++)
		{
			try
			{
				refs.add(new FileRefByRegex(Pattern.compile(regex.get(i)), value.get(i)));
			} catch(Exception e)
			{
				log.error("Failed to compile regex {}", regex.get(i));
			}
		}
		
		return List.copyOf(refs);
	}
	
	public static List<Pattern> getAllowedValues(PsiModifierListOwner element)
	{
		PsiAnnotation ref = PsiHelper.findFirstAnnotation(element, AllowedValues);
		if(ref == null) return List.of();
		
		List<String> value = PsiHelper.getAnnotationAttributeValueList(ref, "value", List.of());
		
		List<Pattern> refs = new ArrayList<>();
		
		for(String s : value)
		{
			try
			{
				refs.add(Pattern.compile(s));
			} catch(Exception e)
			{
				log.error("Failed to compile allowed value regex {}", s);
			}
		}
		
		return List.copyOf(refs);
	}
	
	public static String getNamespace(PsiModifierListOwner element)
	{
		return getValue(element,
				"minecraft",
				PACKAGE + "Namespace"
		);
	}
	
	public static String getDefault(PsiModifierListOwner element)
	{
		return getValue(element,
				"",
				PACKAGE + "Default"
		);
	}
	
	public static boolean isRequired(PsiModifierListOwner element)
	{
		return PsiHelper.findFirstAnnotation(element, Required) != null;
	}
	
	public static boolean isJsAllowed(PsiModifierListOwner element)
	{
		return PsiHelper.findFirstAnnotation(element, AllowJS) != null;
	}
	
	public static List<String> getSuggestions(PsiModifierListOwner element)
	{
		return getValues(element, List.of(), Suggestions);
	}
	
	public static String getValue(PsiModifierListOwner element, String defaultValue, String... annotationTypes)
	{
		return PsiHelper.getAnnotationAttributeValue(
				PsiHelper.findFirstAnnotation(element, annotationTypes),
				"value",
				defaultValue
		);
	}
	
	public static @NotNull List<String> getValues(PsiModifierListOwner element, List<String> defaultValue, String... annotationTypes)
	{
		return PsiHelper.getAnnotationAttributeValueList(
				PsiHelper.findFirstAnnotation(element, annotationTypes),
				"value",
				defaultValue
		);
	}
}