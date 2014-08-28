package io.crowbar.diagnostic.spectrum;

import static org.junit.Assert.*;
import io.crowbar.diagnostic.spectrum.activity.Hit;
import io.crowbar.diagnostic.spectrum.matchers.IdMatcher;
import io.crowbar.diagnostic.spectrum.matchers.NegateMatcher;
import io.crowbar.diagnostic.spectrum.matchers.ValidTransactionMatcher;
import io.crowbar.diagnostic.spectrum.unserializers.HitSpectrumUnserializer;

import java.util.Scanner;

import org.junit.Test;

public class SpectrumViewFactoryTest {
    @Test
    public void testProbeCount () {
        String in = "3 3 1 0 1 1.0 1 1 0 1.0 1 1 1 0.0";


        Spectrum<Hit, ? > s = HitSpectrumUnserializer.unserialize(new Scanner(in));

        SpectrumViewFactory<Hit, ? > svf = new SpectrumViewFactory(s);

        SpectrumView<Hit, ? > sv = svf.getView();

        assertEquals(sv.getProbeCount(), 3);
    }

    @Test
    public void testTransactionCount () {
        String in = "3 3 1 0 1 1.0 1 1 0 1.0 1 1 1 0.0";


        Spectrum<Hit, ? > s = HitSpectrumUnserializer.unserialize(new Scanner(in));

        SpectrumViewFactory<Hit, ? > svf = new SpectrumViewFactory(s);

        SpectrumView<Hit, ? > sv = svf.getView();

        assertEquals(sv.getTransactionCount(), 3);
    }

    @Test
    public void testStage () {
        String in = "3 4 1 0 1 1.0 1 1 0 1.0 1 1 1 0.0 0 0 0 0.0";


        Spectrum<Hit, ? > s = HitSpectrumUnserializer.unserialize(new Scanner(in));

        SpectrumViewFactory<Hit, ? > svf = new SpectrumViewFactory(s);

        ValidTransactionMatcher v = new ValidTransactionMatcher();

        svf.addStage(v);

        SpectrumView<Hit, ? > sv = svf.getView();

        assertEquals(sv.getTransactionCount(), 3);
    }

    @Test
    public void testStage2 () {
        String in = "3 4 1 0 1 1.0 1 1 0 1.0 1 1 1 0.0 0 0 0 0.0";


        Spectrum<Hit, ? > s = HitSpectrumUnserializer.unserialize(new Scanner(in));

        SpectrumViewFactory<Hit, ? > svf = new SpectrumViewFactory(s);

        ValidTransactionMatcher v = new ValidTransactionMatcher();
        svf.addStage(v);

        SpectrumView<Hit, ? > view = svf.getView();
        assertEquals(view.getTransactionCount(), 3);
        assertNotEquals(s, view);

        for (int i = 0; i < 10; i++) {
            svf.addStage(v);
            SpectrumView<Hit, ? > sv = svf.getView();
            assertEquals(view, sv);
        }
    }

    @Test
    public void testStage3 () {
        String in = "4 4 1 1 0 1 1.0 1 1 1 0 1.0 1 1 1 1 0.0 0 0 0 0 0.0";


        Spectrum<Hit, ? > s = HitSpectrumUnserializer.unserialize(new Scanner(in));

        SpectrumViewFactory<Hit, ? > svf = new SpectrumViewFactory(s);

        NegateMatcher m = new NegateMatcher(
            new IdMatcher(new int[] {0}, new int[] {0}));

        for (int i = 4; i > 0; i--) {
            svf.addStage(m);
            SpectrumView<Hit, ? > sv = svf.getView();
            assertEquals(i - 1, sv.getProbeCount());
            assertEquals(i - 1, sv.getTransactionCount());
        }
    }

    @Test
    public void testMappingsTransactions () {
        String in = "4 4 1 1 0 1 1.0 1 1 1 0 1.0 1 1 1 1 0.0 0 0 0 0 0.0";
        int filteredTr = 1;


        Spectrum<Hit, ? > s = HitSpectrumUnserializer.unserialize(new Scanner(in));

        SpectrumViewFactory<Hit, ? > svf = new SpectrumViewFactory(s);


        svf.addStage(new NegateMatcher(new IdMatcher(new int[] {filteredTr}, null)));
        SpectrumView<Hit, ? > view = svf.getView();

        assertEquals(s.getProbeCount(), view.getProbeCount());
        assertEquals(s.getTransactionCount() - 1, view.getTransactionCount());

        for (Transaction t : view.byTransaction()) {
            assertEquals(t.getId() + ((t.getId() >= filteredTr) ? 1 : 0),
                         view.getTransactionMapping(t.getId()));
        }
    }

    // @Test
    // public void testMappingsProbes() {
    // TODO: The spectrum must be created so it contains probe information
    // String in = "4 4 1 1 0 1 1.0 1 1 1 0 1.0 1 1 1 1 0.0 0 0 0 0 0.0";
    // int filteredPr = 1;

    // Spectrum<Hit, ? > s = HitSpectrumUnserializer.unserialize(new Scanner(in));
    // SpectrumViewFactory<Hit, ? > svf = new SpectrumViewFactory(s);


    // svf.addStage(new NegateMatcher(new IdMatcher(null, new int[]{filteredPr})));
    // SpectrumView<Hit, ? > view = svf.getView();

    // assertEquals(s.getProbeCount() - 1, view.getProbeCount());
    // assertEquals(s.getTransactionCount(), view.getTransactionCount());


    // for(Probe p : view.byProbe()) {
    // assertEquals(p.getId() + ((p.getId() >= filteredPr)?1:0),
    // view.getProbeMapping(p.getId()));
    // }
    // }
}