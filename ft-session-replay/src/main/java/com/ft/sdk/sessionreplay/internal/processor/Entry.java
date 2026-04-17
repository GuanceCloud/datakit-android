package com.ft.sdk.sessionreplay.internal.processor;

public abstract class Entry {

    // Nested static class for Reference
    public static class Reference extends Entry {
        private final long id;

        public Reference(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        @Override
        public String toString() {
            return "Reference{" +
                    "id=" + id +
                    '}';
        }
    }

    // Nested static class for Index
    public static class Index extends Entry {
        private final int index;

        public Index(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return "Index{" +
                    "index=" + index +
                    '}';
        }
    }
}
