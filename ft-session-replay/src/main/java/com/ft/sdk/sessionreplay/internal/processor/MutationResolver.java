package com.ft.sdk.sessionreplay.internal.processor;

import com.ft.sdk.sessionreplay.model.Add;
import com.ft.sdk.sessionreplay.model.ImageWireframe;
import com.ft.sdk.sessionreplay.model.ImageWireframeUpdate;
import com.ft.sdk.sessionreplay.model.MobileMutationData;
import com.ft.sdk.sessionreplay.model.PlaceholderWireframe;
import com.ft.sdk.sessionreplay.model.PlaceholderWireframeUpdate;
import com.ft.sdk.sessionreplay.model.Remove;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.ShapeWireframeUpdate;
import com.ft.sdk.sessionreplay.model.TextWireframe;
import com.ft.sdk.sessionreplay.model.TextWireframeUpdate;
import com.ft.sdk.sessionreplay.model.WebviewWireframe;
import com.ft.sdk.sessionreplay.model.WebviewWireframeUpdate;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.model.WireframeClip;
import com.ft.sdk.sessionreplay.model.WireframeUpdateMutation;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class MutationResolver {

    private static final String TAG = "MutationResolver";

    private static final String MISS_MATCHING_TYPES_IN_SNAPSHOTS_ERROR_MESSAGE_FORMAT =
            "SR MutationResolver: wireframe of type [%1s] is " +
                    "not matching the wireframe of type [%2s]";

    private final InternalLogger internalLogger;

    public MutationResolver(InternalLogger internalLogger) {
        this.internalLogger = internalLogger;
    }

    public MobileMutationData resolveMutations(
            List<Wireframe> oldSnapshot,
            List<Wireframe> newSnapshot) {

        Map<Long, Symbol> table = new HashMap<>();
        List<Entry> oa = new ArrayList<>();
        List<Entry> na = new ArrayList<>();

        for (Wireframe wf : newSnapshot) {
            long elementId = wf.getId();
            table.put(elementId, new Symbol(false, true));
            na.add(new Entry.Reference(elementId));
        }

        for (int index = 0; index < oldSnapshot.size(); index++) {
            Wireframe element = oldSnapshot.get(index);
            long elementId = element.getId();
            if (!table.containsKey(elementId)) {
                table.put(elementId, new Symbol(true, false, index));
            } else {
                Symbol symbol = table.get(elementId);
                if (symbol != null) {
                    symbol.setInOld(true);
                    symbol.setIndexInOld(index);
                }

            }
            oa.add(new Entry.Reference(elementId));
        }

        for (int index = 0; index < na.size(); index++) {
            Entry entry = na.get(index);
            if (entry instanceof Entry.Reference) {
                Symbol symbol = table.get(((Entry.Reference) entry).getId());
                if (symbol != null) {
                    if (symbol.isInOld() && symbol.isInNew()) {
                        int indexInOld = symbol.getIndexInOld();
                        na.set(index, new Entry.Index(indexInOld));
                        oa.set(indexInOld, new Entry.Index(index));
                    }
                }
            }
        }

        for (int i = 1; i < na.size() - 1; i++) {
            Entry entry = na.get(i);
            if (entry instanceof Entry.Index && ((Entry.Index) entry).getIndex() + 1 < oa.size()) {
                Entry nextNewEntry = na.get(i + 1);
                Entry nextOldEntry = oa.get(((Entry.Index) entry).getIndex() + 1);

                if (nextNewEntry instanceof Entry.Reference &&
                        nextOldEntry instanceof Entry.Reference &&
                        ((Entry.Reference) nextOldEntry).getId() == ((Entry.Reference) nextNewEntry).getId()) {
                    na.set(i + 1, new Entry.Index(((Entry.Index) entry).getIndex() + 1));
                    oa.set(((Entry.Index) entry).getIndex() + 1, new Entry.Index(i + 1));
                }
            }
        }

        for (int i = na.size() - 2; i > 0; i--) {
            Entry entry = na.get(i);
            if (entry instanceof Entry.Index && ((Entry.Index) entry).getIndex() - 1 >= 0) {
                Entry prevNewEntry = na.get(i - 1);
                Entry prevOldEntry = oa.get(((Entry.Index) entry).getIndex() - 1);

                if (prevNewEntry instanceof Entry.Reference &&
                        prevOldEntry instanceof Entry.Reference &&
                        ((Entry.Reference) prevOldEntry).getId() == ((Entry.Reference) prevNewEntry).getId()) {
                    na.set(i - 1, new Entry.Index(((Entry.Index) entry).getIndex() - 1));
                    oa.set(((Entry.Index) entry).getIndex() - 1, new Entry.Index(i - 1));
                }
            }
        }

        LinkedList<WireframeUpdateMutation> updates = new LinkedList<>();
        LinkedList<Add> adds = new LinkedList<>();
        LinkedList<Remove> removes = new LinkedList<>();
        int[] removalOffsets = new int[oldSnapshot.size()];
        int runningOffset = 0;

        for (int index = 0; index < oa.size(); index++) {
            Entry entry = oa.get(index);
            removalOffsets[index] = runningOffset;
            if (entry instanceof Entry.Reference) {
                Wireframe oldWireframe = oldSnapshot.get(index);
                if (oldWireframe instanceof WebviewWireframe) {
                    if (((WebviewWireframe) oldWireframe).getIsVisible()) {
                        updates.add(
                                new WebviewWireframeUpdate(
                                        ((WebviewWireframe) oldWireframe).getId(),
                                        null, null, null, null,
                                        null, null, null,
                                        ((WebviewWireframe) oldWireframe).getSlotId(),
                                        false
                                )
                        );
                    }
                } else {
                    removes.add(new Remove(oldWireframe.getId()));
                }
                runningOffset++;
            }
        }

        runningOffset = 0;

        for (int index = 0; index < na.size(); index++) {
            Entry entry = na.get(index);
            if (entry instanceof Entry.Index) {
                int indexInOld = ((Entry.Index) entry).getIndex();
                int removalOffset = removalOffsets[indexInOld];
                Wireframe newElement = newSnapshot.get(index);
                Wireframe oldElement = oldSnapshot.get(indexInOld);
                if ((indexInOld - removalOffset + runningOffset) != index) {
                    Long previousId = (index > 0) ? newSnapshot.get(index - 1).getId() : null;
                    removes.add(new Remove(newSnapshot.get(index).getId()));
                    adds.add(new Add(previousId, newSnapshot.get(index)));
                } else if (!newElement.equals(oldElement)) {
                    WireframeUpdateMutation mutation = resolveUpdateMutation(oldElement, newElement);
                    if (mutation != null) {
                        updates.add(mutation);
                    }
                }
            } else if (entry instanceof Entry.Reference) {
                Long previousId = (index > 0) ? newSnapshot.get(index - 1).getId() : null;
                adds.add(new Add(previousId, newSnapshot.get(index)));
                runningOffset++;
            }
        }

        if (!adds.isEmpty() || !removes.isEmpty() || !updates.isEmpty()) {
            return new MobileMutationData(adds, removes, updates);
        } else {
            return null;
        }
    }

    public WireframeUpdateMutation resolveUpdateMutation(
            Wireframe prevWireframe,
            Wireframe currentWireframe
    ) {
        if (prevWireframe.equals(currentWireframe)) {
            return null;
        } else {
            boolean isSameClass = prevWireframe.getClass().isAssignableFrom(currentWireframe.getClass());
            if (!isSameClass) {
                internalLogger.e(TAG, String.format(Locale.ENGLISH,
                        MISS_MATCHING_TYPES_IN_SNAPSHOTS_ERROR_MESSAGE_FORMAT,
                        prevWireframe.getClass().getName(),
                        currentWireframe.getClass().getName()));
                return null;
            } else {
                if (prevWireframe instanceof TextWireframe) {
                    return resolveTextMutation(
                            (TextWireframe) prevWireframe,
                            (TextWireframe) currentWireframe
                    );
                } else if (prevWireframe instanceof ShapeWireframe) {
                    return resolveShapeMutation(
                            (ShapeWireframe) prevWireframe,
                            (ShapeWireframe) currentWireframe
                    );
                } else if (prevWireframe instanceof ImageWireframe) {
                    return resolveImageMutation(
                            (ImageWireframe) prevWireframe,
                            (ImageWireframe) currentWireframe
                    );
                } else if (prevWireframe instanceof PlaceholderWireframe) {
                    return resolvePlaceholderMutation(
                            (PlaceholderWireframe) prevWireframe,
                            (PlaceholderWireframe) currentWireframe
                    );
                } else if (prevWireframe instanceof WebviewWireframe) {
                    return resolveWebViewWireframeMutation(
                            (WebviewWireframe) prevWireframe,
                            (WebviewWireframe) currentWireframe
                    );
                }
            }
        }
        return null;
    }

    public WireframeUpdateMutation resolveWebViewWireframeMutation(
            WebviewWireframe prevWireframe,
            WebviewWireframe currentWireframe
    ) {
        WebviewWireframeUpdate mutation =
                new WebviewWireframeUpdate(
                        currentWireframe.getId(),
                        null, null, null, null, null
                        , null, null, currentWireframe.getSlotId(), null
                );

        if (!Objects.equals(prevWireframe.getX(), currentWireframe.getX())) {
            mutation = mutation.setX(currentWireframe.getX());
        }
        if (!Objects.equals(prevWireframe.getY(), currentWireframe.getY())) {
            mutation = mutation.setY(currentWireframe.getY());
        }
        if (!Objects.equals(prevWireframe.getWidth(), currentWireframe.getWidth())) {
            mutation = mutation.setWidth(currentWireframe.getWidth());
        }
        if (!Objects.equals(prevWireframe.getHeight(), currentWireframe.getHeight())) {
            mutation = mutation.setHeight(currentWireframe.getHeight());
        }
        if (!Objects.equals(prevWireframe.getBorder(), currentWireframe.getBorder())) {
            mutation = mutation.setBorder(currentWireframe.getBorder());
        }
        if (!Objects.equals(prevWireframe.getShapeStyle(), currentWireframe.getShapeStyle())) {
            mutation = mutation.setShapeStyle(currentWireframe.getShapeStyle());
        }
        if (!Objects.equals(prevWireframe.getClip(), currentWireframe.getClip())) {
            mutation = mutation.setClip(
                    currentWireframe.getClip() != null ?
                            currentWireframe.getClip() :
                            new WireframeClip(0L, 0L, 0L, 0L)
            );
        }

        return mutation;
    }

    private WireframeUpdateMutation resolveShapeMutation(
            ShapeWireframe prevWireframe,
            ShapeWireframe currentWireframe) {

        ShapeWireframeUpdate mutation =
                new ShapeWireframeUpdate(currentWireframe.getId(), null, null, null, null, null, null, null);
        if (!Objects.equals(prevWireframe.getX(), currentWireframe.getX())) {
            mutation = mutation.setX(currentWireframe.getX());
        }
        if (!Objects.equals(prevWireframe.getY(), currentWireframe.getY())) {
            mutation = mutation.setY(currentWireframe.getY());
        }
        if (!Objects.equals(prevWireframe.getWidth(), currentWireframe.getWidth())) {
            mutation = mutation.setWidth(currentWireframe.getWidth());
        }
        if (!Objects.equals(prevWireframe.getHeight(), currentWireframe.getHeight())) {
            mutation = mutation.setHeight(currentWireframe.getHeight());
        }
        if (!Objects.equals(prevWireframe.getBorder(), currentWireframe.getBorder())) {
            mutation = mutation.setBorder(currentWireframe.getBorder());
        }
        if (!Objects.equals(prevWireframe.getShapeStyle(), currentWireframe.getShapeStyle())) {
            mutation = mutation.setShapeStyle(currentWireframe.getShapeStyle());
        }
        if (!Objects.equals(prevWireframe.getClip(), currentWireframe.getClip())) {
            mutation = mutation.setClip(
                    currentWireframe.getClip() != null ?
                            currentWireframe.getClip() :
                            new WireframeClip(0L, 0L, 0L, 0L)
            );

        }

        return mutation;
    }

    private WireframeUpdateMutation resolvePlaceholderMutation(
            PlaceholderWireframe prevWireframe,
            PlaceholderWireframe currentWireframe) {

        PlaceholderWireframeUpdate mutation =
                new PlaceholderWireframeUpdate(currentWireframe.getId(), null,
                        null, null, null, null, null);
        if (!Objects.equals(prevWireframe.getX(), currentWireframe.getX())) {
            mutation = mutation.setX(currentWireframe.getX());
        }
        if (!Objects.equals(prevWireframe.getY(), currentWireframe.getY())) {
            mutation = mutation.setY(currentWireframe.getY());
        }
        if (!Objects.equals(prevWireframe.getWidth(), currentWireframe.getWidth())) {
            mutation = mutation.setWidth(currentWireframe.getWidth());
        }
        if (!Objects.equals(prevWireframe.getHeight(), currentWireframe.getHeight())) {
            mutation = mutation.setHeight(currentWireframe.getHeight());
        }
        if (!Objects.equals(prevWireframe.getClip(), currentWireframe.getClip())) {
            mutation = mutation.setClip(
                    currentWireframe.getClip() != null ?
                            currentWireframe.getClip() :
                            new WireframeClip(0L, 0L, 0L, 0L)
            );
        }

        return mutation;
    }

    private WireframeUpdateMutation resolveImageMutation(
            ImageWireframe prevWireframe,
            ImageWireframe currentWireframe) {

        ImageWireframeUpdate mutation =
                new ImageWireframeUpdate(currentWireframe.getId(), null,
                        null, null, null, null, null,
                        null, null, null, null, null);
        if (!Objects.equals(prevWireframe.getX(), currentWireframe.getX())) {
            mutation = mutation.setX(currentWireframe.getX());
        }
        if (!Objects.equals(prevWireframe.getY(), currentWireframe.getY())) {
            mutation = mutation.setY(currentWireframe.getY());
        }
        if (!Objects.equals(prevWireframe.getWidth(), currentWireframe.getWidth())) {
            mutation = mutation.setWidth(currentWireframe.getWidth());
        }
        if (!Objects.equals(prevWireframe.getHeight(), currentWireframe.getHeight())) {
            mutation = mutation.setHeight(currentWireframe.getHeight());
        }
        if (!Objects.equals(prevWireframe.getBorder(), currentWireframe.getBorder())) {
            mutation = mutation.setBorder(currentWireframe.getBorder());
        }
        if (!Objects.equals(prevWireframe.getShapeStyle(), currentWireframe.getShapeStyle())) {
            mutation = mutation.setShapeStyle(currentWireframe.getShapeStyle());
        }
        if (!Objects.equals(prevWireframe.getClip(), currentWireframe.getClip())) {
            mutation = mutation.setClip(
                    currentWireframe.getClip() != null ?
                            currentWireframe.getClip() :
                            new WireframeClip(0L, 0L, 0L, 0L)
            );
        }
        return mutation;
    }

    public WireframeUpdateMutation resolveTextMutation(
            TextWireframe prevWireframe,
            TextWireframe currentWireframe
    ) {
        TextWireframeUpdate mutation =
                new TextWireframeUpdate(currentWireframe.getId(), null, null,
                        null, null, null,
                        null, null, null,
                        null, null);

        if (!Objects.equals(prevWireframe.getX(), currentWireframe.getX())) {
            mutation = mutation.setX(currentWireframe.getX());
        }
        if (!Objects.equals(prevWireframe.getY(), currentWireframe.getY())) {
            mutation = mutation.setY(currentWireframe.getY());
        }
        if (!Objects.equals(prevWireframe.getWidth(), currentWireframe.getWidth())) {
            mutation = mutation.setWidth(currentWireframe.getWidth());
        }
        if (!Objects.equals(prevWireframe.getHeight(), currentWireframe.getHeight())) {
            mutation = mutation.setHeight(currentWireframe.getHeight());
        }
        if (!Objects.equals(prevWireframe.getBorder(), currentWireframe.getBorder())) {
            mutation = mutation.setBorder(currentWireframe.getBorder());
        }
        if (!Objects.equals(prevWireframe.getShapeStyle(), currentWireframe.getShapeStyle())) {
            mutation = mutation.setShapeStyle(currentWireframe.getShapeStyle());
        }
        if (!Objects.equals(prevWireframe.getTextStyle(), currentWireframe.getTextStyle())) {
            mutation = mutation.setTextStyle(currentWireframe.getTextStyle());
        }
        if (!Objects.equals(prevWireframe.getText(), currentWireframe.getText())) {
            mutation = mutation.setText(currentWireframe.getText());
        }
        if (!Objects.equals(prevWireframe.getTextPosition(), currentWireframe.getTextPosition())) {
            mutation = mutation.setTextPosition(currentWireframe.getTextPosition());
        }
        if (!Objects.equals(prevWireframe.getClip(), currentWireframe.getClip())) {
            mutation = mutation.setClip(
                    currentWireframe.getClip() != null ?
                            currentWireframe.getClip() :
                            new WireframeClip(0L, 0L, 0L, 0L)
            );
        }

        return mutation;
    }


}
