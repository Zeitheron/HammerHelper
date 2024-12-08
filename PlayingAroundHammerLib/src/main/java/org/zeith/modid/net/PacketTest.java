package org.zeith.modid.net;

import net.minecraft.network.FriendlyByteBuf;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;
import org.zeith.hammerlib.util.shaded.json.serapi.IgnoreSerialization;

public class PacketTest implements IPacket {

    public int field1;
    public String f2;
    public final int constant = 0;
    public static final int constant2 = 0;

    public PacketTest() {}

    @Override
    public void write(FriendlyByteBuf buf) {
        IPacket.super.write(buf);
        buf.writeInt(field1);
        buf.writeUtf(f2);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        IPacket.super.read(buf);
        field1 = buf.readInt();
        f2 = buf.readUtf();
    }

    @Override
    public boolean equals(Object obj) {

        new IPacket() {
        };

        return super.equals(obj);
    }

    @Override
    public void clientExecute(PacketContext ctx) {
        IPacket.super.clientExecute(ctx);
    }
}
