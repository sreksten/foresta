package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.artefatti.VistaPezzoDelSet;
import com.threeamigos.foresta.modificatori.TestoDeiModificatori;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.CategoriaArtefatto;
import com.threeamigos.foresta.tipi.StatoPezzoDelSet;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole.ImmagineDelNodo;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole.TestoDelNodo;
import com.threeamigos.foresta.ui.componenti.ValoreDiDueTesti;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * L'elenco degli artefatti di una parte di una schermata di scambio (l'inventario, i negozi, l'incantatore), come albero da far scorrere ({@link ElencoScorrevole}),
 * senza disegno: per supertipo e tipo, dal livello più alto al più basso, con un separatore fra un gruppo e l'altro; per ogni artefatto il nome, accanto il
 * livello (e il prezzo, se la parte lo mostra), sotto la descrizione e, se i suoi figli sono visibili, il set con i suoi pezzi, i modificatori e gli
 * incantamenti. Le immagini (i separatori) sono nomi di risorse; il valore con livello e prezzo è un {@link ValoreDiDueTesti}.
 */
public final class ElencoDegliArtefatti {

	/**
	 * Che colori hanno il livello e gli effetti di un artefatto: le schermate lo ridefiniscono per dire a colpo d'occhio se l'artefatto si può prendere
	 * (inventario) o incantare (incantatore)
	 */
	public interface Colori {
		/**
		 * Il colore del livello di un artefatto: di suo grigio
		 */
		default DoomdarkColorModel.Color coloreLivello(VistaArtefatto artefatto, boolean parteAttiva) {
			return DoomdarkColorModel.Color.MEDIUM_GRAY;
		}

		/**
		 * Il colore di una riga di effetto (un ModificatoreAttributoMD o un IncantamentoMD) di un artefatto: di suo quello predefinito; le schermate lo
		 * ridefiniscono per evidenziare certi effetti (l'incantatore, quelli che entrano ed escono con la fusione)
		 */
		default DoomdarkColorModel.Color coloreEffetto(VistaArtefatto artefatto, Object effetto, boolean parteAttiva, DoomdarkColorModel.Color predefinito) {
			return predefinito;
		}
	}

	private static final int INDENTAZIONE = 10;
	private static final int INTERLINEA = 1;

	private ElencoDegliArtefatti() {
	}

	/**
	 * L'albero di quegli artefatti.
	 *
	 * @param evidenziato l'artefatto sotto il mouse (disegnato in bianco invece che in grigio chiaro), o null
	 * @param prezzo      come si calcola il prezzo da mostrare accanto agli artefatti di questa parte, o null se non si mostra
	 * @param larghezza   la larghezza dell'elenco
	 */
	public static ElencoScorrevole<VistaArtefatto> costruisci(VistaPartita vistaPartita, Collection<? extends VistaArtefatto> artefatti, VistaArtefatto evidenziato,
															  boolean parteAttiva, IntUnaryOperator prezzo, Colori colori, int larghezza, MisureDelleImmagini misure) {
		DoomdarkFont font = DoomdarkFont.medio();
		DoomdarkFont fontSmall = DoomdarkFont.piccolo();
		ElencoScorrevole<VistaArtefatto> elenco = new ElencoScorrevole<>(larghezza, INDENTAZIONE, INTERLINEA);
		String separatorePrecedente = null;
		boolean primo = true;
		for (VistaArtefatto artefatto : ordinaArtefattiDaDisegnare(artefatti)) {
			String separatore = separatoreDelTipo(artefatto.getTipo());
			if (primo || !separatore.equals(separatorePrecedente)) {
				separatorePrecedente = separatore;
				primo = false;
				elenco.creaSeparatore(immagine(separatore, misure));
			}

			DoomdarkColorModel.Color colore = artefatto == evidenziato ? DoomdarkColorModel.Color.WHITE : DoomdarkColorModel.Color.LIGHT_GRAY;
			DoomdarkColorModel.Color coloreBase = artefatto == evidenziato ? DoomdarkColorModel.Color.LIGHT_GRAY : DoomdarkColorModel.Color.MEDIUM_GRAY;
			DoomdarkColorModel.Color coloreSeparatori = artefatto == evidenziato ? DoomdarkColorModel.Color.MEDIUM_GRAY : DoomdarkColorModel.Color.DARK_GRAY;

			// In grande il nome proprio (o il nome), in piccolo la descrizione
			String nome = Lingue.corrente().inizialeMaiuscola(artefatto.getNomeBreve());
			ElencoScorrevole<VistaArtefatto>.Nodo nodo = nodoConValore(elenco, artefatto, nome, colore, parteAttiva, prezzo, colori, font, fontSmall);
			nodo.setFigliVisibili(artefatto.isFigliVisibili());
			artefatto.getDescrizioneSet().ifPresent(set -> {
				nodo.creaNodo(testo(set, font, coloreSeparatori), null, null, null, null, artefatto);
				// I pezzi: verdi quelli indossati da chi indossa questo, gialli quelli del gruppo, grigi da trovare (di questi solo il tipo)
				for (VistaPezzoDelSet pezzo : vistaPartita.getGruppoGiocatore().getPezziDelSet(artefatto)) {
					boolean noto = pezzo.getStato() != StatoPezzoDelSet.DA_TROVARE || pezzo.getChiave().equals(artefatto.getPezzoLeggendario());
					DoomdarkColorModel.Color colorePezzo = colorePezzo(pezzo.getStato());
					nodo.creaNodo(testo(pezzo.getTipo().getDescrizione(), font, colorePezzo), null, null,
							testo(noto ? Lingue.corrente().inizialeMaiuscola(pezzo.getNome()) : "???", fontSmall, colorePezzo), null, artefatto);
				}
			});
			if (!artefatto.getModificatori().isEmpty()) {
				nodo.creaNodo(testo(TestiDellaUI.get("MODIFICATORI_ELENCO"), font, coloreSeparatori), null, null, null, null, artefatto);
				for (ModificatoreAttributoMD modificatore : artefatto.getModificatori()) {
					DoomdarkColorModel.Color coloreAttributi = colori.coloreEffetto(artefatto, modificatore, parteAttiva, coloreBase);
					String valore;
					int valoreIntero = (int) modificatore.getQuantita();
					switch (modificatore.getTipoModificatoreAttributo()) {
						case VARIAZIONE_FISSA:
							valore = segno(valoreIntero) + valoreIntero;
							break;
						case VARIAZIONE_PERCENTUALE:
							valore = segno(valoreIntero) + valoreIntero + "%";
							break;
						case QUANTITA_ASSOLUTA:
							valore = TestiDellaUI.get("MODIFICATORE_PORTA_A", valoreIntero);
							break;
						default:
							valore = "";
							break;
					}
					// Il valore resta corto; chi lo colpisce e per quanto va sotto, nella descrizione
					StringBuilder dettaglio = new StringBuilder();
					if (modificatore.getBersaglio() == BersaglioModificatore.BERSAGLIO) {
						dettaglio.append(TestiDellaUI.get("MODIFICATORE_SUL_BERSAGLIO"));
					}
					if (modificatore.isTemporaneo()) {
						dettaglio.append(dettaglio.length() > 0 ? " " : "").append(TestoDeiModificatori.durata(modificatore));
					}
					nodo.creaNodo(testo(modificatore.getTipoAttributo().getNome(), font, coloreAttributi), testo(valore, font, coloreAttributi), null,
							dettaglio.length() > 0 ? testo(dettaglio.toString(), fontSmall, coloreAttributi) : null, null, artefatto);
				}
			}
			if (!artefatto.getIncantamenti().isEmpty()) {
				nodo.creaNodo(testo(TestiDellaUI.get("INCANTAMENTI_ELENCO"), font, coloreSeparatori), null, null, null, null, artefatto);
				for (IncantamentoMD incantamento : artefatto.getIncantamenti()) {
					DoomdarkColorModel.Color coloreAttributi = colori.coloreEffetto(artefatto, incantamento, parteAttiva, coloreBase);
					nodo.creaNodo(testo(incantamento.getNomeIncantamento(), font, coloreAttributi), null, null, null, null, artefatto);
					int bonusFisso = incantamento.getDannoBonusFisso();
					nodo.creaNodo(testo(incantamento.getTipoDannoElementale().getNome(), font, coloreAttributi),
							testo(segno(bonusFisso) + bonusFisso + " + " + (int) (incantamento.getCoefficienteScala() * 100) + "%", font, coloreAttributi), null,
							null, null, artefatto);
				}
			}
		}
		return elenco;
	}

	private static ElencoScorrevole<VistaArtefatto>.Nodo nodoConValore(ElencoScorrevole<VistaArtefatto> elenco, VistaArtefatto artefatto, String nome,
																		DoomdarkColorModel.Color colore, boolean parteAttiva, IntUnaryOperator prezzo,
																		Colori colori, DoomdarkFont font, DoomdarkFont fontSmall) {
		// Accanto al nome: il livello, nel suo colore, e il prezzo in giallo se la parte lo mostra
		String livello = "Lv " + artefatto.getLivello();
		DoomdarkColorModel.Color coloreDelLivello = colori.coloreLivello(artefatto, parteAttiva);
		TestoDelNodo titolo = testo(nome, font, colore);
		TestoDelNodo descrizione = testo(artefatto.getDescrizioneBreve(), fontSmall, colore);
		if (prezzo == null) {
			return elenco.creaNodo(titolo, testo(livello, fontSmall, coloreDelLivello), null, descrizione, null, artefatto);
		}
		ValoreDiDueTesti valore = new ValoreDiDueTesti(livello, coloreDelLivello, String.valueOf(prezzo.applyAsInt(artefatto.getCostoAcquisto())),
				DoomdarkColorModel.Color.YELLOW, fontSmall, Finestra.SPACING);
		return elenco.creaNodo(titolo, null, new ImmagineDelNodo(valore, valore.getMisura()), descrizione, null, artefatto);
	}

	private static TestoDelNodo testo(String testo, DoomdarkFont font, DoomdarkColorModel.Color colore) {
		return new TestoDelNodo(testo, font, colore);
	}

	static ImmagineDelNodo immagine(String risorsa, MisureDelleImmagini misure) {
		Misura m = misure.immagine(risorsa);
		double zoom = NomiDelleImmagini.conZoom(risorsa) ? LivelloDiZoom.valore() : 1.0;
		return new ImmagineDelNodo(risorsa, new Misura((int) Math.round(m.larghezza * zoom), (int) Math.round(m.altezza * zoom)));
	}

	private static String segno(int valore) {
		return valore < 0 ? "" : "+";
	}

	private static DoomdarkColorModel.Color colorePezzo(StatoPezzoDelSet stato) {
		switch (stato) {
			case INDOSSATO:
				return DoomdarkColorModel.Color.GREEN;
			case DEL_GRUPPO:
				return DoomdarkColorModel.Color.YELLOW;
			default:
				return DoomdarkColorModel.Color.MEDIUM_GRAY;
		}
	}

	/**
	 * Per supertipo e tipo, e dentro lo stesso tipo dal livello più alto al più basso: i migliori in cima.
	 */
	public static Collection<VistaArtefatto> ordinaArtefattiDaDisegnare(Collection<? extends VistaArtefatto> artefatti) {
		List<VistaArtefatto> artefattiDaDisegnare = new ArrayList<>(artefatti);
		artefattiDaDisegnare.sort((a1, a2) -> {
			int ordinaleSupertipo1 = a1.getTipo().getCategoria().ordinal();
			int ordinaleSupertipo2 = a2.getTipo().getCategoria().ordinal();
			if (ordinaleSupertipo1 == ordinaleSupertipo2) {
				int ordinaleTipo1 = a1.getTipo().ordinal();
				int ordinaleTipo2 = a2.getTipo().ordinal();
				if (ordinaleTipo1 == ordinaleTipo2) {
					if (a1.getLivello() != a2.getLivello()) {
						return Integer.compare(a2.getLivello(), a1.getLivello());
					}
					return a1.getNome().compareTo(a2.getNome());
				}
				return Integer.compare(ordinaleTipo1, ordinaleTipo2);
			}
			return Integer.compare(ordinaleSupertipo1, ordinaleSupertipo2);
		});
		return artefattiDaDisegnare;
	}

	/**
	 * L'immagine che separa nell'elenco un gruppo di artefatti dal precedente: quella del supertipo, tranne per le maschere, che sono armature ma hanno il
	 * loro separatore (e stanno in fondo alle armature, vedi {@link #ordinaArtefattiDaDisegnare}).
	 */
	public static String separatoreDelTipo(TipoArtefatto tipo) {
		return tipo == TipoArtefatto.MASCHERA ? NomiDelleImmagini.SEPARATORE_MASCHERE : separatoreDellaCategoria(tipo.getCategoria());
	}

	private static String separatoreDellaCategoria(CategoriaArtefatto supertipo) {
		switch (supertipo) {
			case ARMA:
				return NomiDelleImmagini.SEPARATORE_ARMI;
			case ELMO:
				return NomiDelleImmagini.SEPARATORE_ELMI;
			case ARMATURA:
				return NomiDelleImmagini.SEPARATORE_ARMATURE;
			case SCHINIERI:
				return NomiDelleImmagini.SEPARATORE_SCHINIERI;
			case SCUDO:
				return NomiDelleImmagini.SEPARATORE_SCUDI;
			case INCANTAMENTO:
				return NomiDelleImmagini.SEPARATORE_INCANTAMENTI;
			case POTENZIAMENTO_POTERE_MAGICO:
				return NomiDelleImmagini.SEPARATORE_LIBRI_MAGICI;
			case ALTRO:
				return NomiDelleImmagini.SEPARATORE_NINNOLI;
			default:
				throw new IllegalArgumentException("SupertipoArtefatto senza immagine associata");
		}
	}
}
