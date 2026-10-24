package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.tipi.TipoArtefatto;

/**
 * Quel che resta di un incantamento quando una riprogrammazione va male (vedi RegoleIncantatura.riprogramma): un ingrediente di tipo
 * casuale, "la Gemma del Core Dump", "il Sigillo del Core Dump", che porta solo quell'incantamento e si può fondere su un altro
 * artefatto come ogni ingrediente.
 */
public final class IngredienteDelCoreDump {

	private static final TipoArtefatto[] TIPI = {TipoArtefatto.PERGAMENA, TipoArtefatto.GEMMA, TipoArtefatto.MONILE,
			TipoArtefatto.GINGILLO, TipoArtefatto.SIGILLO};
	private static final String[] NOMI = {"la Pergamena", "la Gemma", "il Monile", "il Gingillo", "il Sigillo"};

	private IngredienteDelCoreDump() {
	}

	/**
	 * L'ingrediente che custodisce quell'incantamento, al livello dato.
	 */
	public static Artefatto da(IncantamentoMD incantamento, int livello) {
		int scelto = Dado.tiraAncheAUnaFaccia(TIPI.length) - 1;
		ArtefattoMD md = new ArtefattoMD();
		md.setTipo(TIPI[scelto]);
		md.setNome(NOMI[scelto] + " del Core Dump");
		md.setDescrizione("che conserva " + incantamento.getNomeIncantamento());
		md.setLivello(Math.max(1, livello));
		md.setPeso(GeneratoreArtefattiTabelle.peso(TIPI[scelto]));
		md.addIncantamento(incantamento);
		md.setCostoAcquisto(ListinoPergamene.prezzo(md));
		return Artefatto.di(md);
	}
}
