package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.tools.Punteggio;
import com.threeamigos.foresta.tools.TestataSalvataggio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class DisplayableCanvasIntroOutro implements Finestra {

	// Lo scorrimento dell'intro (vedi ScorrimentoVerticale): pixel al secondo, e quanto è alta la fascia di
	// dissolvenza ai bordi
	private static final double VELOCITA = 30;
	private static final int FASCIA = 40;
	// I loghi compaiono quando la storia è salita almeno tanto sopra di loro
	private static final int MARGINE_LOGHI = 20;
	// Fra la cima del logo 3AM e quella del logo della Foresta
	private static final int DISTANZA_LOGHI = 60;
	// L'alone nero attorno alle lettere della storia e ai loghi, perché si vedano sullo sfondo
	private static final int RAGGIO_ALONE = 6;
	private static final double SECONDI_DISSOLVENZA = 1;
	private static final double SECONDI_LOGHI = 3;
	private static final double SECONDI_CLASSIFICA = 5;
	// Il secondo fotogramma più lento che si accetta: dopo una pausa lunga lo scorrimento non salta
	private static final double SECONDI_FOTOGRAMMA_MASSIMI = 0.1;
	// La classifica: dove si ferma la prima riga e quanto distano le righe
	private static final int QUOTA_CLASSIFICA = 100;
	private static final int RIGA_CLASSIFICA = 50;
	// A sinistra e a destra della classifica e dei trofei
	private static final int MARGINE = 50;

	/**
	 * Le parti dell'intro: la storia che scorre (poi i loghi), la classifica che sale e si ferma, i trofei che scorrono.
	 */
	private enum Fase {
		STORIA, CLASSIFICA, TROFEI
	}

	private final int width;
	private final int height;
	private int sequenza;
	private final int xOffset;
	private final int yOffset;
	private String messaggio;
	private Fase fase = Fase.STORIA;
	private ScorrimentoVerticale scorrimento;
	private BufferedImage immagineStoria;
	// I loghi con l'alone scuro, perché si vedano sullo sfondo della storia: si fanno una volta sola
	private BufferedImage logo3AMConAlone;
	private BufferedImage logoForestaConAlone;
	// La scritta "seleziona lo slot da caricare" con l'alone scuro, perché si legga sullo sfondo del drago
	private BufferedImage immagineSelezionaSlotDaCaricare;
	// Il tempo della fase: dall'ultimo fotogramma, da quando i loghi compaiono, da quando si sta fermi
	private long ultimoFotogramma;
	private double secondiLoghi = -1;
	private double secondiFermo;
	private double secondiFase;

	private Collection<TestataSalvataggio> salvataggiDisponibili;

	private final GestorePunteggi gestorePunteggi;

	/**
	 * @param gestorePunteggi la classifica, che l'intro mostra fra le sue pagine
	 */
	DisplayableCanvasIntroOutro(int width, int height, GestorePunteggi gestorePunteggi) {
		this.gestorePunteggi = gestorePunteggi;
		this.width = width;
		this.height = height;

		if (width > 320) /* Mappa + spazio + locazione (bassa risoluzione Commodore Amiga 😄) */
			xOffset = (width - 320) >> 1;
		else
			xOffset = 0;
		if (height > 256)
			yOffset = (height - 256) >> 1;
		else
			yOffset = 0;
	}
	
	/**
	 * Fa ripartire l'intro dalla storia (e le schermate di fine partita dal primo messaggio).
	 */
	void resettaSequenza() {
		sequenza = 0;
		passaA(Fase.STORIA);
	}

	/**
	 * Porta l'intro alla classifica: da lì si passa ai trofei, poi si riparte dalla storia.
	 */
	void posizionaSuPunteggi() {
		passaA(Fase.CLASSIFICA);
	}

	private void passaA(Fase nuovaFase) {
		fase = nuovaFase;
		scorrimento = null;
		secondiLoghi = -1;
		secondiFermo = 0;
		secondiFase = 0;
		ultimoFotogramma = 0;
	}

	void incrementaSequenza(int lunghezzaMassima) {
		sequenza++;
		if (sequenza >= lunghezzaMassima) {
			sequenza = 0;
		}
	}
	
	void impostaMessaggio(String messaggio) {
		this.messaggio = messaggio;
	}

	void scrivi(Graphics2D graphics, boolean disegnaOmbraDelDrago) {
		if (disegnaOmbraDelDrago) {
			disegnaOmbraDelDrago(graphics);
		}
		if (messaggio != null && !messaggio.isEmpty())
			disegnaStringaCentrataConACapoAutomatico(graphics, messaggio.toLowerCase(), 20);
	}

	/**
	 * Testo centrato in alto, con a capo automatico, nell'alfabeto grande dei messaggi:
	 * per chi compone una schermata attorno al testo (vedi DisplayableCanvasIntermezzo).
	 * Non tocca il messaggio corrente di questo riquadro.
	 */
	void scriviTestoCentrato(Graphics2D graphics, String testo) {
		if (testo != null && !testo.isEmpty()) {
			disegnaStringaCentrataConACapoAutomatico(graphics, testo.toLowerCase(), 20);
		}
	}

	void statistiche(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		int locXOffset = xOffset;
		int locYOffset = yOffset + 20;
		DoomdarkColorModel.Color color = DoomdarkColorModel.Color.MEDIUM_GRAY;
		Image doomdark;
		int giorni = LineaTemporale.getGiorno();
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		doomdark = ImageCache.get("Avversari uccisi in " + (giorni > 1 ? (Misc.getCardinaleM(giorni) + " giorni:") : "un giorno:"), fontMedium, DoomdarkColorModel.Color.LIGHT_GRAY);
		graphics.drawImage(doomdark, locXOffset + 9, locYOffset, null);
		locYOffset += fontMedium.getHeight();
		for (TipoPersonaggio classePersonaggio : TipoPersonaggio.values()) {
			int m = Statistiche.getMostriUccisi(classePersonaggio);
			if (m > 0) {
				color = (color == DoomdarkColorModel.Color.MEDIUM_GRAY ? DoomdarkColorModel.Color.LIGHT_GRAY : DoomdarkColorModel.Color.MEDIUM_GRAY); 
				doomdark = ImageCache.get(m + " " + (m == 1 ? FabbricaPersonaggi.nomeSingolare(classePersonaggio) : FabbricaPersonaggi.nomePlurale(classePersonaggio)), fontMedium, color);
				graphics.drawImage(doomdark, locXOffset + 9, locYOffset, null);
				locYOffset += fontMedium.getHeight();
			}
		}
	}

	void setSalvataggiDisponibili(Collection<TestataSalvataggio> salvataggiDisponibili) {
		this.salvataggiDisponibili = salvataggiDisponibili;
	}

	void selezioneSlotDaCaricare(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		graphics.drawImage(immagineSelezionaSlotDaCaricare(), 0, 50 - RAGGIO_ALONE, null);
		for (TestataSalvataggio testata : salvataggiDisponibili) {
			try {
				disegnaElencoPersonaggiDaElencoClassi(graphics, testata);
			} catch (Exception e) {
				BusEventi.pubblica(new InternoException("Durante lettura intestazione del file di salvataggio " + testata.getId(), e));
			}
		}
	}

	private int numeroDaComando(Comando comando) {
		if (comando == Comando.NUMERO_1) {
			return 1;
		} else if (comando == Comando.NUMERO_2) {
			return 2;
		} else if (comando == Comando.NUMERO_3) {
			return 3;
		} else if (comando == Comando.NUMERO_4) {
			return 4;
		} else if (comando == Comando.NUMERO_5) {
			return 5;
		} else {
			BusEventi.pubblica(new InternoErrore("Comando non valido: " + comando));
			throw new IllegalStateException("Comando non valido: " + comando);
		}
	}

	private void disegnaElencoPersonaggiDaElencoClassi(Graphics2D graphics, TestataSalvataggio testata) {
		int id = numeroDaComando(testata.getId());
		int coordinataY = getCoordinataY(id);
		String descrizione = testata.getDescrizione();
        Collection<Personaggio> personaggi = testata.getGruppoGiocatore().getPersonaggi();
		disegnaPersonaggi(graphics, id, personaggi, coordinataY);
		BufferedImage immagineConAlone = TestoGrande.conAlone(
				TestoGrande.immagine(id + " - " + descrizione.toLowerCase(), width - 2 * RAGGIO_ALONE, true), RAGGIO_ALONE);
		graphics.drawImage(immagineConAlone, 0, coordinataY - RAGGIO_ALONE, null);
	}

	void selezioneSlotDaSalvare(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		disegnaStringaCentrataConACapoAutomatico(graphics, "seleziona lo slot per il salvataggio", 50);
		List<Comando> comandi = new ArrayList<>(Arrays.asList(Comando.NUMERO_1, Comando.NUMERO_2, Comando.NUMERO_3, Comando.NUMERO_4,
                Comando.NUMERO_5));
		for (TestataSalvataggio testata : salvataggiDisponibili) {
			comandi.remove(testata.getId());
			disegnaElencoPersonaggiDaElencoClassi(graphics, testata);
		}
		for (Comando slotDisponibile : comandi) {
			int numero = numeroDaComando(slotDisponibile);
			disegnaStringaCentrataConACapoAutomatico(graphics, numero + " - slot disponibile", getCoordinataY(numero));
		}
	}

	void confermaUscita(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		disegnaStringaCentrataConACapoAutomatico(graphics, "uscire dal gioco?", 100);
	}

	void perso(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		messaggio = Misc.PERSO[sequenza];
		scrivi(graphics, true);
	}

	void vinto(Graphics2D graphics) {
		Image d = ImageCache.trionfo;
		graphics.drawImage(d, (width - d.getWidth(null)) / 2, (height - d.getHeight(null)) / 2, null);
		messaggio = Misc.VINTO[sequenza];
		scrivi(graphics,false);
	}

	/**
	 * L'intro, un fotogramma: la storia sale sullo sfondo della storia, i loghi compaiono appena la storia è salita
	 * sopra di loro, poi loghi e sfondo spariscono insieme; la classifica sale e si ferma, poi sparisce; i trofei
	 * scorrono tutti. Poi si ricomincia.
	 */
	void intro(Graphics2D graphics) {
		double secondi = secondiDalFotogrammaPrecedente();
		switch (fase) {
			case STORIA:
				storia(graphics, secondi);
				break;
			case CLASSIFICA:
				classifica(graphics, secondi);
				break;
			default:
				trofei(graphics, secondi);
				break;
		}
	}

	private double secondiDalFotogrammaPrecedente() {
		long adesso = System.nanoTime();
		double secondi = ultimoFotogramma == 0 ? 0 : (adesso - ultimoFotogramma) / 1_000_000_000d;
		ultimoFotogramma = adesso;
		return Math.min(secondi, SECONDI_FOTOGRAMMA_MASSIMI);
	}

	private void storia(Graphics2D graphics, double secondi) {
		if (scorrimento == null) {
			scorrimento = ScorrimentoVerticale.dalBasso(immagineStoria(), schermo(), FASCIA);
		}
		if (!scorrimento.isUscito()) {
			scorrimento.avanza(secondi, VELOCITA);
		}
		secondiFase += secondi;
		// I loghi arrivano quando la storia non ci finirebbe sopra, e restano un poco dopo che è uscita; il loro
		// centro sta a un terzo dello schermo
		BufferedImage logo3AM = ImageCache.logo3AM;
		BufferedImage logoForesta = ImageCache.logoForesta;
		int cimaLoghi = height / 3 - (DISTANZA_LOGHI + logoForesta.getHeight()) / 2;
		if (secondiLoghi < 0 && scorrimento.getFondo() <= cimaLoghi - MARGINE_LOGHI) {
			secondiLoghi = 0;
		}
		float uscita = 0;
		if (secondiLoghi >= 0) {
			secondiLoghi += secondi;
			if (scorrimento.isUscito() && secondiLoghi >= SECONDI_DISSOLVENZA) {
				secondiFermo += secondi;
			}
			uscita = (float) Math.max(0, Math.min(1, (secondiFermo - SECONDI_LOGHI) / SECONDI_DISSOLVENZA));
		}
		// Lo sfondo della storia compare in dissolvenza, e sparisce insieme ai loghi
		float comparsa = (float) Math.min(1, secondiFase / SECONDI_DISSOLVENZA);
		disegnaConOpacita(graphics, ImageCache.sfondoStoria, (width - ImageCache.sfondoStoria.getWidth()) >> 1,
				(height - ImageCache.sfondoStoria.getHeight()) >> 1, comparsa * (1 - uscita));
		scorrimento.disegna(graphics, 1);
		if (secondiLoghi >= 0) {
			float opacita = (float) Math.min(1, secondiLoghi / SECONDI_DISSOLVENZA) * (1 - uscita);
			// Con l'alone l'immagine è più grande di RAGGIO_ALONE per lato: la si sposta, e il logo resta al suo posto
			if (logo3AMConAlone == null) {
				logo3AMConAlone = TestoGrande.conAlone(logo3AM, RAGGIO_ALONE);
				logoForestaConAlone = TestoGrande.conAlone(logoForesta, RAGGIO_ALONE);
			}
			disegnaConOpacita(graphics, logo3AMConAlone, ((width - logo3AM.getWidth()) >> 1) - RAGGIO_ALONE,
					cimaLoghi - RAGGIO_ALONE, opacita);
			disegnaConOpacita(graphics, logoForestaConAlone, ((width - logoForesta.getWidth()) >> 1) - RAGGIO_ALONE,
					cimaLoghi + DISTANZA_LOGHI - RAGGIO_ALONE, opacita);
		}
		if (uscita >= 1) {
			passaA(Fase.CLASSIFICA);
		}
	}

	private void classifica(Graphics2D graphics, double secondi) {
		disegnaOmbraDelDrago(graphics);
		if (scorrimento == null) {
			scorrimento = ScorrimentoVerticale.dalBasso(immagineClassifica(), schermo(), FASCIA).fermaA(QUOTA_CLASSIFICA);
		}
		scorrimento.avanza(secondi, VELOCITA);
		if (scorrimento.isArrivato()) {
			secondiFermo += secondi;
		}
		float uscita = (float) Math.max(0, Math.min(1, (secondiFermo - SECONDI_CLASSIFICA) / SECONDI_DISSOLVENZA));
		scorrimento.disegna(graphics, 1 - uscita);
		if (uscita >= 1) {
			passaA(Fase.TROFEI);
		}
	}

	private void trofei(Graphics2D graphics, double secondi) {
		disegnaOmbraDelDrago(graphics);
		if (scorrimento == null) {
			scorrimento = ScorrimentoVerticale.dalBasso(ImmagineTrofei.costruisci(width - 2 * MARGINE, true),
					schermo(), FASCIA);
		}
		scorrimento.avanza(secondi, VELOCITA);
		scorrimento.disegna(graphics, 1);
		if (scorrimento.isUscito()) {
			passaA(Fase.STORIA);
		}
	}

	/**
	 * La classifica ferma, a fine partita.
	 */
	void hiscore(Graphics2D graphics) {
		disegnaOmbraDelDrago(graphics);
		graphics.drawImage(immagineClassifica(), 0, QUOTA_CLASSIFICA, null);
	}

	private Rectangle schermo() {
		return new Rectangle(0, 0, width, height);
	}

	/**
	 * Tutta la storia in un'immagine, come un unico testo che va a capo dove serve (i paragrafi di Misc.STORIA erano le
	 * pagine dell'intro di una volta), con un alone nero attorno alle lettere. Non cambia mai: si fa una volta sola.
	 */
	private BufferedImage immagineStoria() {
		if (immagineStoria == null) {
			immagineStoria = TestoGrande.conAlone(
					TestoGrande.immagine(String.join(" ", Misc.STORIA), width - 2 * RAGGIO_ALONE, true), RAGGIO_ALONE);
		}
		return immagineStoria;
	}

	/**
	 * La scritta "seleziona lo slot da caricare", con un alone nero attorno alle lettere perché si legga sullo
	 * sfondo del drago. Non cambia mai: si fa una volta sola.
	 */
	private BufferedImage immagineSelezionaSlotDaCaricare() {
		if (immagineSelezionaSlotDaCaricare == null) {
			immagineSelezionaSlotDaCaricare = TestoGrande.conAlone(
					TestoGrande.immagine("seleziona lo slot da caricare", width - 2 * RAGGIO_ALONE, true), RAGGIO_ALONE);
		}
		return immagineSelezionaSlotDaCaricare;
	}

	/**
	 * La classifica in un'immagine, nell'alfabeto grande: il nome a sinistra, il punteggio a destra.
	 */
	private BufferedImage immagineClassifica() {
		int righe = gestorePunteggi.getConteggio();
		BufferedImage immagine = new BufferedImage(width, Math.max(1, righe * RIGA_CLASSIFICA), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		try {
			for (int posizione = 0; posizione < righe; posizione++) {
				Punteggio punteggio = gestorePunteggi.getPunteggio(posizione);
				int y = posizione * RIGA_CLASSIFICA;
				TestoGrande.disegnaRiga(g, TestoGrande.normalizza(punteggio.getNome()), MARGINE, y);
				String valore = String.valueOf(punteggio.getPunteggio());
				TestoGrande.disegnaRiga(g, valore, width - MARGINE - TestoGrande.larghezza(valore), y);
			}
		} finally {
			g.dispose();
		}
		return immagine;
	}

	private static void disegnaConOpacita(Graphics2D graphics, Image immagine, int x, int y, float opacita) {
		if (opacita <= 0) {
			return;
		}
		Composite composito = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, opacita)));
		graphics.drawImage(immagine, x, y, null);
		graphics.setComposite(composito);
	}

	void disegnaOmbraDelDrago(Graphics2D graphics) {
		Image d = ImageCache.ombraDelDrago;
		graphics.drawImage(d, (width - d.getWidth(null)) >> 1, (height - d.getHeight(null)) >> 1, null);
	}
	
	/**
	 * Il testo nell'alfabeto grande, a capo dove serve, con le righe centrate a partire da quella quota.
	 */
	private void disegnaStringaCentrataConACapoAutomatico(Graphics2D graphics, String s, int yOffset) {
		for (String riga : TestoGrande.righe(s, width)) {
			TestoGrande.disegnaRiga(graphics, riga, (width - TestoGrande.larghezza(riga)) >> 1, yOffset);
			yOffset += TestoGrande.ALTEZZA_RIGA;
		}
	}

	private int getCoordinataY(int id) {
		return 50 + 100 * id;
	}

	private void disegnaPersonaggi(Graphics2D graphics, int id, Collection<Personaggio> personaggi, int coordinataY) {
		List<BufferedImage> immagini = new ArrayList<>();
		List<Integer> coordinateX = new ArrayList<>();
		int coordinataX = (width >> 1) + 100 * (id - 3);
		int altezzaMinima = 999;
		for (Personaggio personaggio : personaggi) {
			BufferedImage immagine = ClassePersonaggioImmagine.getImmagine(personaggio.getClasse());
			immagini.add(0, immagine);
			coordinateX.add(0, coordinataX);
			coordinataX += immagine.getWidth() * 2 / 3;
			if (altezzaMinima > immagine.getHeight()) {
				altezzaMinima = immagine.getHeight();
			}
		}
		coordinataY += altezzaMinima * 2 / 3;
		for (int i = 0; i < immagini.size(); i++) {
			BufferedImage immagine = immagini.get(i);
			graphics.drawImage(immagine, coordinateX.get(i), coordinataY - immagine.getHeight(), null);
		}
	}
}
