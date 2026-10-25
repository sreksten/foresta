package com.threeamigos.foresta.mappa;

import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.mappa.TipoLocazione;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * La mappa della Foresta di questa partita: una griglia di caselle, ciascuna con il suo tipo di locazione, il suo nome e le sue proprietà.
 * I dati stanno nel {@code ForestaMD}; qui ci sono le operazioni sulla mappa: leggere e cambiare una casella, sapere quali il gruppo ha
 * visitato o conosce, trovare una casella libera, trasformare le locazioni uniche (il drago distrugge una città e al suo posto restano delle
 * rovine). Chi costruisce il mondo, e chi lo abita, sta altrove.
 */
public class Foresta {

	// Quante volte provare a pescare un nome che nessun'altra casella dello stesso tipo ha già
	private static final int TENTATIVI_NOME_NUOVO = 20;

	private Foresta() {
	}

	// Da dove prendere i dati della partita in corso: la mappa non sa chi li custodisce (lo dice chi li custodisce, vedi setFornitoreDiDati)
	private static volatile Supplier<ForestaMD> fornitoreDiDati;

	/**
	 * Dice alla mappa da dove prendere i dati della partita in corso. Lo fa chi li custodisce e li sostituisce quando si carica un
	 * salvataggio, quindi il fornitore va chiamato a ogni uso e non una volta sola.
	 */
	public static void setFornitoreDiDati(Supplier<ForestaMD> fornitore) {
		fornitoreDiDati = fornitore;
	}

	private static ForestaMD getForestaMD() {
		Supplier<ForestaMD> fornitore = fornitoreDiDati;
		if (fornitore == null) {
			throw new IllegalStateException("Nessuno ha detto alla mappa dove sono i suoi dati (vedi setFornitoreDiDati)");
		}
		return fornitore.get();
	}

	/**
	 * Svuota la mappa e la riporta a una griglia di quelle dimensioni, senza nessuna locazione.
	 */
	public static void azzera(int dimensioneX, int dimensioneY) {
		getForestaMD().reimposta(dimensioneX, dimensioneY);
	}

	/**
	 * Quante caselle ha la mappa in orizzontale.
	 */
	public static int getDimensioneX() {
		return getForestaMD().getDimensioneX();
	}

	/**
	 * Quante caselle ha la mappa in verticale.
	 */
	public static int getDimensioneY() {
		return getForestaMD().getDimensioneY();
	}

	/**
	 * Fa della casella una locazione di quel tipo.
	 */
	public static void impostaLocazione(CoordinateMD coordinate, TipoLocazione tipoLocazione) {
		getForestaMD().impostaLocazione(coordinate, tipoLocazione);
	}

	/**
	 * Dove sta quella locazione unica (una città, un castello...), o null se non è stata costruita.
	 *
	 * @throws IllegalArgumentException se il tipo non è di una locazione unica
	 */
	public static CoordinateMD getCoordinateLocazioneUnica(TipoLocazione tipoLocazione) {
		if (!tipoLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException();
		}
		return getForestaMD().ottieniCoordinateLocazioneUnica(tipoLocazione);
	}

	/**
	 * Trasforma la locazione unica in un'altra (di solito delle rovine, che prendono il nome da quel che c'era) e non la considera più unica.
	 * Non fa niente se quella locazione non è stata costruita.
	 */
	public static void distruggiLocazioneUnica(TipoLocazione tipoLocazione, TipoLocazione nuovoTipoLocazione) {
		CoordinateMD coordinate = getCoordinateLocazioneUnica(tipoLocazione);
		if (coordinate != null) {
			ForestaMD md = getForestaMD();
			md.impostaLocazione(coordinate, nuovoTipoLocazione);
			md.rimuoviLocazioneUnica(tipoLocazione);
			if (nuovoTipoLocazione == TipoLocazione.ROVINE) {
				// Le rovine di un castello o di una città prendono il nome da quello che c'era
				getLocazioneMD(coordinate).setNome("le Rovine " + Articoli.conPreposizione("di", tipoLocazione.getNomeProprio()));
			}
		}
	}

	/**
	 * Il tipo di locazione di quella casella, o null se è vuota.
	 */
	public static TipoLocazione getLocazione(CoordinateMD coordinate) {
		return getForestaMD().ottieniTipoLocazione(coordinate);
	}

	/**
	 * Il tipo di locazione di quella casella, o null se è vuota.
	 */
	public static TipoLocazione getLocazione(int x, int y) {
		return getForestaMD().ottieniTipoLocazione(x, y);
	}

	/**
	 * I dati della casella: il tipo, il nome e le proprietà che la locazione si ricorda.
	 */
	public static LocazioneMD getLocazioneMD(CoordinateMD coordinate) {
		return getForestaMD().ottieniLocazioneMD(coordinate);
	}

	/**
	 * Costruisce la locazione unica in una casella libera a caso.
	 *
	 * @param conosciutaSuMappa se la casella è già conosciuta dal giocatore
	 * @return le coordinate della casella scelta
	 */
	public static CoordinateMD costruisciLocazioneUnica(TipoLocazione tipoLocazioneUnica, boolean conosciutaSuMappa) {
		return costruisciLocazioneUnica(tipoLocazioneUnica, getCoordinateLibere(), conosciutaSuMappa);
	}

	/**
	 * Costruisce la locazione unica in quelle coordinate, scelte da chi chiama (per esempio da
	 * RegistroMissioni.cerca, per una missione che si procura la propria locazione).
	 */
	public static CoordinateMD costruisciLocazioneUnica(TipoLocazione tipoLocazioneUnica, CoordinateMD coordinate, boolean conosciutaSuMappa) {
		if (!tipoLocazioneUnica.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazione per creare " + tipoLocazioneUnica.name());
		}
		Logger.log(() -> "Costruzione di " + tipoLocazioneUnica + " in " + coordinate);
		impostaLocazione(coordinate, tipoLocazioneUnica);
		getLocazioneMD(coordinate).setNome(tipoLocazioneUnica.getNomeProprio());
		getForestaMD().aggiungiLocazioneUnica(tipoLocazioneUnica, coordinate);
		if (conosciutaSuMappa) {
			setLocazioneConosciuta(coordinate);
		}
		return coordinate;
	}

	/**
	 * Un nome dal generatore, possibilmente diverso da quello di tutte le caselle di quella classe (per esempio di
	 * tutti gli altri templi): se dopo qualche tentativo non ci si riesce, va bene anche un doppione.
	 */
	public static String nomeNuovo(TipoLocazione tipo, Supplier<String> generatore) {
		Set<String> giaDati = new HashSet<>();
		for (int x = 0; x < getDimensioneX(); x++) {
			for (int y = 0; y < getDimensioneY(); y++) {
				LocazioneMD md = getLocazioneMD(new CoordinateMD(x, y));
				if (md != null && md.getTipo() == tipo && md.getNome() != null) {
					giaDati.add(md.getNome());
				}
			}
		}
		String nome = generatore.get();
		for (int i = 1; i < TENTATIVI_NOME_NUOVO && giaDati.contains(nome); i++) {
			nome = generatore.get();
		}
		return nome;
	}

	/**
	 * Il nome della casella da mostrare sulla mappa, se il gruppo la conosce e ha un nome (città, castelli, locande,
	 * templi...); altrimenti null.
	 */
	public static String getNomeDaMostrare(CoordinateMD coordinate) {
		if (!isDentroLaMappa(coordinate) || !isLocazioneConosciuta(coordinate)) {
			return null;
		}
		return getLocazioneMD(coordinate).getNome();
	}

	/**
	 * Se la casella sta dentro la mappa.
	 */
	public static boolean isDentroLaMappa(CoordinateMD coordinate) {
		return coordinate.getX() >= 0 && coordinate.getX() < getDimensioneX() && coordinate.getY() >= 0 && coordinate.getY() < getDimensioneY();
	}

	/**
	 * Una casella libera a caso: vuota, o un bosco.
	 */
	public static CoordinateMD getCoordinateLibere() {
		TipoLocazione tipoLocazione;
		CoordinateMD coordinate;
		do {
			coordinate = new CoordinateMD(Dado.tira(getDimensioneX()) - 1, Dado.tira(getDimensioneY()) - 1);
			tipoLocazione = getLocazione(coordinate);
		} while (!isLibera(tipoLocazione));
		return coordinate;
	}

	/**
	 * Se su una casella di quel tipo si può costruire: vuota, o un bosco. Non una radura: sono poche apposta, e una locanda o il
	 * covo di una missione costruiti sopra le cancellerebbero.
	 */
	public static boolean isLibera(TipoLocazione tipoLocazione) {
		return tipoLocazione == null || tipoLocazione == TipoLocazione.BOSCO;
	}

	/**
	 * Abbiamo appena visitato questa locazione
	 */
	public static void setLocazioneVisitata(CoordinateMD coordinate) {
		getForestaMD().impostaLocazioneVisitata(coordinate);
	}

	/**
	 * Segna la locazione come visitata o non visitata.
	 */
	public static void setLocazioneVisitata(CoordinateMD coordinate, boolean visitata) {
		getForestaMD().impostaLocazioneVisitata(coordinate, visitata);
	}

	/**
	 * Siamo già passati da questa locazione?
	 */
	public static boolean isLocazioneVisitata(CoordinateMD coordinate) {
		return getForestaMD().isLocazioneVisitata(coordinate);
	}

	/**
	 * Sappiamo cosa ci sia in questa locazione
	 */
	public static void setLocazioneConosciuta(CoordinateMD coordinate) {
		getForestaMD().impostaLocazioneConosciuta(coordinate);
	}

	/**
	 * Se il gruppo sa cosa c'è in questa locazione.
	 */
	public static boolean isLocazioneConosciuta(CoordinateMD coordinate) {
		return getForestaMD().isLocazioneConosciuta(coordinate);
	}

	/**
	 * Cambia a ogni modifica di quel che il gruppo conosce della mappa, così chi la disegna sa quando rifarlo.
	 */
	public static int getVersioneMappa() {
		return getForestaMD().getVersioneMappa();
	}

	/**
	 * Un personaggio compra la mappa della foresta da un PNG
	 */
	public static void ottieniMappa() {
		getForestaMD().ottieniMappa();
	}

	/**
	 * Un PNG mostra a un personaggio un pezzetto della mappa della foresta
	 */
	public static void ottieniMappaZona(int daX, int daY, int aX, int aY) {
		for (int x = daX; x <= aX; x++) {
			if (x >= 0 && x < getDimensioneX()) {
				for (int y = daY; y <= aY; y++) {
					if (y >= 0 && y < getDimensioneY()) {
						setLocazioneConosciuta(new CoordinateMD(x, y));
					}
				}
			}
		}
	}

	/**
	 * La colonna più a sinistra con una casella conosciuta.
	 */
	public static int getMinXConosciuta() {
		return getForestaMD().getMinXConosciuta();
	}

	/**
	 * La colonna più a destra con una casella conosciuta.
	 */
	public static int getMaxXConosciuta() {
		return getForestaMD().getMaxXConosciuta();
	}

	/**
	 * La riga più in alto con una casella conosciuta.
	 */
	public static int getMinYConosciuta() {
		return getForestaMD().getMinYConosciuta();
	}

	/**
	 * La riga più in basso con una casella conosciuta.
	 */
	public static int getMaxYConosciuta() {
		return getForestaMD().getMaxYConosciuta();
	}
}
