package org.zeith.hammerhelper.utils;

public class ResourceLocationChecks
{
	public static String[] split(String path)
	{
		String[] split = path.split(":", 2);
		if(split.length == 1) return new String[] { "minecraft", split[0] };
		return split;
	}
	
	public static boolean isValidPath(String pPath)
	{
		for(int i = 0; i < pPath.length(); ++i)
		{
			if(!validPathChar(pPath.charAt(i)))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isValidNamespace(String pNamespace)
	{
		for(int i = 0; i < pNamespace.length(); ++i)
		{
			if(!validNamespaceChar(pNamespace.charAt(i)))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean validPathChar(char pPathChar)
	{
		return pPathChar == '_' || pPathChar == '-' || pPathChar >= 'a' && pPathChar <= 'z' || pPathChar >= '0' && pPathChar <= '9' || pPathChar == '/' || pPathChar == '.';
	}
	
	public static boolean validNamespaceChar(char pNamespaceChar)
	{
		return pNamespaceChar == '_' || pNamespaceChar == '-' || pNamespaceChar >= 'a' && pNamespaceChar <= 'z' || pNamespaceChar >= '0' && pNamespaceChar <= '9' || pNamespaceChar == '.';
	}
}