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
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
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
        double d = pos.getX() - this.x;
        double e = pos.getY() - this.y;
        double f = pos.getZ() - this.z;
        return d * d + e * e + f * f;
    }

    public boolean isAt(Vec3d vec3d) {
        return this.getX() == vec3d.getX() || this.getY() == vec3d.getY() || this.getZ() == vec3d.getZ();
    }
}
