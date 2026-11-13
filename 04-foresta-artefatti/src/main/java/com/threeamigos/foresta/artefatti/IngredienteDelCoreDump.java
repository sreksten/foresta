package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.strumenti.Maiuscole;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Nome;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.strumenti.dado.Dado;

/**
 * Quel che resta di un incantamento quando una riprogrammazione va male (vedi RegoleIncantatura.riprogramma): un ingrediente di tipo
 * casuale, "la Gemma del Core Dump", "il Sigillo del Core Dump", che porta solo quell'incantamento e si può fondere su un altro
 * artefatto come ogni ingrediente.
 */
public final class IngredienteDelCoreDump {

	private static final TipoArtefatto[] TIPI = {TipoArtefatto.PERGAMENA, TipoArtefatto.GEMMA, TipoArtefatto.MONILE,
			TipoArtefatto.GINGILLO, TipoArtefatto.SIGILLO};
	// I nomi dei tipi stanno in nomi_artefatti_<lingua>.properties, le frasi in generatore_artefatti_<lingua>.properties
	private static final String FILE_DEI_NOMI = "nomi_artefatti";
	private static final String FILE_DEI_TESTI = "generatore_artefatti";

	private IngredienteDelCoreDump() {
	}

	/**
	 * L'ingrediente che custodisce quell'incantamento, al livello dato.
	 */
	public static Artefatto da(IncantamentoMD incantamento, int livello) {
		int scelto = Dado.tiraAncheAUnaFaccia(TIPI.length) - 1;
		ArtefattoMD md = new ArtefattoMD();
		md.setTipo(TIPI[scelto]);
		// "la Gemma del Core Dump", "the Core Dump Gem": l'articolo e il tipo con la maiuscola, messi dove li vuole la lingua
		Nome nome = Nome.di(FILE_DEI_NOMI, TIPI[scelto]);
		md.setNome(Testi.formatta(FILE_DEI_TESTI, "CORE_DUMP.NOME", Lingue.corrente().articoloDeterminativo(nome, false),
				Maiuscole.inizialeMaiuscola(nome.getSingolare())));
		md.setDescrizione(Testi.formatta(FILE_DEI_TESTI, "CORE_DUMP.DESCRIZIONE", incantamento.getNomeIncantamento()));
		md.setLivello(Math.max(1, livello));
		md.setPeso(GeneratoreArtefattiTabelle.peso(TIPI[scelto]));
		md.addIncantamento(incantamento);
		md.setCostoAcquisto(ListinoPergamene.prezzo(md));
		return Artefatto.di(md);
	}
}
