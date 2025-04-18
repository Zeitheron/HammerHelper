package org.zeith.hammerhelper.configs.namespaces;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import org.zeith.hammerhelper.configs.HHConfigHelper;

import java.util.*;

public class NamespaceConfigsHH
{
	private static final String EXTRA_NAMESPACES = HHConfigHelper.getConfigGlobalName("extraNamespaces");
	private final PropertiesComponent properties;
	
	public NamespaceConfigsHH(Project project)
	{
		properties = PropertiesComponent.getInstance(project);
	}
	
	public List<String> getList(String key, List<String> defaultValue)
	{
		List<String> list = properties.getList(key);
		if(list == null)
		{
			list = defaultValue;
		}
		return list;
	}
	
	public Map<String, String> getNamespaces()
	{
		Map<String, String> map = new HashMap<>();
		List<String> list = getList(EXTRA_NAMESPACES, new ArrayList<>());
		for(int i = 0; i < list.size(); i += 2)
			map.put(list.get(i), list.get(i + 1));
		return map;
	}
	
	public void setNamespaces(Map<String, String> map)
	{
		List<String> list = getList(EXTRA_NAMESPACES, new ArrayList<>());
		list.clear();
		map.forEach((key, value) ->
		{
			list.add(key);
			list.add(value);
		});
		properties.setList(EXTRA_NAMESPACES, list);
	}
}