package com.ft.sdk.garble.utils;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * BY huangDianHua
 * DATE:2020-01-17 18:57
 * Description:
 */
public class RendererUtil implements GLSurfaceView.Renderer {

    //GPU 渲染器
    public String gl_renderer;

    //GPU 供应商
    public String gl_vendor;

    //GPU 版本
    public String gl_version;

    //GPU  扩展名
    public String gl_extensions;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        gl_renderer = gl.glGetString(GL10.GL_RENDERER);
        gl_vendor = gl.glGetString(GL10.GL_VENDOR);
        gl_version = gl.glGetString(GL10.GL_VERSION);
        gl_extensions = gl.glGetString(GL10.GL_EXTENSIONS);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {

    }

    @Override
    public void onDrawFrame(GL10 gl10) {

    }
}

