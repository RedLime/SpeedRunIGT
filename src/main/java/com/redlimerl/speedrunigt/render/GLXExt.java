package com.redlimerl.speedrunigt.render;

import org.lwjgl.opengl.*;

public class GLXExt {
    private static final boolean gl14Supported;
    public static final boolean blendFuncSeparateSupported;

    static {
        ContextCapabilities var0 = GLContext.getCapabilities();
        blendFuncSeparateSupported = var0.GL_EXT_blend_func_separate && !var0.OpenGL14;
        gl14Supported = var0.OpenGL14 || var0.GL_EXT_blend_func_separate;
    }

    /**
     * Specifies pixel arithmetic for RGB and alpha components separately
     */
    public static void glBlendFuncSeparate(int r, int g, int b, int a) {
        if (gl14Supported) {
            if (blendFuncSeparateSupported) {
                EXTBlendFuncSeparate.glBlendFuncSeparateEXT(r, g, b, a);
            } else {
                GL14.glBlendFuncSeparate(r, g, b, a);
            }
        } else {
            GL11.glBlendFunc(r, g);
        }
    }
}
