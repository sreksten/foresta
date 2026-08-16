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

    // ---- Line continuation (\) ----

    @Test
    void lineEndingWithBackslashIsJoinedWithNextLine() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\tHello \\\nworld\n");
        assertEquals("Hello world", bean.produce().get(0));
    }

    @Test
    void multipleConsecutiveContinuationsAreAllJoined() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\tHello \\\ncruel \\\nworld\n");
        assertEquals("Hello cruel world", bean.produce().get(0));
    }

    @Test
    void continuationAlsoWorksOnAProductionHeaderLine() throws Exception {
        GrammarBean bean = new GrammarBean("RO\\\nOT\n\tx\n");
        assertEquals("ROOT", bean.getRootNode());
        assertEquals("x", bean.produce().get(0));
    }

    @Test
    void danglingContinuationMarkerAtEndOfFileIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\tx \\\n"));
        assertTrue(ex.getMessage().contains("continuation"));
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
        // trailing "|" on a regular production's children line is already handled. The double
        // space this empty leaf would otherwise leave behind is collapsed by postProduce.
        GrammarBean bean = new GrammarBean("ROOT\n\tSomething {   |  } vuoto\n");
        assertEquals("Something vuoto", bean.produce().get(0));
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

    // ---- Raw literal spans ----

    @Test
    void quotedBracesAreKeptAsLiteralTextInsteadOfBecomingAGroup() throws Exception {
        // Without the quotes "{a}" would be parsed as an inline alternation group with a
        // single option "a", collapsing to just "a"; the quotes must make the braces literal.
        GrammarBean bean = new GrammarBean("ROOT\n\tvalue \"{a}\" here\n");
        assertEquals("value {a} here", bean.produce().get(0));
    }

    @Test
    void quotedPipeIsKeptAsLiteralTextInsteadOfSplitting() throws Exception {
        // Without the quotes this line would be split into two alternatives ("x " and " y");
        // the quotes must keep the whole thing as a single leaf.
        GrammarBean bean = new GrammarBean("ROOT\n\t\"x | y\"\n");
        assertEquals("x | y", bean.produce().get(0));
    }

    @Test
    void escapedQuoteInsideSpanProducesLiteralQuoteWithoutClosingIt() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t\"say \\\"hi\\\" now\"\n");
        assertEquals("say \"hi\" now", bean.produce().get(0));
    }

    @Test
    void unclosedQuoteAtEndOfLineIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t\"never closed\n"));
        assertTrue(ex.getMessage().contains("Missing closing '\"'"));
    }

    @Test
    void quotedPipeInsideInlineGroupOptionIsKeptAsLiteralText() throws Exception {
        // The group has two top-level options: the quoted "a|b" (a single literal option, its
        // "|" must not split it) and the plain "c".
        GrammarBean bean = new GrammarBean("ROOT\n\t{ \"a|b\" | c }\n");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 60; i++) {
            seen.add(bean.produce().get(0));
        }
        assertEquals(new HashSet<>(java.util.Arrays.asList("a|b", "c")), seen);
    }

    @Test
    void unclosedQuoteInsideInlineGroupBodyIsInvalid() {
        // The quote is never closed before end of line, so splitTopLevelAlternatives (run on the
        // whole raw line, before the group's braces are ever matched) reports the missing quote —
        // it never even gets to findMatchingClosingBrace/createInlineProduction.
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t{ \"a | b }\n"));
        assertTrue(ex.getMessage().contains("Missing closing '\"'"));
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
    void assignmentValueCanReferenceAnotherProduction() throws Exception {
        // The assignment's value is itself a bracketed production reference: it must be fully
        // resolved before being cached under "k", so "[#k]" retrieves the resolved word, not
        // the raw "[OTHER]" text.
        GrammarBean bean = new GrammarBean("ROOT\n\t[k=[OTHER]] [#k]\nOTHER\n\tresolved\n");
        assertEquals("resolved", bean.produce().get(0));
    }

    @Test
    void assignmentValueReferencingUndefinedProductionIsInvalidAtLoadTime() {
        assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t[k=[NEVER_DEFINED]] [#k]\n"));
    }

    @Test
    void fixedProductionMarkerCanTargetAnAssignmentOnlyKey() throws Exception {
        // "EQUIPAGGIAMENTO" is never declared as its own production, only ever assigned via
        // "=" elsewhere in the grammar; a later "[*EQUIPAGGIAMENTO]" must still be considered
        // valid at load time and resolve to the previously assigned value at runtime.
        GrammarBean bean = new GrammarBean(
                "ROOT\n\t[EQUIPAGGIAMENTO=[EQUIPAGGIAMENTO_POSSIBILE]] preso [*EQUIPAGGIAMENTO] di nuovo\n"
                        + "EQUIPAGGIAMENTO_POSSIBILE\n\tfucile\n");
        assertEquals("preso fucile di nuovo", bean.produce().get(0));
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

    @Test
    void deliberatelyEmptyAlternativeDoesNotLeaveADoubleSpace() throws Exception {
        // EMPTY's only alternative is the empty string; the literal spaces the ROOT
        // template puts on either side of [EMPTY] must collapse into a single space
        // instead of surviving as a double space.
        GrammarBean bean = new GrammarBean("ROOT\n\tleft [EMPTY] right\nEMPTY\n\t\n");
        assertEquals("left right", bean.produce().get(0));
    }

    @Test
    void aRunOfMoreThanTwoSpacesIsAlsoCollapsedToOne() throws Exception {
        // Three consecutive empty references, each flanked by a literal space, leave behind
        // a run of four spaces; MULTIPLE_SPACES_REGEX ("[ \t]{2,}") must collapse any such
        // run, not just exactly two spaces, down to a single one.
        GrammarBean bean = new GrammarBean("ROOT\n\ta [E1] [E2] [E3] b\nE1\n\t\nE2\n\t\nE3\n\t\n");
        assertEquals("a b", bean.produce().get(0));
    }

    @Test
    void aRunOfTabsIsAlsoCollapsedToOneSpace() throws Exception {
        // The template embeds literal tabs (not just spaces) around [EMPTY]; a run of two
        // consecutive tabs left behind by the empty reference must collapse to one space too.
        GrammarBean bean = new GrammarBean("ROOT\n\ta\t[EMPTY]\tb\nEMPTY\n\t\n");
        assertEquals("a b", bean.produce().get(0));
    }

    @Test
    void aMixedRunOfSpacesAndTabsIsAlsoCollapsedToOneSpace() throws Exception {
        // One literal space and one literal tab, back to back, straddling the empty
        // reference: a "run of whitespace" is not just same-character repetition.
        GrammarBean bean = new GrammarBean("ROOT\n\ta \t[EMPTY]\t b\nEMPTY\n\t\n");
        assertEquals("a b", bean.produce().get(0));
    }

    @Test
    void aTabBeforePunctuationIsAlsoRemoved() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\tleft\t[EMPTY].\nEMPTY\n\t\n");
        assertEquals("left.", bean.produce().get(0));
    }

    @Test
    void postProductionSubstitutionThatIntroducesAdjacentSpacesIsAlsoCollapsed() throws Exception {
        // The substitution's own "post" text (" bar ") has a space on both sides, right
        // where the surrounding literal text already has one too; the resulting double
        // spaces must still be collapsed, not just ones present before substitutions run.
        GrammarBean bean = new GrammarBean("ROOT\n\tx foo y\n", "foo: bar ");
        assertEquals("x bar y", bean.produce().get(0));
    }

    @Test
    void deliberatelyEmptyAlternativeBeforePunctuationDoesNotLeaveAStraySpace() throws Exception {
        // EMPTY's only alternative is the empty string, sitting right before the final
        // period; the literal space the ROOT template puts before [EMPTY] must be removed
        // instead of surviving as a space immediately before the period.
        GrammarBean bean = new GrammarBean("ROOT\n\tleft [EMPTY].\nEMPTY\n\t\n");
        assertEquals("left.", bean.produce().get(0));
    }

    @Test
    void spaceBeforePunctuationIsRemovedForEveryCommonMark() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\ta [EMPTY], b [EMPTY]; c [EMPTY]: d [EMPTY]! e [EMPTY]?\nEMPTY\n\t\n");
        assertEquals("a, b; c: d! e?", bean.produce().get(0));
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

    // ---- unbalanced brackets are rejected at load time ----

    @Test
    void unbalancedBracketIsRejectedAtLoadTime() {
        // "[k=X[Y]" has three '[' but only two ']': bracket matching is nesting-aware, so this
        // is caught as malformed as soon as the grammar is loaded, instead of silently producing
        // a broken token that only blows up later when the story is generated.
        assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t[k=X[Y] [#k]\n"));
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

    // ---- Weighted alternatives ----

    @Test
    void weightTokenIsStrippedFromAlternativeText() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t[^5] A|B\n");
        bean.setProductionMode(ProductionModeEnum.FIRST);
        assertEquals("A", bean.produce().get(0));
    }

    @Test
    void weightTokenOnLastAlternativeIsAlsoStripped() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\tA|[^3] B\n");
        bean.setProductionMode(ProductionModeEnum.LAST);
        assertEquals("B", bean.produce().get(0));
    }

    @Test
    void unmarkedAlternativesStillBehaveAsBeforeWhenMixedWithWeights() throws Exception {
        // The unweighted alternatives ("B", "C") must remain reachable exactly as before,
        // regardless of the presence of a weighted sibling ("A").
        GrammarBean bean = new GrammarBean("ROOT\n\t[^5] A|B|C\n");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            seen.add(bean.produce().get(0));
        }
        assertEquals(new HashSet<>(java.util.Arrays.asList("A", "B", "C")), seen);
    }

    @Test
    void zeroWeightIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t[^0] A\n"));
        assertTrue(ex.getMessage().contains("positive number"));
    }

    @Test
    void negativeWeightIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t[^-1] A\n"));
        assertTrue(ex.getMessage().contains("positive number"));
    }

    @Test
    void nonNumericWeightIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t[^abc] A\n"));
        assertTrue(ex.getMessage().contains("positive number"));
    }

    @Test
    void unclosedWeightTokenIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t[^5 A\n"));
        assertTrue(ex.getMessage().contains("Missing ']'"));
    }

    @Test
    void fractionalWeightTokenIsStrippedAndHonored() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t[^2.5] A|B\n");
        bean.setProductionMode(ProductionModeEnum.FIRST);
        assertEquals("A", bean.produce().get(0));
    }

    @Test
    void infiniteWeightIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t[^Infinity] A\n"));
        assertTrue(ex.getMessage().contains("positive number"));
    }

    @Test
    void nanWeightIsInvalid() {
        GrammarBean.InvalidGrammarException ex = assertThrows(GrammarBean.InvalidGrammarException.class,
                () -> new GrammarBean("ROOT\n\t[^NaN] A\n"));
        assertTrue(ex.getMessage().contains("positive number"));
    }

    @Test
    void heavilyWeightedAlternativeIsPickedProportionallyMoreOftenUnderRandomMode() throws Exception {
        // "A" is 20x as likely as "B" under RANDOM mode; over enough trials the observed
        // frequency ordering must reflect that, without asserting an exact ratio (to avoid
        // flakiness).
        GrammarBean bean = new GrammarBean("ROOT\n\t[^20] A|B\n");
        int countA = 0;
        int countB = 0;
        for (int i = 0; i < 2000; i++) {
            if ("A".equals(bean.produce().get(0))) {
                countA++;
            } else {
                countB++;
            }
        }
        assertTrue(countA > countB * 5, "Expected A to dominate B by a wide margin: A=" + countA + " B=" + countB);
    }

    @Test
    void fractionalWeightSkewsRandomSelectionProportionally() throws Exception {
        // "A" (weight 0.5) is a fifth as likely as "B" (weight 2.5) under RANDOM mode.
        GrammarBean bean = new GrammarBean("ROOT\n\t[^0.5] A|[^2.5] B\n");
        int countA = 0;
        int countB = 0;
        for (int i = 0; i < 2000; i++) {
            if ("A".equals(bean.produce().get(0))) {
                countA++;
            } else {
                countB++;
            }
        }
        assertTrue(countB > countA * 2, "Expected B to dominate A by a wide margin: A=" + countA + " B=" + countB);
    }

    @Test
    void weightedAlternativeInsideOneShotProductionIsRemovedAfterUse() throws Exception {
        // X has two weighted alternatives; consuming one via FIRST mode must remove exactly
        // that one (by reference, not by content), leaving the other one reachable afterward.
        GrammarBean bean = new GrammarBean("ROOT\n\t[X]\nX$\n\t[^5] a|[^3] b\n");
        bean.setProductionMode(ProductionModeEnum.FIRST);
        assertEquals("a", bean.produce().get(0));
        assertEquals("b", bean.produce().get(0));
    }

    @Test
    void weightedOptionInsideInlineGroupIsHonored() throws Exception {
        // The inline group becomes an on-the-fly production; its "x" option is heavily
        // weighted relative to "y", and this must be honored without any special handling.
        GrammarBean bean = new GrammarBean("ROOT\n\t{[^20] x|y}\n");
        int countX = 0;
        int countY = 0;
        for (int i = 0; i < 2000; i++) {
            if ("x".equals(bean.produce().get(0))) {
                countX++;
            } else {
                countY++;
            }
        }
        assertTrue(countX > countY * 5, "Expected x to dominate y by a wide margin: x=" + countX + " y=" + countY);
    }

    // ---- Descendant weight adjustment ----

    @Test
    void alternativeReferencingRicherProductionIsBoostedRelativeToPoorerOne() throws Exception {
        // RICH has ten leaf alternatives (aggregate weight 10), POOR has one (aggregate
        // weight 1); ROOT's two alternatives start out with the same base weight, but the
        // one referencing RICH must end up boosted well above the one referencing POOR.
        GrammarBean bean = new GrammarBean("ROOT\n\t[RICH]|[POOR]\nRICH\n\ta|b|c|d|e|f|g|h|i|j\nPOOR\n\tz\n");
        int richCount = 0;
        int poorCount = 0;
        for (int i = 0; i < 3000; i++) {
            if ("z".equals(bean.produce().get(0))) {
                poorCount++;
            } else {
                richCount++;
            }
        }
        assertTrue(richCount > poorCount * 2, "Expected RICH branch to dominate: rich=" + richCount + " poor=" + poorCount);
    }

    @Test
    void grammarWithNoCrossReferencesIsUnaffectedByDescendantAdjustment() throws Exception {
        // No alternative references another production, so findReferencedProductions
        // returns nothing for every one of them: weights must stay exactly as declared.
        GrammarBean bean = new GrammarBean("ROOT\n\t[^5] A|B|C\n");
        int countA = 0;
        int countOther = 0;
        for (int i = 0; i < 2000; i++) {
            if ("A".equals(bean.produce().get(0))) {
                countA++;
            } else {
                countOther++;
            }
        }
        // Same margin as heavilyWeightedAlternativeIsPickedProportionallyMoreOftenUnderRandomMode:
        // A (weight 5) vs B+C (weight 1 each, total 2) is a 5:2 ratio, well above 2x.
        assertTrue(countA > countOther * 2, "Expected A to dominate B/C: A=" + countA + " other=" + countOther);
    }

    @Test
    void assignmentAndReferenceTokensDoNotCountAsDescendantReferences() throws Exception {
        // "[k=X]" assigns a literal value and "[#k]" retrieves it; neither references a
        // production, so they must not contribute to any descendant-weight boost, and
        // must not be mistaken for an undefined-production reference either.
        assertDoesNotThrow(() -> new GrammarBean("ROOT\n\t[k=X] [#k]\n"));
    }

    @Test
    void fixedReferencesContributeToDescendantBoostLikePlainReferences() throws Exception {
        // [*RICH] and [!RICH] must count RICH's aggregate weight toward the boost exactly
        // like a plain [RICH] reference would.
        GrammarBean beanGlobal = new GrammarBean("ROOT\n\t[*RICH]|[POOR]\nRICH\n\ta|b|c|d|e|f|g|h|i|j\nPOOR\n\tz\n");
        GrammarBean beanLocal = new GrammarBean("ROOT\n\t[!RICH]|[POOR]\nRICH\n\ta|b|c|d|e|f|g|h|i|j\nPOOR\n\tz\n");
        for (GrammarBean bean : java.util.Arrays.asList(beanGlobal, beanLocal)) {
            int richCount = 0;
            int poorCount = 0;
            for (int i = 0; i < 3000; i++) {
                if ("z".equals(bean.produce().get(0))) {
                    poorCount++;
                } else {
                    richCount++;
                }
            }
            assertTrue(richCount > poorCount * 2, "Expected RICH branch to dominate: rich=" + richCount + " poor=" + poorCount);
        }
    }

    @Test
    void selfRecursiveProductionAdjustmentDoesNotHang() {
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(2), () ->
                new GrammarBean("LISTA\n\telemento [LISTA]|fine\n"));
    }

    @Test
    void mutuallyRecursiveProductionsAdjustmentDoesNotHang() {
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(2), () ->
                new GrammarBean("A\n\tx[B]|stop\nB\n\ty[A]|stop\n"));
    }

    @Test
    void threeWayCyclicProductionsAdjustmentDoesNotHang() {
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(2), () ->
                new GrammarBean("A\n\t[B]|stopA\nB\n\t[C]|stopB\nC\n\t[A]|stopC\n"));
    }

    // ---- Capitalize marker ----

    @Test
    void capitalizeMarkerCapitalizesPlainProductionResult() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t^[X]\nX\n\thello\n");
        assertEquals("Hello", bean.produce().get(0));
    }

    @Test
    void referenceWithoutMarkerIsUnaffected() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t[X]\nX\n\thello\n");
        assertEquals("hello", bean.produce().get(0));
    }

    @Test
    void caretNotImmediatelyFollowedByBracketIsLiteralText() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\tpower^2 [X]\nX\n\thello\n");
        assertEquals("power^2 hello", bean.produce().get(0));
    }

    @Test
    void capitalizeMarkerWorksOnGloballyFixedProduction() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t^[*NAME]\nNAME\n\treal\n");
        assertEquals("Real", bean.produce().get(0));
    }

    @Test
    void capitalizeMarkerWorksOnLocallyFixedProduction() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t^[!NAME]\nNAME\n\treal\n");
        assertEquals("Real", bean.produce().get(0));
    }

    @Test
    void capitalizeMarkerWorksOnStoredValueReference() throws Exception {
        // [!NAME] caches "value" under key "NAME"; [#NAME] retrieves it, and the leading
        // ^ capitalizes that retrieved copy without affecting the first, uncapitalized one.
        GrammarBean bean = new GrammarBean("ROOT\n\t[!NAME]^[#NAME]\nNAME\n\tvalue\n");
        assertEquals("valueValue", bean.produce().get(0));
    }

    @Test
    void capitalizeMarkerOnEmptyResolutionIsNoOp() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\ta^[X]b\nX\n\thello|\n");
        bean.setProductionMode(ProductionModeEnum.LAST);
        assertEquals("ab", bean.produce().get(0));
    }

    @Test
    void capitalizeMarkerHandlesAccentedFirstLetter() throws Exception {
        GrammarBean bean = new GrammarBean("ROOT\n\t^[X]\nX\n\tàlbero\n");
        assertEquals("Àlbero", bean.produce().get(0));
    }
}
