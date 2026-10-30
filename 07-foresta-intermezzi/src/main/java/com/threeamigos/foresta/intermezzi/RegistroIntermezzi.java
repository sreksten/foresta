package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.modellodati.IntermezziMD;
import com.threeamigos.foresta.tipi.TipoIntermezzo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * Facciata su {@link IntermezziMD}: sceglie il prossimo intermezzo da mostrare e
 * ricorda quelli già scattati. Gli intermezzi fissi sono quelli elencati in
 * {@link TipoIntermezzo}; in più ci sono quelli delle fonti che il registro non conosce ({@link FonteDiIntermezzi}: i passi
 * conclusi delle missioni a passi, che si ricordano nella missione stessa).
 */
public class RegistroIntermezzi {

	// Se nel momento in corso è già scattato un intermezzo: quelli di ripiego non devono
	// sommarsi agli altri (vedi Intermezzo.isDiRipiego)
	private static boolean scattatoNelMomento;
	// Gli intermezzi ripetibili già mostrati nel momento in corso: non si segnano come scattati, ma in uno stesso momento
	// (un ingresso) scattano una volta sola, altrimenti finito l'uno ricomincerebbe subito
	private static final Set<String> ripetibiliDelMomento = new HashSet<>();
	// Chi, oltre agli intermezzi fissi, ha intermezzi da mostrare (le missioni): il registro non sa chi sono
	private static final List<FonteDiIntermezzi> FONTI = new CopyOnWriteArrayList<>();
	// Da dove prendere i dati della partita in corso: gli intermezzi non sanno chi li custodisce
	private static volatile Supplier<IntermezziMD> fornitoreDiDati;

	private RegistroIntermezzi() {
	}

	/**
	 * Comincia un nuovo momento (un ingresso, una fine locazione...): gli intermezzi che
	 * scatteranno da qui in poi gli appartengono.
	 */
	public static void nuovoMomento() {
		scattatoNelMomento = false;
		ripetibiliDelMomento.clear();
	}

	/**
	 * Se nel momento in corso è già scattato un intermezzo: usato da {@link
	 * com.threeamigos.foresta.intermezzi.IntermezzoAccampamento} per non sovrapporsi
	 * all'intermezzo di una missione (vedi LaLealta) già mostrato in questo stesso momento.
	 */
	public static boolean isScattatoNelMomento() {
		return scattatoNelMomento;
	}

	/**
	 * Dice da dove prendere gli intermezzi già scattati nella partita in corso (lo fa chi custodisce i dati: ogni volta si prendono da
	 * quelli in corso).
	 */
	public static void setFornitoreDiDati(Supplier<IntermezziMD> fornitore) {
		fornitoreDiDati = fornitore;
	}

	private static IntermezziMD getIntermezziMD() {
		Supplier<IntermezziMD> fornitore = fornitoreDiDati;
		if (fornitore == null) {
			throw new IllegalStateException("Nessuno ha detto agli intermezzi dove sono i loro dati (vedi setFornitoreDiDati)");
		}
		return fornitore.get();
	}

	/**
	 * Il primo intermezzo non ancora scattato che deve scattare nel momento indicato; null se
	 * nessuno. Prima quelli fissi, nell'ordine di TipoIntermezzo, poi quelli dei passi delle
	 * missioni, nell'ordine in cui i passi si sono conclusi; quelli di ripiego solo se nel
	 * momento non scatta, e non è già scattato, nessun altro.
	 */
	public static Intermezzo getProssimoIntermezzo(MomentoIntermezzo momento) {
		Intermezzo intermezzo = getProssimoIntermezzo(momento, false);
		if (intermezzo == null) {
			intermezzo = getProssimoIntermezzoDelleFonti(momento);
		}
		if (intermezzo == null && !scattatoNelMomento) {
			intermezzo = getProssimoIntermezzo(momento, true);
		}
		return intermezzo;
	}

	private static Intermezzo getProssimoIntermezzo(MomentoIntermezzo momento, boolean diRipiego) {
		for (TipoIntermezzo tipoIntermezzo : TipoIntermezzo.values()) {
			Intermezzo intermezzo = FabbricaIntermezzi.crea(tipoIntermezzo);
			if (intermezzo.isDiRipiego() == diRipiego && !getIntermezziMD().isScattato(intermezzo.getId())
					&& !ripetibiliDelMomento.contains(intermezzo.getId()) && intermezzo.deveScattare(momento)) {
				return intermezzo;
			}
		}
		return null;
	}

	/**
	 * Il primo intermezzo che una delle fonti (le missioni) vuole mostrare in quel momento.
	 */
	private static Intermezzo getProssimoIntermezzoDelleFonti(MomentoIntermezzo momento) {
		for (FonteDiIntermezzi fonte : FONTI) {
			Intermezzo intermezzo = fonte.getProssimo(momento);
			if (intermezzo != null) {
				return intermezzo;
			}
		}
		return null;
	}

	/**
	 * Aggiunge una fonte di intermezzi che il registro non conosce (le missioni, vedi {@link FonteDiIntermezzi}); la stessa fonte
	 * non si aggiunge due volte.
	 */
	public static void aggiungiFonte(FonteDiIntermezzi fonte) {
		if (!FONTI.contains(fonte)) {
			FONTI.add(fonte);
		}
	}

	/**
	 * Se una delle fonti ha un intermezzo che arriverà in quel momento (vedi {@link FonteDiIntermezzi#haUnIntermezzoInArrivo}).
	 */
	public static boolean haUnaFonteUnIntermezzoInArrivo(MomentoIntermezzo momento) {
		return FONTI.stream().anyMatch(fonte -> fonte.haUnIntermezzoInArrivo(momento));
	}

	/**
	 * Se una delle fonti tiene per sé la scena dell'intermezzo fisso di quel tipo (vedi {@link FonteDiIntermezzi#blocca}).
	 */
	public static boolean isBloccato(TipoIntermezzo tipo) {
		return FONTI.stream().anyMatch(fonte -> fonte.blocca(tipo));
	}

	/**
	 * Se l'intermezzo fisso di quel tipo è già scattato (una volta sola per partita, vedi IntermezziMD).
	 */
	public static boolean isScattato(TipoIntermezzo tipo) {
		return getIntermezziMD().isScattato(tipo.name());
	}

	/**
	 * Se l'intermezzo con quell'identificativo è già scattato (vedi Intermezzo.getId).
	 */
	public static boolean isScattato(String id) {
		return getIntermezziMD().isScattato(id);
	}

	/**
	 * L'intermezzo è scattato: lo ricorda (vedi {@link Intermezzo#segnaScattato()}), e nel momento in corso qualcosa è scattato.
	 */
	public static void segnaScattato(Intermezzo intermezzo) {
		intermezzo.segnaScattato();
		scattatoNelMomento = true;
	}

	/**
	 * Ricorda un intermezzo scattato: un ripetibile solo per il momento in corso, gli altri in IntermezziMD.
	 */
	public static void ricorda(Intermezzo intermezzo) {
		if (intermezzo.isRipetibile()) {
			ripetibiliDelMomento.add(intermezzo.getId());
		} else {
			getIntermezziMD().aggiungiScattato(intermezzo.getId());
		}
	}
}
