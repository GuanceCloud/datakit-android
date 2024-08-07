package com.ft.sdk.sessionreplay.internal.processor;

public class Symbol {
    private boolean inOld;
    private boolean inNew;
    private Integer indexInOld;

    public Symbol(boolean inOld, boolean inNew, Integer indexInOld) {
        this.inOld = inOld;
        this.inNew = inNew;
        this.indexInOld = indexInOld;
    }

    public Symbol(boolean inOld, boolean inNew) {
        this(inOld, inNew, null);
    }

    public boolean isInOld() {
        return inOld;
    }

    public void setInOld(boolean inOld) {
        this.inOld = inOld;
    }

    public boolean isInNew() {
        return inNew;
    }

    public void setInNew(boolean inNew) {
        this.inNew = inNew;
    }

    public Integer getIndexInOld() {
        return indexInOld;
    }

    public void setIndexInOld(Integer indexInOld) {
        this.indexInOld = indexInOld;
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "inOld=" + inOld +
                ", inNew=" + inNew +
                ", indexInOld=" + indexInOld +
                '}';
    }
}
