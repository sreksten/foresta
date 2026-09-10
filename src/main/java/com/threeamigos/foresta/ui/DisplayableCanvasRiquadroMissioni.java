package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.missioni.CronacheDiUnFegatoEroico;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.NessunBoccaleLasciatoIndietro;
import com.threeamigos.foresta.motore.RegistroMissioni;

import java.awt.*;
import java.util.List;

class DisplayableCanvasRiquadroMissioni implements Finestra {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE = 16;
	private static final int SPACING = 4;
	// Pixel di scorrimento per ogni scatto della rotella
	private static final int PASSO_SCORRIMENTO = 2;

	private static final DoomdarkFont fontNome = DoomdarkFontMedium.getInstance();
	private static final DoomdarkFont fontDescrizione = DoomdarkFontSmall.getInstance();

	private final int topLeftX;
	private final int topLeftY;
	private final int innerWidth;
	private final int innerHeight;

	int offsetY = 0;

	DisplayableCanvasRiquadroMissioni(int topLeftX, int topLeftY) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		innerWidth = ImageCache.corniceGrande.getWidth() - ((DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE  + SPACING) << 1);
		innerHeight = ImageCache.corniceGrande.getHeight() - ((DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + SPACING) << 1);
	}

	void disegnaMissioni(Graphics2D graphics) {
		graphics.drawImage(ImageCache.corniceGrande, topLeftX, topLeftY, null);

		ComponenteScorrevole<Missione> componenteScorrevole = costruisciComponenteScorrevole();
		// L'elenco può essere cambiato dall'ultimo scorrimento: l'offset va rimesso nei limiti
		offsetY = componenteScorrevole.limitaOffset(innerHeight, offsetY);
		Image image = componenteScorrevole.produci(innerHeight, offsetY);
		graphics.drawImage(image, topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE  + SPACING,
				topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + SPACING, null);
	}

	/**
	 * L'albero viene ricostruito a ogni disegno e a ogni click: lo stato di apertura
	 * dei nodi non vive qui ma nel modello dati delle missioni.
	 */
	private ComponenteScorrevole<Missione> costruisciComponenteScorrevole() {
		ComponenteScorrevole<Missione> componenteScorrevole = new ComponenteScorrevole<>(innerWidth, 10, 2);
		DoomdarkColorAlternante coloreAlternante = new DoomdarkColorAlternante();
		for (Missione missione : RegistroMissioni.getMissioniAttive()) {
			DoomdarkColorModel.Color colore = coloreAlternante.getColor();
			ComponenteScorrevole<Missione>.Nodo nodo = componenteScorrevole.creaNodo(
					missione.getNome(), fontNome, colore,
					missione.getDescrizione(), fontDescrizione, colore,
					getIcona(missione), missione);
			configuraNodo(nodo, missione, colore);
		}
		List<Missione> missioniCompletate = RegistroMissioni.getMissioniCompletate();
		if (!missioniCompletate.isEmpty()) {
			componenteScorrevole.creaSeparatore();
			for (Missione missione : missioniCompletate) {
				DoomdarkColorModel.Color colore = DoomdarkColorModel.Color.DARK_GRAY;
				ComponenteScorrevole<Missione>.Nodo nodo = componenteScorrevole.creaNodo(
						missione.getNome(), fontNome, colore,
						missione.getDescrizione(), fontDescrizione, colore,
						getIcona(missione), missione);
				configuraNodo(nodo, missione, colore);
			}
		}
		return componenteScorrevole;
	}

	/**
	 * Una missione completata resta in elenco ma spenta; una non ancora attivata non
	 * viene mostrata. Vale a ogni livello dell'albero.
	 */
	private void configuraNodo(ComponenteScorrevole<Missione>.Nodo nodo, Missione missione, DoomdarkColorModel.Color colore) {
		nodo.setFigliVisibili(missione.isDescrizioneVisibile());
		for (Missione missioneSecondaria : missione.getMissioniSecondarie()) {
			if (!missioneSecondaria.isAttiva()) {
				continue;
			}
			ComponenteScorrevole<Missione>.Nodo nodoFiglio = nodo.creaNodo(
					missioneSecondaria.getNome(), fontNome, colore,
					missioneSecondaria.getDescrizione(), fontDescrizione, colore,
					null, missioneSecondaria);
			configuraNodo(nodoFiglio, missioneSecondaria, colore);
		}
	}

	@Override
	public void processaClick(int x, int y, Tasto tasto) {
		if (tasto != Tasto.SINISTRO) {
			return;
		}
		int bordo = DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + SPACING;
		int xInterno = x - bordo;
		int yInterno = y - bordo;
		// Il click può cadere sulla cornice, fuori dall'elenco
		if (xInterno < 0 || xInterno >= innerWidth || yInterno < 0 || yInterno >= innerHeight) {
			return;
		}
		// La quota va espressa in coordinate della lista, non della finestra visibile
		Missione missione = costruisciComponenteScorrevole().riferimentoTitoloAllaQuota(yInterno + offsetY);
		if (missione == null) {
			return;
		}
		if (missione.isDescrizioneVisibile()) {
			missione.nascondiDescrizione();
		} else {
			missione.mostraDescrizione();
		}
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		if (movimentoRotella == MovimentoRotella.SU) {
			offsetY = Math.max(0, offsetY - numeroRotazioni * PASSO_SCORRIMENTO);
		} else if (movimentoRotella == MovimentoRotella.GIU) {
			offsetY += numeroRotazioni * PASSO_SCORRIMENTO;
		}
	}

	private Image getIcona(Missione missione) {
		if (NessunBoccaleLasciatoIndietro.class.isAssignableFrom(missione.getClass()) ||
				CronacheDiUnFegatoEroico.class.isAssignableFrom(missione.getClass())) {
			return ImageCache.missioneBirra;
		}
		return null;
	}
}
