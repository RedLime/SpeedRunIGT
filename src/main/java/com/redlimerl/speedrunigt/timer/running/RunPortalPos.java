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
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public double squaredDistanceTo(Vec3d vec) {
        double d = vec.x - this.x;
        double e = vec.y - this.y;
        double f = vec.z - this.z;
        return d * d + e * e + f * f;
    }

    public double squaredDistanceTo(Vec3i pos) {
        double d = pos.x - this.x;
        double e = pos.y - this.y;
        double f = pos.z - this.z;
        return d * d + e * e + f * f;
    }

    public boolean isAt(Vec3d vec3d) {
        return this.getX() == vec3d.x || this.getY() == vec3d.y || this.getZ() == vec3d.z;
    }
}
