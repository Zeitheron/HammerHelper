package org.zeith.modid.init;

import org.zeith.hammeranims.api.geometry.IGeometryContainer;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public interface GeometryMI {
    @RegistryName("test")
    IGeometryContainer TEST_1 = IGeometryContainer.create();

    @RegistryName("test_np")
    IGeometryContainer TEST_2 = IGeometryContainer.createNoSuffix();

    @SimplyRegister(prefix = "sub/")
    interface SubAnimations {
        @RegistryName("test_np_sub")
        IGeometryContainer TEST_2 = IGeometryContainer.createNoSuffix();
    }
}
