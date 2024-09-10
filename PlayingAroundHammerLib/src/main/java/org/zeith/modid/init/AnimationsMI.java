package org.zeith.modid.init;

import org.zeith.hammeranims.api.animation.IAnimationContainer;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister()
public interface AnimationsMI {
    @RegistryName("test")
    IAnimationContainer TEST_1 = IAnimationContainer.create();

    @RegistryName("test_np")
    IAnimationContainer TEST_2 = IAnimationContainer.createNoSuffix();

    @RegistryName("test_np")
    IAnimationContainer TEST_3 = IAnimationContainer.createNoSuffix();

    @RegistryName("test_np")
    IAnimationContainer TEST_4 = IAnimationContainer.createNoSuffix();

    @SimplyRegister(prefix = "sub/")
    interface SubAnimations {
        @RegistryName("test_np_sub")
        IAnimationContainer TEST_2 = IAnimationContainer.createNoSuffix();
    }
}
