package org.zeith.hammerhelper.icons;

import com.intellij.ide.IconLayerProvider;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zeith.hammerhelper.utils.PsiHelper;

import javax.swing.*;

import static org.zeith.hammerhelper.inspections.classes.packets.MissingEmptyPacketConstructor.PACKET_TYPE;

public class PacketIconLayerProvider
		implements IconLayerProvider
{
	@Override
	public @Nullable Icon getLayerIcon(@NotNull Iconable element, boolean isLocked)
	{
		if(element instanceof PsiClass pc && PsiHelper.inHierarchy(pc, PACKET_TYPE))
			return HLIcons.PACKET;
		return null;
	}
	
	@Override
	public @NotNull String getLayerDescription()
	{
		return "IPacket Class";
	}
}