package com.github.cstroe.svndumpgui.internal.utility.range;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MultiSpanTest {
    @Test
    public void one_span()  {
        MultiSpan multiSpan = new MultiSpan();
        multiSpan.add(new SpanImpl(0, 1));
        assertThat(multiSpan.contains(0), is(true));
        assertThat(multiSpan.contains(1), is(true));
        assertThat(multiSpan.contains(-1), is(false));
        assertThat(multiSpan.contains(2), is(false));
        assertThat(multiSpan.contains(-10), is(false));
        assertThat(multiSpan.contains(12), is(false));
    }

    @Test
    public void two_spans() {
        MultiSpan multiSpan = new MultiSpan();
        multiSpan.add(new SpanImpl(0, 1));
        multiSpan.add(new SpanImpl(3, 4));
        assertThat(multiSpan.contains(-20000), is(false));
        assertThat(multiSpan.contains(-2), is(false));
        assertThat(multiSpan.contains(-1), is(false));
        assertThat(multiSpan.contains(0), is(true));
        assertThat(multiSpan.contains(1), is(true));
        assertThat(multiSpan.contains(2), is(false));
        assertThat(multiSpan.contains(3), is(true));
        assertThat(multiSpan.contains(4), is(true));
        assertThat(multiSpan.contains(5), is(false));
        assertThat(multiSpan.contains(6), is(false));
        assertThat(multiSpan.contains(6000000), is(false));
    }

    @Test
    public void three_overlapping_spans() {
        MultiSpan multiSpan = new MultiSpan();
        multiSpan.add(new SpanImpl(0, 1));
        multiSpan.add(new SpanImpl(3, 4));
        multiSpan.add(new SpanImpl(1, 2));
        assertThat(multiSpan.contains(-20000), is(false));
        assertThat(multiSpan.contains(-2), is(false));
        assertThat(multiSpan.contains(-1), is(false));
        assertThat(multiSpan.contains(0), is(true));
        assertThat(multiSpan.contains(1), is(true));
        assertThat(multiSpan.contains(2), is(true));
        assertThat(multiSpan.contains(3), is(true));
        assertThat(multiSpan.contains(4), is(true));
        assertThat(multiSpan.contains(5), is(false));
        assertThat(multiSpan.contains(6), is(false));
        assertThat(multiSpan.contains(6000000), is(false));
    }

    @Test
    public void span_merging() {
        MultiSpan multiSpan = new MultiSpan();
        multiSpan.add(new SpanImpl(0, 1));
        assertThat(multiSpan.getSpans().size(), is(1));
        multiSpan.add(new SpanImpl(1, 2));
        assertThat(multiSpan.getSpans().size(), is(1));

        assertThat(multiSpan.contains(-1), is(false));
        assertThat(multiSpan.contains(0), is(true));
        assertThat(multiSpan.contains(1), is(true));
        assertThat(multiSpan.contains(2), is(true));
        assertThat(multiSpan.contains(3), is(false));
    }

    @Test
    public void span_merge_reduction() {
        MultiSpan multiSpan = new MultiSpan();
        multiSpan.add(new SpanImpl(0, 1));
        assertThat(multiSpan.getSpans().size(), is(1));
        multiSpan.add(new SpanImpl(2, 3));
        assertThat(multiSpan.getSpans().size(), is(2));
        multiSpan.add(new SpanImpl(1, 2));
        assertThat(multiSpan.getSpans().size(), is(1));

        assertThat(multiSpan.contains(-1), is(false));
        assertThat(multiSpan.contains(0), is(true));
        assertThat(multiSpan.contains(1), is(true));
        assertThat(multiSpan.contains(2), is(true));
        assertThat(multiSpan.contains(3), is(true));
        assertThat(multiSpan.contains(4), is(false));
    }

    @Test
    public void cutoff() {
        {
            MultiSpan multiSpan = new MultiSpan();
            multiSpan.add(new SpanImpl(0, 10));
            multiSpan.cutoff(5);
            assertThat(multiSpan.contains(5), is(true));
            assertThat(multiSpan.contains(6), is(false));
        }{
            MultiSpan multiSpan = new MultiSpan();
            multiSpan.add(new SpanImpl(6, 10));
            multiSpan.add(new SpanImpl(0, 4));
            multiSpan.cutoff(7);
            assertThat(multiSpan.contains(4), is(true));
            assertThat(multiSpan.contains(5), is(false));
            assertThat(multiSpan.contains(6), is(true));
            assertThat(multiSpan.contains(7), is(true));
            assertThat(multiSpan.contains(8), is(false));
        }
    }
}