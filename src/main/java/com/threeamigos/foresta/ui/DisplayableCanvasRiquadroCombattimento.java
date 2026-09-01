package com.threeamigos.foresta.ui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Optional;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.personaggi.Personaggio;

class DisplayableCanvasRiquadroCombattimento {

	private boolean visible;
	private int xOffset;
	private int yOffset;
	private BufferedImage cornice;
	private Personaggio combattente;
	private Personaggio avversario;
	private String nomeCombattente;
	private String nomeAvversario;
	private DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();

	DisplayableCanvasRiquadroCombattimento(int parentWidth, int parentHeight) {
		cornice = ImageCache.cornicePiccola;
		visible = false;
		xOffset = (parentWidth - cornice.getWidth()) >> 1;
		yOffset = (parentHeight - cornice.getHeight()) >> 1;
	}

	void setVisible(boolean visible) {
		this.visible = visible;
	}

	void setCombattente(Personaggio combattente) {
		this.combattente = combattente;
		nomeCombattente = combattente.getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		int l = cornice.getWidth() - 30;
		while (FontTool.getWidth(fontMedium, nomeCombattente) > l)
			nomeCombattente = nomeCombattente.substring(0, nomeCombattente.length() - 2);
	}

	void setAvversario(Personaggio avversario) {
		this.avversario = avversario;
		if (avversario != null) {
			Optional<String> nomeOpt = avversario.getNomeProprio();
			if (nomeOpt.isPresent()) {
				nomeAvversario = nomeOpt.get();
			} else {
				nomeAvversario = avversario.getNomeSingolare();
				if (GruppoAvversario.getIstanza().getNumeroPersonaggi() > 1) {
					int l = avversario.getOrdinale();
					if (l > 0) {
						nomeAvversario += " " + l;
					}
				}
			}
			int l = cornice.getWidth() - 30;
			while (FontTool.getWidth(fontMedium, nomeAvversario) > l) {
				nomeAvversario = nomeAvversario.substring(0, nomeAvversario.length() - 2);
			}
		}
	}

	void disegnaInfoCombattimento(Graphics2D graphics) {
		if (!visible) {
			return;
		}

		graphics.drawImage(cornice, xOffset, yOffset, null);

		graphics.drawImage(DoomdarkTextProducer.getImage(nomeCombattente, fontMedium), xOffset + 8, yOffset + 8, null);
		Image salute = DoomdarkTextProducer.getImage(combattente.getSalute(), fontMedium);
		graphics.drawImage(salute, xOffset + cornice.getWidth() - salute.getWidth(null) - 8, yOffset + 8, null);

		if (avversario != null) {
			graphics.drawImage(DoomdarkTextProducer.getImage(nomeAvversario, fontMedium), xOffset + 8, yOffset + 12 + fontMedium.getHeight(), null);
			salute = DoomdarkTextProducer.getImage(avversario.getSalute(), fontMedium);
			graphics.drawImage(salute, xOffset + cornice.getWidth() - salute.getWidth(null) - 8, yOffset + 12 + fontMedium.getHeight(), null);
		}
	}
}
