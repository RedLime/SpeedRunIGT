package com.redlimerl.speedrunigt.timer.running;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.io.Serializable;

@SuppressWarnings("unused")
public class RunPortalPos implements Serializable {
    private final double x;
    private final double y;
    private final double z;

    public RunPortalPos(Vec3d pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    public RunPortalPos(Vec3i pos) {
        this.x = pos.field_4613;
        this.y = pos.field_4614;
        this.z = pos.field_4615;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double squaredDistanceTo(Vec3d vec) {
        double d = vec.x - this.x;
        double e = vec.y - this.y;
        double f = vec.z - this.z;
        return d * d + e * e + f * f;
    }

    public double squaredDistanceTo(Vec3i pos) {
        double d = pos.field_4613 - this.x;
        double e = pos.field_4614 - this.y;
        double f = pos.field_4615 - this.z;
        return d * d + e * e + f * f;
    }
}
