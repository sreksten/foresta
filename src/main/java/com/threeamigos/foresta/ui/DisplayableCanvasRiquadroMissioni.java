package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.missioni.CronacheDiUnFegatoEroico;
import com.threeamigos.foresta.missioni.DisturbatoreDellaQuietePubblica;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.NessunBoccaleLasciatoIndietro;

import java.awt.*;
import java.util.ArrayList;
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

	// Dove sta il mouse, relativo al riquadro, per l'aiuto (-1 fuori)
	private int mouseX = -1;
	private int mouseY = -1;

	private final VistaPartita vistaPartita;

	DisplayableCanvasRiquadroMissioni(int topLeftX, int topLeftY, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
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
		for (Missione missione : vistaPartita.getMissioniAttive()) {
			DoomdarkColorModel.Color colore = coloreAlternante.getColor();
			ComponenteScorrevole<Missione>.Nodo nodo = componenteScorrevole.creaNodo(
					missione.getNome(), fontNome, colore,
					missione.getDescrizione(), fontDescrizione, colore,
					getIcona(missione), missione);
			configuraNodo(nodo, missione, colore, true);
		}
		List<Missione> missioniCompletate = vistaPartita.getMissioniCompletate();
		if (!missioniCompletate.isEmpty()) {
			componenteScorrevole.creaSeparatore();
			for (Missione missione : missioniCompletate) {
				DoomdarkColorModel.Color colore = DoomdarkColorModel.Color.DARK_GRAY;
				ComponenteScorrevole<Missione>.Nodo nodo = componenteScorrevole.creaNodo(
						missione.getNome(), fontNome, colore,
						missione.getDescrizione(), fontDescrizione, colore,
						getIcona(missione), missione);
				configuraNodo(nodo, missione, colore, false);
			}
		}
		// Le fallite per ultime, dopo un altro separatore, in rosso
		List<Missione> missioniFallite = vistaPartita.getMissioniFallite();
		if (!missioniFallite.isEmpty()) {
			componenteScorrevole.creaSeparatore();
			for (Missione missione : missioniFallite) {
				DoomdarkColorModel.Color colore = DoomdarkColorModel.Color.RED;
				ComponenteScorrevole<Missione>.Nodo nodo = componenteScorrevole.creaNodo(
						missione.getNome(), fontNome, colore,
						missione.getDescrizione(), fontDescrizione, colore,
						getIcona(missione), missione);
				configuraNodo(nodo, missione, colore, false);
			}
		}
		return componenteScorrevole;
	}

	/**
	 * Una sotto-missione non ancora attivata non viene mostrata. Fra le missioni in corso si nascondono anche le
	 * sotto-missioni completate, che compaiono fra le completate (vedi RegistroMissioni.getMissioniCompletate).
	 * Vale a ogni livello dell'albero.
	 */
	private void configuraNodo(ComponenteScorrevole<Missione>.Nodo nodo, Missione missione, DoomdarkColorModel.Color colore,
			boolean nascondiCompletate) {
		nodo.setFigliVisibili(missione.isDescrizioneVisibile());
		for (Missione missioneSecondaria : missione.getMissioniSecondarie()) {
			if (!missioneSecondaria.isAttiva() || (nascondiCompletate && missioneSecondaria.isCompleta())) {
				continue;
			}
			ComponenteScorrevole<Missione>.Nodo nodoFiglio = nodo.creaNodo(
					missioneSecondaria.getNome(), fontNome, colore,
					missioneSecondaria.getDescrizione(), fontDescrizione, colore,
					null, missioneSecondaria);
			configuraNodo(nodoFiglio, missioneSecondaria, colore, nascondiCompletate);
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
	public void processaMovimento(int x, int y) {
		mouseX = x;
		mouseY = y;
	}

	@Override
	public void processaUscita(int x, int y) {
		mouseX = -1;
		mouseY = -1;
	}

	/**
	 * Ad aiuto acceso, i cartigli accanto al mouse dentro l'elenco: sul nome di una missione cosa fa il click (aprire
	 * o chiudere la descrizione), e se l'elenco è più alto del riquadro la rotella. Va chiamato dopo aver disegnato
	 * tutto lo schermo, grande larghezzaSchermo x altezzaSchermo.
	 */
	void disegnaAiuto(Graphics2D graphics, int larghezzaSchermo, int altezzaSchermo) {
		if (!vistaPartita.isAiutoAbilitato() || mouseX < 0) {
			return;
		}
		int bordo = DIMENSIONE_BORDO_INTERNO_CORNICE_GRANDE + SPACING;
		int xInterno = mouseX - bordo;
		int yInterno = mouseY - bordo;
		if (xInterno < 0 || xInterno >= innerWidth || yInterno < 0 || yInterno >= innerHeight) {
			return;
		}
		ComponenteScorrevole<Missione> componenteScorrevole = costruisciComponenteScorrevole();
		List<String> righe = new ArrayList<>();
		Missione missione = componenteScorrevole.riferimentoTitoloAllaQuota(yInterno + offsetY);
		if (missione != null) {
			righe.add(Cartiglio.aiutoClick(missione.isDescrizioneVisibile(), "descrizione"));
		}
		if (componenteScorrevole.isScorrevole(innerHeight)) {
			righe.add(Cartiglio.AIUTO_ROTELLA);
		}
		Cartiglio.disegnaAccantoAlMouse(graphics, righe, topLeftX + mouseX, topLeftY + mouseY, larghezzaSchermo, altezzaSchermo);
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
		if (DisturbatoreDellaQuietePubblica.class.isAssignableFrom(missione.getClass())) {
			return ImageCache.missioneGallo;
		}
		return null;
	}
}
