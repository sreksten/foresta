package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoCommutazioneElenco;
import com.threeamigos.foresta.interfacce.VistaMissione;
import com.threeamigos.foresta.interfacce.VistaPartita;

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

		ComponenteScorrevole<VistaMissione> componenteScorrevole = costruisciComponenteScorrevole();
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
	private ComponenteScorrevole<VistaMissione> costruisciComponenteScorrevole() {
		ComponenteScorrevole<VistaMissione> componenteScorrevole = new ComponenteScorrevole<>(innerWidth, 10, 2);
		DoomdarkColorAlternante coloreAlternante = new DoomdarkColorAlternante();
		for (VistaMissione missione : vistaPartita.getMissioniAttive()) {
			DoomdarkColorModel.Color colore = coloreAlternante.getColor();
			ComponenteScorrevole<VistaMissione>.Nodo nodo = componenteScorrevole.creaNodo(
					missione.getNome(), fontNome, colore,
					missione.getDescrizione(), fontDescrizione, colore,
					getIcona(missione), missione);
			configuraNodo(nodo, missione, colore, true);
		}
		List<? extends VistaMissione> missioniCompletate = vistaPartita.getMissioniCompletate();
		if (!missioniCompletate.isEmpty()) {
			componenteScorrevole.creaSeparatore();
			for (VistaMissione missione : missioniCompletate) {
				DoomdarkColorModel.Color colore = DoomdarkColorModel.Color.DARK_GRAY;
				ComponenteScorrevole<VistaMissione>.Nodo nodo = componenteScorrevole.creaNodo(
						missione.getNome(), fontNome, colore,
						missione.getDescrizione(), fontDescrizione, colore,
						getIcona(missione), missione);
				configuraNodo(nodo, missione, colore, false);
			}
		}
		// Le fallite per ultime, dopo un altro separatore, in rosso
		List<? extends VistaMissione> missioniFallite = vistaPartita.getMissioniFallite();
		if (!missioniFallite.isEmpty()) {
			componenteScorrevole.creaSeparatore();
			for (VistaMissione missione : missioniFallite) {
				DoomdarkColorModel.Color colore = DoomdarkColorModel.Color.RED;
				ComponenteScorrevole<VistaMissione>.Nodo nodo = componenteScorrevole.creaNodo(
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
	private void configuraNodo(ComponenteScorrevole<VistaMissione>.Nodo nodo, VistaMissione missione, DoomdarkColorModel.Color colore,
			boolean nascondiCompletate) {
		nodo.setFigliVisibili(missione.isDescrizioneVisibile());
		for (VistaMissione missioneSecondaria : missione.getMissioniSecondarie()) {
			if (!missioneSecondaria.isAttiva() || (nascondiCompletate && missioneSecondaria.isCompleta())) {
				continue;
			}
			ComponenteScorrevole<VistaMissione>.Nodo nodoFiglio = nodo.creaNodo(
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
		VistaMissione missione = costruisciComponenteScorrevole().riferimentoTitoloAllaQuota(yInterno + offsetY);
		if (missione == null) {
			return;
		}
		BusEventi.pubblica(ComandoCommutazioneElenco.missione(missione.getId()));
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
		ComponenteScorrevole<VistaMissione> componenteScorrevole = costruisciComponenteScorrevole();
		List<String> righe = new ArrayList<>();
		VistaMissione missione = componenteScorrevole.riferimentoTitoloAllaQuota(yInterno + offsetY);
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

	private Image getIcona(VistaMissione missione) {
		switch (missione.getClasse()) {
			case NESSUN_BOCCALE_LASCIATO_INDIETRO:
			case CRONACHE_DI_UN_FEGATO_EROICO:
				return ImageCache.missioneBirra;
			case DISTURBATORE_DELLA_QUIETE_PUBBLICA:
				return ImageCache.missioneGallo;
			default:
				return null;
		}
	}
}
