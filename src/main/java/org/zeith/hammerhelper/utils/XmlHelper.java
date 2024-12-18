package org.zeith.hammerhelper.utils;

import java.util.HashMap;
import java.util.Iterator;

public class XmlHelper
{
	public static final HashMap<String, Character> entity = new HashMap(8);
	public static final Character AMP = '&';
	public static final Character APOS = '\'';
	public static final Character GT = '>';
	public static final Character LT = '<';
	public static final Character QUOT = '"';
	static
	{
		entity.put("amp", AMP);
		entity.put("apos", APOS);
		entity.put("gt", GT);
		entity.put("lt", LT);
		entity.put("quot", QUOT);
	}
	
	public static String escape(String string)
	{
		StringBuilder sb = new StringBuilder(string.length());
		
		for(int cp : codePointIterator(string))
		{
			switch(cp)
			{
				case 34:
					sb.append("&quot;");
					break;
				case 38:
					sb.append("&amp;");
					break;
				case 39:
					sb.append("&apos;");
					break;
				case 60:
					sb.append("&lt;");
					break;
				case 62:
					sb.append("&gt;");
					break;
				default:
					if(mustEscape(cp))
					{
						sb.append("&#x");
						sb.append(Integer.toHexString(cp));
						sb.append(';');
					} else
					{
						sb.appendCodePoint(cp);
					}
			}
		}
		
		return sb.toString();
	}
	
	public static String unescape(String string)
	{
		StringBuilder sb = new StringBuilder(string.length());
		int i = 0;
		
		for(int length = string.length(); i < length; ++i)
		{
			char c = string.charAt(i);
			if(c == '&')
			{
				int semic = string.indexOf(59, i);
				if(semic > i)
				{
					String entity = string.substring(i + 1, semic);
					sb.append(unescapeEntity(entity));
					i += entity.length() + 1;
				} else
				{
					sb.append(c);
				}
			} else
			{
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	static String unescapeEntity(String e)
	{
		if(e != null && !e.isEmpty())
		{
			if(e.charAt(0) != '#')
			{
				Character knownEntity = (Character) entity.get(e);
				return knownEntity == null ? '&' + e + ';' : knownEntity.toString();
			} else
			{
				int cp;
				if(e.charAt(1) != 'x' && e.charAt(1) != 'X')
				{
					cp = Integer.parseInt(e.substring(1));
				} else
				{
					cp = Integer.parseInt(e.substring(2), 16);
				}
				
				return new String(new int[] { cp }, 0, 1);
			}
		} else
		{
			return "";
		}
	}
	
	private static Iterable<Integer> codePointIterator(final String string)
	{
		return new Iterable<Integer>()
		{
			public Iterator<Integer> iterator()
			{
				return new Iterator<Integer>()
				{
					private int nextIndex = 0;
					private int length = string.length();
					
					public boolean hasNext()
					{
						return this.nextIndex < this.length;
					}
					
					public Integer next()
					{
						int result = string.codePointAt(this.nextIndex);
						this.nextIndex += Character.charCount(result);
						return result;
					}
					
					public void remove()
					{
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
	private static boolean mustEscape(int cp)
	{
		return Character.isISOControl(cp) && cp != 9 && cp != 10 && cp != 13 || (cp < 32 || cp > 55295) && (cp < 57344 || cp > 65533) && (cp < 65536 || cp > 1114111);
	}
}
