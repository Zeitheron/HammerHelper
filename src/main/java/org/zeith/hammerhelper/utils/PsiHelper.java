package org.zeith.hammerhelper.utils;

import com.intellij.psi.*;
import org.jetbrains.annotations.Nullable;

public class PsiHelper
{
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
			if(elem instanceof PsiField psf && psf.computeConstantValue() instanceof String str)
				return str;
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