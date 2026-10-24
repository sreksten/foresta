package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

/**
 * La .spada normalmente nascosta: una spada con attributi a caso, come gli altri artefatti, che il gruppo ottiene solo con
 * l'intermezzo dell'alchimista (vedi IntermezzoSpadaNascosta) dopo aver trovato il Manuale di Unix System V. Ha l'aspetto di
 * {@link TipoOggetto#SPADA_NORMALMENTE_NASCOSTA} e, a differenza di ogni altro artefatto, è sempre al livello del mondo: il livello
 * e i danni base si ricalcolano da quello di adesso ogni volta che li si chiede, senza toccare i dati salvati. Equipaggiarla fa
 * vincere il trofeo Trovarobe (vedi InternoSpadaNascostaImpugnata).
 */
public class SpadaNormalmenteNascosta extends ArmaFisica {

	private static final String NOME = ".spada normalmente nascosta";
	private static final String DESCRIZIONE = "che ls non mostra";

	public SpadaNormalmenteNascosta(ArtefattoMD md) {
		super(md);
	}

	/**
	 * Se il modello dati è quello della spada normalmente nascosta: ne ha l'aspetto.
	 */
	public static boolean isQuesta(ArtefattoMD md) {
		return md.getAspetto() == TipoOggetto.SPADA_NORMALMENTE_NASCOSTA;
	}

	/**
	 * Una spada nuova, al livello del mondo di adesso, con il nome e l'aspetto della spada normalmente nascosta.
	 */
	public static SpadaNormalmenteNascosta costruisci(GeneratoreArtefatti generatore) {
		ArtefattoMD md = generatore.generaArtefatto(TipoArtefatto.SPADA, Statistiche.getLivello()).getModelloDati();
		md.setNomeProprio(null);
		md.setNome("la " + NOME);
		md.setDescrizione(DESCRIZIONE);
		md.setAspetto(TipoOggetto.SPADA_NORMALMENTE_NASCOSTA);
		return new SpadaNormalmenteNascosta(md);
	}

	/**
	 * Sempre il livello del mondo di adesso.
	 */
	@Override
	public int getLivello() {
		return Statistiche.getLivello();
	}

	/**
	 * I danni base che ha avuto alla nascita, più la differenza fra quelli medi di un'arma del livello di adesso e di quello di allora.
	 */
	@Override
	public int getDanni() {
		int differenza = GeneratoreArtefatti.danniMediArma(getLivello()) - GeneratoreArtefatti.danniMediArma(md.getLivello());
		return Math.max(1, md.getDanni() + differenza);
	}
}
