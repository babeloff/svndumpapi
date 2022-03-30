package com.github.cstroe.svndumpgui.internal.utility.range;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SpanImplTest {

    private Span span(int low, int high)  {
        return new SpanImpl(low, high);
    }

    @Test
    public void simplest_range() {
        Span span = span(1,1);
        assertThat(span.contains(0), is(false));
        assertThat(span.contains(1), is(true));
        assertThat(span.contains(2), is(false));
    }

    @Test
    public void simple_range() {
        Span span = span(0, 1);
        assertThat(span.low(), is(0));
        assertThat(span.high(), is(1));
        assertThat(span.contains(0), is(true));
        assertThat(span.contains(1), is(true));
        assertThat(span.contains(-1), is(false));
        assertThat(span.contains(2), is(false));
    }

    @Test
    public void negative_infinity() {
        Span span = span(Span.NEGATIVE_INFINITY, 10);
        assertThat(span.contains(9), is(true));
        assertThat(span.contains(10), is(true));
        assertThat(span.contains(11), is(false));
        assertThat(span.contains(-1234565), is(true));
        assertThat(span.contains(1234), is(false));
    }

    @Test
    public void positive_infinity() {
        Span span = span(2, Span.POSITIVE_INFINITY);
        assertThat(span.contains(1), is(false));
        assertThat(span.contains(2), is(true));
        assertThat(span.contains(3), is(true));
        assertThat(span.contains(-1234565), is(false));
        assertThat(span.contains(1234), is(true));
    }

    @Test
    public void all_numbers() {
        Span span = span(Span.NEGATIVE_INFINITY, Span.POSITIVE_INFINITY);
        assertThat(span.contains(0), is(true));
        assertThat(span.contains(1), is(true));
        assertThat(span.contains(1000000), is(true));
        assertThat(span.contains(-1), is(true));
        assertThat(span.contains(-999999), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void does_not_allow_invalid_parameters_1() {
        span(1,0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void does_not_allow_invalid_parameters_2() {
        span(Span.POSITIVE_INFINITY,0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void does_not_allow_invalid_parameters_3() {
        span(Span.POSITIVE_INFINITY,Span.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void does_not_allow_invalid_parameters_4() {
        span(-10000000,Span.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negative_infinity_does_not_specify_a_span() {
        span(Span.NEGATIVE_INFINITY,Span.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void positive_infinity_does_not_specify_a_span() {
        span(Span.POSITIVE_INFINITY,Span.POSITIVE_INFINITY);
    }

    @Test
    public void overlapping() {
        {
            Span s1 = span(0,1);
            assertThat(s1.overlaps(s1), is(true));
        }{
            Span s1 = span(0,1);
            Span s2 = span(0,1);
            assertThat(s1.overlaps(s2), is(true));
            assertThat(s2.overlaps(s1), is(true));
        }{
            Span s1 = span(0,1);
            Span s2 = span(1,2);
            assertThat(s1.overlaps(s2), is(true));
            assertThat(s2.overlaps(s1), is(true));
        }{
            Span s1 = span(0,1);
            Span s2 = span(2,3);
            assertThat(s1.overlaps(s2), is(false));
            assertThat(s2.overlaps(s1), is(false));
        }{
            Span s1 = span(0,Span.POSITIVE_INFINITY);
            Span s2 = span(1,2);
            assertThat(s1.overlaps(s2), is(true));
            assertThat(s2.overlaps(s1), is(true));
        }{
            Span s1 = span(0,Span.POSITIVE_INFINITY);
            Span s2 = span(Span.NEGATIVE_INFINITY,-1);
            assertThat(s1.overlaps(s2), is(false));
            assertThat(s2.overlaps(s1), is(false));
        }{
            Span s1 = span(Span.NEGATIVE_INFINITY,Span.POSITIVE_INFINITY);
            Span s2 = span(1,2);
            assertThat(s1.overlaps(s2), is(true));
            assertThat(s2.overlaps(s1), is(true));
        }{
            Span s1 = span(Span.NEGATIVE_INFINITY,Span.POSITIVE_INFINITY);
            Span s2 = span(Span.NEGATIVE_INFINITY,Span.POSITIVE_INFINITY);
            assertThat(s1.overlaps(s2), is(true));
            assertThat(s2.overlaps(s1), is(true));
        }
    }

    @Test
    public void cutoff() {
        Span s1 = span(3, 20);
        s1.cutoff(15);
        assertThat(15, equalTo(s1.high()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cutoff_low_range() {
        Span s1 = span(10, 20);
        s1.cutoff(5);
    }
}
