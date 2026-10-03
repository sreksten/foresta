package com.threeamigos.foresta.ui;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * La pagina dei trofei, che si apre dall'inventario (comando MOSTRA_TROFEI) e si chiude con ANNULLA: il titolo in
 * cima e sotto tutti i trofei (vedi ImmagineTrofei), che si scorrono a mano come il riquadro delle missioni, con la
 * rotella o con le frecce, a passi più lunghi perché l'elenco è lungo.
 */
class DisplayableCanvasTrofei implements Finestra {

	/**
	 * Di quanti pixel scorre l'elenco a ogni scatto della rotella: il triplo del riquadro delle missioni.
	 */
	static final int PASSO_SCORRIMENTO = 6;
	/**
	 * Quanti scatti vale un clic su una freccia.
	 */
	private static final int SCATTI_PER_CLIC = 10;
	private static final int MARGINE = 50;
	private static final int QUOTA_TITOLO = 20;
	private static final int SPAZIO_SOTTO_IL_TITOLO = 24;

	private final int width;
	private final int height;
	private ScorrimentoVerticale scorrimento;

	DisplayableCanvasTrofei(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Prepara la pagina con i trofei come sono adesso, dalla cima dell'elenco.
	 */
	void apri() {
		int cimaElenco = QUOTA_TITOLO + TestoGrande.ALTEZZA_RIGA + SPAZIO_SOTTO_IL_TITOLO;
		scorrimento = new ScorrimentoVerticale(ImmagineTrofei.costruisci(width - 2 * MARGINE, false),
				new Rectangle(0, cimaElenco, width, height - cimaElenco));
	}

	void disegna(Graphics2D graphics) {
		graphics.drawImage(ImageCache.ombraDelDrago, (width - ImageCache.ombraDelDrago.getWidth()) >> 1,
				(height - ImageCache.ombraDelDrago.getHeight()) >> 1, null);
		String titolo = TestoGrande.normalizza("trofei");
		TestoGrande.disegnaRiga(graphics, titolo, (width - TestoGrande.larghezza(titolo)) >> 1, QUOTA_TITOLO);
		if (scorrimento != null) {
			scorrimento.disegna(graphics, 1);
			scorrimento.disegnaFrecce(graphics);
		}
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		if (scorrimento != null) {
			scorrimento.scorri((movimentoRotella == MovimentoRotella.SU ? -1 : 1) * numeroRotazioni * PASSO_SCORRIMENTO);
		}
	}

	@Override
	public void processaClick(int x, int y, Tasto tasto) {
		if (scorrimento == null) {
			return;
		}
		if (scorrimento.isSullaFrecciaSu(x, y)) {
			scorrimento.scorri(-SCATTI_PER_CLIC * PASSO_SCORRIMENTO);
		} else if (scorrimento.isSullaFrecciaGiu(x, y)) {
			scorrimento.scorri(SCATTI_PER_CLIC * PASSO_SCORRIMENTO);
		}
	}
}
