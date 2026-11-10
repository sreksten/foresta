package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.incantesimi.IncantesimoMalefico;
import com.threeamigos.foresta.tipi.ComandiAssociati;
import com.threeamigos.foresta.tipi.CategoriaIncantesimo;
import com.threeamigos.foresta.tipi.PortataIncantesimo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Le due colonne del grimorio, come descrizioni senza immagini (vedi {@link SchermataGrimorio}): l'elenco di tutti gli incantesimi, a sinistra, e la
 * scheda dell'incantesimo scelto, a destra.
 * <p>
 * La scheda dice se è benefico o malefico, su chi agisce, quanta Magia costa lanciarlo e quanto la pergamena, che cosa fa, con che tipo di danno colpisce
 * (e quali stati alterati quel danno può mettere) e, per ogni reazione che quel danno fa scattare su un bersaglio che ha già un certo stato (per esempio
 * il fulmine su chi è bagnato), che cosa succede: quanto cambia il danno, quali stati si tolgono e quali si mettono.
 */
public final class ContenutiDelGrimorio {

	public static final int ALTEZZA_RIGA_ELENCO = 40;
	private static final int MARGINE_RIGA = 8;
	private static final int SPAZIO_FRA_SEZIONI = 14;
	private static final int SPAZIO_ICONA_NOME = 8;
	// L'icona sulla scheda è grande il doppio di com'è nell'elenco
	private static final int INGRANDIMENTO_ICONA = 2;
	private static final int COLORE_SELEZIONE = (45 << 24) | 0xFFFFFF;

	private static final DoomdarkFont MEDIO = DoomdarkFont.medio();

	private ContenutiDelGrimorio() {
	}

	/**
	 * Gli incantesimi del grimorio, nell'ordine in cui li dichiara l'enum: tutti, anche quelli che il gruppo non ha mai visto
	 */
	public static List<TipoIncantesimo> voci() {
		return java.util.Arrays.asList(TipoIncantesimo.values());
	}

	private static List<String> righe(DoomdarkFont font, String testo, int larghezzaMassima) {
		return FontTool.split(font, testo, larghezzaMassima);
	}

	/**
	 * L'elenco degli incantesimi, uno per riga alta {@link #ALTEZZA_RIGA_ELENCO}, con quello scelto in evidenza: i benefici in verde, i malefici in rosso
	 */
	public static ContenutoDiColonna elenco(List<TipoIncantesimo> voci, int scelta, int larghezza, Function<String, Misura> misure) {
		List<Elemento> elementi = new ArrayList<>();
		for (int i = 0; i < voci.size(); i++) {
			TipoIncantesimo voce = voci.get(i);
			int y = i * ALTEZZA_RIGA_ELENCO;
			if (i == scelta) {
				elementi.add(Elemento.rettangolo(larghezza, ALTEZZA_RIGA_ELENCO, COLORE_SELEZIONE, 0, y));
			}
			// L'icona dell'incantesimo a sinistra del nome, com'è, centrata nella riga
			String risorsa = risorsaDellIcona(voce);
			Misura icona = misure.apply(risorsa);
			elementi.add(Elemento.immagine(risorsa, icona.larghezza, icona.altezza, false, null, MARGINE_RIGA, y + (ALTEZZA_RIGA_ELENCO - icona.altezza) / 2));
			int xTesto = MARGINE_RIGA + icona.larghezza + SPAZIO_ICONA_NOME;
			List<String> testo = righe(MEDIO, voce.getNome(), Math.max(MEDIO.getHeight(), larghezza - xTesto - MARGINE_RIGA));
			elementi.add(Elemento.testo(testo, MEDIO, i == scelta ? DoomdarkColorModel.Color.WHITE : DoomdarkColorModel.Color.LIGHT_GRAY, xTesto,
					y + (ALTEZZA_RIGA_ELENCO - testo.size() * MEDIO.getHeight()) / 2));
		}
		return new ContenutoDiColonna(larghezza, voci.size() * ALTEZZA_RIGA_ELENCO, elementi);
	}

	/**
	 * La prima e l'ultima riga dell'immagine che hanno qualcosa di scritto (non trasparente): dove stanno davvero le lettere nel riquadro del testo
	 */
	static int[] estremiDelleLettere(com.threeamigos.foresta.ui.immagini.ImmagineDiPixel immagine) {
		int primo = -1;
		int ultimo = -1;
		for (int y = 0; y < immagine.getAltezza(); y++) {
			for (int x = 0; x < immagine.getLarghezza(); x++) {
				if ((immagine.getArgb(x, y) >>> 24) != 0) {
					if (primo < 0) {
						primo = y;
					}
					ultimo = y;
					break;
				}
			}
		}
		return primo < 0 ? new int[] {0, Math.max(0, immagine.getAltezza() - 1)} : new int[] {primo, ultimo};
	}

	/**
	 * Il nome dell'immagine dell'icona dell'incantesimo: la stessa del suo comando
	 */
	/**
	 * Il costo di lancio: per bersaglio per chi ne colpisce più d'uno (MULTIPLO e GLOBALE), intero per chi ne ha uno solo (il Silenzio,
	 * anche se è di GRUPPO, ne silenzia uno)
	 */
	static String costoDiLancio(TipoIncantesimo voce) {
		PortataIncantesimo portata = voce.getPortata();
		boolean perBersaglio = portata == PortataIncantesimo.MULTIPLO || portata == PortataIncantesimo.GLOBALE;
		return TestiDellaUI.get(perBersaglio ? "GRIMORIO_COSTO_DI_LANCIO_PER_BERSAGLIO" : "GRIMORIO_COSTO_DI_LANCIO", voce.getCostoPerBersaglio());
	}

	static String risorsaDellIcona(TipoIncantesimo voce) {
		return TipoIcona.ofComando(ComandiAssociati.di(voce)).getNomeRisorsa();
	}

	/**
	 * La scheda dell'incantesimo, larga quanto chiesto: in cima l'icona ingrandita e, subito dopo, il nome: i due centrati insieme
	 */
	public static ContenutoDiColonna scheda(TipoIncantesimo voce, int larghezza, Function<String, Misura> misure) {
		Colonna colonna = new Colonna(larghezza);
		String risorsa = risorsaDellIcona(voce);
		Misura originale = misure.apply(risorsa);
		Misura icona = new Misura(originale.larghezza * INGRANDIMENTO_ICONA, originale.altezza * INGRANDIMENTO_ICONA);
		// Il nome può occupare al più lo spazio che l'icona lascia a destra
		int larghezzaPerIlNome = Math.max(1, larghezza - icona.larghezza - SPAZIO_ICONA_NOME);
		// L'immagine del nome è larga quanto il testo (a capo solo se non ci sta), non quanto lo spazio disponibile: così icona e nome si centrano insieme
		int larghezzaDelTesto = Math.max(1, Math.min(LayoutDelTestoGrande.larghezza(voce.getNome()), larghezzaPerIlNome));
		com.threeamigos.foresta.ui.immagini.ImmagineDiPixel nome = LayoutDelTestoGrande.immagine(voce.getNome(), larghezzaDelTesto, true);
		// Si centra la parte scritta del nome (le lettere), non il riquadro dell'immagine, che ha spazio in più per gli accenti: l'icona sta alla stessa
		// altezza delle lettere
		int[] inchiostro = estremiDelleLettere(nome);
		int centroDelleLettere = (inchiostro[0] + inchiostro[1] + 1) / 2;
		int yNome = Math.max(0, icona.altezza / 2 - centroDelleLettere);
		int yIcona = Math.max(0, centroDelleLettere - icona.altezza / 2);
		int altezzaRiga = Math.max(yNome + nome.getAltezza(), yIcona + icona.altezza);
		int yRiga = colonna.iniziaUnaRiga(SPAZIO_FRA_SEZIONI);
		// L'icona e il nome stanno insieme, centrati come un blocco solo: l'icona a sinistra e il nome subito dopo
		int xIcona = Math.max(0, (larghezza - (icona.larghezza + SPAZIO_ICONA_NOME + nome.getLarghezza())) / 2);
		colonna.aggiungi(Elemento.immagine(risorsa, icona.larghezza, icona.altezza, false, null, xIcona, yRiga + yIcona));
		colonna.aggiungi(Elemento.testoGrande(nome, "grimorio|" + voce + "|" + larghezza, xIcona + icona.larghezza + SPAZIO_ICONA_NOME, yRiga + yNome));
		colonna.finisciLaRiga(altezzaRiga);
		colonna.testo(righe(MEDIO, voce.getEffetto(), larghezza), MEDIO, DoomdarkColorModel.Color.WHITE, 0, SPAZIO_FRA_SEZIONI);
		colonna.testo(righe(MEDIO, portata(voce), larghezza), MEDIO, DoomdarkColorModel.Color.LIGHT_GRAY, 0, SPAZIO_FRA_SEZIONI);
		int danniBase = danniBase(voce);
		if (danniBase > 0) {
			colonna.testo(righe(MEDIO, TestiDellaUI.get("GRIMORIO_DANNI_BASE", danniBase), larghezza), MEDIO, DoomdarkColorModel.Color.LIGHT_GRAY, 0, 4);
			// La nota: i danni veri dipendono da chi lancia e da quel che porta
			colonna.testo(righe(MEDIO, TestiDellaUI.get("GRIMORIO_DANNI_BASE_NOTA"), larghezza), MEDIO, DoomdarkColorModel.Color.MEDIUM_GRAY, 0, 2);
		}
		colonna.testo(righe(MEDIO, costoDiLancio(voce), larghezza), MEDIO,
				DoomdarkColorModel.Color.LIGHT_GRAY, 0, 4);
		colonna.testo(righe(MEDIO, TestiDellaUI.get("GRIMORIO_COSTO_PERGAMENA", voce.getCostoAcquisto()), larghezza), MEDIO,
				DoomdarkColorModel.Color.LIGHT_GRAY, 0, 4);
		TipoDanno danno = voce.getTipoDanno();
		if (danno != null) {
			aggiungiIlDanno(danno, larghezza, colonna);
			aggiungiLeReazioni(danno, larghezza, colonna);
		}
		return colonna.contenuto();
	}

	/**
	 * I danni base dell'incantesimo (a livello 1: il calcolo del combattimento li moltiplica per la radice quadrata del livello di chi lo lancia), o 0 se non fa danni propri
	 */
	static int danniBase(TipoIncantesimo voce) {
		Incantesimo incantesimo = FabbricaIncantesimi.crea(voce, 1);
		return incantesimo instanceof IncantesimoMalefico ? ((IncantesimoMalefico) incantesimo).getDanni() : 0;
	}

	/**
	 * Su chi agisce, a parole
	 */
	static String portata(TipoIncantesimo voce) {
		PortataIncantesimo portata = voce.getPortata();
		if (portata == PortataIncantesimo.SINGOLO_SOLO_VIVI) {
			return TestiDellaUI.get(voce.getCategoriaIncantesimo() == CategoriaIncantesimo.BENEFICO ? "GRIMORIO_PORTATA_SINGOLO_SOLO_VIVI_BENEFICO"
					: "GRIMORIO_PORTATA_SINGOLO_SOLO_VIVI_MALEFICO");
		}
		return TestiDellaUI.get("GRIMORIO_PORTATA_" + portata.name());
	}

	/**
	 * Il contenuto che si compone dall'alto in basso: ogni pezzo lascia un po' di spazio sopra di sé
	 */
	private static final class Colonna {
		private final int larghezza;
		private final List<Elemento> elementi = new ArrayList<>();
		private int y;

		Colonna(int larghezza) {
			this.larghezza = larghezza;
		}

		void testo(List<String> righe, DoomdarkFont font, DoomdarkColorModel.Color colore, int x, int spazioSopra) {
			y += spazioSopra;
			elementi.add(Elemento.testo(righe, font, colore, x, y));
			y += righe.size() * font.getHeight();
		}

		/**
		 * Una riga di elementi: sopra di lei lo spazio, e poi la sua altezza
		 */
		int iniziaUnaRiga(int spazioSopra) {
			y += spazioSopra;
			return y;
		}

		void aggiungi(Elemento elemento) {
			elementi.add(elemento);
		}

		void finisciLaRiga(int altezza) {
			y += altezza;
		}

		ContenutoDiColonna contenuto() {
			return new ContenutoDiColonna(larghezza, y, elementi);
		}
	}

	private static void titolo(Colonna colonna, String testo) {
		colonna.testo(Collections.singletonList(testo), MEDIO, DoomdarkColorModel.Color.YELLOW, 0, SPAZIO_FRA_SEZIONI);
	}

	/**
	 * Il tipo di danno e gli stati alterati che può mettere
	 */
	private static void aggiungiIlDanno(TipoDanno danno, int larghezza, Colonna colonna) {
		titolo(colonna, TestiDellaUI.get("GRIMORIO_DANNO"));
		colonna.testo(righe(MEDIO, danno.getNome(), larghezza), MEDIO, DoomdarkColorModel.Color.WHITE, 0, 6);
		TipoEffettoDiStato[] effetti = danno.getEffetti();
		if (effetti != null && effetti.length > 0) {
			colonna.testo(righe(MEDIO, TestiDellaUI.get("GRIMORIO_PUO_METTERE", nomi(java.util.Arrays.asList(effetti))), larghezza), MEDIO,
					DoomdarkColorModel.Color.MEDIUM_GRAY, 0, 2);
		}
	}

	/**
	 * Le reazioni che il danno di questo incantesimo fa scattare: per ognuna il nome, su chi (gli stati che il bersaglio deve avere), quanto cambia il
	 * danno, quali stati toglie e quali mette
	 */
	private static void aggiungiLeReazioni(TipoDanno danno, int larghezza, Colonna colonna) {
		boolean titoloAggiunto = false;
		for (TipoInterazioneConEffettiDiStato reazione : TipoInterazioneConEffettiDiStato.values()) {
			if (!reazione.getInnescanti().contains(danno)) {
				continue;
			}
			if (!titoloAggiunto) {
				titolo(colonna, TestiDellaUI.get("GRIMORIO_REAZIONI"));
				titoloAggiunto = true;
			}
			colonna.testo(righe(MEDIO, reazione.getDescrizione(), larghezza), MEDIO, DoomdarkColorModel.Color.WHITE, 0, 8);
			String moltiplicatore = numero(reazione.getMoltiplicatore());
			colonna.testo(righe(MEDIO, TestiDellaUI.get("GRIMORIO_SU_CHI_E", nomi(reazione.getPrecondizioni()), moltiplicatore), larghezza), MEDIO,
					DoomdarkColorModel.Color.MEDIUM_GRAY, 0, 0);
			if (!reazione.getStatiRimossi().isEmpty()) {
				colonna.testo(righe(MEDIO, TestiDellaUI.get("GRIMORIO_TOGLIE", nomi(reazione.getStatiRimossi())), larghezza), MEDIO,
						DoomdarkColorModel.Color.MEDIUM_GRAY, 0, 0);
			}
			if (!reazione.getStatiApplicati().isEmpty()) {
				colonna.testo(righe(MEDIO, TestiDellaUI.get("GRIMORIO_METTE", nomi(reazione.getStatiApplicati())), larghezza), MEDIO,
						DoomdarkColorModel.Color.MEDIUM_GRAY, 0, 0);
			}
		}
		if (!titoloAggiunto) {
			titolo(colonna, TestiDellaUI.get("GRIMORIO_REAZIONI"));
			colonna.testo(righe(MEDIO, TestiDellaUI.get("GRIMORIO_NESSUNA_REAZIONE"), larghezza), MEDIO, DoomdarkColorModel.Color.MEDIUM_GRAY, 0, 6);
		}
	}

	private static String nomi(Collection<TipoEffettoDiStato> stati) {
		return stati.stream().map(TipoEffettoDiStato::getDescrizione).collect(Collectors.joining(", "));
	}

	/**
	 * Un moltiplicatore a parole, senza gli zeri inutili e con la virgola, come nel bestiario: "1,5", "0,5", "1"
	 */
	static String numero(double valore) {
		return String.format(Locale.ROOT, "%.2f", valore).replaceAll("0+$", "").replaceAll("[.]$", "").replace('.', ',');
	}
}
