package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

class DisplayableCanvasRiquadroCombattimento implements Finestra {

	private boolean visible;
	private final int xOffset;
	private final int yOffset;
	private final BufferedImage cornice;
	private final Rectangle rettangolo;
	private Personaggio combattente;
	private Personaggio avversario;
	private String nomeCombattente;
	private String nomeAvversario;
	private final DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();

	DisplayableCanvasRiquadroCombattimento(int parentWidth, int parentHeight) {
		cornice = ImageCache.cornicePiccola;
		visible = false;
		xOffset = (parentWidth - cornice.getWidth()) >> 1;
		yOffset = (parentHeight - cornice.getHeight()) >> 1;

		rettangolo = new Rectangle(xOffset, yOffset, cornice.getWidth(), cornice.getHeight());
	}

	public Rectangle getRettangolo() {
		return rettangolo;
	}

	@Override
	public boolean isVisibile() {
		return visible;
	}

	public int getXOffset() {
		return xOffset;
	}

	public int getYOffset() {
		return yOffset;
	}

	public

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
