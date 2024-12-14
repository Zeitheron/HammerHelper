package org.zeith.hammerhelper.utils;

import com.intellij.json.JsonUtil;
import com.intellij.json.psi.*;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;

import java.util.*;

public class PsiJsonHelper
{
	private static final Key<Optional<JsonFile>> JSON_FILE_KEY = Key.create("hammerhelper json file");
	
	public static String reconstructPathFromRoot(JsonElement element)
	{
		StringBuilder s = new StringBuilder();
		
		record Either(String name, int index) {}
		
		List<Either> invPath = new ArrayList<>();
		
		while(true)
		{
			PsiElement p = element.getParent();
			if(!(p instanceof JsonElement je) || je instanceof JsonFile) break;
			if(je instanceof JsonProperty prop) invPath.add(new Either(prop.getName(), prop.getStartOffsetInParent()));
			element = je;
		}
		
		for(int i = invPath.size() - 1; i >= 0; i--)
		{
			var e = invPath.get(i);
			
			if(e.name() != null)
			{
				if(!s.isEmpty()) s.append('/');
				s.append(e.name());
			}
			
			else s.append('[').append(e.index()).append(']');
		}
		
		return s.toString();
	}
	
	public static boolean isElementValueByPath(JsonFile file, JsonValue value, String... path)
	{
		JsonProperty prop = getElementByPath(file, path);
		return prop != null && value == prop.getValue();
	}
	
	public static JsonProperty getElementByPath(JsonFile file, String... path)
	{
		JsonObject obj = JsonUtil.getTopLevelObject(file);
		if(obj == null) return null;
		
		for(int i = 0; i < path.length; i++)
		{
			JsonProperty prop = obj.findProperty(path[i]);
			
			if(i == path.length - 1 || prop == null)
				return prop;
			
			if(!(prop.getValue() instanceof JsonObject innerOBJ))
				return null;
			
			obj = innerOBJ;
		}
		
		return null;
	}
	
	public static JsonFile getJsonFile(JsonElement element, ProcessingContext ctx)
	{
		Optional<JsonFile> jsonFile = ctx.get(JSON_FILE_KEY);
		if(jsonFile != null) return jsonFile.orElse(null);
		
		JsonFile f = getJsonFile(element);
		ctx.put(JSON_FILE_KEY, Optional.ofNullable(f));
		
		return f;
	}
	
	public static JsonFile getJsonFile(JsonElement element)
	{
		while(true)
		{
			PsiElement par = element.getParent();
			if(par instanceof JsonFile jf) return jf;
			if(par instanceof JsonElement je) element = je;
			else break;
		}
		
		return null;
	}
}