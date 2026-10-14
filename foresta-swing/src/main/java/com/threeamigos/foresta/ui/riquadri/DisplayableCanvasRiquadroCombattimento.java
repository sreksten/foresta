package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFontMedium;
import com.threeamigos.foresta.ui.testo.FontTool;

/**
 * Il riquadro del combattimento: tutti i personaggi della locazione, dei due gruppi, nell'ordine in cui agiscono nel round (vedi
 * VistaPartita.getOrdineDiCombattimento). Chi è vivo ha il nome e la barra della salute; chi è vivo ma non combatte (in panchina)
 * ha la barra ma il nome grigio; chi è morto ha solo il nome, in grigio scuro. Sotto il personaggio che agisce c'è l'azione che compie (un
 * solo posto, che scorre con lui).
 * <p>
 * La cornice è quella di sempre, tagliata a metà: la metà alta sta in cima e la metà bassa in fondo, e in mezzo c'è lo sfondo nero con i due
 * bordi laterali. Così il riquadro cresce quanto serve. Sta centrato in orizzontale, sotto l'immagine della locazione; se non ci sta sale
 * quanto basta per non coprire la barra delle icone.
 */
public class DisplayableCanvasRiquadroCombattimento implements Finestra {

	private static final Color COLORE_BORDO = new Color(0x8c, 0x42, 0x10);
	private static final Color COLORE_SFONDO = Color.BLACK;
	private static final Color COLORE_GRUPPO = new Color(70, 130, 230);
	private static final Color COLORE_AVVERSARI = new Color(170, 50, 50);
	private static final Color COLORE_PANCHINA = new Color(230, 200, 40);
	// Larghezza del bordo laterale nell'immagine originale (poi si applica lo zoom)
	private static final int BORDO_LATERALE = 3;
	private static final int OFFSET_DA_CORNICE = 8;
	private static final int ALTEZZA_BARRA = 26;
	private static final int SPAZIATURA = 2;
	private static final int LARGHEZZA_SEGNALE = 4;

	private boolean visible;
	private int xOffset;
	private int yOffset;
	private final int parentWidth;
	private final int parentHeight;
	private final int yPreferita;
	private final BufferedImage cornice;
	private final Rectangle rettangolo;
	private VistaPersonaggio combattente;
	private VistaPersonaggio avversario;
	private VistaPersonaggio chiHaDecisoLAzione;
	private String testoAzione;
	private final DoomdarkFont fontMedium = DoomdarkFontMedium.getInstance();

	private final VistaPartita vistaPartita;

	/**
	 * @param yPreferita dove il riquadro comincia di norma: subito sotto l'immagine della locazione
	 */
	public DisplayableCanvasRiquadroCombattimento(int parentWidth, int parentHeight, int yPreferita, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.parentWidth = parentWidth;
		this.parentHeight = parentHeight;
		this.yPreferita = yPreferita;
		cornice = ImageCache.corniceCombattimento;
		visible = false;
		xOffset = (parentWidth - cornice.getWidth()) >> 1;
		yOffset = Math.min(yPreferita, Math.max(0, parentHeight - cornice.getHeight()));

		rettangolo = new Rectangle(xOffset, yOffset, cornice.getWidth(), cornice.getHeight());
	}

	/** Il riquadro com'è stato disegnato l'ultima volta (cresce con i personaggi). */
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

	public void setVisible(boolean visible) {
		this.visible = visible;
		if (!visible) {
			chiHaDecisoLAzione = null;
			testoAzione = null;
		}
	}

	public VistaPersonaggio getCombattente() {
		return combattente;
	}

	public VistaPersonaggio getAvversario() {
		return avversario;
	}

	public void setCombattente(VistaPersonaggio combattente) {
		this.combattente = combattente;
	}

	public void setAvversario(VistaPersonaggio avversario) {
		this.avversario = avversario;
	}

	/**
	 * Che cosa fa il personaggio che agisce, da scrivere sotto di lui.
	 */
	public void setAzione(VistaPersonaggio personaggio, String testo) {
		chiHaDecisoLAzione = personaggio;
		testoAzione = testo;
	}

	/**
	 * Se c'è già un testo per l'azione di quel personaggio.
	 */
	public boolean haAzionePer(VistaPersonaggio personaggio) {
		return testoAzione != null && chiHaDecisoLAzione == personaggio;
	}

	/**
	 * Il testo da scrivere se la tattica non ha detto che cosa fa chi agisce: attacca il bersaglio, se c'è.
	 */
	public static String azionePredefinita(VistaPersonaggio bersaglio) {
		return TestoAzioneInCombattimento.predefinito(bersaglio);
	}

	/**
	 * Il testo dell'azione scelta con la tattica.
	 */
	public static String testoDellAzione(TipoAzione azione, VistaPersonaggio attore, VistaPersonaggio bersaglio) {
		return TestoAzioneInCombattimento.di(azione, attore, bersaglio);
	}

	/**
	 * Disegna il riquadro con tutti i personaggi del combattimento.
	 */
	public void disegnaInfoCombattimento(Graphics2D graphics) {
		if (!visible) {
			return;
		}
		List<? extends VistaPersonaggio> personaggi = vistaPartita.getOrdineDiCombattimento();
		if (personaggi.isEmpty()) {
			return;
		}

		final int altezzaAzione = fontMedium.getHeight() + SPAZIATURA;
		final int altezzaRiga = ALTEZZA_BARRA + SPAZIATURA;
		// Un solo posto per l'azione, sotto chi agisce: le righe sotto di lui scorrono in giù
		final int altezzaContenuto = personaggi.size() * altezzaRiga + altezzaAzione;

		final int metaAlta = cornice.getHeight() / 2;
		final int metaBassa = cornice.getHeight() - metaAlta;
		final int altezza = Math.max(cornice.getHeight(), altezzaContenuto + 2 * OFFSET_DA_CORNICE);
		final int larghezza = cornice.getWidth();

		xOffset = (parentWidth - larghezza) >> 1;
		yOffset = Math.max(0, Math.min(yPreferita, parentHeight - altezza));
		rettangolo.setBounds(xOffset, yOffset, larghezza, altezza);

		// Lo sfondo nero e i bordi laterali per primi, poi la cornice: la metà alta sopra e quella bassa sotto
		final int bordo = Math.max(1, (int) Math.round(BORDO_LATERALE * LivelloDiZoom.valore()));
		graphics.setColor(COLORE_SFONDO);
		graphics.fillRect(xOffset, yOffset, larghezza, altezza);
		graphics.setColor(COLORE_BORDO);
		graphics.fillRect(xOffset, yOffset, bordo, altezza);
		graphics.fillRect(xOffset + larghezza - bordo, yOffset, bordo, altezza);
		graphics.drawImage(cornice.getSubimage(0, 0, larghezza, metaAlta), xOffset, yOffset, null);
		graphics.drawImage(cornice.getSubimage(0, metaAlta, larghezza, metaBassa), xOffset, yOffset + altezza - metaBassa, null);

		final int larghezzaMassimaBarra = larghezza - 2 * OFFSET_DA_CORNICE - LARGHEZZA_SEGNALE - SPAZIATURA;
		final int offsetDaBarra = (ALTEZZA_BARRA - fontMedium.getHeight()) / 2;
		final int xSegnale = xOffset + OFFSET_DA_CORNICE;
		final int xBarra = xSegnale + LARGHEZZA_SEGNALE + SPAZIATURA;
		int y = yOffset + OFFSET_DA_CORNICE;

		for (VistaPersonaggio personaggio : personaggi) {
			boolean agisce = personaggio == combattente;
			disegnaSegnaleDelGruppo(graphics, personaggio, xSegnale, y);
			if (personaggio.isVivo()) {
				double percentualeSalute = (double) personaggio.getSalute() / personaggio.getSaluteMassima();
				disegnaBarraSalute(graphics, xBarra, y, percentualeSalute, larghezzaMassimaBarra, ALTEZZA_BARRA);
				String salute = personaggio.getSalute() + "/" + personaggio.getSaluteMassima();
				int xSalute = xBarra + larghezzaMassimaBarra - SPAZIATURA;
				disegnaTestoConOmbra(graphics, salute, xSalute - FontTool.getWidth(fontMedium, salute), y + offsetDaBarra, DoomdarkColorModel.Color.WHITE, true);
				String nome = nomeEntro(personaggio, larghezzaMassimaBarra - 3 * SPAZIATURA - FontTool.getWidth(fontMedium, "000/000"));
				disegnaTestoConOmbra(graphics, nome, xBarra + SPAZIATURA, y + offsetDaBarra,
						personaggio.isInPanchina() ? DoomdarkColorModel.Color.MEDIUM_GRAY : DoomdarkColorModel.Color.WHITE, true);
			} else {
				String nome = nomeEntro(personaggio, larghezzaMassimaBarra - 2 * SPAZIATURA);
				disegnaTestoConOmbra(graphics, nome, xBarra + SPAZIATURA, y + offsetDaBarra, DoomdarkColorModel.Color.DARK_GRAY, false);
			}
			y += altezzaRiga;
			if (agisce) {
				if (testoAzione != null) {
					String azione = testoAzione;
					int l = larghezzaMassimaBarra - 2 * SPAZIATURA;
					while (azione.length() > 1 && FontTool.getWidth(fontMedium, azione) > l) {
						azione = azione.substring(0, azione.length() - 2);
					}
					disegnaTestoConOmbra(graphics, azione, xBarra + SPAZIATURA, y, DoomdarkColorModel.Color.YELLOW, true);
				}
				y += altezzaAzione;
			}
		}
	}

	/**
	 * Una piccola striscia a sinistra della riga, per distinguere il gruppo dagli avversari; gialla per chi è in panchina.
	 */
	private void disegnaSegnaleDelGruppo(Graphics2D graphics, VistaPersonaggio personaggio, int x, int y) {
		if (personaggio.isVivo() && personaggio.isInPanchina()) {
			graphics.setColor(COLORE_PANCHINA);
		} else {
			graphics.setColor(vistaPartita.getGruppoAvversario().contiene(personaggio) ? COLORE_AVVERSARI : COLORE_GRUPPO);
		}
		graphics.fillRect(x, y, LARGHEZZA_SEGNALE, ALTEZZA_BARRA);
	}

	/**
	 * Il nome (con il numero, se ce n'è più d'uno della stessa specie), accorciato finché ci sta.
	 */
	private String nomeEntro(VistaPersonaggio personaggio, int larghezzaMassima) {
		String nome = personaggio.getNome(VistaPersonaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		while (nome.length() > 1 && FontTool.getWidth(fontMedium, nome) > larghezzaMassima) {
			nome = nome.substring(0, nome.length() - 2);
		}
		return nome;
	}

	private void disegnaBarraSalute(Graphics2D graphics, int x, int y, double percentualeSalute, int larghezzaMassimaBarra, int altezzaBarra) {
		int larghezzaCorrenteBarra = (int) (larghezzaMassimaBarra * percentualeSalute);
		int differenza = larghezzaMassimaBarra - larghezzaCorrenteBarra;
		graphics.setColor(new Color(40, 40, 40));
		graphics.fillRect(x + larghezzaCorrenteBarra, y, differenza, altezzaBarra);
		graphics.setColor(determinaColoreSalute(percentualeSalute));
		graphics.fillRect(x, y, larghezzaCorrenteBarra, altezzaBarra);
	}

	private void disegnaTestoConOmbra(Graphics2D graphics, String testo, int x, int y, DoomdarkColorModel.Color colore, boolean conOmbra) {
		final int OFFSET_OMBRA = 2;
		if (conOmbra) {
			graphics.drawImage(ImageCache.get(testo, fontMedium, DoomdarkColorModel.Color.BLACK), x + OFFSET_OMBRA, y + OFFSET_OMBRA, null);
		}
		graphics.drawImage(ImageCache.get(testo, fontMedium, colore), x, y, null);
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
