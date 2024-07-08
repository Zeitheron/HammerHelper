package org.zeith.hammerhelper.utils;

import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.classlayout.ClassMayBeInterfaceInspection;

import java.lang.reflect.Method;

public class QuickFixHelper
{
	public static InspectionGadgetsFix convertClassToInterface()
	{
		try
		{
			Method met = ClassMayBeInterfaceInspection.class.getDeclaredMethod("buildFix", Object[].class);
			met.setAccessible(true);
			return (InspectionGadgetsFix) met.invoke(new ClassMayBeInterfaceInspection(),
					(Object) new Object[0]
			);
		} catch(ReflectiveOperationException e)
		{
		}
		return null;
	}
}