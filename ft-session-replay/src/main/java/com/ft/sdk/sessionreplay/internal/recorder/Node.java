package com.ft.sdk.sessionreplay.internal.recorder;

import com.ft.sdk.sessionreplay.model.Wireframe;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private List<Wireframe> wireframes;
    private List<Node> children;
    private List<Wireframe> parents;

    public Node(List<Wireframe> wireframes, List<Node> children, List<Wireframe> parents) {
        this.wireframes = wireframes != null ? wireframes : new ArrayList<>();
        this.children = children != null ? children : new ArrayList<>();
        this.parents = parents != null ? parents : new ArrayList<>();
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
}
