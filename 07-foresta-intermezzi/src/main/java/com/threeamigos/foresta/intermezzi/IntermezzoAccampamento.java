package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.tipi.TipoIntermezzo;

import java.util.List;
import com.threeamigos.foresta.testi.Testi;

/**
 * Intermezzo che scatta quando il gruppo si accampa: stesso sfondo e fuoco di
 * {@link IntermezzoIntroduttivo}, con il gruppo intorno al fuoco (vedi {@link ScenaFraCompagni}). A differenza degli altri
 * intermezzi scatta una volta per accampamento (non di più nello stesso ciclo, vedi
 * {@link com.threeamigos.foresta.motore.Automa}), fino a un massimo di
 * {@value #MASSIMO_OCCORRENZE} volte nella partita: {@link #getId()} include quindi la sua
 * occorrenza (vedi {@link #prossimaOccorrenza()}), cosa possibile perché
 * {@link FabbricaIntermezzi#crea} crea una nuova istanza a ogni controllo e
 * {@link com.threeamigos.foresta.modellodati.IntermezziMD} è un semplice insieme di stringhe senza vincoli di formato.
 * <p>
 * Ogni occorrenza ha la sua scena, con la sua luna e le sue battute: la storia di quello trasformato in scarafaggio
 * sotto una luna normale, la luna che è una stazione da battaglia, e la luna strana di cui non farsi prendere dal panico.
 * <p>
 * Se in questo stesso accampamento una missione mostrerà il suo intermezzo (vedi LaLealta), questo non scatta: si
 * vedrebbero in sovrapposizione, e quello della missione viene prima (vedi {@link #deveScattare}). Non essendo
 * scattato, lo si vedrà al prossimo accampamento libero da missioni: l'occorrenza (e quindi la scena) si basa su
 * quante volte l'intermezzo è stato effettivamente mostrato finora, non su quanti accampamenti sono avvenuti in
 * tutto, altrimenti un accampamento in cui non scatta ne "mangerebbe" la scena, mai più mostrata.
 */
public class IntermezzoAccampamento implements Intermezzo {

	private static final int MASSIMO_OCCORRENZE = 3;

	/**
	 * Le scene, una per occorrenza: l'immagine della luna e le battute, alternate fra il primo e il secondo
	 * personaggio.
	 */
	private static final String[] LUNE = {"fondinon2x2/Luna.png", "fondinon2x2/Mimas.png" /* Una luna di Saturno */, "fondinon2x2/LunaHHGTTG.png"};
	// Quante battute ha ogni scena (nei testi: ACCAMPAMENTO.<scena>.<battuta>)
	private static final int[] BATTUTE = {2, 3, 2};

	/**
	 * Calcolata una sola volta alla creazione dell'istanza, perché {@link #getPagine()} viene
	 * chiamato dopo che {@link com.threeamigos.foresta.intermezzi.RegistroIntermezzi#segnaScattato}
	 * ha già segnato questa stessa occorrenza come scattata: ricalcolarla lì vedrebbe quella
	 * marcatura e salterebbe erroneamente alla successiva.
	 */
	private final int occorrenza = prossimaOccorrenza();

	@Override
	public String getId() {
		return idPer(occorrenza);
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return momento == MomentoIntermezzo.ACCAMPAMENTO && occorrenza <= MASSIMO_OCCORRENZE
				&& !RegistroIntermezzi.isScattatoNelMomento() && !RegistroIntermezzi.haUnaFonteUnIntermezzoInArrivo(MomentoIntermezzo.ACCAMPAMENTO);
	}

	private static String idPer(int occorrenza) {
		return TipoIntermezzo.INTERMEZZO_ACCAMPAMENTO.name() + "_" + occorrenza;
	}

	/**
	 * La prossima occorrenza (1-based) non ancora mostrata nella partita, trovata scorrendo
	 * {@link com.threeamigos.foresta.modellodati.IntermezziMD#isScattato} finché non se ne trova una libera: se tutte fino a
	 * {@value #MASSIMO_OCCORRENZE} sono già state mostrate, restituisce un numero oltre il
	 * massimo.
	 */
	private static int prossimaOccorrenza() {
		int occorrenza = 1;
		while (occorrenza <= MASSIMO_OCCORRENZE && RegistroIntermezzi.isScattato(idPer(occorrenza))) {
			occorrenza++;
		}
		return occorrenza;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		int indice = (occorrenza - 1) % LUNE.length;
		ScenaFraCompagni pagina = ScenaFraCompagni.allAccampamento(Testi.formatta("intermezzi", "IntermezzoAccampamento.1"),
				GruppoGiocatore.getIstanza().getPersonaggiVivi()).conLuna(LUNE[indice]);
		for (int i = 1; i <= BATTUTE[indice]; i++) {
			String battuta = Testi.formatta("intermezzi", "ACCAMPAMENTO." + (indice + 1) + "." + i);
			if (i % 2 == 1) {
				pagina.parlaIlPrimo(battuta);
			} else {
				pagina.parlaIlSecondo(battuta);
			}
		}
		return pagina.getPagine();
	}
}
