package com.github.cstroe.svndumpgui.internal.utility.range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MultiSpan implements Cloneable, Collection<Span> {
    private List<Span> spans = new ArrayList<>();

    public MultiSpan() {}

    private MultiSpan(List<Span> spans) {
        this.spans = spans;
    }

    @Override
    public int size() {
        return this.spans.size();
    }

    @Override
    public boolean isEmpty() {
        return this.spans.isEmpty();
    }

    @Override
    public boolean contains(Object obj) {
        if (! (obj instanceof Integer)) { return false; }
        int value = ((Integer) obj).intValue();
        return spans.parallelStream().anyMatch(s -> s.contains(value));
    }

    @Override
    public Iterator<Span> iterator() {
        return this.spans.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.spans.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.spans.toArray(a);
    }

    @Override
    public boolean add(Span span) {
        for(Span currentSpan : spans) {
            if(currentSpan.merge(span)) {
                reduce();
                return true;
            }
        }
        spans.add(span);
        return true;
    }

    @Override
    public boolean remove(Object obj) {
        return this.spans.remove(obj);
    }

    @Override
    public boolean containsAll(Collection<?> col) {
        return this.spans.containsAll(col);
    }

    @Override
    public boolean addAll(Collection<? extends Span> col) {
        return this.spans.addAll(col);
    }

    @Override
    public boolean removeAll(Collection<?> col) {
        return this.spans.removeAll(col);
    }

    @Override
    public boolean retainAll(Collection<?> col) {
        return this.spans.retainAll(col);
    }

    @Override
    public void clear() {
        this.spans.clear();
    }

    private void reduce() {
        MultiSpan multiSpan = new MultiSpan();
        for(Span currentSpan : spans) {
            multiSpan.add(currentSpan);
        }

        if(spans.size() > multiSpan.spans.size()) {
            spans = multiSpan.spans;
            reduce();
        }
    }


    public void cutoff(int value) {
        spans = spans.parallelStream().filter(s -> s.low() <= value).collect(Collectors.toList());
        spans.parallelStream().forEach(s -> s.cutoff(value));
    }

    List<Span> getSpans() {
        return spans;
    }

    @SuppressWarnings({"CloneDoesntCallSuperClone", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    public MultiSpan clone() {
        List<Span> newSpans = new ArrayList<>(spans.size());
        for(Span currentSpan : spans) {
            newSpans.add(new SpanImpl(currentSpan.low(), currentSpan.high()));
        }
        return new MultiSpan(newSpans);
    }

    @Override
    public String toString() {
        return String.join(",", spans.parallelStream().map(Object::toString).collect(Collectors.toList()));
    }

}
