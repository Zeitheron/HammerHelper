package org.zeith.hammerhelper.utils.hlide;

import java.util.Optional;
import java.util.regex.Pattern;

public record FileRefByRegex(Pattern regex, String path)
{
	public Optional<String> resolve(String input)
	{
		var m = regex.matcher(input);
		if(!m.find()) return Optional.empty();
		
		String path = path();
		
		for(String group : m.namedGroups().keySet())
			path = path.replace("%" + group + "%", m.group(group));
		
		return Optional.of(path);
	}
}