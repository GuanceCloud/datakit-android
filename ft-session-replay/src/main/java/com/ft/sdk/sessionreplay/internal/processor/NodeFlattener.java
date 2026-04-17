package com.ft.sdk.sessionreplay.internal.processor;

import com.ft.sdk.sessionreplay.internal.recorder.Node;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.model.WireframeClip;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class NodeFlattener {
    private final WireframeUtils wireframeUtils;

    public NodeFlattener() {
        this.wireframeUtils = new WireframeUtils();
    }

    public NodeFlattener(WireframeUtils wireframeUtils) {
        this.wireframeUtils = wireframeUtils;
    }

    public List<Wireframe> flattenNode(Node root) {
        Stack<Node> stack = new Stack<>();
        LinkedList<Wireframe> list = new LinkedList<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Node node = stack.pop();
            for (Wireframe wireframe : node.getWireframes()) {
                WireframeClip clip = wireframeUtils.resolveWireframeClip(wireframe, node.getParents());
                list.add(wireframe.setClip(clip));
            }
            for (int i = node.getChildren().size() - 1; i >= 0; i--) {
                stack.push(node.getChildren().get(i));
            }
        }

        return filterOutInvalidWireframes(list);
    }

    private List<Wireframe> filterOutInvalidWireframes(List<Wireframe> wireframes) {
        List<Wireframe> filteredWireframes = new ArrayList<>();
        for (int index = 0; index < wireframes.size(); index++) {
            Wireframe wireframe = wireframes.get(index);
            if (wireframeUtils.checkWireframeIsValid(wireframe) &&
                    !wireframeUtils.checkWireframeIsCovered(wireframe, wireframes.subList(index + 1, wireframes.size()))) {
                filteredWireframes.add(wireframe);
            }
        }
        return filteredWireframes;
    }
}
