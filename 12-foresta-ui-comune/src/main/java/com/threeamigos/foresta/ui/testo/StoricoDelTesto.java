package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.modellodati.NotizieMD;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo storico di un rettangolo di testo scorrevole, senza disegno: le righe già spezzate alla larghezza del rettangolo, dalla più vecchia
 * alla più recente, in cui si torna indietro con la rotella invece di far scorrere via il testo vecchio. Si scorre a pixel, non a righe: la
 * prima e l'ultima riga visibili possono essere tagliate dal bordo. Lo storico condivide il limite con quanto ricordato da
 * {@code motore.Notizie} (vedi {@link NotizieMD#MASSIMO_MESSAGGI_RICORDATI}), così che dopo un ricaricamento il pannello possa essere
 * ripopolato per intero. Chi disegna (Swing, libGDX) si fa dire dove sta ogni riga con {@link #perOgniRigaVisibile}.
 * <p>
 * Una riga di testo è alta quanto l'immagine del font medio, con le righe in più per accenti e cediglie, e fra una riga e l'altra c'è un
 * pixel solo, perché le righe in più le separano già.
 */
public final class StoricoDelTesto {

	/**
	 * Chi riceve le righe visibili
	 */
	public interface PerOgniRiga {
		/**
		 * @param testo la riga già spezzata
		 * @param quota la y della sua cima nel rettangolo (può essere negativa o oltre il fondo se la riga è tagliata dal bordo)
		 */
		void riga(String testo, int quota);
	}

	// Pixel aggiuntivi fra il fondo di una riga e l'inizio della successiva
	private static final int INTERLINEA = 1;

	// Le righe già spezzate alla larghezza del rettangolo, dalla più vecchia alla più recente
	private final List<String> righe = new ArrayList<>();
	private final int larghezza;
	private final int altezza;
	private final DoomdarkFont font = DoomdarkFont.medio();
	private final int altezzaDellaRiga = font.getHeight();
	// Distanza fra le cime di due righe consecutive
	private final int passoRiga = altezzaDellaRiga + INTERLINEA;
	private final int righeVisibili;

	// Di quanti pixel si è tornati indietro rispetto alla più recente (0: la più recente in fondo); fuori dai limiti
	// di getScorrimentoMassimo di poco, per l'elastico di chi trascina
	private int offsetPixel;
	// Cresce a ogni cambiamento: chi conserva un'immagine del testo sa quando rifarla
	private int versione;

	public StoricoDelTesto(int larghezza, int altezza) {
		this.larghezza = larghezza;
		this.altezza = altezza;
		// La riga più in basso sta a altezza - altezzaDellaRiga, le altre a salire di passoRiga
		righeVisibili = altezza < altezzaDellaRiga ? 0 : (altezza - altezzaDellaRiga) / passoRiga + 1;
		imbottisci();
	}

	/**
	 * Mette in testa allo storico un cuscino di righe vuote. Serve allo scorrimento: la parte alta del rettangolo è coperta da una
	 * sfumatura che spegne il testo, e senza il cuscino le righe più vecchie resterebbero inchiodate lassù, illeggibili. Con mezzo
	 * rettangolo di righe vuote sopra di loro, arrivano invece a metà altezza, dove la sfumatura non le tocca più.
	 * <p>
	 * Le righe vuote fanno parte dello storico come le altre, quindi sono le prime a essere dimenticate quando il testo cresce.
	 */
	private void imbottisci() {
		for (int i = 0; i < righeVisibili / 2; i++) {
			righe.add("");
		}
	}

	/**
	 * Aggiunge una riga in fondo, spezzandola se è più larga del rettangolo, e la ricorda nello storico.
	 */
	public synchronized void addString(String s) {
		// Gestisce i \n letterali come vere newline
		String[] lines = s.split("\\\\n");
		for (String line : lines) {
			righe.addAll(FontTool.split(font, line, larghezza));
		}
		while (righe.size() > NotizieMD.MASSIMO_MESSAGGI_RICORDATI) {
			righe.remove(0);
		}
		// Un messaggio nuovo riporta in fondo: nel mezzo di una partita non deve poter passare inosservato perché si stava
		// rileggendo il testo vecchio
		offsetPixel = 0;
		versione++;
	}

	/**
	 * Svuota lo storico.
	 */
	public synchronized void clear() {
		righe.clear();
		imbottisci();
		offsetPixel = 0;
		versione++;
	}

	/**
	 * Di quanti pixel si può tornare indietro al massimo: fino a che la riga più vecchia arriva in cima.
	 */
	public synchronized int getScorrimentoMassimo() {
		return Math.max(0, righe.size() - righeVisibili) * passoRiga;
	}

	/**
	 * Mette il testo a quell'offset, in pixel indietro rispetto alla riga più recente (0: la più recente in fondo).
	 * Non lo limita: chi trascina può tirare di poco oltre i capi (elastico) e riportarlo dentro.
	 */
	public synchronized void impostaOffset(int pixel) {
		if (pixel != offsetPixel) {
			offsetPixel = pixel;
			versione++;
		}
	}

	/**
	 * Sposta indietro (o avanti) di righe intere, senza uscire dallo storico disponibile.
	 *
	 * @param righeIndietro positivo per tornare al testo più vecchio
	 */
	public synchronized void scorri(int righeIndietro) {
		int nuovoOffset = offsetPixel + righeIndietro * passoRiga;
		nuovoOffset = Math.max(0, Math.min(nuovoOffset, getScorrimentoMassimo()));
		impostaOffset(nuovoOffset);
	}

	/**
	 * Se si può ancora scorrere all'indietro verso i messaggi più vecchi (freccia su).
	 */
	public synchronized boolean isScorribileSu() {
		return offsetPixel < getScorrimentoMassimo();
	}

	/**
	 * Se si è già scorsi all'indietro e si può tornare verso i messaggi più recenti (freccia giù).
	 */
	public synchronized boolean isScorribileGiu() {
		return offsetPixel > 0;
	}

	/**
	 * Un numero che cambia ogni volta che il contenuto visibile cambia (testo aggiunto, svuotato o fatto scorrere)
	 */
	public synchronized int getVersione() {
		return versione;
	}

	/**
	 * Quante righe di testo stanno nel rettangolo
	 */
	public int getRigheVisibili() {
		return righeVisibili;
	}

	public int getLarghezza() {
		return larghezza;
	}

	public int getAltezza() {
		return altezza;
	}

	public DoomdarkFont getFont() {
		return font;
	}

	/**
	 * Dice a chi disegna dove sta ogni riga che l'offset corrente fa vedere, almeno in parte: la più recente in basso, spostata
	 * dell'offset, le precedenti a salire, dalla più recente alla più vecchia.
	 */
	public synchronized void perOgniRigaVisibile(PerOgniRiga destinatario) {
		// La riga i, contando dalla più recente, sta a altezza - altezzaDellaRiga - i * passoRiga + offsetPixel: si scrivono
		// solo quelle che ne vedono almeno una parte
		int da = Math.max(0, Math.floorDiv(offsetPixel - altezzaDellaRiga, passoRiga));
		int fine = Math.min(righe.size() - 1, Math.floorDiv(altezza + offsetPixel, passoRiga) + 1);
		for (int i = da; i <= fine; i++) {
			int quota = altezza - altezzaDellaRiga - i * passoRiga + offsetPixel;
			if (quota + altezzaDellaRiga <= 0 || quota >= altezza) {
				continue;
			}
			destinatario.riga(righe.get(righe.size() - 1 - i), quota);
		}
	}
}
