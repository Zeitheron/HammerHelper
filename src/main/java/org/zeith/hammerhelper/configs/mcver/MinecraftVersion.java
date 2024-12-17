package org.zeith.hammerhelper.configs.mcver;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class MinecraftVersion
{
	public String id;
	public String name;
	public long world_version;
	public String series_id;
	public long protocol_version;
	public PackVersions pack_version;
	public String build_time;
	public String java_component;
	public int java_version;
	public boolean stable;
	
	public record PackVersions(int resource, int data) {}
}