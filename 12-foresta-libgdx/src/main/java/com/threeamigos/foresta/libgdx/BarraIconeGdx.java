package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.barra.BarraIcone;
import com.threeamigos.foresta.ui.barra.BarraIconeDock;
import com.threeamigos.foresta.ui.barra.DisposizioneDelCartiglio;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

/**
 * La barra delle icone di comando in libGDX, classica o come il Dock di macOS: le icone, lo scorrimento, i tocchi e l'aiuto sono del modello comune
 * ({@link BarraIcone}, {@link BarraIconeDock}: gli stessi di Swing, e il modello è la {@link com.threeamigos.foresta.ui.Finestra} che riceve gli input);
 * qui c'è il disegno: lo sfondo nero, le icone (ritagliate alla fascia se scorrono, con quella sotto il cursore evidenziata), le frecce di scorrimento, il
 * copyright e il cartiglio dell'aiuto.
 */
public final class BarraIconeGdx {

	private final BarraIcone modello;
	private final ImmaginiGdx immagini;
	private final IconeGdx icone;
	private final TestoGdx piccolo;
	private final CartiglioGdx cartiglio;
	private final String[] copyright = {"La Foresta", "copyright 1984-2026", "Stefano Reksten"};
	private final float zoom = (float) LivelloDiZoom.valore();

	/**
	 * @param dock se vera la barra si ingrandisce sotto il cursore come il Dock, in entrambi gli orientamenti
	 */
	public BarraIconeGdx(Orientamento orientamento, boolean dock, int x, int y, int larghezza, int altezza, VistaPartita vistaPartita, ImmaginiGdx immagini,
						 TestoGdx piccolo, TestoGdx medio) {
		this.immagini = immagini;
		this.icone = new IconeGdx(immagini);
		this.piccolo = piccolo;
		this.cartiglio = new CartiglioGdx(immagini, medio);
		int larghezzaIcona = Math.round(icone.larghezza() * zoom);
		int altezzaIcona = Math.round(icone.altezza() * zoom);
		this.modello = dock
				? new BarraIconeDock(orientamento, x, y, larghezza, altezza, larghezzaIcona, altezzaIcona, vistaPartita)
				: new BarraIcone(orientamento, x, y, larghezza, altezza, larghezzaIcona, altezzaIcona, vistaPartita);
	}

	/**
	 * Il modello: la finestra a cui il router manda gli input
	 */
	public BarraIcone getModello() {
		return modello;
	}

	public void disegna(Batch batch, Camera camera, Viewport viewport) {
		modello.preparaPerDisegno();
		int offsetX = modello.getOffsetX();
		int offsetY = modello.getOffsetY();
		immagini.riempi(batch, offsetX, offsetY, modello.getLarghezza(), modello.getAltezza(), Color.BLACK);
		boolean ritagliato = false;
		if (modello.isScorrevole()) {
			Area fascia = modello.fasciaDiRitaglio();
			ritagliato = RitaglioGdx.entra(batch, camera, viewport, offsetX + fascia.getX(), offsetY + fascia.getY(), fascia.getLarghezza(), fascia.getAltezza());
		}
		for (int i = 0; i < modello.iconeVisibili.size(); i++) {
			disegnaIcona(batch, i);
		}
		if (ritagliato) {
			RitaglioGdx.esci(batch);
		}
		disegnaFrecce(batch);
		disegnaCopyright(batch);
	}

	private void disegnaIcona(Batch batch, int indice) {
		BarraIcone.IconaVisibile icona = modello.iconeVisibili.get(indice);
		Area r = modello.rettangoloDisegnato(indice);
		int x = modello.getOffsetX() + r.getX();
		int y = modello.getOffsetY() + r.getY();
		batch.draw(icone.regione(icona), x, y, r.getLarghezza(), r.getAltezza());
		// Un comando in ricarica non si può dare: sotto il cursore non si evidenzia
		if (modello.isDaEvidenziare(indice)) {
			Color colore = modello.isMousePremuto() ? Color.WHITE : Color.LIGHT_GRAY;
			immagini.riempi(batch, x, y, r.getLarghezza(), 1, colore);
			immagini.riempi(batch, x, y + r.getAltezza() - 1, r.getLarghezza(), 1, colore);
			immagini.riempi(batch, x, y, 1, r.getAltezza(), colore);
			immagini.riempi(batch, x + r.getLarghezza() - 1, y, 1, r.getAltezza(), colore);
		}
	}

	private void disegnaFrecce(Batch batch) {
		boolean orizzontale = modello.getOrientamento() == Orientamento.ORIZZONTALE;
		String prima = orizzontale ? NomiDelleImmagini.FRECCIA_SINISTRA : NomiDelleImmagini.FRECCIA_SU;
		String dopo = orizzontale ? NomiDelleImmagini.FRECCIA_DESTRA : NomiDelleImmagini.FRECCIA_GIU;
		if (modello.isAltroPrima()) {
			Punto p = modello.posizioneDellaFreccia(true, new Misura(immagini.larghezza(prima, 1f), immagini.altezza(prima, 1f)));
			immagini.disegna(batch, prima, modello.getOffsetX() + p.x, modello.getOffsetY() + p.y, 1f);
		}
		if (modello.isAltroDopo()) {
			Punto p = modello.posizioneDellaFreccia(false, new Misura(immagini.larghezza(dopo, 1f), immagini.altezza(dopo, 1f)));
			immagini.disegna(batch, dopo, modello.getOffsetX() + p.x, modello.getOffsetY() + p.y, 1f);
		}
	}

	private void disegnaCopyright(Batch batch) {
		// In verticale la barra è stretta e in fondo ha la freccia: il copyright non ci sta
		if (modello.getOrientamento() != Orientamento.ORIZZONTALE) {
			return;
		}
		DoomdarkFont font = piccolo.getFont();
		for (int i = 0; i < copyright.length; i++) {
			int x = modello.getOffsetX() + modello.getLarghezza() - piccolo.larghezza(copyright[i]) - 1;
			int y = modello.getOffsetY() + modello.getAltezza() - font.getHeight() * (copyright.length - i) - 1;
			piccolo.disegna(batch, copyright[i], x, y, DoomdarkColorModel.Color.VERY_DARK_GRAY);
		}
	}

	/**
	 * Ad aiuto acceso, il cartiglio con la descrizione del comando sotto il mouse, sopra tutto il resto: va chiamato dopo aver disegnato lo schermo intero,
	 * grande larghezzaSchermo x altezzaSchermo.
	 */
	public void disegnaAiuto(Batch batch, int larghezzaSchermo, int altezzaSchermo) {
		int i = modello.getIndiceDelCartiglio();
		if (i < 0) {
			return;
		}
		Area r = modello.rettangoloDisegnato(i);
		String descrizione = modello.descrizioneAiuto(modello.iconeVisibili.get(i).comando);
		if (descrizione == null) {
			return;
		}
		DisposizioneDelCartiglio d = cartiglio.disposizione(descrizione);
		Punto posizione = BarraIcone.posizioneCartiglio(modello.getOrientamento(),
				new Area(modello.getOffsetX() + r.getX(), modello.getOffsetY() + r.getY(), r.getLarghezza(), r.getAltezza()), d.getLarghezza(), d.getAltezza(),
				larghezzaSchermo, altezzaSchermo);
		cartiglio.disegna(batch, descrizione, posizione.x, posizione.y);
	}
}
