package org.zeith.modid.init;

import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public interface BlocksMI
{
    @RegistryName("test_block")
    Block TEST = new AirBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.AIR));
}