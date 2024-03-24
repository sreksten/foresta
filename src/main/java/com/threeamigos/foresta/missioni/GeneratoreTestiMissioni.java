package com.threeamigos.foresta.missioni;

import java.io.IOException;
import java.util.List;

import com.threeamigos.foresta.motore.GrammarBean;
import com.threeamigos.foresta.motore.GrammarBean.InvalidGrammarException;
import com.threeamigos.foresta.motore.Logger;

public class GeneratoreTestiMissioni {

	private static final GeneratoreTestiMissioni istanza = new GeneratoreTestiMissioni();
	
	private GeneratoreTestiMissioni() {
		try {
			grammarBean = new GrammarBean(
					GeneratoreTestiMissioni.class.getResourceAsStream("/com/threeamigos/foresta/motore/missioni.txt"),
					GeneratoreTestiMissioni.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
		} catch (IOException | InvalidGrammarException e) {
			Logger.log(e);
		}
	}

	private GrammarBean grammarBean;

	public static final GeneratoreTestiMissioni getIstanza() {
		return istanza;
	}

	public void reset() {
		grammarBean.reset();
	}

	public List<String> produceMultiple(String rootNode) {
		return grammarBean.produce(rootNode);
	}

	public String produceSingle(String rootNode) {
		List<String> result = grammarBean.produce(rootNode);
		if (result.size() != 1) {
			throw new IllegalStateException("Numero errato di linee prodotto da " + rootNode);
		}
		return result.get(0);
	}
}
