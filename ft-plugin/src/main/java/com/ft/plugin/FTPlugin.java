package com.ft.plugin;

import com.android.build.gradle.AppExtension;
import com.ft.plugin.garble.FTTransform;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Collections;

/**
 * BY huangDianHua
 * DATE:2019-11-29 12:33
 * Description:插件入口
 */
public class FTPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        AppExtension appExtension = (AppExtension) project.getProperties().get("android");
        appExtension.registerTransform(new FTTransform(project), Collections.EMPTY_LIST);
    }
}
