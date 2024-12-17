package org.zeith.hammerhelper.configs.mcver;

import com.google.gson.*;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerhelper.configs.HHConfigHelper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

public class MinecraftVersionConfigs
{
	private static final Gson GSON = new GsonBuilder()
			.create();
	
	private static final String MC_VERSION = HHConfigHelper.getConfigGlobalName("minecraftVersion");
	private static final String MC_VERSION_CHECK_TIME = HHConfigHelper.getConfigGlobalName("minecraftVersionCheckTime");
	
	public static MinecraftVersion getMinecraftVersion(Project project)
	{
		PropertiesComponent props = PropertiesComponent.getInstance(project);
		MinecraftVersion cfg;
		
		// Reuse cached value for 5 seconds.
		Instant ct = parseInstant(props.getValue(MC_VERSION_CHECK_TIME));
		if(ct != null && Duration.between(ct, Instant.now()).abs().getSeconds() < 5)
		{
			cfg = GSON.fromJson(props.getValue(MC_VERSION, "{}"), MinecraftVersion.class);
			if(cfg != null && cfg.protocol_version > 0)
				return cfg;
		}
		
		props.setValue(MC_VERSION, GSON.toJson(cfg = detectMinecraftVersion(project)));
		props.setValue(MC_VERSION_CHECK_TIME, stringifyInstant(Instant.now()));
		
		return cfg;
	}
	
	private static Instant parseInstant(String time)
	{
		if(time == null) return null;
		try
		{
			return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(time));
		} catch(Throwable e)
		{
			return null;
		}
	}
	
	private static String stringifyInstant(Instant inst)
	{
		return DateTimeFormatter.ISO_INSTANT.format(inst);
	}
	
	private static MinecraftVersion detectMinecraftVersion(Project project)
	{
		AtomicReference<MinecraftVersion> version = new AtomicReference<>();
		
		OrderEnumerator.orderEntries(project)
				.librariesOnly()
				.forEachLibrary(library ->
				{
					VirtualFile vf = findFileInLibrary(library, "version.json");
					if(vf != null)
					{
						try
						{
							JsonObject jso = GSON.fromJson(new String(vf.getInputStream().readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);
							if(!jso.has("pack_version")) return true;
							MinecraftVersion mcv = GSON.fromJson(jso, MinecraftVersion.class);
							if(mcv != null && mcv.pack_version != null)
							{
								version.set(mcv);
								return false;
							}
						} catch(Exception e)
						{
						}
					}
					return true; // Stop iteration if we found the file
				});
		
		return version.get();
	}
	
	@Nullable
	private static VirtualFile findFileInLibrary(Library library, String fileName)
	{
		for(VirtualFile root : library.getFiles(OrderRootType.CLASSES))
		{
			VirtualFile targetFile = root.findFileByRelativePath(fileName);
			if(targetFile != null && !targetFile.isDirectory())
				return targetFile;
		}
		return null;
	}
}