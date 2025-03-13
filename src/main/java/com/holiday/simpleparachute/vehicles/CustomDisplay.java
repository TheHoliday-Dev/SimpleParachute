package com.holiday.simpleparachute.vehicles;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class CustomDisplay extends Display.BlockDisplay {

    public CustomDisplay(Level var1) {
        super(EntityType.BLOCK_DISPLAY, var1);
    }
}
