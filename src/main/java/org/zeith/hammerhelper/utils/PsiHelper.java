package org.zeith.hammerhelper.utils;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

public class PsiHelper
{
	public static Set<String> KNOWN_NON_REGISTRY_INTERFACES = Set.of(
			"org.zeith.hammeranims.api.utils.IHammerReloadable",
			"java.lang.Runnable",
			"net.minecraft.world.level.ItemLike",
			"net.minecraft.world.flag.FeatureElement"
	);
	
	public static Set<String> sharingSameInheritance(PsiClass a, PsiClass b)
	{
		Set<String> typesB = new HashSet<>();
		visitPossibleTypes(b, typesB);
		return sharingSameInheritance(a, typesB);
	}
	
	public static Set<String> sharingSameInheritance(PsiClass a, Set<String> typesB)
	{
		Set<String> typesA = new HashSet<>();
		visitPossibleTypes(a, typesA);
		typesA.retainAll(typesB);
		typesA.remove("java.lang.Object");
		return typesA;
	}
	
	public static Set<String> sharingSameRegistryInheritance(PsiClass a, Set<String> typesB)
	{
		var typesA = sharingSameInheritance(a, typesB);
		typesA.removeAll(KNOWN_NON_REGISTRY_INTERFACES);
		return typesA;
	}
	
	public static boolean instanceOf(PsiClass psiClass, Set<String> possibleTypes)
	{
		if(psiClass == null) return false;
		if(possibleTypes.contains(psiClass.getQualifiedName())) return true;
		return instanceOf(psiClass.getSuperClass(), possibleTypes)
			   || Arrays.stream(psiClass.getInterfaces()).anyMatch(itf -> instanceOf(itf, possibleTypes));
	}
	
	public static void visitPossibleTypes(PsiClass psiClass, Set<String> possibleTypes)
	{
		if(psiClass == null) return;
		var qn = psiClass.getQualifiedName();
		if(qn != null) possibleTypes.add(qn);
		visitPossibleTypes(psiClass.getSuperClass(), possibleTypes);
		Arrays.stream(psiClass.getInterfaces()).forEach(itf -> visitPossibleTypes(itf, possibleTypes));
	}
	
	public static String findInstance(PsiClass psiClass, Set<String> possibleTypes)
	{
		if(psiClass == null) return null;
		var qfn = psiClass.getQualifiedName();
		if(qfn != null && possibleTypes.contains(qfn)) return psiClass.getQualifiedName();
		var qn = findInstance(psiClass.getSuperClass(), possibleTypes);
		if(qn != null) return qn;
		return Arrays.stream(psiClass.getInterfaces())
				.map(itf -> findInstance(itf, possibleTypes))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}
	
	public static PsiClass getClassFromPsiAnnotation(PsiAnnotation annotation, String attributeName)
	{
		AtomicReference<PsiClass> resolved = new AtomicReference<>();
		PsiAnnotationMemberValue value = annotation.findAttributeValue(attributeName);
		if(value == null) return null;
		value.acceptChildren(new PsiElementVisitor()
		{
			@Override
			public void visitElement(@NotNull PsiElement element)
			{
				if(element instanceof PsiTypeElement pt)
				{
					var pc = PsiTypesUtil.getPsiClass(pt.getType());
					if(pc != null) resolved.set(pc);
				}
			}
		});
		return resolved.get();
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
		StringBuilder sb = new StringBuilder();
		AtomicBoolean valid = new AtomicBoolean(true);
		visitExpressionStringRepresentation(value, (psi, str) -> sb.append(str), v -> valid.set(false));
		return valid.get() ? sb.toString() : defaultValue;
	}
	
	public static void visitExpressionStringRepresentation(PsiAnnotationMemberValue value, BiConsumer<PsiAnnotationMemberValue, String> handled)
	{
		visitExpressionStringRepresentation(value, handled, psi ->
				{
				}
		);
	}
	
	public static void visitExpressionStringRepresentation(PsiAnnotationMemberValue value, BiConsumer<PsiAnnotationMemberValue, String> handled, Consumer<PsiAnnotationMemberValue> unhandled)
	{
		if(value instanceof PsiLiteralExpression literalExpression && literalExpression.getValue() instanceof String s)
		{
			handled.accept(value, s);
			return;
		} else if(value instanceof PsiBinaryExpression binaryExpression && binaryExpression.getOperationSign().toString().endsWith(":PLUS"))
		{
			visitExpressionStringRepresentation(binaryExpression.getLOperand(), handled, unhandled);
			visitExpressionStringRepresentation(binaryExpression.getROperand(), handled, unhandled);
			return;
		} else if(value instanceof PsiPolyadicExpression polyadicExpression)
		{
			for(PsiExpression op : polyadicExpression.getOperands())
				visitExpressionStringRepresentation(op, handled, unhandled);
			return;
		} else if(value instanceof PsiReferenceExpression referenceExpression)
		{
			var elem = referenceExpression.resolve();
			if(elem instanceof PsiField psf)
			{
				var obj = psf.computeConstantValue();
				if(obj instanceof String str)
				{
					handled.accept(value, str);
					return;
				}
				
				if(obj instanceof PsiAnnotationMemberValue amv)
				{
					visitExpressionStringRepresentation(amv, handled, unhandled);
				}
			} else unhandled.accept(value);
		}
		unhandled.accept(value);
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
	
	public static Set<PsiClass> getClassHierarchy(PsiClass type)
	{
		Set<PsiClass> hierarchy = new HashSet<>();
		
		Stack<PsiClass> stack = new Stack<>();
		stack.push(type);
		while(!stack.isEmpty())
		{
			PsiClass psi = stack.pop();
			hierarchy.add(psi);
			for(PsiClass itf : psi.getInterfaces()) stack.push(itf);
			for(PsiClassType itf : psi.getExtendsListTypes()) Optional.ofNullable(itf.resolve()).ifPresent(stack::push);
		}
		
		return hierarchy;
	}
	
	public static boolean inHierarchy(PsiClass type, Predicate<PsiClass> filter)
	{
		Stack<PsiClass> stack = new Stack<>();
		stack.push(type);
		while(!stack.isEmpty())
		{
			PsiClass psi = stack.pop();
			if(filter.test(psi)) return true;
			for(PsiClass itf : psi.getInterfaces()) stack.push(itf);
			for(PsiClassType itf : psi.getExtendsListTypes()) Optional.ofNullable(itf.resolve()).ifPresent(stack::push);
		}
		return false;
	}
	
	public static Predicate<PsiClass> oneOf(String... types)
	{
		Set<String> typeSet = Set.of(types);
		return pc ->
		{
			String qn;
			return pc != null && (qn = pc.getQualifiedName()) != null && typeSet.contains(qn);
		};
	}
	
	public static Predicate<PsiClass> oneOfImplementedMethods(String baseClass, String... names)
	{
		return oneOf(baseClass).negate().and(pc ->
		{
			for(String name : names)
			{
				for(var method : pc.findMethodsByName(name, false))
				{
					if(method.hasAnnotation("java.lang.Override"))
					{
						return true;
					}
				}
			}
			
			return false;
		});
	}
	
	public static PsiClass findClassParent(PsiElement parent)
	{
		while(parent != null)
		{
			parent = parent.getParent();
			if(parent instanceof PsiClass pc)
				return pc;
		}
		return null;
	}
}