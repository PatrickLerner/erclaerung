package de.tudarmstadt.awesome.erclaerung.feature;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.awesome.erclaerung.precomputation.LevenshteinDistancePreComp;
import de.tudarmstadt.awesome.erclaerung.precomputation.LevenshteinStep;
import de.tudarmstadt.awesome.erclaerung.precomputation.LevenshteinStep.Operation;
import de.tudarmstadt.awesome.erclaerung.precomputation.LevenshteinTransformation;

/**
 * @author Manuel
 *
 */
public class LevenshteinDistancePreCompTest {
	LevenshteinDistancePreComp levenshtein;

	@Before
	public void setUp() throws ResourceInitializationException, AnalysisEngineProcessException, FileNotFoundException,
	                UnsupportedEncodingException {
		levenshtein = new LevenshteinDistancePreComp();
	}

	@Test
	public void transformationTest() {
		LevenshteinTransformation trans11 = LevenshteinDistancePreComp.getTransformation("bruch", "geruch");
		LevenshteinStep step1 = new LevenshteinStep(0, Operation.INSERT, 'g');
		LevenshteinStep step2 = new LevenshteinStep(0, Operation.SUBSTITUTION, 'e', 'b');
		LevenshteinStep step3 = new LevenshteinStep(1, Operation.NONOP, 'r');
		LevenshteinStep step4 = new LevenshteinStep(2, Operation.NONOP, 'u');
		LevenshteinStep step5 = new LevenshteinStep(3, Operation.NONOP, 'c');
		LevenshteinStep step6 = new LevenshteinStep(4, Operation.NONOP, 'h');
		List<LevenshteinStep> steps = new ArrayList<LevenshteinStep>();
		steps.add(step1);
		steps.add(step2);
		steps.add(step3);
		steps.add(step4);
		steps.add(step5);
		steps.add(step6);
		LevenshteinTransformation trans12 = new LevenshteinTransformation("bruch", "geruch", steps);
		assertEquals(trans11, trans12);
		LevenshteinTransformation trans2 = LevenshteinDistancePreComp.getTransformation("parfum", "pxyzarfm");
		LevenshteinTransformation trans3 = LevenshteinDistancePreComp.getTransformation("parfum", "pxyzarfm");
		LevenshteinTransformation trans4 = LevenshteinDistancePreComp.getTransformation("test", "ts");
		LevenshteinTransformation trans5 = LevenshteinDistancePreComp.getTransformation("verboten", "xxxxx");
		LevenshteinTransformation trans6 = LevenshteinDistancePreComp.getTransformation("parfum", "hallo");
		LevenshteinTransformation trans7 = LevenshteinDistancePreComp.getTransformation("parfum", "putzm");
		String string2 = "parfum->pxyzarfm:\nInsert: x at 1: pxarfum\nInsert: y at 1: pxyarfum\nInsert: z at 1: pxyzarfum\nDelete: u at 4: pxyzarfm";
		String string3 = "parfum->pxyzarfm:\nInsert: x at 1: pxarfum\nInsert: y at 1: pxyarfum\nInsert: z at 1: pxyzarfum\nDelete: u at 4: pxyzarfm";
		String string4 = "test->ts:\nDelete: e at 1: tst\nDelete: t at 3: ts";
		String string5 = "verboten->xxxxx:\nDelete: v at 0: erboten\nDelete: e at 1: rboten\nDelete: r at 2: boten\nSubstitution: x at 3 substituting b: xoten\nSubstitution: x at 4 substituting o: xxten\nSubstitution: x at 5 substituting t: xxxen\nSubstitution: x at 6 substituting e: xxxxn\nSubstitution: x at 7 substituting n: xxxxx";
		String string6 = "parfum->hallo:\nSubstitution: h at 0 substituting p: harfum\nDelete: r at 2: hafum\nSubstitution: l at 3 substituting f: halum\nSubstitution: l at 4 substituting u: hallm\nSubstitution: o at 5 substituting m: hallo";
		String string7 = "parfum->putzm:\nDelete: a at 1: prfum\nSubstitution: u at 2 substituting r: pufum\nSubstitution: t at 3 substituting f: putum\nSubstitution: z at 4 substituting u: putzm";
		assertEquals(trans2.toStringWithTransformation(false), string2);
		assertEquals(trans3.toStringWithTransformation(false), string3);
		assertEquals(trans4.toStringWithTransformation(false), string4);
		assertEquals(trans5.toStringWithTransformation(false), string5);
		assertEquals(trans6.toStringWithTransformation(false), string6);
		assertEquals(trans7.toStringWithTransformation(false), string7);
	}

	@Test
	public void DistanceTest() {
		assertEquals(LevenshteinDistancePreComp.levenshteinDistance("parfait", "parfum"), 3);
		assertEquals(LevenshteinDistancePreComp.levenshteinDistance("tea", "tee"), 1);
		assertEquals(LevenshteinDistancePreComp.levenshteinDistance("Patricia", "Patrick"), 2);
		assertEquals(LevenshteinDistancePreComp.levenshteinDistance("Manuel", "Manuela"), 1);
		assertEquals(LevenshteinDistancePreComp.levenshteinDistance("Abend", "Morgen"), 5);
		assertEquals(LevenshteinDistancePreComp.levenshteinDistance("Würfel", "Karten"), 4);
		assertEquals(LevenshteinDistancePreComp.levenshteinDistance("Glück", "Pech"), 4);
		assertEquals(LevenshteinDistancePreComp.levenshteinDistance("kurz", "sehrsehrlangundlängerdasgehtimmerweiter"),
		                LevenshteinDistancePreComp.levenshteinDistance("sehrsehrlangundlängerdasgehtimmerweiter",
		                                "kurz"));

		assertEquals(LevenshteinDistancePreComp.levenshteinDistance("kurz", "sehrsehrlangundlängerdasgehtimmerweiter"),
		                37);

	}
}
