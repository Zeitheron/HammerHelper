package org.zeith.hammerhelper.utils;

import com.intellij.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PsiHelper
{
	public static boolean instanceOf(PsiClass psiClass, Set<String> possibleTypes)
	{
		if(psiClass == null) return false;
		if(possibleTypes.contains(psiClass.getQualifiedName())) return true;
		return instanceOf(psiClass.getSuperClass(), possibleTypes)
			   || Arrays.stream(psiClass.getInterfaces()).anyMatch(itf -> instanceOf(itf, possibleTypes));
	}
	
	public static String findInstance(PsiClass psiClass, Set<String> possibleTypes)
	{
		if(psiClass == null) return null;
		if(possibleTypes.contains(psiClass.getQualifiedName())) return psiClass.getQualifiedName();
		var qn = findInstance(psiClass.getSuperClass(), possibleTypes);
		if(qn != null) return qn;
		return Arrays.stream(psiClass.getInterfaces())
				.map(itf -> findInstance(itf, possibleTypes))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}
	
	public static @Nullable PsiAnnotation findFirstAnnotation(PsiModifierListOwner element, String... annotations)
	{
		for(String an : annotations)
		{
			var a = element.getAnnotation(an);
			if(a != null) return a;
		}
		return null;
	}
	
	public static @Nullable String getAnnotationAttributeValue(PsiAnnotation annotation, String attributeName, String defaultValue)
	{
		if(annotation == null) return defaultValue;
		PsiAnnotationMemberValue value = annotation.findAttributeValue(attributeName);
		return getExpressionStringRepresentation(value, defaultValue);
	}
	
	public static String getExpressionStringRepresentation(PsiAnnotationMemberValue value, String defaultValue)
	{
		if(value instanceof PsiLiteralExpression literalExpression)
			return literalExpression.getValue() instanceof String s ? s : defaultValue;
		if(value instanceof PsiBinaryExpression binaryExpression && binaryExpression.getOperationSign().toString().endsWith(":PLUS"))
		{
			var left = getExpressionStringRepresentation(binaryExpression.getLOperand(), null);
			if(left == null) return defaultValue;
			
			var right = getExpressionStringRepresentation(binaryExpression.getROperand(), null);
			if(right == null) return defaultValue;
			
			return left + right;
		}
		if(value instanceof PsiPolyadicExpression polyadicExpression)
		{
			StringBuilder sb = new StringBuilder();
			for(PsiExpression op : polyadicExpression.getOperands())
			{
				var str = getExpressionStringRepresentation(op, null);
				if(str == null) return defaultValue;
				sb.append(str);
			}
			return sb.toString();
		}
		if(value instanceof PsiReferenceExpression referenceExpression)
		{
			var elem = referenceExpression.resolve();
			if(elem instanceof PsiField psf)
			{
				var obj = psf.computeConstantValue();
				if(obj instanceof String str) return str;
				if(obj instanceof PsiAnnotationMemberValue amv) return getExpressionStringRepresentation(amv, defaultValue);
			}
			return defaultValue;
		}
		return defaultValue;
	}
	
	public static boolean isOneOf(PsiAnnotation annotation, String... names)
	{
		var qn = annotation.getQualifiedName();
		if(qn == null) return false;
		for(String name : names)
			if(qn.equals(name))
				return true;
		return false;
	}
}