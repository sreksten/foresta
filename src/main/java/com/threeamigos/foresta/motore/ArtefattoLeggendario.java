package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.tipi.TipoArtefatto;
import com.threeamigos.foresta.motore.tipi.TipoAttributo;
import com.threeamigos.foresta.motore.tipi.TipoDanno;
import com.threeamigos.foresta.motore.tipi.TipoModificatore;
import com.threeamigos.foresta.motore.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tools.CostruttoreArtefatto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Gli artefatti leggendari che le missioni di recupero possono mettere in palio (vedi
 * RecuperaUnArtefattoLeggendario): non stanno nei templi dall'inizio, li custodisce un tempio che la missione fa
 * sorgere quando il gruppo ne sente la leggenda. Ognuno ha il nome con cui se ne parla, la leggenda che racconta
 * l'armaiolo (una battuta per elemento) e il modo di costruirlo.
 */
public enum ArtefattoLeggendario {

	SPADA_DELLA_MORTE("la Spada della Morte",
			Arrays.asList(
					"Avete mai sentito parlare della Spada della Morte alata con rinterzo laterale?",
					"La forgiò un fabbro che voleva farla finita con i porci giganti di Malgaard, e ci riuscì fin troppo bene.",
					"Dicono che ogni fendente sussurri il nome di chi sta per cadere, e che RomyJona in persona l'abbia incantata.",
					"Oggi riposa in un tempio nella foresta, guardato da un nido di viverne."),
			() -> {
				Artefatto spada = CostruttoreArtefatto.istanza()
						.setTipo(TipoArtefatto.SPADA)
						.setNome("la Spada della Morte alata con rinterzo laterale")
						.setDescrizione("che massacra i porci")
						.setLivello(5)
						.setDanniBase(16)
						.setCostoAcquisto(60)
						.setPeso(2)
						.setModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 100)
						.setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.AUMENTO_PERCENTUALE, 20)
						.setModificatore(TipoAttributo.VALORE, TipoModificatore.AUMENTO_FISSO, 2)
						.setIncantamento("Incantesimo di RomyJona", TipoDanno.NECROTICO, 12, 0.15)
						.costruisci();
				spada.getModelloDati().setRarita(TipoRaritaArtefatto.LEGGENDARIO);
				return spada;
			}),

	SCUDO_FISCALE("lo Scudo Fiscale",
			Arrays.asList(
					"Conoscete la storia dello Scudo Fiscale? Non ridete, è una cosa seria.",
					"Lo portava un esattore delle tasse che nessun mercante riuscì mai a imbrogliare: chi lo guardava abbassava i prezzi da sé.",
					"Ripara dal gelo e dalle magie, ma soprattutto dai conti salati.",
					"Quando l'esattore morì, i mercanti lo nascosero in un tempio nella foresta, e ci misero le viverne a guardia."),
			() -> {
				Artefatto scudo = CostruttoreArtefatto.istanza()
						.setTipo(TipoArtefatto.SCUDO)
						.setNome("lo Scudo Fiscale")
						.setDescrizione("che si fa fare sconti sugli acquisti")
						.setLivello(5)
						.setCostoAcquisto(60)
						.setPeso(3)
						.setModificatore(TipoAttributo.COSTITUZIONE, TipoModificatore.AUMENTO_PERCENTUALE, 10)
						.setModificatore(TipoAttributo.RESISTENZA_MAGICA, TipoModificatore.AUMENTO_FISSO, 3)
						.setModificatore(TipoAttributo.CONTRATTAZIONE, TipoModificatore.AUMENTO_FISSO, 4)
						.setIncantamento("La battuta del cavolo", TipoDanno.GELO, 12, 0.15)
						.costruisci();
				scudo.getModelloDati().setRarita(TipoRaritaArtefatto.LEGGENDARIO);
				return scudo;
			});

	private final String nomeBreve;
	private final List<String> leggenda;
	private final Supplier<Artefatto> costruttore;

	ArtefattoLeggendario(String nomeBreve, List<String> leggenda, Supplier<Artefatto> costruttore) {
		this.nomeBreve = nomeBreve;
		this.leggenda = Collections.unmodifiableList(leggenda);
		this.costruttore = costruttore;
	}

	/**
	 * Il nome con cui se ne parla, con l'articolo: "la Spada della Morte".
	 */
	public String getNomeBreve() {
		return nomeBreve;
	}

	/**
	 * La leggenda che racconta l'armaiolo, una battuta per elemento.
	 */
	public List<String> getLeggenda() {
		return leggenda;
	}

	/**
	 * Un artefatto nuovo, leggendario.
	 */
	public Artefatto costruisci() {
		return costruttore.get();
	}
}
