package com.threeamigos.foresta.motore;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GrammarBeanTest {

    // ---- Constructors ----

    @Test
    void constructorWithGrammarOnlyHasNoPostProduction() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\tHello\n");
        assertEquals("Hello", bean.produce().get(0));
    }

    @Test
    void constructorWithNullPostProductionStringIsAccepted() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\tHello\n", null);
        assertEquals("Hello", bean.produce().get(0));
    }

    @Test
    void constructorWithPostProductionStringAppliesSubstitutions() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\ta il gatto\n", "a il:al");
        assertEquals("al gatto", bean.produce().get(0));
    }

    @Test
    void constructorWithInputStreamsWorks() throws Exception {
        ByteArrayInputStream grammar = new ByteArrayInputStream("ROOT\n\tHi\n".getBytes(StandardCharsets.UTF_8));
        GrammarBean bean = new GrammarBean(grammar, null);
        assertEquals("Hi", bean.produce().get(0));
    }

    // ---- Production headers / children parsing ----

    @Test
    void firstProductionBecomesRootNode() throws Exception {
        GrammarBean bean = new GrammarBean("FIRST\n\tfirstText\nSECOND\n\tsecondText\n");
        assertEquals("FIRST", bean.getRootNode());
    }

    @Test
    void leafAlternativesAreTrimmedOfSurroundingWhitespace() throws Exception {
        // handleChildren calls child.trim() on each split alternative before storing it,
        // so stray leading/trailing spaces around "|"-separated alternatives must not leak
        // into the produced text.
        GrammarBean bean = new GrammarBean("ROOT\n\t  a  |  b  \n");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            seen.add(bean.produce().get(0));
        }
        assertEquals(new HashSet<>(java.util.Arrays.asList("a", "b")), seen);
    }

    @Test
    void duplicateProductionNameIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\tx\nROOT\n\ty\n"));
        assertTrue(ex.getMessage().contains("already found"));
    }

    @Test
    void productionWithNoChildrenIsInvalid() {
        assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("A\nB\nC\n\tx\n"));
    }

    @Test
    void childLineWithoutPrecedingProductionIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("\tx\n"));
        assertTrue(ex.getMessage().contains("Missing parent production"));
    }

    @Test
    void commentAndEmptyLinesAreSkipped() throws Exception {
        GrammarBean bean = new GrammarBean("# a comment\nROOT\n\n\tHello\n");
        assertEquals("Hello", bean.produce().get(0));
    }

    @Test
    void multipleAlternativesOnSameLineAreAllReachable() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\tA|B|C\n");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            seen.add(bean.produce().get(0));
        }
        assertEquals(new HashSet<>(java.util.Arrays.asList("A", "B", "C")), seen);
    }

    @Test
    void oneShotProductionMarkerIsRecognizedAndConsumed() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT$\n\tonly\n");
        assertEquals("only", bean.produce().get(0));
    }

    // ---- Inline alternation groups ----

    @Test
    void inlineAlternationGroupExpandsToAllOptions() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\tIl {vecchio|giovane|misterioso} viandante\n");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 60; i++) {
            seen.add(bean.produce().get(0));
        }
        assertEquals(3, seen.size());
        seen.forEach(s -> assertTrue(s.startsWith("Il ") && s.endsWith(" viandante")));
    }

    @Test
    void multipleInlineAlternationGroupsOnSameChildBothExpand() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t{a|b} and {c|d}\n");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 60; i++) {
            seen.add(bean.produce().get(0));
        }
        assertEquals(4, seen.size());
    }

    @Test
    void topLevelPipeOutsideBraceIsStillASeparator() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t{x|y} | B semplice\n");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 60; i++) {
            seen.add(bean.produce().get(0));
        }
        assertEquals(new HashSet<>(java.util.Arrays.asList("x", "y", "B semplice")), seen);
    }

    @Test
    void unclosedInlineAlternationBraceIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\tSomething {a|b senza chiusura\n"));
        assertTrue(ex.getMessage().contains("Missing '}'"));
    }

    @Test
    void emptyInlineAlternationGroupProducesEmptyLeaf() throws Exception {
        // Both options between the braces are blank once trimmed; unlike before, this is no
        // longer invalid: it's kept as an empty-string leaf, aligning inline groups with how a
        // trailing "|" on a regular production's children line is already handled.
        GrammarBean bean = new GrammarBean("ROOT\n\tSomething {   |  } vuoto\n");
        assertEquals("Something  vuoto", bean.produce().get(0));
    }

    @Test
    void inlineAlternationGroupWithEmptyLeafIsUsedAsOptionalElement() throws Exception {
        // A trailing "|" in the inline group represents "no adjective at all" — an optional
        // element commonly needed when generating natural-language text.
        GrammarBean bean = new GrammarBean("ROOT\n\tUn {grande|feroce|}lupo\n");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 60; i++) {
            seen.add(bean.produce().get(0));
        }
        assertEquals(new HashSet<>(java.util.Arrays.asList("Un grandelupo", "Un ferocelupo", "Un lupo")), seen);
    }

    @Test
    void inlineProductionNameCollisionIsAvoided() throws Exception {
        // PROD_0 is declared by hand BEFORE the inline group is parsed, so the auto-naming
        // do-while loop must detect the collision and fall through to PROD_1.
        GrammarBean bean = new GrammarBean("PROD_0\n\tfixed\nROOT\n\t[PROD_0] {x|y}\n");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 40; i++) {
            seen.add(bean.produce("ROOT").get(0));
        }
        assertEquals(new HashSet<>(java.util.Arrays.asList("fixed x", "fixed y")), seen);
    }

    @Test
    void nestedInlineAlternationGroupExpandsRecursively() throws Exception {
        // The outer group's only alternative is "x {y|z}"; expanding it recursively turns the
        // nested group into its own on-the-fly production before the outer one is registered.
        GrammarBean bean = new GrammarBean("ROOT\n\ta { x { y | z } } b\n");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 60; i++) {
            seen.add(bean.produce().get(0));
        }
        assertEquals(new HashSet<>(java.util.Arrays.asList("a x y b", "a x z b")), seen);
    }

    @Test
    void nestedInlineAlternationGroupAtTopLevelExpandsRecursively() throws Exception {
        // The outer group has two top-level alternatives: "a" and the nested group "{b|c}";
        // splitTopLevelAlternatives must not be fooled by the nested group's own "|".
        GrammarBean bean = new GrammarBean("ROOT\n\t{a|{b|c}}\n");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 60; i++) {
            seen.add(bean.produce().get(0));
        }
        assertEquals(new HashSet<>(java.util.Arrays.asList("a", "b", "c")), seen);
    }

    @Test
    void nestedInlineAlternationGroupWithLastModeAndEmptyLeafProducesTrimmedResult() throws Exception {
        // The outer group's alternatives are "x" and "y {z|}"; with LAST mode the outer group
        // picks the second one, whose nested group ("{z|}") in turn picks its last (empty) leaf.
        // Every production produces a trimmed string, so the space left between "y" and the
        // nested group's now-empty expansion is stripped, yielding exactly "y", not "y ".
        GrammarBean bean = new GrammarBean("ROOT\n\t{ x | y { z | } }\n");
        bean.setProductionMode(ProductionModeEnum.LAST);
        assertEquals("y", bean.produce().get(0));
    }

    @Test
    void unclosedNestedInlineAlternationBraceIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t{a|{b|c}\n"));
        assertTrue(ex.getMessage().contains("Missing '}'"));
    }

    // ---- [Name] reference validity checks ----

    @Test
    void referenceToUndefinedProductionIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t[Missing]\n"));
        assertTrue(ex.getMessage().contains("is not defined"));
    }

    @Test
    void referenceMissingClosingBracketIsInvalidAtLoadTime() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t[Missing\n"));
        assertTrue(ex.getMessage().contains("Missing ']'"));
    }

    @Test
    void globalFixedMarkerAloneIsInvalid() {
        assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t[*]\n"));
    }

    @Test
    void localFixedMarkerAloneIsInvalid() {
        assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t[!]\n"));
    }

    @Test
    void assignmentMarkerWithoutKeyIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t[=novalue]\n"));
        assertTrue(ex.getMessage().contains("must be preceded by a node name"));
    }

    @Test
    void referenceToDefinedProductionIsValid() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t[OTHER]\nOTHER\n\ttext\n");
        assertEquals("text", bean.produce().get(0));
    }

    // ---- Fixed productions (* and !) ----

    @Test
    void globalFixedProductionIsCachedAcrossOccurrences() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t[*NAME] [*NAME]\nNAME\n\ta|b|c\n");
        for (int i = 0; i < 20; i++) {
            String result = bean.produce().get(0);
            String[] parts = result.split(" ");
            assertEquals(parts[0], parts[1]);
        }
    }

    @Test
    void localFixedProductionDoesNotLeakAcrossSiblingBranches() throws Exception {
        GrammarBean bean = new GrammarBean(
                "ROOT\n\t[BRANCH] [BRANCH]\nBRANCH\n\t[!NAME]-[!NAME]\nNAME\n\ta|b|c|d|e|f|g|h\n");
        boolean sawDifference = false;
        for (int i = 0; i < 60 && !sawDifference; i++) {
            String result = bean.produce().get(0);
            String[] branches = result.split(" ");
            assertEquals(2, branches.length);
            // within a branch, the two locally-fixed halves must be equal
            String[] halvesLeft = branches[0].split("-");
            String[] halvesRight = branches[1].split("-");
            assertEquals(halvesLeft[0], halvesLeft[1]);
            assertEquals(halvesRight[0], halvesRight[1]);
            if (!branches[0].equals(branches[1])) {
                sawDifference = true;
            }
        }
        assertTrue(sawDifference, "Expected the two independently-produced branches to differ at least once");
    }

    @Test
    void addFixedProductionPreSeedsGlobalFixedValue() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t[*NAME]\nNAME\n\treal\n");
        bean.addFixedProduction("NAME", "overridden");
        assertEquals("overridden", bean.produce().get(0));
    }

    @Test
    void globalFixedProductionIsClearedBetweenProduceCalls() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t[*NAME]\nNAME\n\ta|b|c|d|e|f|g|h|i|j\n");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 60; i++) {
            seen.add(bean.produce().get(0));
        }
        assertTrue(seen.size() > 1, "Global fixed cache should reset between produce() calls");
    }

    // ---- Assignment / reference (# ) ----

    @Test
    void assignmentAndReferenceRoundTrip() throws Exception {
        // ROOT's own alternative is "[k=hello] [#k]", which expands to " hello": every
        // production is expected to produce a trimmed string, so ROOT's leading space is stripped.
        GrammarBean bean = new GrammarBean("ROOT\n\t[k=hello] [#k]\n");
        assertEquals("hello", bean.produce().get(0));
    }

    @Test
    void referenceToUnassignedKeyIsInvalidAtRuntime() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t[#neverset]\n");
        assertThrows(IllegalArgumentException.class, bean::produce);
    }

    @Test
    void referenceFindsLocallyFixedValueWithoutFallback() throws Exception {
        // [!NAME] caches its result into the current call frame's own local-fixed map under
        // key "NAME"; the following [#NAME] then finds it there directly, with no need to
        // fall back to the super map.
        GrammarBean bean = new GrammarBean("ROOT\n\t[!NAME][#NAME]\nNAME\n\tvalue\n");
        assertEquals("valuevalue", bean.produce().get(0));
    }

    // ---- setRootNode / getRootNode ----

    @Test
    void setRootNodeChangesProductionStartPoint() throws Exception {
        GrammarBean bean = new GrammarBean("FIRST\n\tfirstText\nSECOND\n\tsecondText\n");
        bean.setRootNode("SECOND");
        assertEquals("SECOND", bean.getRootNode());
        assertEquals("secondText", bean.produce().get(0));
    }

    @Test
    void setRootNodeWithUndefinedProductionThrows() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\tx\n");
        assertThrows(IllegalArgumentException.class, () -> bean.setRootNode("NOPE"));
    }

    @Test
    void produceWithExplicitRootNodeDoesNotChangeDefaultRootNode() throws Exception {
        GrammarBean bean = new GrammarBean("FIRST\n\tfirstText\nSECOND\n\tsecondText\n");
        assertEquals("secondText", bean.produce("SECOND").get(0));
        assertEquals("FIRST", bean.getRootNode());
    }

    // ---- production mode ----

    @Test
    void productionModeDefaultsToRandom() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\tA|B|C\n");
        assertEquals(ProductionModeEnum.RANDOM, bean.getProductionMode());
    }

    @Test
    void firstProductionModeAlwaysPicksFirstOption() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\tA|B|C\n");
        bean.setProductionMode(ProductionModeEnum.FIRST);
        assertEquals(ProductionModeEnum.FIRST, bean.getProductionMode());
        for (int i = 0; i < 10; i++) {
            assertEquals("A", bean.produce().get(0));
        }
    }

    @Test
    void lastProductionModeAlwaysPicksLastOption() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\tA|B|C\n");
        bean.setProductionMode(ProductionModeEnum.LAST);
        assertEquals(ProductionModeEnum.LAST, bean.getProductionMode());
        for (int i = 0; i < 10; i++) {
            assertEquals("C", bean.produce().get(0));
        }
    }

    // ---- one-shot exhaustion / removal cascade ----

    @Test
    void oneShotProductionIsRemovedAfterUse() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t[X]\nX$\n\tonly\n");
        assertEquals("only", bean.produce().get(0));
        // second call within the same cycle (no reset) must fail: X was fully consumed and removed,
        // and the cascade also stripped ROOT's only alternative referencing it, so ROOT itself
        // is removed too and the lookup finds no production at all for it.
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, bean::produce);
        assertTrue(ex.getMessage().contains("ROOT"));
    }

    @Test
    void oneShotProductionWithRemainingAlternativesIsNotRemoved() throws Exception {
        // X has 3 alternatives; consuming one must only strip that alternative, leaving the
        // production (and ROOT's reference to it) intact for the next call.
        GrammarBean bean = new GrammarBean("ROOT\n\t[X]\nX$\n\ta|b|c\n");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            seen.add(bean.produce().get(0));
        }
        assertEquals(new HashSet<>(java.util.Arrays.asList("a", "b", "c")), seen);
    }

    @Test
    void cascadeStripsFixedReferencesButSparesUnrelatedAlternatives() throws Exception {
        // ROOT has four alternatives: one references X plainly, one via [*X], one via [!X],
        // and one ("Z") doesn't reference X at all. Once X$ is exhausted, the cascade in
        // removeProduction must strip the first three but leave "Z" and ROOT itself standing.
        GrammarBean bean = new GrammarBean("ROOT\n\t[X]|[*X]|[!X]|Z\nX$\n\tonly\n");
        bean.setProductionMode(ProductionModeEnum.FIRST);
        assertEquals("only", bean.produce().get(0));
        assertEquals("Z", bean.produce().get(0));
        assertEquals("Z", bean.produce().get(0));
    }

    @Test
    void oneShotProductionIsAvailableAgainAfterReset() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t[X] end\nX$\n\tonly\n");
        assertEquals("only end", bean.produce().get(0));
        bean.reset();
        assertEquals("only end", bean.produce().get(0));
    }

    @Test
    void referencingExhaustedOneShotProductionAgainViaPlainReferenceThrows() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t[X] [X]\nX$\n\tonly\n");
        assertThrows(IllegalArgumentException.class, bean::produce);
    }

    @Test
    void referencingExhaustedOneShotProductionAgainViaFixedReferenceThrows() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t[X] [*X]\nX$\n\tonly\n");
        assertThrows(IllegalArgumentException.class, bean::produce);
    }

    // ---- post-production substitutions ----

    @Test
    void nullPostProductionStreamMeansNoSubstitutions() throws Exception {
        GrammarBean bean = new GrammarBean(
                new ByteArrayInputStream("ROOT\n\tunchanged\n".getBytes(StandardCharsets.UTF_8)), null);
        assertEquals("unchanged", bean.produce().get(0));
    }

    @Test
    void postProductionLineWithoutColonIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\tx\n", "justtext"));
        assertTrue(ex.getMessage().contains("No post production"));
    }

    @Test
    void postProductionLineWithEmptyPreIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\tx\n", ":post"));
        assertTrue(ex.getMessage().contains("No pre production"));
    }

    @Test
    void postProductionLineWithEmptyPostIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\tx\n", "pre:"));
        assertTrue(ex.getMessage().contains("Empty post production"));
    }

    @Test
    void postProductionSubstitutesEveryOccurrence() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\ta il gatto e a il cane\n", "a il:al");
        assertEquals("al gatto e al cane", bean.produce().get(0));
    }

    @Test
    void postProductionSubstitutionAtVeryEndOfTextIsHandled() throws Exception {
        // The matched "pre" text is the last thing in the produced string, so nothing
        // trails it after the substitution.
        GrammarBean bean = new GrammarBean("ROOT\n\thello world\n", "world:earth");
        assertEquals("hello earth", bean.produce().get(0));
    }

    // ---- multi-line output ----

    @Test
    void producedTextIsSplitIntoLines() throws Exception {
        // Neither the grammar file nor the post-production file can embed a literal newline
        // inside a single entry (both are parsed via BufferedReader.readLine()), so the only way
        // to exercise the final line-splitting is to call the public produceImpl(String) directly
        // with a string that already contains one.
        GrammarBean bean = new GrammarBean("ROOT\n\tirrelevant\n");
        List<String> result = bean.produceImpl("line1\nline2");
        assertEquals(java.util.Arrays.asList("line1", "line2"), result);
    }

    // ---- runtime bracket mismatch introduced through assignment + reference splicing ----

    @Test
    void strayUnmatchedBracketReintroducedAtRuntimeThrows() throws Exception {
        // "[k=X[Y]" is accepted at load time because the FIRST ']' found closes the outer token;
        // the embedded '[' inside the assigned value is only discovered once "[#k]" splices the
        // raw value ("X[Y") back into the text being expanded, leaving an unmatched '[' with no ']'
        // anywhere in the remainder of the produced text.
        GrammarBean bean = new GrammarBean("ROOT\n\t[k=X[Y] [#k]\n");
        assertThrows(IllegalArgumentException.class, bean::produce);
    }

    // ---- self-referencing (recursive) productions ----

    @Test
    void productionCanReferenceItselfRecursively() throws Exception {
        // LISTA's own children reference "[LISTA]" again: produceImpl's token-resolution
        // loop has no cycle/depth guard, so this recurses through resolveToken/produceImpl
        // however many times the random alternative "elemento [LISTA]" is picked, until the
        // terminating alternative "fine" is eventually chosen. Across enough trials this must
        // (a) never fail/overflow and (b) actually reach more than one level of recursion.
        GrammarBean bean = new GrammarBean("LISTA\n\telemento [LISTA]|fine\n");
        int maxDepthSeen = 0;
        for (int i = 0; i < 300; i++) {
            String result = bean.produce().get(0);
            assertTrue(result.matches("(elemento )*fine"), "Unexpected produced text: " + result);
            int depth = result.split(" ").length - 1;
            maxDepthSeen = Math.max(maxDepthSeen, depth);
        }
        assertTrue(maxDepthSeen >= 3, "Expected at least one run to recurse 3+ levels deep, max seen: " + maxDepthSeen);
    }

    @Test
    void mutuallyRecursiveProductionsResolveEachOther() throws Exception {
        // A references B and B references A right back; nothing in checkProductionsValidity
        // rejects this cycle at load time, and produceImpl happily bounces between the two
        // until a non-referencing alternative ("stop") is picked.
        GrammarBean bean = new GrammarBean("A\n\tx[B]|stop\nB\n\ty[A]|stop\n");
        boolean sawBounce = false;
        for (int i = 0; i < 300; i++) {
            String result = bean.produce().get(0);
            assertTrue(result.matches("([xy])*stop"), "Unexpected produced text: " + result);
            if (result.length() > "stop".length()) {
                sawBounce = true;
            }
        }
        assertTrue(sawBounce, "Expected at least one run to bounce between A and B before stopping");
    }
}
