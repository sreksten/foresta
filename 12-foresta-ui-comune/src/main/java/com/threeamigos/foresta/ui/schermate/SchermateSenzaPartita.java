package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.interfacce.GestoreSalvataggi;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.schermate.DisposizioneDiSchermata.Sfondo;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Le schermate senza partita in corso, come descrizioni senza disegno ({@link DisposizioneDiSchermata}): i messaggi su sfondo con l'ombra del drago, le
 * statistiche di fine partita, la scelta dello slot di salvataggio da caricare o dove salvare, la conferma di uscita, la sconfitta, la vittoria e la classifica
 * ferma. Tiene il messaggio, il passo della sequenza dei finali e i salvataggi disponibili. Lo usano Swing e libGDX.
 */
public final class SchermateSenzaPartita {

	/**
	 * L'alone nero attorno alle lettere della storia e ai loghi, perché si vedano sullo sfondo
	 */
	public static final int RAGGIO_ALONE = 6;
	// La classifica: dove si ferma la prima riga e quanto distano le righe
	public static final int QUOTA_CLASSIFICA = 40;
	public static final int RIGA_CLASSIFICA = 50;
	// A sinistra e a destra della classifica e dei trofei
	public static final int MARGINE = 50;
	// Gli slot: la scritta in cima sta a QUOTA_DEGLI_SLOT, lo slot n a QUOTA_DEGLI_SLOT + n * PASSO_DEGLI_SLOT (meno, su uno schermo basso), cinque in tutto
	private static final int QUOTA_DEGLI_SLOT = 50;
	private static final int PASSO_DEGLI_SLOT = 100;
	private static final int SLOT_A_SCHERMO = 5;

	private final int width;
	private final int height;
	private final int xOffset;
	private final int yOffset;
	private final VistaPartita vistaPartita;
	private final MisureDelleImmagini misure;

	private int sequenza;
	private String messaggio;
	private Collection<TestataSalvataggio> salvataggiDisponibili;
	// Le schermate già fatte, per la chiave che ne riassume lo stato: si ridisegnano a ogni fotogramma e i loro pixel non vanno rifatti
	private final java.util.Map<String, DisposizioneDiSchermata> fatte = new java.util.HashMap<>();

	/**
	 * @param vistaPartita la partita appena finita, per le statistiche e i finali
	 */
	public SchermateSenzaPartita(int width, int height, VistaPartita vistaPartita, MisureDelleImmagini misure) {
		this.vistaPartita = vistaPartita;
		this.misure = misure;
		this.width = width;
		this.height = height;
		/* Mappa + spazio + locazione (bassa risoluzione Commodore Amiga 😄) */
		xOffset = width > 320 ? (width - 320) >> 1 : 0;
		yOffset = height > 256 ? (height - 256) >> 1 : 0;
	}

	/**
	 * Fa ripartire la sequenza dei finali dal primo messaggio.
	 */
	public void resettaSequenza() {
		sequenza = 0;
	}

	/**
	 * Passa al passo successivo della sequenza, ripartendo da zero oltre la lunghezza massima.
	 */
	public void incrementaSequenza(int lunghezzaMassima) {
		sequenza++;
		if (sequenza >= lunghezzaMassima) {
			sequenza = 0;
		}
	}

	/**
	 * Imposta il messaggio che {@link #scrivi} mostra.
	 */
	public void impostaMessaggio(String messaggio) {
		this.messaggio = messaggio;
	}

	public void setSalvataggiDisponibili(Collection<TestataSalvataggio> salvataggiDisponibili) {
		this.salvataggiDisponibili = salvataggiDisponibili;
	}

	/**
	 * Un testo nell'alfabeto grande, a capo dove serve, con le righe centrate a partire da quella quota
	 */
	private void testoCentrato(List<Elemento> elementi, String s, int y) {
		for (String riga : LayoutDelTestoGrande.righe(s, width)) {
			int larghezza = LayoutDelTestoGrande.larghezza(riga);
			elementi.add(Elemento.testoGrande(LayoutDelTestoGrande.immagine(riga, Math.max(1, larghezza), false), (width - larghezza) >> 1, y));
			y += LayoutDelTestoGrande.ALTEZZA_RIGA;
		}
	}

	private DisposizioneDiSchermata ricordata(String chiaveSenzaLingua, java.util.function.Supplier<DisposizioneDiSchermata> costruttore) {
		// Le scritte cambiano con la lingua, che si può cambiare dalle opzioni: una schermata fatta in un'altra lingua non vale
		String chiave = Testi.getLingua() + "|" + chiaveSenzaLingua;
		DisposizioneDiSchermata fatta = fatte.get(chiave);
		if (fatta == null) {
			if (fatte.size() > 40) {
				fatte.clear();
			}
			fatta = costruttore.get();
			fatte.put(chiave, fatta);
		}
		return fatta;
	}

	private String chiaveDeiSalvataggi() {
		StringBuilder chiave = new StringBuilder(Lingue.corrente().toString());
		if (salvataggiDisponibili != null) {
			for (TestataSalvataggio testata : salvataggiDisponibili) {
				chiave.append('|').append(testata.getId()).append(':').append(testata.getDescrizione());
				for (VistaPersonaggio personaggio : testata.getGruppoGiocatore().getPersonaggi()) {
					chiave.append(',').append(personaggio.getClasse());
				}
			}
		}
		return chiave.toString();
	}

	private DisposizioneDiSchermata schermata(Sfondo sfondo, List<Elemento> elementi) {
		return new DisposizioneDiSchermata(sfondo, new ContenutoDiColonna(width, height, elementi));
	}

	/**
	 * Il messaggio corrente, centrato e con a capo automatico, sopra l'ombra del drago se richiesta.
	 */
	public DisposizioneDiSchermata scrivi(boolean conOmbraDelDrago) {
		return ricordata("scrivi|" + conOmbraDelDrago + "|" + messaggio, () -> {
			List<Elemento> elementi = new ArrayList<>();
			if (messaggio != null && !messaggio.isEmpty()) {
				testoCentrato(elementi, messaggio.toLowerCase(Locale.ROOT), 20);
			}
			return schermata(conOmbraDelDrago ? Sfondo.OMBRA_DEL_DRAGO : Sfondo.NESSUNO, elementi);
		});
	}

	/**
	 * Un testo centrato in alto, con a capo automatico, nell'alfabeto grande dei messaggi: per chi compone una schermata attorno al testo (gli intermezzi).
	 * Non tocca il messaggio corrente.
	 */
	public DisposizioneDiSchermata testo(String testo) {
		return ricordata("testo|" + testo, () -> {
			List<Elemento> elementi = new ArrayList<>();
			if (testo != null && !testo.isEmpty()) {
				testoCentrato(elementi, testo.toLowerCase(Locale.ROOT), 20);
			}
			return schermata(Sfondo.NESSUNO, elementi);
		});
	}

	/**
	 * I mostri uccisi per classe, uno per riga.
	 */
	public DisposizioneDiSchermata statistiche() {
		StringBuilder chiave = new StringBuilder("statistiche|" + Lingue.corrente() + "|" + vistaPartita.getGiorno());
		for (TipoPersonaggio classe : TipoPersonaggio.values()) {
			chiave.append(',').append(vistaPartita.getMostriUccisi(classe));
		}
		return ricordata(chiave.toString(), this::costruisciLeStatistiche);
	}

	private DisposizioneDiSchermata costruisciLeStatistiche() {
		List<Elemento> elementi = new ArrayList<>();
		int x = xOffset + 9;
		int y = yOffset + 20;
		DoomdarkFont font = DoomdarkFont.medio();
		int giorni = vistaPartita.getGiorno();
		String titolo = giorni > 1 ? TestiDellaUI.get("MOSTRI_UCCISI_IN_GIORNI", Lingue.corrente().cardinale(giorni, Genere.MASCHILE))
				: TestiDellaUI.get("MOSTRI_UCCISI_IN_UN_GIORNO");
		elementi.add(Elemento.testo(Collections.singletonList(titolo), font, DoomdarkColorModel.Color.LIGHT_GRAY, x, y));
		y += font.getHeight();
		DoomdarkColorModel.Color colore = DoomdarkColorModel.Color.MEDIUM_GRAY;
		for (TipoPersonaggio classe : TipoPersonaggio.values()) {
			int m = vistaPartita.getMostriUccisi(classe);
			if (m > 0) {
				colore = colore == DoomdarkColorModel.Color.MEDIUM_GRAY ? DoomdarkColorModel.Color.LIGHT_GRAY : DoomdarkColorModel.Color.MEDIUM_GRAY;
				String riga = m + " " + (m == 1 ? vistaPartita.getNomeSingolare(classe) : vistaPartita.getNomePlurale(classe));
				elementi.add(Elemento.testo(Collections.singletonList(riga), font, colore, x, y));
				y += font.getHeight();
			}
		}
		return schermata(Sfondo.OMBRA_DEL_DRAGO, elementi);
	}

	private static int numeroDaComando(Comando comando) {
		switch (comando) {
			case NUMERO_1:
				return 1;
			case NUMERO_2:
				return 2;
			case NUMERO_3:
				return 3;
			case NUMERO_4:
				return 4;
			case NUMERO_5:
				return 5;
			default:
				BusEventi.pubblica(new InternoErrore("Comando non valido: " + comando));
				throw new IllegalStateException("Comando non valido: " + comando);
		}
	}

	/**
	 * La quota dello slot con quel numero (da 1 a 5, l'ultimo è il salvataggio automatico): uno ogni 100 pixel, o meno se lo schermo è basso (sul telefono),
	 * perché anche l'ultimo, con la sua riga di testo grande, stia sullo schermo.
	 */
	int getCoordinataY(int id) {
		int passo = Math.min(PASSO_DEGLI_SLOT, (height - QUOTA_DEGLI_SLOT - LayoutDelTestoGrande.ALTEZZA_RIGA - RAGGIO_ALONE) / SLOT_A_SCHERMO);
		return QUOTA_DEGLI_SLOT + passo * id;
	}

	/**
	 * La scritta "seleziona lo slot da caricare", con un alone nero attorno alle lettere perché si legga sullo sfondo del drago, e gli slot con il gruppo di
	 * ciascuno.
	 */
	public DisposizioneDiSchermata selezioneSlotDaCaricare() {
		return ricordata("slotDaCaricare|" + width + "|" + chiaveDeiSalvataggi(), this::costruisciSlotDaCaricare);
	}

	private DisposizioneDiSchermata costruisciSlotDaCaricare() {
		List<Elemento> elementi = new ArrayList<>();
		ImmagineDiPixel scritta = LayoutDelTestoGrande.conAlone(
				LayoutDelTestoGrande.immagine(TestiDellaUI.get("SELEZIONA_SLOT_CARICAMENTO"), width - 2 * RAGGIO_ALONE, true), RAGGIO_ALONE);
		elementi.add(Elemento.testoGrande(scritta, "slotDaCaricare|" + TestiDellaUI.get("SELEZIONA_SLOT_CARICAMENTO") + "|" + width, 0, 50 - RAGGIO_ALONE));
		aggiungiGliSlot(elementi, salvataggiDisponibili);
		return schermata(Sfondo.OMBRA_DEL_DRAGO, elementi);
	}

	private void aggiungiGliSlot(List<Elemento> elementi, Collection<TestataSalvataggio> salvataggi) {
		// Prima i personaggi di tutti gli slot, poi tutte le scritte: su uno schermo basso gli slot si avvicinano, e i personaggi di uno slot non devono
		// coprire la scritta di quello sopra
		List<Elemento> scritte = new ArrayList<>();
		for (TestataSalvataggio testata : salvataggi) {
			try {
				int id = numeroDaComando(testata.getId());
				int y = getCoordinataY(id);
				aggiungiIPersonaggi(elementi, id, testata.getGruppoGiocatore().getPersonaggi(), y);
				ImmagineDiPixel descrizione = LayoutDelTestoGrande.conAlone(LayoutDelTestoGrande.immagine(
						id + " - " + testata.getDescrizione().toLowerCase(Locale.ROOT), width - 2 * RAGGIO_ALONE, true), RAGGIO_ALONE);
				scritte.add(Elemento.testoGrande(descrizione, 0, y - RAGGIO_ALONE));
			} catch (Exception e) {
				BusEventi.pubblica(new InternoException("Durante lettura intestazione del file di salvataggio " + testata.getId(), e));
			}
		}
		elementi.addAll(scritte);
	}

	private void aggiungiIPersonaggi(List<Elemento> elementi, int id, Collection<? extends VistaPersonaggio> personaggi, int coordinataY) {
		List<Misura> misureDeiPersonaggi = new ArrayList<>();
		List<VistaPersonaggio> ordinati = new ArrayList<>();
		List<Integer> coordinateX = new ArrayList<>();
		int coordinataX = (width >> 1) + 100 * (id - 3);
		int altezzaMinima = 999;
		for (VistaPersonaggio personaggio : personaggi) {
			Misura misura = misure.personaggio(personaggio.getClasse());
			misureDeiPersonaggi.add(0, misura);
			ordinati.add(0, personaggio);
			coordinateX.add(0, coordinataX);
			coordinataX += misura.larghezza * 2 / 3;
			altezzaMinima = Math.min(altezzaMinima, misura.altezza);
		}
		coordinataY += altezzaMinima * 2 / 3;
		for (int i = 0; i < ordinati.size(); i++) {
			Misura misura = misureDeiPersonaggi.get(i);
			elementi.add(Elemento.immagine(NomiDelleImmagini.personaggio(ordinati.get(i).getClasse()), misura.larghezza, misura.altezza, false, null,
					coordinateX.get(i), coordinataY - misura.altezza));
		}
	}

	/**
	 * Gli slot di salvataggio fra cui scegliere dove salvare, con il gruppo di quelli occupati.
	 */
	public DisposizioneDiSchermata selezioneSlotDaSalvare() {
		return ricordata("slotDaSalvare|" + width + "|" + chiaveDeiSalvataggi(), this::costruisciSlotDaSalvare);
	}

	private DisposizioneDiSchermata costruisciSlotDaSalvare() {
		List<Elemento> elementi = new ArrayList<>();
		testoCentrato(elementi, TestiDellaUI.get("SELEZIONA_SLOT_SALVATAGGIO"), 50);
		// Si salva solo negli slot del giocatore: la quinta riga è il salvataggio automatico in memoria, che compare solo fra quelli da caricare
		List<Comando> comandi = new ArrayList<>(GestoreSalvataggi.SLOT_MANUALI);
		for (TestataSalvataggio testata : salvataggiDisponibili) {
			comandi.remove(testata.getId());
		}
		aggiungiGliSlot(elementi, salvataggiDisponibili);
		for (Comando slotDisponibile : comandi) {
			int numero = numeroDaComando(slotDisponibile);
			testoCentrato(elementi, TestiDellaUI.get("SLOT_DISPONIBILE", numero), getCoordinataY(numero));
		}
		return schermata(Sfondo.OMBRA_DEL_DRAGO, elementi);
	}

	/**
	 * La richiesta di conferma per uscire dal gioco.
	 */
	public DisposizioneDiSchermata confermaUscita() {
		return ricordata("uscita|" + TestiDellaUI.get("USCIRE_DAL_GIOCO"), () -> {
			List<Elemento> elementi = new ArrayList<>();
			testoCentrato(elementi, TestiDellaUI.get("USCIRE_DAL_GIOCO"), 100);
			return schermata(Sfondo.OMBRA_DEL_DRAGO, elementi);
		});
	}

	/**
	 * La schermata della sconfitta: il messaggio del finale a quel punto della sequenza.
	 */
	public DisposizioneDiSchermata perso() {
		messaggio = vistaPartita.getFinaleDellaSconfitta().get(sequenza);
		return scrivi(true);
	}

	/**
	 * La schermata della vittoria: l'immagine del trionfo e il messaggio del finale a quel punto della sequenza.
	 */
	public DisposizioneDiSchermata vinto() {
		messaggio = vistaPartita.getFinaleDellaVittoria().get(sequenza);
		return scrivi(false).conSfondo(Sfondo.TRIONFO);
	}

	/**
	 * La classifica ferma, a fine partita: il nome a sinistra, il punteggio a destra, nell'alfabeto grande
	 */
	public DisposizioneDiSchermata hiscore(com.threeamigos.foresta.interfacce.GestorePunteggi gestorePunteggi) {
		StringBuilder chiave = new StringBuilder("hiscore|" + width);
		for (int i = 0; i < gestorePunteggi.getConteggio(); i++) {
			chiave.append('|').append(gestorePunteggi.getPunteggio(i).getNome()).append(':').append(gestorePunteggi.getPunteggio(i).getPunteggio());
		}
		return ricordata(chiave.toString(), () -> {
			ContenutoDiColonna classifica = IntroDelGioco.classifica(gestorePunteggi, width);
			List<Elemento> elementi = new ArrayList<>();
			for (Elemento e : classifica.getElementi()) {
				elementi.add(Elemento.testoGrande(e.getPixel(), e.getX(), e.getY() + QUOTA_CLASSIFICA));
			}
			return schermata(Sfondo.OMBRA_DEL_DRAGO, elementi);
		});
	}
}
