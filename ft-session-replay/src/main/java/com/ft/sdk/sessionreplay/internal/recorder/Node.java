package com.ft.sdk.sessionreplay.internal.recorder;

import com.ft.sdk.sessionreplay.model.Wireframe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {

    private List<Wireframe> wireframes;
    private List<Node> children;
    private List<Wireframe> parents;
    private Map<String, Object> metadata; // 添加metadata字段

    public Node(List<Wireframe> wireframes, List<Node> children, List<Wireframe> parents) {
        this.wireframes = wireframes != null ? wireframes : new ArrayList<>();
        this.children = children != null ? children : new ArrayList<>();
        this.parents = parents != null ? parents : new ArrayList<>();
        this.metadata = new HashMap<>(); // 初始化metadata
    }

    public List<Wireframe> getWireframes() {
        return wireframes;
    }

    public void setWireframes(List<Wireframe> wireframes) {
        this.wireframes = wireframes != null ? wireframes : new ArrayList<>();
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children != null ? children : new ArrayList<>();
    }

    public List<Wireframe> getParents() {
        return parents;
    }

    public void setParents(List<Wireframe> parents) {
        this.parents = parents != null ? parents : new ArrayList<>();
    }

    // 添加metadata相关方法
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
}
