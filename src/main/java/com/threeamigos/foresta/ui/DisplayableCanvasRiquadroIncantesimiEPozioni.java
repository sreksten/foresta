package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Gli incantesimi e le pozioni del gruppo: una lista scrollabile con la rotella del mouse, una voce per riga, con il
 * nome per intero (senza abbreviazioni) e la quantità a destra. Gli incantesimi sono nell'ordine di ClasseIncantesimo, poi le pozioni.
 */
class DisplayableCanvasRiquadroIncantesimiEPozioni implements Finestra {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI = 16;
	// Lo spazio a destra delle quantità per le frecce che dicono che la lista continua
	private static final int LARGHEZZA_FRECCE = 12;

	/**
	 * Una riga della lista: l'icona, il nome e la quantità posseduta
	 */
	private static final class Riga {
		private final BufferedImage icona;
		private final String descrizione;
		private final ToIntFunction<VistaGruppoGiocatore> quantita;

		private Riga(BufferedImage icona, String descrizione, ToIntFunction<VistaGruppoGiocatore> quantita) {
			this.icona = icona;
			this.descrizione = descrizione;
			this.quantita = quantita;
		}
	}

	private final List<Riga> righe = new ArrayList<>();
	private final int righeVisibili;
	private final int topLeftX;
	private final int topLeftY;
	private final int iconaX;
	private final int nomeX;
	private final int totaleX;
	private final int frecceX;

	private int primaRigaVisibile = 0;

	private final VistaPartita vistaPartita;

	DisplayableCanvasRiquadroIncantesimiEPozioni(int topLeftX, int topLeftY, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;

		int maxIconWidth = 0;
		for (ClasseIncantesimo classeIncantesimo : ClasseIncantesimo.values()) {
			BufferedImage icona = ImageCache.spriteIncantesimi[classeIncantesimo.ordinal()];
			maxIconWidth = Math.max(maxIconWidth, icona.getWidth());
			righe.add(new Riga(icona, classeIncantesimo.getNome(), g -> g.getIncantesimi(classeIncantesimo)));
		}
		righe.add(new Riga(ImageCache.spritePozioneSalute, "Salute", VistaGruppoGiocatore::getPozioniSalute));
		righe.add(new Riga(ImageCache.spritePozioneSaluteGrande, "Salute grande", VistaGruppoGiocatore::getPozioniSaluteGrande));
		righe.add(new Riga(ImageCache.spritePozioneMagia, "Magia", VistaGruppoGiocatore::getPozioniMagia));
		righe.add(new Riga(ImageCache.spritePozioneMagiaGrande, "Magia grande", VistaGruppoGiocatore::getPozioniMagiaGrande));
		for (int i = ClasseIncantesimo.values().length; i < righe.size(); i++) {
			maxIconWidth = Math.max(maxIconWidth, righe.get(i).icona.getWidth());
		}

		int altezzaUtile = ImageCache.corniceIncantesimi.getHeight() - 2 * DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI;
		righeVisibili = Math.max(1, altezzaUtile / DoomdarkFontMedium.getInstance().getHeight());
		iconaX = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI;
		nomeX = iconaX + maxIconWidth + 2;
		frecceX = topLeftX + ImageCache.corniceIncantesimi.getWidth() - DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI - LARGHEZZA_FRECCE;
		totaleX = frecceX - 2;
	}

	private int primaRigaMassima() {
		return Math.max(0, righe.size() - righeVisibili);
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		int spostamento = movimentoRotella == MovimentoRotella.SU ? -numeroRotazioni : numeroRotazioni;
		primaRigaVisibile = Math.max(0, Math.min(primaRigaMassima(), primaRigaVisibile + spostamento));
	}

	void disegnaIncantesimi(Graphics2D graphics) {
		graphics.drawImage(ImageCache.corniceIncantesimi, topLeftX, topLeftY, null);

		graphics.setClip(topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI,
				topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI,
				ImageCache.corniceIncantesimi.getWidth() - 2 * DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI,
				ImageCache.corniceIncantesimi.getHeight() - 2 * DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI);

		VistaGruppoGiocatore g = vistaPartita.getGruppoGiocatore();
		DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();
		DoomdarkColorAlternante color = new DoomdarkColorAlternante();
		// Si alternano i colori su tutte le righe, anche quelle fuori vista, perché scorrendo non cambino
		for (int i = 0; i < righe.size(); i++) {
			DoomdarkColorModel.Color colore = color.getColor();
			if (i >= primaRigaVisibile && i < primaRigaVisibile + righeVisibili) {
				Riga riga = righe.get(i);
				disegnaRiga(graphics, fontMedium, yDellaRiga(i), riga, riga.quantita.applyAsInt(g), colore);
			}
		}
		disegnaFrecce(graphics);

		graphics.setClip(null);
	}

	private int yDellaRiga(int indice) {
		return topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI
				+ (indice - primaRigaVisibile) * DoomdarkFontMedium.getInstance().getHeight();
	}

	private void disegnaRiga(Graphics2D graphics, DoomdarkFont fontMedium, int y, Riga riga, int quantita, DoomdarkColorModel.Color color) {
		BufferedImage icona = riga.icona;
		graphics.drawImage(icona, iconaX, y - (icona.getHeight() - fontMedium.getHeight()) / 2, null);
		graphics.drawImage(ImageCache.get(riga.descrizione, fontMedium, color), nomeX, y, null);
		Image numero = ImageCache.get(quantita, fontMedium, color);
		graphics.drawImage(numero, totaleX - numero.getWidth(null), y, null);
	}

	/**
	 * Le frecce in alto e in basso a destra, se la lista continua da quella parte
	 */
	private void disegnaFrecce(Graphics2D graphics) {
		int larghezza = LARGHEZZA_FRECCE - 4;
		int altezza = larghezza / 2 + 1;
		int yAlto = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI + 2;
		int yBasso = topLeftY + ImageCache.corniceIncantesimi.getHeight() - DIMENSIONE_BORDO_INTERNO_CORNICE_INCANTESIMI - 2;
		graphics.setColor(Color.LIGHT_GRAY);
		if (primaRigaVisibile > 0) {
			graphics.fillPolygon(new int[] { frecceX, frecceX + larghezza, frecceX + larghezza / 2 },
					new int[] { yAlto + altezza, yAlto + altezza, yAlto }, 3);
		}
		if (primaRigaVisibile < primaRigaMassima()) {
			graphics.fillPolygon(new int[] { frecceX, frecceX + larghezza, frecceX + larghezza / 2 },
					new int[] { yBasso - altezza, yBasso - altezza, yBasso }, 3);
		}
	}

	void gestisciEventoVariazioneIncantesimi(NotificaVariazioneDisponibilitaIncantesimi evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneIncantesimi(evento.getClasseIncantesimo(),
				evento.getNuovoValore() - evento.getValorePrecedente());
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneIncantesimi(ClasseIncantesimo classeIncantesimo, int variazione) {
		return costruisciSprite(classeIncantesimo.ordinal(), variazione, "Incantesimo " + classeIncantesimo + " variato");
	}

	/**
	 * Lo sprite della variazione sulla riga indicata; null se non c'è variazione o la riga è fuori vista
	 */
	private SpriteATempo costruisciSprite(int indiceRiga, int variazione, String descrizione) {
		if (variazione == 0 || indiceRiga < primaRigaVisibile || indiceRiga >= primaRigaVisibile + righeVisibili) {
			return null;
		}
		return new SpriteATempo(righe.get(indiceRiga).icona, variazione, DoomdarkFontMedium.getInstance(),
				totaleX, yDellaRiga(indiceRiga), descrizione);
	}

	private int indicePozione(int numeroPozione) {
		return ClasseIncantesimo.values().length + numeroPozione;
	}

	void gestisciEventoVariazionePozioniSalute(NotificaVariazioneDisponibilitaPozioniSalute evento) {
		SpriteATempo sprite = costruisciSprite(indicePozione(0), evento.getNuovoValore() - evento.getValorePrecedente(),
				"Pozioni salute variate");
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	void gestisciEventoVariazionePozioniSaluteGrandi(NotificaVariazioneDisponibilitaPozioniSaluteGrandi evento) {
		SpriteATempo sprite = costruisciSprite(indicePozione(1), evento.getNuovoValore() - evento.getValorePrecedente(),
				"Pozioni salute grande variate");
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	void gestisciEventoVariazionePozioniMagia(NotificaVariazioneDisponibilitaPozioniMagia evento) {
		SpriteATempo sprite = costruisciSprite(indicePozione(2), evento.getNuovoValore() - evento.getValorePrecedente(),
				"Pozioni magia variate");
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	void gestisciEventoVariazionePozioniMagiaGrandi(NotificaVariazioneDisponibilitaPozioniMagiaGrandi evento) {
		SpriteATempo sprite = costruisciSprite(indicePozione(3), evento.getNuovoValore() - evento.getValorePrecedente(),
				"Pozioni magia grande variate");
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}
}
