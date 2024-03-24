package com.threeamigos.foresta.motore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

public class GrammarBean {

	private static final String PRODUCTION = "Production ";
	private static final String LINE = "Line ";
	/**
	 * Root production from which the generation process starts.
	 */
	private String rootNode = null;
	/**
	 * Map of all available productions
	 */
	private final Map<String, List<String>> productionsMap = new HashMap<>();
	/**
	 * Map of all available productions to be reused in a cycle of productions
	 */
	private final Map<String, List<String>> currentProductionsMap = new HashMap<>();
	/**
	 * Fixed productions map
	 */
	private Map<String, String> globalFixedProductions = new HashMap<>();
	/**
	 * Map of all productions that happen after the main production has finished, to adjust the result fixing natural language grammar issues
	 * (e.g. in Italian language, 'a il' is transformed to 'al').
	 */
	private final Map<String, String> postProductions = new HashMap<>();
	/**
	 * To randomly choose a production
	 */
	private final Random rnd = new Random(System.currentTimeMillis());
	/**
	 * Productions that must be removed once used within a cycle
	 */
	private Set<String> oneShotProductions = new HashSet<>();

	public GrammarBean(InputStream grammar, InputStream postProduction) throws InvalidGrammarException, IOException {
		setSourceFile(grammar);
		setPostProductionFile(postProduction);
		reset();
	}

	/**
	 * Reads and parses the grammar file.
	 * @param inputStream
	 */
	private final void setSourceFile(InputStream inputStream) throws InvalidGrammarException, IOException {
		readSourceFileAndCreateProductionsMap(inputStream);
		checkProductionsValidity();
	}

	private final void readSourceFileAndCreateProductionsMap(InputStream inputStream) throws IOException, InvalidGrammarException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String currentProduction = null;
			String previousProduction = null;
			int currentLineNumber = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				currentLineNumber++;
				if (line.length() == 0 || line.startsWith("#")) {
					continue;
				}
				if (line.startsWith("\t")) {
					handleChildren(line, currentLineNumber, currentProduction);
				} else {
					checkPreviousProduction(currentLineNumber, previousProduction);
					previousProduction = currentProduction;
					currentProduction = handleProduction(currentLineNumber, line);
				}
			}
		}
	}

	private final void checkPreviousProduction(int currentLineNumber, String previousProduction) throws InvalidGrammarException {
		if (previousProduction != null) {
			List<String> previousProductionChildren = productionsMap.get(previousProduction);
			if (previousProductionChildren == null || previousProductionChildren.isEmpty()) {
				throw new InvalidGrammarException(LINE + currentLineNumber + ": Production " + previousProduction + " does not produce anything.");
			}
		}
	}

	private final String handleProduction(int currentLineNumber, String line) throws InvalidGrammarException {
		String currentProduction = line.trim();
		if (currentProduction.endsWith("{1}")) {
			currentProduction = currentProduction.substring(0, currentProduction.length() - 3);
			Logger.log("One-shot production: " + currentProduction);
			oneShotProductions.add(currentProduction);
		}
		if (rootNode == null) {
			rootNode = "[" + currentProduction + "]";
		}
		if (productionsMap.get(currentProduction) != null) {
			throw new InvalidGrammarException(LINE + currentLineNumber + ": Production " + currentProduction + " already found.");
		}
		productionsMap.put(currentProduction, new ArrayList<>());
		return currentProduction;
	}

	private final void handleChildren(String line, int currentLine, String currentProduction) throws InvalidGrammarException {
		if (currentProduction == null) {
			throw new InvalidGrammarException(LINE + currentLine + ": Missing parent production. Lines beginning with TAB must be preceded by a production.");
		} else {
			List<String> currentProductionChildren = productionsMap.get(currentProduction);
			StringTokenizer st = new StringTokenizer(line, "|");
			while (st.hasMoreTokens()) {
				currentProductionChildren.add(st.nextToken().trim());
			}
		}
	}

	private final void checkProductionsValidity() throws InvalidGrammarException {
		for (Iterator<String> productionsIterator = productionsMap.keySet().iterator(); productionsIterator.hasNext(); ) {
			String key = productionsIterator.next();
			for (Iterator<String> childrenIterator = productionsMap.get(key).iterator(); childrenIterator.hasNext(); ) {
				String thisProduction = childrenIterator.next();
				int openingBracketIndex;
				int closingBracketIndex;
				while ((openingBracketIndex = thisProduction.indexOf('[')) >= 0) {
					closingBracketIndex = thisProduction.indexOf(']');
					if (closingBracketIndex == -1) {
						throw new InvalidGrammarException("Missing ']' element after token " + thisProduction);
					} else {
						checkTokenValidity(thisProduction.substring(openingBracketIndex + 1, closingBracketIndex));
						thisProduction = thisProduction.substring(closingBracketIndex + 1);
					}
				}
			}
		}
	}

	private final void checkTokenValidity(String thisProduction) throws InvalidGrammarException {
		if (thisProduction.startsWith("*") || thisProduction.startsWith("!")) {
			thisProduction = thisProduction.substring(1);
			if (thisProduction.length() == 0) {
				throw new InvalidGrammarException("Empty fixed production present (* or ! alone)");
			}
		}
		if (thisProduction.startsWith("=")) {
			throw new InvalidGrammarException("= symbol must be preceded by a node name");
		}
		if (productionsMap.get(thisProduction) == null && !thisProduction.startsWith("#") && thisProduction.indexOf('=') < 0) {
			throw new InvalidGrammarException(PRODUCTION + thisProduction + " is not defined");
		}
	}

	private final void setPostProductionFile(InputStream inputStream) throws InvalidGrammarException, IOException {
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
				StringTokenizer st = new StringTokenizer(line, ":");
				pre = st.nextToken();
				if (pre == null || pre.trim().length() == 0) {
					throw new InvalidGrammarException(LINE + currentLine + ": No pre production");
				}
				if (!st.hasMoreTokens()) {
					throw new InvalidGrammarException(LINE + currentLine + ": No post production");
				}
				post = st.nextToken();
				if (post == null || post.trim().length() == 0) {
					throw new InvalidGrammarException(LINE + currentLine + ": Empty post production");
				}
				postProductions.put(pre, post);
			}
		}
	}

	public void setRootNode(String rootNode) {
		if (productionsMap.get(rootNode) == null) {
			throw new IllegalArgumentException(rootNode + ": not a valid production");
		}
		this.rootNode = new StringBuilder("[").append(rootNode).append(']').toString();
	}

	public String getRootNode() {
		return rootNode;
	}

	public void reset() {
		currentProductionsMap.clear();
		currentProductionsMap.putAll(productionsMap);
		globalFixedProductions.clear();
	}

	public void addFixedProduction(String name, String value) {
		globalFixedProductions.put(name, value);
	}

	public List<String> produce() {
		List<String> result = privProduce(rootNode);
		globalFixedProductions.clear();
		return result;
	}

	public List<String> produce(String rootNode) {
		return privProduce(new StringBuilder("[").append(rootNode).append(']').toString());
	}

	/**
	 * Function to be called to produce a random story
	 * @return a random story generated from the root node
	 */
	public List<String> privProduce(String startNode) {
		String firstResult = privProduce(startNode, currentProductionsMap, globalFixedProductions);
		String intermediateResult = postProduce(firstResult);
		List<String> finalResult = new ArrayList<>();
		int carriageReturnPos = intermediateResult.indexOf("\\n");
		while (carriageReturnPos >= 0) {
			finalResult.add(intermediateResult.substring(0, carriageReturnPos));
			intermediateResult = intermediateResult.substring(carriageReturnPos + 2);
			carriageReturnPos = intermediateResult.indexOf("\\n");
		}
		finalResult.add(intermediateResult);
		return finalResult;
	}

	private String privProduce(String production, Map<String, List<String>> superProductionsMap, Map<String, String> superFixedProductions) {
		Map<String, List<String>> localProductionsMap = new HashMap<>();
		localProductionsMap.putAll(superProductionsMap);
		Map<String, String> localFixedProductions = new HashMap<>();
		int openingBracketIndex;
		int closingBracketIndex;
		while ((openingBracketIndex = production.indexOf('[')) >= 0) {
			String prefix;
			String postfix;
			String t;
			closingBracketIndex = production.indexOf(']');
			if (openingBracketIndex > 0) {
				prefix = production.substring(0, openingBracketIndex);
			} else {
				prefix = "";
			}
			postfix = production.substring(closingBracketIndex + 1);
			production = production.substring(openingBracketIndex + 1, closingBracketIndex);
			StringBuilder sb = new StringBuilder(prefix);
			if (production.startsWith("*")) {
				production = production.substring(1);
				t = globalFixedProductions.get(production);
				if (t == null) {
					List<String> productions = localProductionsMap.get(production);
					if (productions == null) {
						throw new IllegalArgumentException(PRODUCTION + production + " is empty!");
					}
					t = privProduce(getProduction(localProductionsMap, production), localProductionsMap, superFixedProductions);
					globalFixedProductions.put(production, t);
				}
				sb.append(t);
			} else if (production.startsWith("!")) {
				production = production.substring(1);
				t = localFixedProductions.get(production);
				if (t == null) {
					List<String> productions = localProductionsMap.get(production);
					if (productions == null) {
						throw new IllegalArgumentException(PRODUCTION + production + " is empty!");
					}
					t = privProduce(getProduction(localProductionsMap, production), localProductionsMap, superFixedProductions);
					localFixedProductions.put(production, t);
				}
				sb.append(t);
			} else if (production.indexOf('=') >= 0) {
				int equalsPosition = production.indexOf('=');
				String key = production.substring(0, equalsPosition);
				String value = production.substring(equalsPosition + 1);
				superFixedProductions.put(key, value);
			} else if (production.startsWith("#")) {
				String key = production.substring(1);
				String value = localFixedProductions.get(key);
				if (value == null) {
					value = superFixedProductions.get(key);
				}
				if (value == null) {
					throw new IllegalArgumentException(PRODUCTION + production + " not yet defined!");
				}
				sb.append(value);
			} else {
				sb.append(privProduce(getProduction(localProductionsMap, production), localProductionsMap, localFixedProductions));
			}
			sb.append(postfix);
			production = sb.toString();
		}
		return production;
	}
	
	private String getProduction(Map<String, List<String>> localProductionsMap, String production) {
		List<String> productions = localProductionsMap.get(production);
		String result = productions.get(rnd.nextInt(productions.size()));
		if (oneShotProductions.contains(production)) {
			productions.remove(result);
			if (productions.isEmpty()) {
				removeProduction(localProductionsMap, production);
			}
		}
		return result;
	}

	private void removeProduction(Map<String, List<String>> localProductionsMap, String production) {
		localProductionsMap.remove(production);
		List<String> productionsToRemove = new ArrayList<String>();
		for (Map.Entry<String, List<String>> entry : localProductionsMap.entrySet()) {
			List<String> productsToRemove = new ArrayList<String>();
			for (String product : entry.getValue()) {
				if (product.indexOf(production) >= 0) {
					productsToRemove.add(product);
				}
			}
			for (String productToRemove : productsToRemove) {
				entry.getValue().remove(productToRemove);
			}
			if (entry.getValue().isEmpty()) {
				productionsToRemove.add(entry.getKey());
			}
		}
		for (String productionToRemove : productionsToRemove) {
			removeProduction(localProductionsMap, productionToRemove);
		}
	}

	private final String postProduce(String intermediateProduction) {
		for (Map.Entry<String, String> entry : postProductions.entrySet()) {
			String pre = entry.getKey();
			String post = entry.getValue();
			int pos;
			while ((pos = intermediateProduction.indexOf(pre)) > 0) {
				StringBuilder sb = new StringBuilder(intermediateProduction.substring(0, pos)).append(post);
				int l = pos + pre.length();
				if (l < intermediateProduction.length()) {
					sb.append(intermediateProduction.substring(l));
				}
				intermediateProduction = sb.toString();
			}
		}
		return intermediateProduction;
	}

	public class InvalidGrammarException extends Exception {
		private static final long serialVersionUID = 1L;

		public InvalidGrammarException(String error) {
			super(error);
		}
	}
}
