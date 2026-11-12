package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.modificatori.ModificatoreAttivo;
import com.threeamigos.foresta.modificatori.TestoDeiModificatori;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole.TestoDelNodo;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * L'elenco delle caratteristiche di un personaggio nella colonna centrale dell'inventario, come albero da far scorrere ({@link ElencoScorrevole}), senza
 * disegno: in cima, se ce ne sono, i modificatori temporanei con il tempo che resta, poi gli attributi primari e quelli secondari con il valore e, sotto, la
 * descrizione (e la causa di un valore fisso). Il riferimento di un nodo è il {@link TipoAttributo} (o {@link #MODIFICATORI_TEMPORANEI} per il titolo
 * dell'elenco dei temporanei). Le caratteristiche non hanno un modello dati proprio: l'elenco ricorda quali sono aperte e quali chiuse.
 */
public final class ElencoDegliAttributi {

	/** Il riferimento del titolo dell'elenco dei modificatori temporanei, per distinguerlo dagli attributi */
	public static final Object MODIFICATORI_TEMPORANEI = new Object();

	private static final int INDENTAZIONE = 10;
	private static final int INTERLINEA = 1;
	// Il valore più largo che un attributo mostra, per la larghezza necessaria
	private static final String VALORE_PIU_LARGO = "999";

	/**
	 * Quanto deve essere largo l'elenco perché ogni attributo stia su una riga con il suo valore: il nome più largo nella lingua in uso, lo spazio e il
	 * valore più largo (le schermate di scambio stringono le cornici finché la colonna centrale non è larga così: vedi SchermataDiScambio)
	 */
	public static int larghezzaNecessaria() {
		DoomdarkFont font = DoomdarkFont.medio();
		int nome = 0;
		for (TipoAttributo attributo : TipoAttributo.values()) {
			nome = Math.max(nome, FontTool.getWidth(font, attributo.getNome()));
		}
		return nome + Finestra.SPACING + FontTool.getWidth(font, VALORE_PIU_LARGO);
	}

	private boolean modificatoriTemporaneiVisibili = true;
	private final Map<TipoAttributo, Boolean> figliVisibili = new EnumMap<>(TipoAttributo.class);

	private boolean isFigliVisibili(TipoAttributo attributo) {
		return figliVisibili.getOrDefault(attributo, Boolean.TRUE);
	}

	/**
	 * Apre o chiude ciò che sta sotto il titolo indicato (un attributo o i modificatori temporanei); dice se il riferimento era uno dei due.
	 */
	public boolean invertiVisibilita(Object riferimento) {
		if (riferimento == MODIFICATORI_TEMPORANEI) {
			modificatoriTemporaneiVisibili = !modificatoriTemporaneiVisibili;
			return true;
		}
		if (riferimento instanceof TipoAttributo) {
			TipoAttributo attributo = (TipoAttributo) riferimento;
			figliVisibili.put(attributo, !isFigliVisibili(attributo));
			return true;
		}
		return false;
	}

	/**
	 * L'albero degli attributi di quel personaggio.
	 *
	 * @param evidenziato l'attributo primario sotto il mouse (bianco invece del suo colore consueto), o null
	 * @param larghezza   la larghezza dell'elenco
	 */
	public ElencoScorrevole<Object> costruisci(VistaPersonaggio p, TipoAttributo evidenziato, int larghezza) {
		ElencoScorrevole<Object> elenco = new ElencoScorrevole<>(larghezza, INDENTAZIONE, INTERLINEA);
		DoomdarkColorModel.Color colore = DoomdarkColorModel.Color.LIGHT_GRAY;

		creaNodoModificatoriTemporanei(elenco, p);

		creaNodo(elenco, p, colore, TipoAttributo.FORZA, p.get(TipoAttributo.FORZA), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.DESTREZZA, p.get(TipoAttributo.DESTREZZA), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.COSTITUZIONE, p.get(TipoAttributo.COSTITUZIONE), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.INTELLIGENZA, p.get(TipoAttributo.INTELLIGENZA), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.SAGGEZZA, p.get(TipoAttributo.SAGGEZZA), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.CARISMA, p.get(TipoAttributo.CARISMA), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.FORTUNA, p.get(TipoAttributo.FORTUNA), evidenziato);

		colore = DoomdarkColorModel.Color.MEDIUM_GRAY;

		creaNodo(elenco, p, colore, TipoAttributo.NUMERO_BERSAGLI, p.getBersagli(), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.POTERE_MAGICO, p.getPotereMagico(), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.RIGENERAZIONE_SALUTE, p.getRigenerazioneSalute(), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.RIGENERAZIONE_MAGIA, p.getRigenerazioneMagia(), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.CARICO_MASSIMO, p.getCaricoMassimo(), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.CRITICO, p.get(TipoAttributo.CRITICO), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.PRECISIONE, p.get(TipoAttributo.PRECISIONE), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.VELOCITA, p.get(TipoAttributo.VELOCITA), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.FURTIVITA, p.get(TipoAttributo.FURTIVITA), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.PARATA, p.get(TipoAttributo.PARATA), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.RESISTENZA_MAGICA, p.get(TipoAttributo.RESISTENZA_MAGICA), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.PERCEZIONE, p.get(TipoAttributo.PERCEZIONE), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.SOGGEZIONE, p.get(TipoAttributo.SOGGEZIONE), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.FURIA, p.get(TipoAttributo.FURIA), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.CORAGGIO, p.get(TipoAttributo.CORAGGIO), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.VALORE, p.get(TipoAttributo.VALORE), evidenziato);
		creaNodo(elenco, p, colore, TipoAttributo.CONTRATTAZIONE, p.get(TipoAttributo.CONTRATTAZIONE), evidenziato);

		return elenco;
	}

	/**
	 * In cima, se il personaggio ha modificatori che scadono, l'elenco di quelli attivi con il tempo che resta
	 */
	private void creaNodoModificatoriTemporanei(ElencoScorrevole<Object> elenco, VistaPersonaggio p) {
		Collection<ModificatoreAttributoMD> temporanei = p.getModificatoriTemporanei();
		if (temporanei.isEmpty()) {
			return;
		}
		DoomdarkFont font = DoomdarkFont.medio();
		DoomdarkFont fontSmall = DoomdarkFont.piccolo();
		DoomdarkColorModel.Color grigio = DoomdarkColorModel.Color.LIGHT_GRAY;
		ElencoScorrevole<Object>.Nodo nodo = elenco.creaNodo(testo(TestiDellaUI.get("MODIFICATORI_TEMPORANEI"), font, grigio),
				testo(String.valueOf(temporanei.size()), font, grigio), null, null, null, MODIFICATORI_TEMPORANEI);
		nodo.setFigliVisibili(modificatoriTemporaneiVisibili);
		for (ModificatoreAttributoMD modificatore : temporanei) {
			DoomdarkColorModel.Color coloreModificatore = modificatore.getQuantita() >= 0
					&& modificatore.getTipoModificatoreAttributo() != TipoModificatore.QUANTITA_ASSOLUTA
					? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED;
			nodo.creaNodo(testo(TestoDeiModificatori.effetto(modificatore), font, coloreModificatore),
					testo(TestoDeiModificatori.durata(modificatore).replaceFirst("^(per|for) ", ""), font, coloreModificatore), null,
					modificatore.getNote().isEmpty() ? null : testo(TestoDeiModificatori.nota(modificatore.getNote()), fontSmall, coloreModificatore),
					null, MODIFICATORI_TEMPORANEI);
		}
	}

	/**
	 * Giallo se un modificatore porta l'attributo a un valore fisso (da qualunque fonte); altrimenti verde o rosso se i modificatori temporanei lo alzano o
	 * lo abbassano. Un attributo primario è arancione se ci sono punti abilità da spendere (e nessun modificatore lo colora già). Sotto il mouse un
	 * attributo primario è bianco.
	 */
	private void creaNodo(ElencoScorrevole<Object> elenco, VistaPersonaggio p, DoomdarkColorModel.Color colore, TipoAttributo tipoAttributo,
						  double valoreAttributo, TipoAttributo evidenziato) {
		DoomdarkFont font = DoomdarkFont.medio();
		DoomdarkFont fontSmall = DoomdarkFont.piccolo();
		Optional<ModificatoreAttivo> fissaggio = p.getFissaggio(tipoAttributo);
		double variazione = p.getVariazioneDaTemporanei(tipoAttributo);
		DoomdarkColorModel.Color coloreBase = fissaggio.isPresent() ? DoomdarkColorModel.Color.YELLOW
				: variazione > 0 ? DoomdarkColorModel.Color.GREEN
				: variazione < 0 ? DoomdarkColorModel.Color.RED
				: tipoAttributo.isPrimario() && p.getPuntiAbilitaDisponibili() > 0 ? DoomdarkColorModel.Color.ORANGE
				: colore;
		DoomdarkColorModel.Color coloreEffettivo = tipoAttributo.isPrimario() && tipoAttributo == evidenziato
				? DoomdarkColorModel.Color.WHITE
				: coloreBase;
		ElencoScorrevole<Object>.Nodo nodo = elenco.creaNodo(testo(tipoAttributo.getNome(), font, coloreEffettivo),
				testo(String.valueOf((int) valoreAttributo), font, coloreEffettivo), null,
				testo(tipoAttributo.getDescrizione(), fontSmall, coloreEffettivo), null, tipoAttributo);
		nodo.setFigliVisibili(isFigliVisibili(tipoAttributo));
		// La causa del valore fisso (sotto l'attributo) è gialla, ma diventa bianca insieme all'attributo sotto il mouse
		DoomdarkColorModel.Color coloreCausa = coloreEffettivo == DoomdarkColorModel.Color.WHITE ? DoomdarkColorModel.Color.WHITE : DoomdarkColorModel.Color.YELLOW;
		fissaggio.ifPresent(f -> {
			ModificatoreAttributoMD modificatore = f.getModificatore();
			String da = f.getFonte().isEmpty() ? "" : TestiDellaUI.get("FONTE_DA", f.getFonte());
			String durata = modificatore.isTemporaneo() ? " (" + TestoDeiModificatori.durataResidua(modificatore) + ")" : "";
			String causa = (da + durata).trim();
			nodo.creaNodo(testo(TestiDellaUI.get("FISSATO_A", (int) modificatore.getQuantita()), fontSmall, coloreCausa), null, null,
					causa.isEmpty() ? null : testo(causa, fontSmall, coloreCausa), null, tipoAttributo);
		});
	}

	private static TestoDelNodo testo(String testo, DoomdarkFont font, DoomdarkColorModel.Color colore) {
		return new TestoDelNodo(testo, font, colore);
	}
}
