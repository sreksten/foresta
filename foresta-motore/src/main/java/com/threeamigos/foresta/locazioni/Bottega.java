package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.mappa.LocazioneMD;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Stato;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoRiposo;

/**
 * La bottega di un alchimista, di un armaiolo, di un venditore di pergamene o di un incantatore nel mezzo della Foresta:
 * nasce da delle rovine, alla fine della missione di chi la apre (vedi missioni.incarichi.IlNegozio). Il gruppo vi entra
 * appena arrivato, come in una locanda (l'automa lo fa entrare con il comando del negozio di città, dopo il suo intermezzo d'ingresso), e vi trova le stesse cose del negozio di una città (vedi GestoreBotteghe), ma con un magazzino suo; non
 * vi si combatte, e chiusa la bottega si riparte. Il nome della bottega e quello del negoziante stanno nel modello dati
 * della casella.
 */
public abstract class Bottega extends LocazioneBase {

	/**
	 * Il negoziante, con la provenienza ("Mastro Ferruccio da Ruuna"), scritto sulla casella quando la bottega nasce.
	 */
	public static final String NEGOZIANTE = "NEGOZIANTE";

	private final Comando ingresso;
	private final GestoreBotteghe botteghe = new GestoreBotteghe();

	protected Bottega(Comando ingresso) {
		this.ingresso = ingresso;
	}

	/**
	 * Il negoziante della bottega in quella casella, con la provenienza; null se la casella non ne ha uno.
	 */
	public static String getNegoziante(LocazioneMD modelloDati) {
		return modelloDati.ottieniProprieta(NEGOZIANTE);
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		setCompleta(true);
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		String negoziante = getNegoziante(getModelloDati());
		BusEventi.pubblica(new NotificaTestoParagrafo(g.chiMaiuscolo() + " arriva " + Articoli.conPreposizione("a", getNome()) + ". "
				+ (negoziante == null ? "" : Articoli.inizialeMaiuscola(negoziante) + " vi aspetta sulla porta della sua bottega.")));
	}

	@Override
	public Stato impostaAzioni(GruppoGiocatore g, GruppoAvversario gng, Comando azione) {
		if (botteghe.isAperta()) {
			return botteghe.gestisci(g, azione) ? Stato.FINE_LOCAZIONE : Stato.IN_LOCAZIONE;
		}
		if (azione == ingresso) {
			botteghe.apri(g, azione);
		}
		return Stato.IN_LOCAZIONE;
	}

	/**
	 * Il nome proprio dell'artefatto da fondere, dall'incantatore.
	 */
	@Override
	public Stato riceviTesto(GruppoGiocatore g, String testo) {
		botteghe.riceviTesto(testo);
		return Stato.IN_LOCAZIONE;
	}

	/**
	 * Il comando con cui l'automa fa entrare il gruppo nella bottega appena arrivato (come in una locanda: nessun comando da
	 * dare), dopo l'intermezzo d'ingresso.
	 */
	public Comando getComandoDiIngresso() {
		return ingresso;
	}

	/**
	 * La bottega non offre né mappa né inventario: appena arrivato il gruppo entra, e chiusa la bottega riparte.
	 */
	@Override
	public void ripresentaComandi() {
		throw new IllegalStateException("Bottega::ripresentaComandi(): la bottega non offre né mappa né inventario");
	}

	public TipoRiposo getTipoRiposo() {
		throw new IllegalArgumentException("Non si può riposare in una bottega");
	}
}
