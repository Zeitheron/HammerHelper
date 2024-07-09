package org.zeith.modid.init;

import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.modid.ModId;

@SimplyRegister
public interface BlocksMI
{
    @RegistryName(ModId.MOD_ID + "/" + "test_block")
    AirBlock TEST = new AirBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.AIR));
}