package com.threeamigos.foresta.motore;

import java.io.*;
import java.util.*;

/**
 * Reads a text-based grammar file and uses it to randomly generate ("produce") text
 * by recursively expanding production references.
 * <p>
 * Grammar file syntax:
 * <ul>
 *     <li>A line starting at column 0 declares a <b>production</b>: its name.</li>
 *     <li>Lines indented with a TAB or a space are the production's <b>children</b>
 *     (alternatives): the text that can be randomly chosen when the production is
 *     expanded. Multiple alternatives can be listed on the same line, separated by
 *     {@code |}.</li>
 *     <li>A child can reference another production by wrapping its name in square
 *     brackets, e.g. {@code [OtherProduction]}; when expanded, the reference is
 *     replaced by one randomly chosen (and in turn recursively expanded) alternative
 *     of {@code OtherProduction}.</li>
 *     <li>A child can contain an <b>inline alternation group</b>,
 *     {@code {opt1|opt2|...}}: at load time this is transformed into a brand new,
 *     auto-named production (see {@link #expandInlineAlternations}) and the group is
 *     replaced with a reference to it, so from then on it behaves exactly like a
 *     regular {@code [Name]} reference. Example:
 *     <pre>{@code
 *     FATTO_PROEMIO
 *         Il {vecchio|giovane|misterioso} viandante bussò alla porta
 *     }</pre></li>
 *     <li>A span delimited by double quotes ({@code "..."}) is a <b>raw literal
 *     span</b>: {@code {}}, {@code }}} and {@code |} inside it are treated as plain
 *     text instead of inline-alternation-group syntax, and {@code \"} inside it is an
 *     escaped literal quote that does not close the span. A span left open at the end
 *     of the line (or of an inline alternation group's content) is an error. Note
 *     that this only protects {@code {}}, {@code }}} and {@code |}: literal
 *     {@code [Name]}-looking text inside a span would still be parsed as a
 *     production reference.</li>
 *     <li>A production name ending with {@code $} is <b>one-shot</b>: once one of
 *     its alternatives is chosen during a production cycle, that alternative (and,
 *     if it was the last one left, the whole production and every reference to it)
 *     is removed for the rest of the cycle.</li>
 *     <li>A reference prefixed with {@code *} (e.g. {@code [*Name]}) is a
 *     <b>globally fixed production</b>: the first time it is expanded within a call
 *     to {@link #produce()}, the result is cached and reused for every subsequent
 *     occurrence, anywhere in the produced text.</li>
 *     <li>A reference prefixed with {@code !} (e.g. {@code [!Name]}) is a
 *     <b>locally fixed production</b>: like the global one, but the cached value is
 *     only reused within the same production subtree (i.e. it does not leak into
 *     sibling branches produced independently).</li>
 *     <li>A reference of the form {@code [key=value]} <b>assigns</b> a literal value
 *     to {@code key}, to be later retrieved with a {@code #} reference.</li>
 *     <li>A reference prefixed with {@code #} (e.g. {@code [#key]}) retrieves the
 *     value previously assigned to {@code key} (locally first, then globally).</li>
 *     <li>A line starting with {@code #} is a full-line comment and is ignored, as
 *     are empty lines.</li>
 *     <li>An alternative may start with a {@code [^N]} token ({@code N} a positive
 *     number, e.g. {@code 10} or {@code 2.5}) to give it a selection <b>weight</b>: under
 *     {@link ProductionModeEnum#RANDOM} it is picked {@code N} times as often as an
 *     alternative with the default weight of 1.
 *     The token is resolved and stripped when the grammar is loaded (unlike
 *     {@code [key=value]}, which is resolved lazily during production), so it never appears
 *     in produced text. An alternative with no {@code [^N]} token defaults to weight 1,
 *     exactly matching the uniform selection every alternative had before this feature
 *     existed — including the auto-named productions created from inline alternation
 *     groups, whose options may optionally carry a {@code [^N]} token of their own but need
 *     no special handling to stay uniformly random.</li>
 *     <li>Once the whole grammar has been parsed and validated, every alternative's
 *     weight is automatically boosted by {@link #SCALE_FACTOR} times the aggregate
 *     weight of every production it references (recursively, since a referenced
 *     production's own weight has by then already been boosted by whatever it in turn
 *     references), so alternatives that expand into structurally richer subtrees get
 *     picked proportionally more often under {@link ProductionModeEnum#RANDOM} without
 *     requiring a hand-written {@code [^N]} token. A production referenced more than
 *     once in the same alternative contributes to the boost once per occurrence. This
 *     adjustment is computed once, at grammar-load time, and baked into the weights
 *     used for the rest of the {@code GrammarBean}'s lifetime. Reference cycles
 *     (self- or mutually-recursive productions) are handled by using a production's
 *     original, not-yet-boosted weight wherever it is re-encountered while its own
 *     boost is still being computed — deterministic for a given grammar, but not
 *     symmetric between structurally identical cycle members (see
 *     {@link #adjustWeightsForDescendants}).</li>
 * </ul>
 * A second, optional post-production file lists literal text substitutions
 * ({@code pre:post}) applied to the final produced text, to fix natural-language
 * issues arising from the mechanical concatenation (e.g., in Italian, {@code "a il"}
 * becoming {@code "al"}).
 * <p>
 * Every production is expected to produce a trimmed string (see
 * {@link #resolvePlainProduction} and {@link #resolveFixedProduction}): any leading or
 * trailing whitespace left over once a production's chosen alternative has been fully
 * expanded is stripped before it is spliced into whatever referenced it. This matters in
 * particular for inline alternation groups, where a nested group's empty leaf, chosen next
 * to literal text, would otherwise leave a stray space behind.
 * <p>
 * That trimming only ever protects the edges of a single reference's own expansion: a
 * production reference that intentionally resolves to an empty string (e.g. an
 * alternative meant to represent "no extra text here") still leaves behind the literal
 * spaces the surrounding template text put on either side of the reference, producing a
 * double space, or, if the reference sits right before a punctuation mark at the end of a
 * sentence, a single stray space before that punctuation. To keep such deliberately-empty
 * alternatives usable without requiring every template to special-case them, any run of
 * two or more spaces left in the final produced text is automatically collapsed into a
 * single space, and any space left immediately before a punctuation mark is removed,
 * once, after every post-production substitution has been applied (see
 * {@link #postProduce}).
 */
public class GrammarBean {

	private static final String PRODUCTION = "Production ";
	private static final String LINE = "Line ";
	/**
	 * Prefix marking a full-line comment in the grammar/post-production source files.
	 */
	private static final String COMMENT_PREFIX = "#";
	/**
	 * Characters marking a line as a child (alternative) of the current production: a tab or a space.
	 */
	private static final String CHILD_LINE_PREFIX_CHARS = "\t ";
	/**
	 * Suffix marking a production as one-shot (removed once used within a cycle). A single
	 * character rather than {@code {1}} to avoid visual confusion with inline alternation
	 * groups ({@link #OPENING_BRACE}/{@link #CLOSING_BRACE}), which use the same braces.
	 */
	private static final String ONE_SHOT_MARKER = "$";
	private static final String OPENING_BRACKET = "[";
	private static final String CLOSING_BRACKET = "]";
	/**
	 * Characters delimiting an inline alternation group in a leaf ({@code {opt1|opt2|...}}).
	 */
	private static final char OPENING_BRACE = '{';
	private static final char CLOSING_BRACE = '}';
	/**
	 * Character delimiting a raw literal span in a leaf ({@code "..."}), inside which
	 * {@link #OPENING_BRACE}, {@link #CLOSING_BRACE} and {@code |} are treated as plain text.
	 */
	private static final char QUOTE_CHAR = '"';
	/**
	 * Character escaping a {@link #QUOTE_CHAR} inside a raw literal span so it doesn't close it.
	 */
	private static final char ESCAPE_CHAR = '\\';
	/**
	 * Prefix used to name productions auto-generated from inline alternation groups.
	 */
	private static final String INLINE_PRODUCTION_PREFIX = "PROD_";
	/**
	 * Marker for a production whose result is fixed globally, for the whole call to produce().
	 */
	private static final String GLOBAL_FIXED_PRODUCTION_MARKER = "*";
	/**
	 * Marker for a production whose result is fixed locally, within the current production tree.
	 */
	private static final String LOCAL_FIXED_PRODUCTION_MARKER = "!";
	/**
	 * Marker assigning a literal value to a fixed production key (key=value).
	 */
	private static final String ASSIGNMENT_MARKER = "=";
	/**
	 * Marker referencing a previously fixed production's value.
	 */
	private static final String REFERENCE_MARKER = "#";
	/**
	 * Marker prefixing an alternative's leading {@code [^N]} weight token (e.g.
	 * {@code [^10] some text} or {@code [^2.5] some text}), used to bias random selection
	 * towards it; see {@link #parseWeight}. Resolved and stripped at grammar-load time,
	 * unlike {@link #ASSIGNMENT_MARKER}/{@link #REFERENCE_MARKER} which are resolved
	 * lazily at production time.
	 */
	private static final String WEIGHT_MARKER = "^";
	/**
	 * Scale factor applied to a referenced production's aggregate weight when boosting the
	 * weight of an alternative that references it; see {@link #adjustWeightsForDescendants}.
	 */
	private static final double SCALE_FACTOR = 0.33;
	/**
	 * Separator between the pre- and post-production text in the post-production file.
	 */
	private static final String POST_PRODUCTION_SEPARATOR = ":";
	/**
	 * Regex splitting the produced text into individual lines.
	 */
	private static final String LINE_BREAK_REGEX = "\\n";
	/**
	 * Regex matching a run of two or more spaces, collapsed to one by {@link #postProduce}.
	 */
	private static final String MULTIPLE_SPACES_REGEX = " {2,}";
	/**
	 * Regex matching one or more spaces immediately before a sentence/clause punctuation
	 * mark, removed by {@link #postProduce}.
	 */
	private static final String SPACE_BEFORE_PUNCTUATION_REGEX = " +(?=[.,;:!?])";
	/**
	 * Root production from which the generation process starts.
	 */
	private String rootNode = null;
	/**
	 * Map of all available productions
	 */
	private final Map<String, List<WeightedAlternative>> productionsMap = new HashMap<>();
	/**
	 * Map of all available productions to be reused in a cycle of productions
	 */
	private final Map<String, List<WeightedAlternative>> currentProductionsMap = new HashMap<>();
	/**
	 * Fixed productions map
	 */
	private final Map<String, String> globalFixedProductions = new HashMap<>();
	/**
	 * Map of all productions that happen after the main production has finished, to adjust the result fixing natural language grammar issues
	 * (e.g., in the Italian language, 'a il' is transformed to 'al').
	 */
	private final Map<String, String> postProductions = new HashMap<>();
	/**
	 * To randomly choose a production
	 */
	private final Random rnd = new Random(System.currentTimeMillis());
	/**
	 * Controls how a production's alternative is picked; see {@link ProductionModeEnum}.
	 * Defaults to {@link ProductionModeEnum#RANDOM}. Setting it to {@link ProductionModeEnum#FIRST}
	 * or {@link ProductionModeEnum#LAST} makes {@link #produce()}/{@link #produce(String)} fully
	 * deterministic, which is mostly useful for testing.
	 */
	private ProductionModeEnum productionMode = ProductionModeEnum.RANDOM;
	/**
	 * Productions that must be removed once used within a cycle
	 */
	private final Set<String> oneShotProductions = new HashSet<>();
	/**
	 * Progressive counter used to name productions auto-generated from inline
	 * alternation groups ({@link #INLINE_PRODUCTION_PREFIX} + this counter).
	 */
	private int inlineProductionCounter = 0;

	/**
	 * An alternative's text together with its selection weight (see {@link #WEIGHT_MARKER},
	 * {@link #parseWeight}). Immutable; instances are shared between {@link #productionsMap}
	 * and {@link #currentProductionsMap} copies made by {@link #reset()}.
	 */
	private static final class WeightedAlternative {
		private final String text;
		private final double weight;

		private WeightedAlternative(String text, double weight) {
			this.text = text;
			this.weight = weight;
		}
	}

	/**
	 * Builds a grammar from a source string, with no post-production substitutions.
	 * @param grammar the grammar source text, see the class documentation for its syntax
	 * @throws InvalidGrammarException if the text does not describe a valid grammar
	 * @throws IOException if the text cannot be read
	 */
	public GrammarBean(String grammar) throws InvalidGrammarException, IOException {
		this(grammar, null);
	}

	/**
	 * Builds a grammar from a source string and an optional post-production string.
	 * @param grammar the grammar source text, see the class documentation for its syntax
	 * @param postProduction the optional (may be {@code null}) post-production text, listing
	 *                       {@code pre:post} literal text substitutions applied to produced text
	 * @throws InvalidGrammarException if either text does not describe a valid grammar
	 * @throws IOException if either text cannot be read
	 */
	public GrammarBean(String grammar, String postProduction) throws InvalidGrammarException, IOException {
		this(new ByteArrayInputStream(grammar.getBytes()),
				postProduction == null ? null : new ByteArrayInputStream(postProduction.getBytes()));
	}

	/**
	 * Builds a grammar from a source file and an optional post-production file.
	 * @param grammar the grammar source file, see the class documentation for its syntax
	 * @param postProduction the optional (may be {@code null}) post-production file, listing
	 *                        {@code pre:post} literal text substitutions applied to produced text
	 * @throws InvalidGrammarException if either file does not describe a valid grammar
	 * @throws IOException if either file cannot be read
	 */
	public GrammarBean(InputStream grammar, InputStream postProduction) throws InvalidGrammarException, IOException {
		setSourceFile(grammar);
		setPostProductionFile(postProduction);
		reset();
	}

	/**
	 * Reads and parses the grammar file.
	 */
	private void setSourceFile(InputStream inputStream) throws InvalidGrammarException, IOException {
		readSourceFileAndCreateProductionsMap(inputStream);
		checkProductionsValidity();
		adjustWeightsForDescendants();
	}

	/**
	 * Reads the grammar file line by line, populating {@link #productionsMap}: each
	 * unindented line starts a new production (see {@link #handleProduction}), each
	 * TAB/space-indented line adds children to the current production (see
	 * {@link #handleChildren}). Comments and empty lines are skipped.
	 */
	private void readSourceFileAndCreateProductionsMap(InputStream inputStream) throws IOException, InvalidGrammarException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String currentProduction = null;
			String previousProduction = null;
			int currentLineNumber = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				currentLineNumber++;
				if (line.isEmpty() || line.startsWith(COMMENT_PREFIX)) {
					continue;
				}
				if (CHILD_LINE_PREFIX_CHARS.indexOf(line.charAt(0)) >= 0) {
					handleChildren(line, currentLineNumber, currentProduction);
				} else {
					checkPreviousProduction(currentLineNumber, previousProduction);
					previousProduction = currentProduction;
					currentProduction = handleProduction(currentLineNumber, line);
				}
			}
			checkPreviousProduction(currentLineNumber, currentProduction);
		}
	}

	/**
	 * Checks that the production just closed off by reaching a new production header
	 * (or the end of file) has at least one child, since a production that produces
	 * nothing would break generation later on.
	 * @throws InvalidGrammarException if {@code previousProduction} has no children at all
	 */
	private void checkPreviousProduction(int currentLineNumber, String previousProduction) throws InvalidGrammarException {
		if (previousProduction != null) {
			List<WeightedAlternative> previousProductionChildren = productionsMap.get(previousProduction);
			if (previousProductionChildren.isEmpty()) {
				throw new InvalidGrammarException(LINE + currentLineNumber + ": Production " + previousProduction + " does not produce anything.");
			}
		}
	}

	/**
	 * Registers a new production header line: strips the {@link #ONE_SHOT_MARKER}
	 * suffix if present (marking the production as one-shot), sets it as the
	 * {@link #rootNode} if it is the first production found, and adds it (initially
	 * childless) to {@link #productionsMap}.
	 * @return the plain production name (without the one-shot marker)
	 * @throws InvalidGrammarException if a production with the same name was already declared
	 */
	private String handleProduction(int currentLineNumber, String line) throws InvalidGrammarException {
		String currentProduction = line.trim();
		if (currentProduction.endsWith(ONE_SHOT_MARKER)) {
			currentProduction = currentProduction.substring(0, currentProduction.length() - ONE_SHOT_MARKER.length());
			Logger.log("One-shot production: " + currentProduction);
			oneShotProductions.add(currentProduction);
		}
		if (rootNode == null) {
			rootNode = currentProduction;
		}
		if (productionsMap.containsKey(currentProduction)) {
			throw new InvalidGrammarException(LINE + currentLineNumber + ": Production " + currentProduction + " already found.");
		}
		productionsMap.put(currentProduction, new ArrayList<>());
		return currentProduction;
	}

	/**
	 * Adds the alternatives found on an indented line as children of {@code currentProduction}.
	 * The line is first split into top-level alternatives on {@code |}
	 * (ignoring any {@code |} found inside a {@code {...}} inline alternation group, see
	 * {@link #splitTopLevelAlternatives}); each alternative then has its leading {@code [^N]}
	 * weight token, if any, parsed off (see {@link #parseWeight}) before its inline alternation
	 * groups are expanded into auto-generated productions (see {@link #expandInlineAlternations}).
	 * @throws InvalidGrammarException if the line is not preceded by a production header,
	 *                                  it contains a malformed inline alternation group, or a
	 *                                  malformed weight token
	 */
	private void handleChildren(String line, int currentLine, String currentProduction) throws InvalidGrammarException {
		if (currentProduction == null) {
			throw new InvalidGrammarException(LINE + currentLine + ": Missing parent production. Lines beginning with TAB or space must be preceded by a production.");
		} else {
			List<WeightedAlternative> currentProductionChildren = productionsMap.get(currentProduction);
			for (String child : splitTopLevelAlternatives(line, currentLine)) {
				WeightedAlternative weighted = parseWeight(child.trim(), currentLine);
				currentProductionChildren.add(new WeightedAlternative(expandInlineAlternations(weighted.text, currentLine), weighted.weight));
			}
		}
	}

	/**
	 * Parses and strips a leading {@code [^N]} weight token from {@code trimmedChild}, if
	 * present, so that this alternative is picked {@code N} times as often as one with the
	 * default weight of 1 (see {@link #getProduction}). {@code N} may be fractional (e.g.
	 * {@code [^2.5]}). An alternative with no such token gets weight 1, exactly matching the
	 * uniform selection behavior of every alternative before this feature existed.
	 * @return a {@link WeightedAlternative} holding the remaining text and the parsed weight
	 *         (or weight 1 and the untouched text, if no weight token is present)
	 * @throws InvalidGrammarException if a weight token is opened but not closed, or its
	 *                                  value is not a positive number
	 */
	private WeightedAlternative parseWeight(String trimmedChild, int currentLineNumber) throws InvalidGrammarException {
		String weightTokenStart = OPENING_BRACKET + WEIGHT_MARKER;
		if (!trimmedChild.startsWith(weightTokenStart)) {
			return new WeightedAlternative(trimmedChild, 1);
		}
		int closingBracketIndex = trimmedChild.indexOf(CLOSING_BRACKET);
		if (closingBracketIndex < 0) {
			throw new InvalidGrammarException(LINE + currentLineNumber + ": Missing '" + CLOSING_BRACKET + "' for weight token");
		}
		String weightText = trimmedChild.substring(weightTokenStart.length(), closingBracketIndex);
		double weight;
		try {
			weight = Double.parseDouble(weightText);
		} catch (NumberFormatException e) {
			throw new InvalidGrammarException(LINE + currentLineNumber + ": Invalid weight '" + weightText + "': must be a positive number");
		}
		if (!(weight > 0) || Double.isInfinite(weight)) {
			throw new InvalidGrammarException(LINE + currentLineNumber + ": Invalid weight " + weightText + ": must be a positive number");
		}
		return new WeightedAlternative(trimmedChild.substring(closingBracketIndex + 1).trim(), weight);
	}

	/**
	 * Splits an indented line into its top-level alternatives on {@code |},
	 * treating any {@code |} found between an opening and matching closing curly brace as part of
	 * an inline alternation group rather than an alternative separator, so that a line like
	 * <code>A {a|b} | C</code> is split into two pieces (<code>A {a|b} </code> and <code> C</code>),
	 * not four. A {@code |} (or {@code {}}/{@code }}}) found inside a raw literal span
	 * ({@code "..."}) is likewise never treated as a separator; quotes and escapes are left
	 * untouched here (they are resolved later, in {@link #expandInlineAlternations}).
	 * @throws InvalidGrammarException if a raw literal span is opened but never closed
	 */
	private List<String> splitTopLevelAlternatives(String line, int currentLineNumber) throws InvalidGrammarException {
		List<String> alternatives = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		int braceDepth = 0;
		boolean inQuotes = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (inQuotes) {
				if (c == ESCAPE_CHAR && i + 1 < line.length() && line.charAt(i + 1) == QUOTE_CHAR) {
					current.append(c).append(line.charAt(i + 1));
					i++;
				} else if (c == QUOTE_CHAR) {
					inQuotes = false;
					current.append(c);
				} else {
					current.append(c);
				}
			} else if (c == QUOTE_CHAR) {
				inQuotes = true;
				current.append(c);
			} else if (c == OPENING_BRACE) {
				braceDepth++;
				current.append(c);
			} else if (c == CLOSING_BRACE) {
				braceDepth--;
				current.append(c);
			} else if (c == '|' && braceDepth == 0) {
				alternatives.add(current.toString());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		if (inQuotes) {
			throw new InvalidGrammarException(LINE + currentLineNumber + ": Missing closing '" + QUOTE_CHAR + "'");
		}
		alternatives.add(current.toString());
		return alternatives;
	}

	/**
	 * Expands every top-level inline alternation group (<code>{opt1|opt2|...}</code>) found in
	 * {@code child} into a reference to a brand new, auto-named production (see
	 * {@link #createInlineProduction}), so that the returned text can be treated exactly like any
	 * other leaf from then on. A group's own content may contain further nested groups (e.g.
	 * <code>{a|{b|c}}</code>); those are expanded recursively by {@link #createInlineProduction} into
	 * their own on-the-fly productions before the outer group is registered.
	 * @throws InvalidGrammarException if an opening curly brace is not matched by a closing one,
	 *                                  if a raw literal span is opened but never closed, or if the
	 *                                  resulting inline production would be empty
	 */
	private String expandInlineAlternations(String child, int currentLineNumber) throws InvalidGrammarException {
		StringBuilder result = new StringBuilder();
		int index = 0;
		int openingBraceIndex;
		while ((openingBraceIndex = findNextUnquotedOpeningBrace(child, index)) >= 0) {
			int closingBraceIndex = findMatchingClosingBrace(child, openingBraceIndex, currentLineNumber);
			result.append(unescapeQuotes(child.substring(index, openingBraceIndex)));
			String inlineOptionsText = child.substring(openingBraceIndex + 1, closingBraceIndex);
			String inlineProductionName = createInlineProduction(inlineOptionsText, currentLineNumber);
			result.append(OPENING_BRACKET).append(inlineProductionName).append(CLOSING_BRACKET);
			index = closingBraceIndex + 1;
		}
		result.append(unescapeQuotes(child.substring(index)));
		return result.toString();
	}

	/**
	 * Finds the index of the next {@link #OPENING_BRACE} at or after {@code fromIndex} that is not
	 * inside a raw literal span ({@code "..."}), so that e.g., in <code>"a {b} c"</code> the whole
	 * text (including the brace) is treated as literal, not as an inline alternation group.
	 * @return the index of the next unquoted opening brace, or {@code -1} if there is none
	 */
	private int findNextUnquotedOpeningBrace(String child, int fromIndex) {
		boolean inQuotes = false;
		for (int i = fromIndex; i < child.length(); i++) {
			char c = child.charAt(i);
			if (inQuotes) {
				if (c == ESCAPE_CHAR && i + 1 < child.length() && child.charAt(i + 1) == QUOTE_CHAR) {
					i++;
				} else if (c == QUOTE_CHAR) {
					inQuotes = false;
				}
			} else if (c == QUOTE_CHAR) {
				inQuotes = true;
			} else if (c == OPENING_BRACE) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Strips the {@link #QUOTE_CHAR} delimiters of every raw literal span found in {@code text},
	 * turning a {@code \"} escape into a literal {@code "}. This is the single point where a raw
	 * literal span's markers are actually resolved: earlier scans ({@link #splitTopLevelAlternatives},
	 * {@link #findMatchingClosingBrace}, {@link #findNextUnquotedOpeningBrace}) only need to know
	 * where a span is, not strip it, since doing so before group detection is finished would make a
	 * quote-protected {@link #OPENING_BRACE}/{@link #CLOSING_BRACE} indistinguishable from real syntax.
	 */
	private String unescapeQuotes(String text) {
		StringBuilder result = new StringBuilder();
		boolean inQuotes = false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (inQuotes) {
				if (c == ESCAPE_CHAR && i + 1 < text.length() && text.charAt(i + 1) == QUOTE_CHAR) {
					result.append(QUOTE_CHAR);
					i++;
				} else if (c == QUOTE_CHAR) {
					inQuotes = false;
				} else {
					result.append(c);
				}
			} else if (c == QUOTE_CHAR) {
				inQuotes = true;
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * Finds the closing curly brace matching the opening one at {@code openingBraceIndex}, honoring
	 * nesting depth so that e.g. in <code>{a|{b|c}}</code> the outer group's match is the very last
	 * <code>}</code>, not the first one encountered.
	 * A {@link #OPENING_BRACE}/{@link #CLOSING_BRACE} found inside a raw literal span
	 * ({@code "..."}) does not affect the depth count, so a group's own text may contain
	 * literal braces without breaking its matching.
	 * @throws InvalidGrammarException if no matching closing brace exists
	 */
	private int findMatchingClosingBrace(String child, int openingBraceIndex, int currentLineNumber) throws InvalidGrammarException {
		int depth = 0;
		boolean inQuotes = false;
		for (int i = openingBraceIndex; i < child.length(); i++) {
			char c = child.charAt(i);
			if (inQuotes) {
				if (c == ESCAPE_CHAR && i + 1 < child.length() && child.charAt(i + 1) == QUOTE_CHAR) {
					i++;
				} else if (c == QUOTE_CHAR) {
					inQuotes = false;
				}
			} else if (c == QUOTE_CHAR) {
				inQuotes = true;
			} else if (c == OPENING_BRACE) {
				depth++;
			} else if (c == CLOSING_BRACE) {
				depth--;
				if (depth == 0) {
					return i;
				}
			}
		}
		throw new InvalidGrammarException(LINE + currentLineNumber + ": Missing '" + CLOSING_BRACE
				+ "' for inline alternation started at '" + child.substring(openingBraceIndex) + "'");
	}

	/**
	 * Creates a new production out of an inline alternation group's content, named
	 * {@link #INLINE_PRODUCTION_PREFIX} followed by a unique progressive number, and
	 * registers it in {@link #productionsMap}. Alternatives are split with
	 * {@link #splitTopLevelAlternatives} (not a naive regex split) so that a nested group's own
	 * <code>|</code> separators aren't mistaken for top-level ones, and each alternative is passed
	 * back through {@link #expandInlineAlternations} so any nested group it contains becomes its
	 * own on-the-fly production before this one is registered. A blank alternative (e.g. the
	 * trailing empty option in {@code {big|fierce|}}) is kept as an empty-string leaf, exactly like
	 * a trailing {@code |} on a regular production's children line — this is what lets an inline
	 * group express an optional element (e.g. an optional adjective). Each option may also start
	 * with a (possibly fractional) {@code [^N]} weight token (see {@link #parseWeight}), letting
	 * an on-the-fly production be weighted, though this is entirely opt-in: an option with no such
	 * token gets the default weight of 1, so inline groups need no special handling to stay
	 * uniformly random.
	 * @param inlineOptionsText the raw text between the opening and closing curly braces
	 * @return the name of the newly created production
	 * @throws InvalidGrammarException if a nested inline alternation group is malformed, or an
	 *                                  option's weight token is malformed
	 */
	private String createInlineProduction(String inlineOptionsText, int currentLineNumber) throws InvalidGrammarException {
		List<WeightedAlternative> options = new ArrayList<>();
		for (String option : splitTopLevelAlternatives(inlineOptionsText, currentLineNumber)) {
			WeightedAlternative weighted = parseWeight(option.trim(), currentLineNumber);
			options.add(new WeightedAlternative(expandInlineAlternations(weighted.text, currentLineNumber), weighted.weight));
		}
		String name;
		do {
			name = INLINE_PRODUCTION_PREFIX + inlineProductionCounter++;
		} while (productionsMap.containsKey(name));
		productionsMap.put(name, options);
		return name;
	}

	/**
	 * Walks every child of every production looking for {@code [Name]} references and
	 * validates each of them via {@link #checkTokenValidity}. Called once, right after
	 * the whole grammar file (including any inline-alternation-generated productions)
	 * has been parsed.
	 * @throws InvalidGrammarException if a reference is malformed or points to an undefined production
	 */
	private void checkProductionsValidity() throws InvalidGrammarException {
        for (String key : productionsMap.keySet()) {
            for (WeightedAlternative alternative : productionsMap.get(key)) {
                String thisProduction = alternative.text;
                int openingBracketIndex;
                int closingBracketIndex;
                while ((openingBracketIndex = thisProduction.indexOf(OPENING_BRACKET)) >= 0) {
                    closingBracketIndex = thisProduction.indexOf(CLOSING_BRACKET, openingBracketIndex + 1);
                    if (closingBracketIndex == -1) {
                        throw new InvalidGrammarException("Missing '" + CLOSING_BRACKET + "' element after token " + thisProduction);
                    } else {
                        checkTokenValidity(thisProduction.substring(openingBracketIndex + 1, closingBracketIndex));
                        thisProduction = thisProduction.substring(closingBracketIndex + 1);
                    }
                }
            }
        }
	}

	/**
	 * Validates the body of a single {@code [...]} reference (a fixed-production marker,
	 * an assignment, a value reference, or a plain production name).
	 * @throws InvalidGrammarException if a fixed-production marker has no name after it,
	 *                                  an assignment marker has no key before it, or the
	 *                                  token is a plain name that is not a defined production
	 */
	private void checkTokenValidity(String thisProduction) throws InvalidGrammarException {
		if (thisProduction.startsWith(GLOBAL_FIXED_PRODUCTION_MARKER) || thisProduction.startsWith(LOCAL_FIXED_PRODUCTION_MARKER)) {
			thisProduction = thisProduction.substring(1);
			if (thisProduction.isEmpty()) {
				throw new InvalidGrammarException("Empty fixed production present (" + GLOBAL_FIXED_PRODUCTION_MARKER
						+ " or " + LOCAL_FIXED_PRODUCTION_MARKER + " alone)");
			}
		}
		if (thisProduction.startsWith(ASSIGNMENT_MARKER)) {
			throw new InvalidGrammarException(ASSIGNMENT_MARKER + " symbol must be preceded by a node name");
		}
		if (productionsMap.get(thisProduction) == null && !thisProduction.startsWith(REFERENCE_MARKER) && thisProduction.indexOf(ASSIGNMENT_MARKER) < 0) {
			throw new InvalidGrammarException(PRODUCTION + thisProduction + " is not defined");
		}
	}

	/**
	 * Boosts every alternative's weight by {@link #SCALE_FACTOR} times the (recursively
	 * adjusted) aggregate weight of every production it references (see
	 * {@link #findReferencedProductions}), so alternatives that expand into structurally
	 * richer subtrees are picked proportionally more often under
	 * {@link ProductionModeEnum#RANDOM}, without needing a hand-written {@code [^N]}
	 * token on every alternative. Called once, right after the whole grammar (including
	 * inline-alternation-group productions) has been parsed and validated, and before
	 * {@link #reset()} ever copies {@link #productionsMap} into
	 * {@link #currentProductionsMap}.
	 */
	private void adjustWeightsForDescendants() {
		Map<String, Double> aggregateWeightCache = new HashMap<>();
		Set<String> inProgressProductions = new HashSet<>();
		for (String name : productionsMap.keySet()) {
			computeAdjustedAggregateWeight(name, aggregateWeightCache, inProgressProductions);
		}
	}

	/**
	 * Returns the aggregate (sum of all alternatives') weight of the production {@code name}
	 * after recursively boosting each of its alternatives via
	 * {@link #adjustWeightsForDescendants}, replacing its entry in {@link #productionsMap}
	 * with the boosted alternatives as a side effect. Results are memoized in
	 * {@code aggregateWeightCache}.
	 * <p>
	 * Cycle-safe: production references may form cycles (self- or mutually-recursive
	 * productions). A production still being computed higher up the call stack (tracked in
	 * {@code inProgressProductions}) is not re-entered; its current, not-yet-boosted
	 * alternatives are summed directly instead, breaking the cycle with that production's
	 * original base weight. For a cycle spanning more than one production, whichever member
	 * is reached first ends up with a bigger boost than the other(s): deterministic for a
	 * given grammar, but not symmetric between structurally identical cycle members.
	 */
	private double computeAdjustedAggregateWeight(String name, Map<String, Double> aggregateWeightCache, Set<String> inProgressProductions) {
		Double cached = aggregateWeightCache.get(name);
		if (cached != null) {
			return cached;
		}
		List<WeightedAlternative> alternatives = productionsMap.get(name);
		if (!inProgressProductions.add(name)) {
			double baseSum = 0;
			for (WeightedAlternative alternative : alternatives) {
				baseSum += alternative.weight;
			}
			return baseSum;
		}
		List<WeightedAlternative> adjusted = new ArrayList<>(alternatives.size());
		double total = 0;
		for (WeightedAlternative alternative : alternatives) {
			double boost = 0;
			for (String referencedProduction : findReferencedProductions(alternative.text)) {
				boost += computeAdjustedAggregateWeight(referencedProduction, aggregateWeightCache, inProgressProductions);
			}
			double adjustedWeight = alternative.weight + SCALE_FACTOR * boost;
			adjusted.add(new WeightedAlternative(alternative.text, adjustedWeight));
			total += adjustedWeight;
		}
		inProgressProductions.remove(name);
		productionsMap.put(name, adjusted);
		aggregateWeightCache.put(name, total);
		return total;
	}

	/**
	 * Returns every production name referenced (as {@code [Name]}, {@code [*Name]} or
	 * {@code [!Name]}) directly in {@code text}, in order of occurrence, with duplicates
	 * kept: a production referenced twice in the same alternative contributes its
	 * aggregate weight twice to that alternative's descendant-weight boost (see
	 * {@link #adjustWeightsForDescendants}). Mirrors {@link #checkTokenValidity}'s
	 * marker-stripping so {@code [key=value]}/{@code [#key]} tokens, which don't
	 * reference a production, are skipped. Assumes {@code text} is already known to be
	 * well-formed (called only after {@link #checkProductionsValidity} has succeeded).
	 */
	private List<String> findReferencedProductions(String text) {
		List<String> referenced = new ArrayList<>();
		String remaining = text;
		int openingBracketIndex;
		while ((openingBracketIndex = remaining.indexOf(OPENING_BRACKET)) >= 0) {
			int closingBracketIndex = remaining.indexOf(CLOSING_BRACKET, openingBracketIndex + 1);
			String tokenBody = remaining.substring(openingBracketIndex + 1, closingBracketIndex);
			if (tokenBody.startsWith(GLOBAL_FIXED_PRODUCTION_MARKER) || tokenBody.startsWith(LOCAL_FIXED_PRODUCTION_MARKER)) {
				tokenBody = tokenBody.substring(1);
			}
			if (!tokenBody.startsWith(REFERENCE_MARKER) && tokenBody.indexOf(ASSIGNMENT_MARKER) < 0) {
				referenced.add(tokenBody);
			}
			remaining = remaining.substring(closingBracketIndex + 1);
		}
		return referenced;
	}

	/**
	 * Reads the optional post-production file into {@link #postProductions}. Each line
	 * must be of the form {@code pre:post}; a missing file (a {@code null} stream) simply
	 * results in no post-production substitutions.
	 * @throws InvalidGrammarException if a line is missing its pre- or post-production text
	 */
	private void setPostProductionFile(InputStream inputStream) throws InvalidGrammarException, IOException {
		if (inputStream == null) {
			return;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			String pre;
			String post;
			int currentLine = 0;
			while ((line = reader.readLine()) != null) {
				currentLine++;
				String[] parts = line.split(POST_PRODUCTION_SEPARATOR, 2);
				pre = parts[0];
				if (pre.trim().isEmpty()) {
					throw new InvalidGrammarException(LINE + currentLine + ": No pre production");
				}
				if (parts.length < 2) {
					throw new InvalidGrammarException(LINE + currentLine + ": No post production");
				}
				post = parts[1];
				if (post.trim().isEmpty()) {
					throw new InvalidGrammarException(LINE + currentLine + ": Empty post production");
				}
				postProductions.put(pre, post);
			}
		}
	}

	/**
	 * Changes the production {@link #produce()} starts from.
	 * @param rootNode the plain name (no brackets) of an existing production
	 * @throws IllegalArgumentException if {@code rootNode} is not a defined production
	 */
	public void setRootNode(String rootNode) {
		if (productionsMap.get(rootNode) == null) {
			throw new IllegalArgumentException(rootNode + ": not a valid production");
		}
		this.rootNode = rootNode;
	}

	/**
	 * @return the plain name (no brackets) of the production {@link #produce()} currently starts from
	 */
	public String getRootNode() {
		return rootNode;
	}

	/**
	 * @return the current alternative-picking mode (see {@link ProductionModeEnum})
	 */
	public ProductionModeEnum getProductionMode() {
		return productionMode;
	}

	/**
	 * Sets how a production's alternative is picked.
	 * @param productionMode the new mode; see {@link ProductionModeEnum}
	 */
	public void setProductionMode(ProductionModeEnum productionMode) {
		this.productionMode = productionMode;
	}

	/**
	 * Restores {@link #currentProductionsMap} to a fresh copy of {@link #productionsMap} and
	 * clears {@link #globalFixedProductions}, so that any one-shot alternative consumed by a
	 * previous production cycle is available again and no stale fixed value leaks into the next
	 * cycle.
	 */
	public void reset() {
		currentProductionsMap.clear();
		for (Map.Entry<String, List<WeightedAlternative>> entry : productionsMap.entrySet()) {
			currentProductionsMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		globalFixedProductions.clear();
	}

	/**
	 * Pre-sets the value that will be returned for a globally fixed production
	 * ({@code [*name]}) reference, before calling {@link #produce()}.
	 */
	public void addFixedProduction(String name, String value) {
		globalFixedProductions.put(name, value);
	}

	/**
	 * Produces a random text starting from {@link #rootNode}.
	 * @return the produced text, split into one entry per line
	 */
	public List<String> produce() {
		try {
			return produceImpl(OPENING_BRACKET + rootNode + CLOSING_BRACKET);
		} finally {
			globalFixedProductions.clear();
		}
	}

	/**
	 * Produces a random text starting from an explicitly given production, without
	 * changing {@link #rootNode}.
	 * @param rootNode the plain name (no brackets) of an existing production
	 * @return the produced text, split into one entry per line
	 */
	public List<String> produce(String rootNode) {
		return produceImpl(OPENING_BRACKET + rootNode + CLOSING_BRACKET);
	}

	/**
	 * Expands {@code startNode} (already wrapped in brackets by {@link #produce()} or
	 * {@link #produce(String)}) into its final text, then applies post-production
	 * substitutions and splits the result into lines.
	 * @param startNode a single {@code [Name]} token to expand
	 * @return the produced text, split into one entry per line
	 */
	public List<String> produceImpl(String startNode) {
		String firstResult = produceImpl(startNode, currentProductionsMap, globalFixedProductions);
		String intermediateResult = postProduce(firstResult);
        return new ArrayList<>(Arrays.asList(intermediateResult.split(LINE_BREAK_REGEX)));
	}

	/**
	 * Repeatedly finds the leftmost {@code [...]} token in {@code production} and replaces
	 * it with its resolved value (see {@link #resolveToken}) until none are left.
	 * @param production the text to expand, generally starting as a single {@code [Name]} token
	 * @param superProductionsMap the productions map to resolve plain/fixed references against
	 *                            (the whole grammar's, or a one-shot-pruned copy of it)
	 * @param superFixedProductions the fixed-production cache inherited from the caller
	 *                              (global cache for a top-level call, or a subtree's local cache)
	 * @return the fully expanded text, with no {@code [...]} tokens left
	 */
	private String produceImpl(String production, Map<String, List<WeightedAlternative>> superProductionsMap, Map<String, String> superFixedProductions) {
		Map<String, String> localFixedProductions = new HashMap<>();
		int openingBracketIndex;
		while ((openingBracketIndex = production.indexOf(OPENING_BRACKET)) >= 0) {
			int closingBracketIndex = findClosingBracket(production, openingBracketIndex);
			String prefix = production.substring(0, openingBracketIndex);
			String tokenBody = production.substring(openingBracketIndex + 1, closingBracketIndex);
			String postfix = production.substring(closingBracketIndex + 1);
			String resolved = resolveToken(tokenBody, superProductionsMap, superFixedProductions, localFixedProductions);
			production = prefix + resolved + postfix;
		}
		return production;
	}

	/**
	 * @throws IllegalArgumentException if {@code production} has no {@code ]} after {@code openingBracketIndex}
	 */
	private int findClosingBracket(String production, int openingBracketIndex) {
		int closingBracketIndex = production.indexOf(CLOSING_BRACKET, openingBracketIndex + 1);
		if (closingBracketIndex == -1) {
			throw new IllegalArgumentException("Missing '" + CLOSING_BRACKET + "' element after token " + production);
		}
		return closingBracketIndex;
	}

	/**
	 * Resolves the content of a single [token], dispatching to the appropriate handler
	 * depending on its marker ({@code *}, {@code !}, {@code =} or {@code #}).
	 */
	private String resolveToken(String tokenBody, Map<String, List<WeightedAlternative>> productionsMap, Map<String, String> superFixedProductions, Map<String, String> localFixedProductions) {
		if (tokenBody.startsWith(GLOBAL_FIXED_PRODUCTION_MARKER)) {
			String name = tokenBody.substring(GLOBAL_FIXED_PRODUCTION_MARKER.length());
			return resolveFixedProduction(name, globalFixedProductions, productionsMap, superFixedProductions);
		}
		if (tokenBody.startsWith(LOCAL_FIXED_PRODUCTION_MARKER)) {
			String name = tokenBody.substring(LOCAL_FIXED_PRODUCTION_MARKER.length());
			return resolveFixedProduction(name, localFixedProductions, productionsMap, superFixedProductions);
		}
		if (tokenBody.indexOf(ASSIGNMENT_MARKER) >= 0) {
			assignFixedProduction(tokenBody, superFixedProductions);
			return "";
		}
		if (tokenBody.startsWith(REFERENCE_MARKER)) {
			return resolveReference(tokenBody, localFixedProductions, superFixedProductions);
		}
		return resolvePlainProduction(tokenBody, productionsMap, localFixedProductions);
	}

	/**
	 * Resolves a production whose result is cached (globally or locally, depending on
	 * {@code fixedProductionsCache}) so that further references to the same name reuse
	 * the value generated the first time. Like {@link #resolvePlainProduction}, the result is
	 * trimmed: every production is expected to produce a trimmed string.
	 */
	private String resolveFixedProduction(String name, Map<String, String> fixedProductionsCache, Map<String, List<WeightedAlternative>> productionsMap, Map<String, String> superFixedProductions) {
		String cached = fixedProductionsCache.get(name);
		if (cached != null) {
			return cached;
		}
		List<WeightedAlternative> productions = productionsMap.get(name);
		if (productions == null) {
			throw new IllegalArgumentException(PRODUCTION + name + " is empty!");
		}
		String result = produceImpl(getProduction(productionsMap, name), productionsMap, superFixedProductions).trim();
		fixedProductionsCache.put(name, result);
		return result;
	}

	/**
	 * Handles a {@code [key=value]} token by storing {@code value} under {@code key} in
	 * {@code superFixedProductions}, to be retrieved later by a {@code [#key]} reference.
	 */
	private void assignFixedProduction(String tokenBody, Map<String, String> superFixedProductions) {
		int equalsPosition = tokenBody.indexOf(ASSIGNMENT_MARKER);
		String key = tokenBody.substring(0, equalsPosition);
		String value = tokenBody.substring(equalsPosition + ASSIGNMENT_MARKER.length());
		superFixedProductions.put(key, value);
	}

	/**
	 * Handles a {@code [#key]} token by looking up the value previously assigned to
	 * {@code key} (via {@link #assignFixedProduction}), checking the local map first
	 * and then the super one.
	 * @throws IllegalArgumentException if {@code key} was never assigned
	 */
	private String resolveReference(String tokenBody, Map<String, String> localFixedProductions, Map<String, String> superFixedProductions) {
		String key = tokenBody.substring(REFERENCE_MARKER.length());
		String value = localFixedProductions.get(key);
		if (value == null) {
			value = superFixedProductions.get(key);
		}
		if (value == null) {
			throw new IllegalArgumentException(PRODUCTION + tokenBody + " not yet defined!");
		}
		return value;
	}

	/**
	 * Resolves a plain {@code [Name]} reference: picks one alternative of {@code name}
	 * (see {@link #getProduction}) and recursively expands it. The result is trimmed: every
	 * production is expected to produce a trimmed string, so that e.g. an inline alternation
	 * group's chosen leaf being empty doesn't leave a stray space behind from the literal text
	 * surrounding the reference to it.
	 * @throws IllegalArgumentException if {@code name} is not a (currently available) production
	 */
	private String resolvePlainProduction(String name, Map<String, List<WeightedAlternative>> productionsMap, Map<String, String> localFixedProductions) {
		List<WeightedAlternative> productions = productionsMap.get(name);
		if (productions == null) {
			throw new IllegalArgumentException(PRODUCTION + name + " is empty!");
		}
		return produceImpl(getProduction(productionsMap, name), productionsMap, localFixedProductions).trim();
	}

	/**
	 * Picks an alternative of {@code production} from {@code localProductionsMap}, according to
	 * {@link #productionMode}: always the first one, always the last one, or a weighted-random one
	 * (see {@link #pickWeighted}, weighing each alternative by its {@link WeightedAlternative#weight}
	 * so that e.g. an alternative with weight 10 is picked ten times as often as one with the default
	 * weight of 1). If {@code production} is one-shot, the chosen alternative (and, if it was the
	 * last one, the whole production, cascading via {@link #removeProduction}) is removed so it
	 * cannot be picked again within the same production cycle.
	 */
	private String getProduction(Map<String, List<WeightedAlternative>> localProductionsMap, String production) {
		List<WeightedAlternative> productions = localProductionsMap.get(production);
		WeightedAlternative chosen;
		switch (productionMode) {
			case FIRST:
				chosen = productions.get(0);
				break;
			case LAST:
				chosen = productions.get(productions.size() - 1);
				break;
			default:
				chosen = pickWeighted(productions);
		}
		if (oneShotProductions.contains(production)) {
			productions.remove(chosen);
			if (productions.isEmpty()) {
				removeProduction(localProductionsMap, production);
			}
		}
		return chosen.text;
	}

	/**
	 * Picks one alternative from {@code productions} at random, weighing each one by its
	 * (possibly fractional) {@link WeightedAlternative#weight}: the total weight is summed, a
	 * random value is drawn in {@code [0, totalWeight)}, and the alternative whose cumulative
	 * weight range contains it is returned. An alternative with the default weight of 1 is picked
	 * exactly as often as it would be under plain uniform selection, so a grammar with no
	 * {@code [^N]} tokens at all behaves identically to before this method existed.
	 */
	private WeightedAlternative pickWeighted(List<WeightedAlternative> productions) {
		double totalWeight = 0;
		for (WeightedAlternative alternative : productions) {
			totalWeight += alternative.weight;
		}
		double r = rnd.nextDouble() * totalWeight;
		double cumulativeWeight = 0;
		for (WeightedAlternative alternative : productions) {
			cumulativeWeight += alternative.weight;
			if (r < cumulativeWeight) {
				return alternative;
			}
		}
		return productions.get(productions.size() - 1);
	}

	/**
	 * Removes an exhausted one-shot production from {@code localProductionsMap}, and cascades:
	 * strips any {@code [production]}/{@code [*production]}/{@code [!production]} reference to it
	 * from every other production's alternatives, recursively removing any production that is
	 * thereby left with no alternatives of its own.
	 */
	private void removeProduction(Map<String, List<WeightedAlternative>> localProductionsMap, String production) {
		localProductionsMap.remove(production);
		String plainReference = OPENING_BRACKET + production + CLOSING_BRACKET;
		String globalReference = OPENING_BRACKET + GLOBAL_FIXED_PRODUCTION_MARKER + production + CLOSING_BRACKET;
		String localReference = OPENING_BRACKET + LOCAL_FIXED_PRODUCTION_MARKER + production + CLOSING_BRACKET;
		List<String> productionsToRemove = new ArrayList<>();
		for (Map.Entry<String, List<WeightedAlternative>> entry : localProductionsMap.entrySet()) {
			List<WeightedAlternative> options = entry.getValue();
			options.removeIf(option -> option.text.contains(plainReference) || option.text.contains(globalReference) || option.text.contains(localReference));
			if (options.isEmpty()) {
				productionsToRemove.add(entry.getKey());
			}
		}
		for (String productionToRemove : productionsToRemove) {
			removeProduction(localProductionsMap, productionToRemove);
		}
	}

	/**
	 * Applies every {@code pre:post} substitution from {@link #postProductions} to the
	 * fully-expanded text, fixing natural-language issues arising from the mechanical
	 * concatenation of production alternatives, then collapses any run of two or more
	 * spaces left behind (typically by a deliberately-empty alternative) into one, and
	 * finally strips any space left immediately before a punctuation mark (typically left
	 * behind when a reference at the very end of a sentence expands to an empty string).
	 */
	private String postProduce(String intermediateProduction) {
		for (Map.Entry<String, String> entry : postProductions.entrySet()) {
			String pre = entry.getKey();
			String post = entry.getValue();
			int pos;
			while ((pos = intermediateProduction.indexOf(pre)) >= 0) {
				StringBuilder sb = new StringBuilder(intermediateProduction.substring(0, pos)).append(post);
				int l = pos + pre.length();
				if (l < intermediateProduction.length()) {
					sb.append(intermediateProduction.substring(l));
				}
				intermediateProduction = sb.toString();
			}
		}
		intermediateProduction = intermediateProduction.replaceAll(MULTIPLE_SPACES_REGEX, " ");
		return intermediateProduction.replaceAll(SPACE_BEFORE_PUNCTUATION_REGEX, "");
	}

	public static class InvalidGrammarException extends Exception {
		private static final long serialVersionUID = 1L;

		public InvalidGrammarException(String error) {
			super(error);
		}
	}
}
