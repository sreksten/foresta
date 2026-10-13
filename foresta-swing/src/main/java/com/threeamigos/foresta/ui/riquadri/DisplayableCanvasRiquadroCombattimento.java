package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;

import java.awt.*;
import java.awt.image.BufferedImage;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFontMedium;
import com.threeamigos.foresta.ui.testo.FontTool;

/**
 * Il riquadro che mostra gli avversari della locazione e le informazioni sul combattimento in corso.
 */
public class DisplayableCanvasRiquadroCombattimento implements Finestra {

	private boolean visible;
	private final int xOffset;
	private final int yOffset;
	private final BufferedImage cornice;
	private final Rectangle rettangolo;
	private VistaPersonaggio combattente;
	private VistaPersonaggio avversario;
	private String nomeCombattente;
	private String nomeAvversario;
	private final DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();

	private final VistaPartita vistaPartita;

	public DisplayableCanvasRiquadroCombattimento(int parentWidth, int parentHeight, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		cornice = ImageCache.corniceCombattimento;
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

	public VistaPersonaggio getCombattente() {
		return combattente;
	}

	public VistaPersonaggio getAvversario() {
		return avversario;
	}

	/** Larghezza a disposizione del nome: la cornice meno lo spazio per "salute/massima". */
	private int larghezzaMassimaNome() {
		return cornice.getWidth() - 30 - FontTool.getWidth(fontMedium, "000/000");
	}

	public void setCombattente(VistaPersonaggio combattente) {
		this.combattente = combattente;
		nomeCombattente = combattente.getNome(VistaPersonaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		int l = larghezzaMassimaNome();
		while (FontTool.getWidth(fontMedium, nomeCombattente) > l)
			nomeCombattente = nomeCombattente.substring(0, nomeCombattente.length() - 2);
	}

	public void setAvversario(VistaPersonaggio avversario) {
		this.avversario = avversario;
		if (avversario != null) {
			// Il nome porta già il numero ("Viverna 2") quando ce n'è più d'uno della stessa specie
			nomeAvversario = avversario.getNome(VistaPersonaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
			int l = larghezzaMassimaNome();
			while (FontTool.getWidth(fontMedium, nomeAvversario) > l) {
				nomeAvversario = nomeAvversario.substring(0, nomeAvversario.length() - 2);
			}
		}
	}

	/**
	 * Disegna le informazioni sul combattimento: chi si affronta e come va.
	 */
	public void disegnaInfoCombattimento(Graphics2D graphics) {
		if (!visible) {
			return;
		}

		graphics.drawImage(cornice, xOffset, yOffset, null);

		final int larghezzaMassimaBarra = cornice.getWidth() - 16;
		final int altezzaBarra = 26;
		final int offsetDaBarra = (altezzaBarra - fontMedium.getHeight()) / 2;

		final int OFFSET_DA_CORNICE = 8;
		final int SPAZIATURA = 2;

		int x = xOffset + OFFSET_DA_CORNICE;
		int y = yOffset + OFFSET_DA_CORNICE;

		double percentualeSalute = (double)combattente.getSalute() / combattente.getSaluteMassima();
		disegnaBarraSalute(graphics, x, y, percentualeSalute, larghezzaMassimaBarra, altezzaBarra);
		disegnaNomeConOmbra(graphics, nomeCombattente, x + SPAZIATURA, y + offsetDaBarra);
		x = xOffset + cornice.getWidth() - OFFSET_DA_CORNICE - SPAZIATURA;
		disegnaSaluteConOmbra(graphics, combattente.getSalute(), combattente.getSaluteMassima(), x, y + offsetDaBarra);

		if (avversario != null) {
			x = xOffset + OFFSET_DA_CORNICE;
			y = yOffset + OFFSET_DA_CORNICE + altezzaBarra + SPAZIATURA;

			percentualeSalute = (double)avversario.getSalute() / avversario.getSaluteMassima();
			disegnaBarraSalute(graphics, x, y, percentualeSalute, larghezzaMassimaBarra, altezzaBarra);
			disegnaNomeConOmbra(graphics, nomeAvversario, x + SPAZIATURA, y + offsetDaBarra);
			x = xOffset + cornice.getWidth() - OFFSET_DA_CORNICE;
			disegnaSaluteConOmbra(graphics, avversario.getSalute(), avversario.getSaluteMassima(), x, y + offsetDaBarra);
		}
	}

	private void disegnaBarraSalute(Graphics2D graphics, int x, int y, double percentualeSalute, int larghezzaMassimaBarra, int altezzaBarra) {
		int larghezzaCorrenteBarra = (int) (larghezzaMassimaBarra * percentualeSalute);
		int differenza = larghezzaMassimaBarra - larghezzaCorrenteBarra;
		graphics.setColor(new Color(40, 40, 40));
		graphics.fillRect(x + larghezzaCorrenteBarra, y, differenza, altezzaBarra);
		graphics.setColor(determinaColoreSalute(percentualeSalute));
		graphics.fillRect(x, y, larghezzaCorrenteBarra, altezzaBarra);
	}

	private void disegnaNomeConOmbra(Graphics2D graphics, String testo, int x, int y) {
		final int OFFSET_OMBRA = 2;
		Image testoTrasformatoNero = ImageCache.get(testo, fontMedium, DoomdarkColorModel.Color.BLACK);
		graphics.drawImage(testoTrasformatoNero, x + OFFSET_OMBRA, y + OFFSET_OMBRA, null);
		Image testoTrasformatoBianco = ImageCache.get(testo, fontMedium, DoomdarkColorModel.Color.WHITE);
		graphics.drawImage(testoTrasformatoBianco, x, y, null);
	}

	private void disegnaSaluteConOmbra(Graphics2D graphics, int salute, int saluteMassima, int xIniziale, int y) {
		final int OFFSET_OMBRA = 2;
		String testo = salute + "/" + saluteMassima;
		Image testoTrasformatoNero = ImageCache.get(testo, fontMedium, DoomdarkColorModel.Color.BLACK);
		int x = xIniziale - testoTrasformatoNero.getWidth(null);
		graphics.drawImage(testoTrasformatoNero, x + OFFSET_OMBRA, y + OFFSET_OMBRA, null);
		Image testoTrasformatoBianco = ImageCache.get(testo, fontMedium, DoomdarkColorModel.Color.WHITE);
		graphics.drawImage(testoTrasformatoBianco, x, y, null);
	}

	/**
	 * Calcola dinamicamente il colore della barra della salute in base ai punti vita rimasti.
	 * Sfuma fluidamente: Verde (100%) &rarr; Giallo (50%) &rarr; Arancione (25%) &rarr; Rosso (0%).
	 *
	 * @param percentuale la percentuale di salute del Personaggio
	 * @return L'oggetto Color corretto per il rendering con Graphics2D
	 */
	private static Color determinaColoreSalute(double percentuale) {
		percentuale = Math.max(0.0d, Math.min(1.0d, percentuale));

		int r = 0;
		int g = 0;
		final int b = 0; // Il blu rimane sempre a zero in questo gradiente

		// 2. BIVIO A TRE STADI PER LA SFUMATURA LINEARE (LERP)
		if (percentuale >= 0.5d) {
			// --- STADIO 1: DA VERDE (1.0) A GIALLO (0.5) ---
			// Il verde è fisso al massimo (255), il rosso sale man mano che la salute scende
			double fattoreSettore = (1.0d - percentuale) / 0.5d; // Mappa la fascia 0.5-1.0 in un raggio 0.0-1.0
			r = (int) (fattoreSettore * 255);
			g = 255;

		} else if (percentuale >= 0.25d) {
			// --- STADIO 2: DA GIALLO (0.5) A ARANCIONE (0.25) ---
			// Il rosso è fisso al massimo (255), il verde scende lentamente da 255 a 128
			double fattoreSettore = (0.5d - percentuale) / 0.25d; // Mappa la fascia 0.25-0.50 in un raggio 0.0-1.0
			r = 255;
			g = 255 - (int) (fattoreSettore * 127); // 255 - 127 = 128 (Arancione)

		} else {
			// --- STADIO 3: DA ARANCIONE (0.25) A ROSSO (0.0) ---
			// Il rosso è fisso al massimo (255), il verde crolla da 128 a 0
			double fattoreSettore = (0.25d - percentuale) / 0.25d; // Mappa la fascia 0.0-0.25 in un raggio 0.0-1.0
			r = 255;
			g = 128 - (int) (fattoreSettore * 128);
		}

		final double fattoreOscuramento = 0.65d;

		r = (int) (r * fattoreOscuramento);
		g = (int) (g * fattoreOscuramento);
		// Restituisce il colore calcolato al millesimo
		return new Color(r, g, b);
	}
}
