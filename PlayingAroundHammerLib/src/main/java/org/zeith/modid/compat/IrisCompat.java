package org.zeith.modid.compat;

import org.zeith.hammerlib.compat.base.BaseCompat;
import org.zeith.hammerlib.compat.base.CompatContext;

@BaseCompat.LoadCompat(modid = "hammerlib", compatType = BaseCompatMI.class)
public class IrisCompat extends BaseCompatMI {
    public IrisCompat(CompatContext ctx) {
        super(ctx);
    }
}
