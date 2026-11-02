package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.sprite.SpriteATempo;
import com.threeamigos.foresta.ui.testo.DoomdarkColorAlternante;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

/**
 * Gli incantesimi e le pozioni del gruppo: una lista scrollabile con la rotella del mouse, una voce per riga, con il
 * nome per intero (senza abbreviazioni) e la quantità a destra. Gli incantesimi sono nell'ordine di ClasseIncantesimo, poi le pozioni.
 */
public class DisplayableCanvasRiquadroIncantesimiEPozioni implements Finestra {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE = 16;
	// Pixel di scorrimento per ogni scatto della rotella, come per le missioni
	private static final int PASSO_SCORRIMENTO = 2;

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
	private final int altezzaVisibile;
	private final int altezzaRiga = DoomdarkFont.medio().getHeight();
	private final int topLeftX;
	private int topLeftY;
	private final int iconaX;
	private final int nomeX;
	private final int totaleX;
	private final Rectangle areaLista;

	// Quanto la lista è scorsa: con la rotella e trascinandola (anche col dito, e prosegue per inerzia)
	private final ScorrimentoSuUnAsse scorrimento = new ScorrimentoSuUnAsse(System::nanoTime);

	private final VistaPartita vistaPartita;

	public DisplayableCanvasRiquadroIncantesimiEPozioni(int topLeftX, int topLeftY, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;

		int maxIconWidth = 0;
		for (TipoIncantesimo tipoIncantesimo : TipoIncantesimo.values()) {
			BufferedImage icona = ImageCache.spriteIncantesimi[tipoIncantesimo.ordinal()];
			maxIconWidth = Math.max(maxIconWidth, icona.getWidth());
			righe.add(new Riga(icona, tipoIncantesimo.getNome(), g -> g.getIncantesimi(tipoIncantesimo)));
		}
		righe.add(new Riga(ImageCache.spritePozioneSalute, TestiDellaUI.get("POZIONE_SALUTE"), VistaGruppoGiocatore::getPozioniSalute));
		righe.add(new Riga(ImageCache.spritePozioneSaluteGrande, TestiDellaUI.get("POZIONE_SALUTE_GRANDE"), VistaGruppoGiocatore::getPozioniSaluteGrande));
		righe.add(new Riga(ImageCache.spritePozioneMagia, TestiDellaUI.get("POZIONE_MAGIA"), VistaGruppoGiocatore::getPozioniMagia));
		righe.add(new Riga(ImageCache.spritePozioneMagiaGrande, TestiDellaUI.get("POZIONE_MAGIA_GRANDE"), VistaGruppoGiocatore::getPozioniMagiaGrande));
		righe.add(new Riga(ImageCache.spritePozionePanacea, TestiDellaUI.get("POZIONE_PANACEA"), VistaGruppoGiocatore::getPozioniPanacea));
		righe.add(new Riga(ImageCache.spritePozioneAumentoLivello, TestiDellaUI.get("POZIONE_AUMENTO_LIVELLO"), VistaGruppoGiocatore::getPozioniAumentoLivello));
		for (int i = TipoIncantesimo.values().length; i < righe.size(); i++) {
			maxIconWidth = Math.max(maxIconWidth, righe.get(i).icona.getWidth());
		}

		altezzaVisibile = ImageCache.corniceGrande.getHeight() - 2 * DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE;
		areaLista = new Rectangle(topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE,
				topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE,
				ImageCache.corniceGrande.getWidth() - 2 * DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE, altezzaVisibile);
		iconaX = areaLista.x;
		nomeX = iconaX + maxIconWidth + 2;
		// Le quantità stanno a sinistra delle frecce, che si disegnano sopra la lista
		totaleX = areaLista.x + areaLista.width - ImageCache.componenteScorrevoleFrecciaSu.getWidth(null) - 2 * ImageCache.SPACING;
	}

	/**
	 * Sposta il riquadro in verticale: topLeftY è la nuova quota del bordo superiore della cornice
	 */
	public void spostaInVerticale(int topLeftY) {
		this.topLeftY = topLeftY;
		areaLista.y = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE;
	}

	private int offsetMassimo() {
		return Math.max(0, righe.size() * altezzaRiga - altezzaVisibile);
	}

	/**
	 * Di quanti pixel la lista è scorsa
	 */
	private int offsetY() {
		return (int) Math.round(scorrimento.getScorrimento());
	}

	/**
	 * Il gesto che comincia sul riquadro prosegue anche se il puntatore ne esce, finché non si lascia
	 */
	@Override
	public boolean catturaIlTrascinamento() {
		return true;
	}

	@Override
	public void processaPressione(int x, int y, Tasto tasto) {
		if (tasto == Tasto.SINISTRO) {
			scorrimento.premi(y, true);
		}
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		scorrimento.trascina(y);
	}

	@Override
	public void processaRilascio(int x, int y, Tasto tasto) {
		if (tasto == Tasto.SINISTRO) {
			scorrimento.rilascia();
		}
	}

	@Override
	public int passiPerScattoDellaRotella() {
		return PASSI_PER_SCATTO_DELLE_LISTE;
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		int pixel = numeroRotazioni * PASSO_SCORRIMENTO;
		scorrimento.scorriDi(movimentoRotella == MovimentoRotella.SU ? -pixel : pixel);
	}

	/**
	 * Disegna gli incantesimi disponibili e le pozioni.
	 */
	public void disegnaIncantesimi(Graphics2D graphics) {
		graphics.drawImage(ImageCache.corniceGrande, topLeftX, topLeftY, null);
		IntestazioneRiquadro.disegna(graphics, TestiDellaUI.get("RIQUADRO_INCANTESIMI_E_POZIONI"), topLeftX, topLeftY, ImageCache.corniceGrande.getWidth());

		// L'elenco può essere cambiato dall'ultimo scorrimento: il limite va aggiornato e lo scorrimento rimesso dentro
		scorrimento.imposta(offsetMassimo() > 0, offsetMassimo());
		scorrimento.aggiorna();

		Shape clipOriginale = graphics.getClip();
		graphics.setClip(areaLista);

		VistaGruppoGiocatore g = vistaPartita.getGruppoGiocatore();
		DoomdarkFont fontMedium = DoomdarkFont.medio();
		DoomdarkColorAlternante color = new DoomdarkColorAlternante();
		// Si alternano i colori su tutte le righe, anche quelle fuori vista, perché scorrendo non cambino
		for (int i = 0; i < righe.size(); i++) {
			DoomdarkColorModel.Color colore = color.getColor();
			int y = yDellaRiga(i);
			if (y + altezzaRiga > areaLista.y && y < areaLista.y + altezzaVisibile) {
				Riga riga = righe.get(i);
				disegnaRiga(graphics, fontMedium, y, riga, riga.quantita.applyAsInt(g), colore);
			}
		}
		disegnaFrecce(graphics);

		graphics.setClip(clipOriginale);
	}

	private int yDellaRiga(int indice) {
		return areaLista.y + indice * altezzaRiga - offsetY();
	}

	private void disegnaRiga(Graphics2D graphics, DoomdarkFont fontMedium, int y, Riga riga, int quantita, DoomdarkColorModel.Color color) {
		BufferedImage icona = riga.icona;
		// L'icona sta centrata sul corpo del testo; il testo sale delle righe in più per gli accenti
		graphics.drawImage(icona, iconaX, y - (icona.getHeight() - fontMedium.getAltezzaDelCorpo()) / 2, null);
		int yTesto = y - fontMedium.getRigheInCima();
		graphics.drawImage(ImageCache.get(riga.descrizione, fontMedium, color), nomeX, yTesto, null);
		Image numero = ImageCache.get(quantita, fontMedium, color);
		graphics.drawImage(numero, totaleX - numero.getWidth(null), yTesto, null);
	}

	/**
	 * Le frecce delle missioni (vedi ComponenteScorrevole), in alto e in basso a destra se la lista continua da
	 * quella parte
	 */
	private void disegnaFrecce(Graphics2D graphics) {
		Image frecciaSu = ImageCache.componenteScorrevoleFrecciaSu;
		Image frecciaGiu = ImageCache.componenteScorrevoleFrecciaGiu;
		if (offsetY() > 0) {
			graphics.drawImage(frecciaSu, areaLista.x + areaLista.width - frecciaSu.getWidth(null) - ImageCache.SPACING,
					areaLista.y + ImageCache.SPACING, null);
		}
		if (offsetY() < offsetMassimo()) {
			graphics.drawImage(frecciaGiu, areaLista.x + areaLista.width - frecciaGiu.getWidth(null) - ImageCache.SPACING,
					areaLista.y + altezzaVisibile - ImageCache.SPACING - frecciaGiu.getHeight(null), null);
		}
	}

	/**
	 * Aggiorna gli incantesimi disponibili.
	 */
	public void gestisciEventoVariazioneIncantesimi(NotificaVariazioneDisponibilitaIncantesimi evento) {
		SpriteATempo sprite = costruisciSpritePerVariazioneIncantesimi(evento.getClasseIncantesimo(),
				evento.getNuovoValore() - evento.getValorePrecedente());
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	private SpriteATempo costruisciSpritePerVariazioneIncantesimi(TipoIncantesimo tipoIncantesimo, int variazione) {
		return costruisciSprite(tipoIncantesimo.ordinal(), variazione, "Incantesimo " + tipoIncantesimo + " variato");
	}

	/**
	 * Lo sprite della variazione parte dalla riga indicata, anche se non si vede tutta (o per niente) perché la lista
	 * è scrollata: si vede solo la parte dentro la lista
	 */
	private SpriteATempo costruisciSprite(int indiceRiga, int variazione, String descrizione) {
		if (variazione == 0) {
			return null;
		}
		SpriteATempo sprite = new SpriteATempo(righe.get(indiceRiga).icona, variazione, DoomdarkFont.medio(),
				totaleX, yDellaRiga(indiceRiga), descrizione);
		sprite.setRitaglio(areaLista);
		return sprite;
	}

	private int indicePozione(int numeroPozione) {
		return TipoIncantesimo.values().length + numeroPozione;
	}

	/**
	 * Aggiorna il numero di pozioni di salute.
	 */
	public void gestisciEventoVariazionePozioniSalute(NotificaVariazioneDisponibilitaPozioniSalute evento) {
		SpriteATempo sprite = costruisciSprite(indicePozione(0), evento.getNuovoValore() - evento.getValorePrecedente(),
				"Pozioni salute variate");
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	/**
	 * Aggiorna il numero di pozioni di salute grandi.
	 */
	public void gestisciEventoVariazionePozioniSaluteGrandi(NotificaVariazioneDisponibilitaPozioniSaluteGrandi evento) {
		SpriteATempo sprite = costruisciSprite(indicePozione(1), evento.getNuovoValore() - evento.getValorePrecedente(),
				"Pozioni salute grande variate");
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	/**
	 * Aggiorna il numero di pozioni di magia.
	 */
	public void gestisciEventoVariazionePozioniMagia(NotificaVariazioneDisponibilitaPozioniMagia evento) {
		SpriteATempo sprite = costruisciSprite(indicePozione(2), evento.getNuovoValore() - evento.getValorePrecedente(),
				"Pozioni magia variate");
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	/**
	 * Aggiorna il numero di pozioni di magia grandi.
	 */
	public void gestisciEventoVariazionePozioniMagiaGrandi(NotificaVariazioneDisponibilitaPozioniMagiaGrandi evento) {
		SpriteATempo sprite = costruisciSprite(indicePozione(3), evento.getNuovoValore() - evento.getValorePrecedente(),
				"Pozioni magia grande variate");
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	/**
	 * Aggiorna il numero di panacee.
	 */
	public void gestisciEventoVariazionePozioniPanacea(NotificaVariazioneDisponibilitaPozioniPanacea evento) {
		SpriteATempo sprite = costruisciSprite(indicePozione(4), evento.getNuovoValore() - evento.getValorePrecedente(),
				"Panacee variate");
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	/**
	 * Aggiorna il numero di pozioni dell'aumento di livello.
	 */
	public void gestisciEventoVariazionePozioniAumentoLivello(NotificaVariazioneDisponibilitaPozioniAumentoLivello evento) {
		SpriteATempo sprite = costruisciSprite(indicePozione(5), evento.getNuovoValore() - evento.getValorePrecedente(),
				"Pozioni aumento livello variate");
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}
}
