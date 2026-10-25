package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.tipi.AspettoArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;

import java.util.function.IntSupplier;

/**
 * La .spada normalmente nascosta: una spada con attributi a caso, come gli altri artefatti, che il gruppo ottiene solo con
 * l'intermezzo dell'alchimista (vedi IntermezzoSpadaNascosta) dopo aver trovato il Manuale di Unix System V. Ha l'aspetto di
 * {@link AspettoArtefatto#SPADA_NORMALMENTE_NASCOSTA} e, a differenza di ogni altro artefatto, segue un livello che cambia: il livello
 * e i danni base si ricalcolano da quello di adesso ogni volta che li si chiede, senza toccare i dati salvati. Da dove viene quel
 * livello (nel gioco è il livello del mondo, di cui un artefatto non sa niente) lo decide chi la usa, con
 * {@link #seguiIlLivello(IntSupplier)}. Equipaggiarla fa
 * vincere il trofeo Trovarobe (vedi InternoSpadaNascostaImpugnata).
 */
public class SpadaNormalmenteNascosta extends ArmaFisica {

	private static final String NOME = ".spada normalmente nascosta";
	// Da dove prende il livello di adesso; null finché nessuno lo dice, e allora resta quello con cui è nata
	private static volatile IntSupplier fonteDelLivello;
	private static final String DESCRIZIONE = "che ls non mostra";

	/**
	 * La spada sui dati dati (vedi {@link #isQuesta}).
	 */
	public SpadaNormalmenteNascosta(ArtefattoMD md) {
		super(md);
	}

	/**
	 * Se il modello dati è quello della spada normalmente nascosta: ne ha l'aspetto.
	 */
	public static boolean isQuesta(ArtefattoMD md) {
		return md.getAspetto() == AspettoArtefatto.SPADA_NORMALMENTE_NASCOSTA;
	}

	/**
	 * Dice da dove la spada prende il livello di adesso, ogni volta che glielo si chiede (nel gioco, il livello del mondo). Va
	 * installata una volta, all'avvio; con null torna al livello con cui è nata.
	 */
	public static void seguiIlLivello(IntSupplier fonte) {
		fonteDelLivello = fonte;
	}

	/**
	 * Una spada nuova, nata al livello dato, con il nome e l'aspetto della spada normalmente nascosta.
	 *
	 * @param livello il livello a cui nasce
	 */
	public static SpadaNormalmenteNascosta costruisci(GeneratoreArtefatti generatore, int livello) {
		ArtefattoMD md = generatore.generaArtefatto(TipoArtefatto.SPADA, livello).getModelloDati();
		md.setNomeProprio(null);
		md.setNome("la " + NOME);
		md.setDescrizione(DESCRIZIONE);
		md.setAspetto(AspettoArtefatto.SPADA_NORMALMENTE_NASCOSTA);
		return new SpadaNormalmenteNascosta(md);
	}

	/**
	 * Sempre il livello di adesso (vedi {@link #seguiIlLivello}); quello con cui è nata se nessuno lo dice.
	 */
	@Override
	public int getLivello() {
		IntSupplier fonte = fonteDelLivello;
		return fonte != null ? fonte.getAsInt() : md.getLivello();
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
