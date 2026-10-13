package com.threeamigos.foresta.ui.tattiche;

import com.threeamigos.foresta.modellodati.tattiche.*;
import com.threeamigos.foresta.tipi.tattiche.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * I testi italiani della schermata delle tattiche: le etichette dei menu e la frase che spiega una riga della tattica (il riquadro
 * destro), costruiti solo dai dati <code>*MD</code> e dagli enumerati di <code>tipi/tattiche</code>, mai dal motore.
 */
final class TestoTattica {

	private TestoTattica() {
	}

	/**
	 * Il confronto a parole: minore di, uguale a, maggiore di
	 */
	static String confronto(Confronto confronto) {
		switch (confronto) {
			case MINORE:
				return "minore di";
			case MAGGIORE:
				return "maggiore di";
			default:
				return "uguale a";
		}
	}

	/**
	 * Il confronto di un filtro o di una condizione, che nei dati può essere rovesciato ("NON MINORE"): la schermata non ne crea più, ma una tattica letta
	 * o scritta nel codice (quelle dei mostri) ne ha, e si leggono con un "non" davanti.
	 */
	static String relazione(Confronto confronto, boolean negato) {
		return negato ? "non " + confronto(confronto) : confronto(confronto);
	}

	static String criterio(CriterioIncantesimo criterio) {
		switch (criterio) {
			case A_CASO:
				return "a caso";
			case PIU_POTENTE:
				return "il più potente";
			case PIU_ADATTO:
				return "il più adatto al bersaglio";
			case PIU_ADATTO_ESPERTO:
				return "il più adatto, conoscendo l'equipaggiamento";
			default:
				return "il minimo sufficiente a ucciderlo";
		}
	}

	/** Un nome di enumerato per i testi: MINOTAURO_GIGANTE diventa "minotauro gigante" */
	static String nome(Enum<?> valore) {
		return valore.name().toLowerCase(Locale.ITALIAN).replace('_', ' ');
	}

	static String filtro(FiltroMD filtro) {
		StringBuilder sb = new StringBuilder();
		boolean conConfronto = filtro.getTipo().getParametro() == TipoParametro.CONFRONTO_E_NUMERO
				|| filtro.getTipo().getParametro() == TipoParametro.CONFRONTO;
		if (filtro.isNegato() && !conConfronto) {
			sb.append("senza ");
		}
		sb.append(filtro.getTipo().getDescrizione().toLowerCase(Locale.ITALIAN));
		if (conConfronto) {
			sb.append(' ').append(relazione(filtro.getConfronto(), filtro.isNegato()));
			if (filtro.getTipo().getParametro() == TipoParametro.CONFRONTO_E_NUMERO) {
				sb.append(' ').append(filtro.getNumero());
			}
		}
		if (filtro.getStato() != null) {
			sb.append(' ').append(filtro.getStato().getDescrizione().toLowerCase(Locale.ITALIAN));
		} else if (filtro.getClasse() != null) {
			sb.append(' ').append(nome(filtro.getClasse()));
		} else if (filtro.getCategoria() != null) {
			sb.append(' ').append(filtro.getCategoria().getNome().toLowerCase(Locale.ITALIAN));
		} else if (filtro.getTipoDanno() != null) {
			sb.append(' ').append(filtro.getTipoDanno().getNome().toLowerCase(Locale.ITALIAN));
		}
		return sb.toString();
	}

	static String filtri(List<FiltroMD> filtri) {
		List<String> testi = new ArrayList<>();
		for (FiltroMD filtro : filtri) {
			testi.add(filtro(filtro));
		}
		return String.join(", ", testi);
	}

	static String ordinamento(OrdinamentoMD ordinamento) {
		String testo = ordinamento.getTipo().getDescrizione().toLowerCase(Locale.ITALIAN);
		return ordinamento.getTipoDanno() == null ? testo : testo + " " + ordinamento.getTipoDanno().getNome().toLowerCase(Locale.ITALIAN);
	}

	static String selettore(SelettoreMD selettore) {
		StringBuilder sb = new StringBuilder(selettore.getRosa().getDescrizione());
		if (!selettore.getFiltri().isEmpty()) {
			sb.append(" (").append(filtri(selettore.getFiltri())).append(')');
		}
		if (selettore.getOrdinamento() != null) {
			sb.append(": ").append(ordinamento(selettore.getOrdinamento()));
		}
		return sb.toString();
	}

	static String predicato(PredicatoMD predicato) {
		StringBuilder sb = new StringBuilder();
		switch (predicato.getTipo()) {
			case PROBABILITA:
				return (predicato.isNegato() ? "non nel " : "nel ") + predicato.getNumero() + "% dei casi";
			case QUANTI:
				sb.append("quanti: ").append(predicato.getRosa().getDescrizione().toLowerCase(Locale.ITALIAN));
				if (!predicato.getFiltri().isEmpty()) {
					sb.append(" (").append(filtri(predicato.getFiltri())).append(')');
				}
				break;
			default:
				sb.append(predicato.getTipo().getDescrizione().toLowerCase(Locale.ITALIAN));
		}
		sb.append(' ').append(relazione(predicato.getConfronto(), predicato.isNegato())).append(' ').append(predicato.getNumero());
		return sb.toString();
	}

	static String azione(AzioneMD azione) {
		StringBuilder sb = new StringBuilder(azione.getTipo().getDescrizione());
		if (azione.getCriterio() != null) {
			sb.append(": ").append(criterio(azione.getCriterio()));
		}
		if (azione.getArmaDi() != null) {
			sb.append(" di ").append(azione.getArmaDi().getNome().toLowerCase(Locale.ITALIAN));
		}
		if (azione.isSoloSePiuForteDellAttacco()) {
			sb.append(", solo se più forte dell'attacco");
		}
		return sb.toString();
	}

	/**
	 * La riga corta di una voce, come sta nell'elenco: la rosa e le azioni
	 */
	static String riga(VoceMD voce) {
		List<String> azioni = new ArrayList<>();
		for (AzioneMD azione : voce.getAzioni()) {
			azioni.add(azione(azione));
		}
		String testo = String.join(" oppure ", azioni);
		return voce.getSelettore() == null ? testo : testo + " su " + selettore(voce.getSelettore());
	}

	/**
	 * La spiegazione per esteso di una voce, righe di testo per il riquadro destro
	 */
	static List<String> spiegazione(VoceMD voce) {
		List<String> righe = new ArrayList<>();
		if (!voce.isAttiva()) {
			righe.add("Voce disattivata: viene saltata.");
		}
		if (voce.getCondizioni().isEmpty()) {
			righe.add("Sempre, se si può fare.");
		} else {
			for (PredicatoMD condizione : voce.getCondizioni()) {
				righe.add("Se " + predicato(condizione) + ";");
			}
		}
		if (voce.getSelettore() != null) {
			righe.add("su: " + selettore(voce.getSelettore()) + ".");
		}
		for (AzioneMD azione : voce.getAzioni()) {
			righe.add("fa: " + azione(azione) + ".");
		}
		righe.add("La prima azione che si può fare vince; se nessuna si può fare, passa alla voce dopo.");
		return righe;
	}

	// ================================================================ le spiegazioni degli elementi (riquadro destro della schermata)

	static String spiega(TipoRosa rosa) {
		switch (rosa) {
			case SE_STESSO:
				return "Solo il personaggio che sta decidendo.";
			case ALLEATI:
				return "I compagni vivi e in campo, lui compreso.";
			case ALLEATI_CADUTI:
				return "I compagni caduti (non in panchina): servono per la Resurrezione.";
			case AVVERSARI:
				return "I nemici vivi e in campo.";
			case OSPITI:
				return "Gli ospiti vulnerabili vivi, cioè chi viaggia con il gruppo e va protetto (un ostaggio, un ferito da soccorrere).";
			case ALLEATI_E_OSPITI:
				return "I compagni e poi gli ospiti vulnerabili: per le cure.";
			default:
				return "Gli avversari e poi gli ospiti vulnerabili (solo i mostri).";
		}
	}

	static String spiega(TipoFiltro filtro) {
		switch (filtro) {
			case SALUTE:
				return "La salute attuale, in punti, confrontata con un numero.";
			case SALUTE_PERCENTUALE:
				return "La salute attuale rispetto alla massima, in percentuale (50 vuol dire a metà).";
			case MAGIA:
				return "La magia attuale, in punti, confrontata con un numero.";
			case LIVELLO:
				return "Il livello, confrontato con un numero.";
			case LIVELLO_RISPETTO_AL_MIO:
				return "Il livello del bersaglio confrontato con quello del personaggio corrente.";
			case CLASSE:
				return "La classe del personaggio (indipendentemente dal sesso).";
			case CATEGORIA:
				return "La categoria di creatura: umanoide, bestia, non morto, volante, draconico, gigantesco, ibrido, costrutto (ne basta una).";
			case STATO:
				return "Ha un certo effetto di stato attivo (silenziato, bagnato, avvelenato...).";
			case STATO_NEGATIVO:
				return "Ha almeno un effetto di stato che è un male da togliere (avvelenato, stordito, silenziato...): non conta l'ispirazione del Bardo né il berserk del Guerriero.";
			case MAGICO:
				return "Può lanciare magie: non è silenziato e ha abbastanza magia per formularle (abilità innate o "
						+ "un incantesimo a disposizione). Chi non ha poteri innati conta solo dopo aver lanciato il suo primo incantesimo nello scontro.";
			case DEBOLE_A:
				return "Subisce più danno di un certo tipo, per la sua classe o per i suoi stati. Su se stessi controlla se il personaggio è esposto a quel tipo di danno.";
			case DEBOLE_ALL_AZIONE:
				return "È debole al tipo di danno dell'azione che la voce sta per fare (per esempio per una interazione elementale).";
			case IMMUNE_A:
				return "Non subisce alcun danno di un certo tipo, per la sua classe o per l'equipaggiamento (per esempio ha un'armatura che dà resistenza al fuoco). "
						+ "Su se stessi controlla se il personaggio è protetto da quel danno.";
			case IL_CAPO:
				return "È il capo del suo gruppo (se cade, la partita è persa). Serve a scegliere un bersaglio, non a contare.";
			case ME_STESSO:
				return "È il personaggio che sta decidendo. Serve a scegliere un bersaglio, non a contare.";
			case SCELTO_L_ULTIMA_VOLTA:
				return "È l'ultimo avversario o alleato che il personaggio ha scelto con una sua azione: per non cambiare obiettivo a metà. Serve a scegliere un bersaglio, non a contare.";
			case MI_HA_ATTACCATO_PER_ULTIMO:
				return "È il nemico che ha colpito per ultimo il personaggio: per vendicarsi. Ha senso solo fra gli avversari, e serve a scegliere un bersaglio, non a contare.";
			default:
				return "Ha in mano un'arma (principale o secondaria) che dà un certo tipo di danno. Su se stessi serve al cambio d'arma, per non cambiare se ce l'ha già.";
		}
	}

	static String spiega(TipoPredicato predicato) {
		switch (predicato) {
			case QUANTI:
				return "Quanti candidati ci sono in una rosa dopo i filtri. Serve a controllare che esista qualcuno (maggiore di 0) o a contare.";
			case ROUND:
				return "Il numero del round dello scontro (il primo è 1).";
			case ASTUZIA:
				return "L'astuzia del personaggio: l'intelligenza massima della sua classe, che sale col livello. Divide i mostri in fasce.";
			case INTELLIGENZA:
				return "L'intelligenza attuale del personaggio.";
			case PROBABILITA:
				return "Vera solo in una certa percentuale dei casi: serve a non fare sempre la stessa cosa.";
			case VOCE_USATA:
				return "Quante volte il personaggio ha già usato questa voce nello scontro.";
			default:
				return "Quanti round sono passati dall'ultima volta che il personaggio ha usato questa voce.";
		}
	}

	static String spiega(TipoOrdinamento ordinamento) {
		switch (ordinamento) {
			case PRIMO:
				return "Il primo dei candidati rimasti, nell'ordine del gruppo.";
			case A_CASO:
				return "Uno a caso, tutti con la stessa probabilità.";
			case MENO_SALUTE:
				return "Quello con meno salute.";
			case PIU_SALUTE:
				return "Quello con più salute.";
			case LIVELLO_PIU_BASSO:
				return "Quello di livello più basso.";
			case LIVELLO_PIU_ALTO:
				return "Quello di livello più alto.";
			case PIU_DEBOLE_A:
				return "Quello che subisce più danno da un certo tipo di danno.";
			case PIU_RESISTENTE_A:
				return "Quello che subisce meno danno da un certo tipo di danno (un immune è il più resistente).";
			case ULTIMO_ATTACCANTE:
				return "Il nemico che ha colpito per ultimo il personaggio: per vendicarsi. Se non c'è fra i candidati la voce non si esegue e si passa alla successiva.";
			case ULTIMO_SCELTO:
				return "L'ultimo avversario o alleato che il personaggio ha scelto con una sua azione: per non cambiare obiettivo a metà. Se non c'è fra i candidati "
						+ "la voce non si esegue e si passa alla successiva.";
			case PIU_MAGIA:
				return "Quello con più magia.";
			default:
				return "Quello con meno magia.";
		}
	}

	static String spiega(Confronto confronto) {
		switch (confronto) {
			case MINORE:
				return "Il valore è minore del numero.";
			case MAGGIORE:
				return "Il valore è maggiore del numero.";
			default:
				return "Il valore è uguale al numero.";
		}
	}

	static String spiega(Confronto confronto, boolean negato) {
		return negato ? "Il contrario: vale quando il valore NON è " + confronto(confronto) + " il numero." : spiega(confronto);
	}

	static String spiega(CriterioIncantesimo criterio) {
		switch (criterio) {
			case A_CASO:
				return "Uno a caso fra gli incantesimi che può lanciare.";
			case PIU_POTENTE:
				return "Quello con più danno base, senza guardare le debolezze del bersaglio.";
			case PIU_ADATTO:
				return "Quello che fa più danno atteso su quel bersaglio, con le sue debolezze e resistenze di classe e di stato.";
			case PIU_ADATTO_ESPERTO:
				return "Come il più adatto, ma tiene conto anche dell'equipaggiamento già osservato del bersaglio.";
			default:
				return "Fra quelli che bastano a uccidere il bersaglio, il meno caro; se nessuno basta, il più adatto conoscendo l'equipaggiamento.";
		}
	}

	static String spiega(TipoAzione azione) {
		switch (azione) {
			case ATTACCO_FISICO:
				return "Attacca con le armi. Si può sempre fare.";
			case ARIA:
			case ACQUA:
			case TERRA:
			case FUOCO:
			case FULMINE:
			case GELO:
			case VELENO:
			case LUCE_DIVINA:
				return "Lancia l'incantesimo se la pergamena è disponibile e ha la Magia sufficiente per formularlo. Non viene lanciato se il bersaglio è immune.";
			case MORTE:
				return "Il colpo di grazia: se va a segno su chi ha meno di un quarto della salute lo uccide, altrimenti si ritorce contro chi lo lancia. "
						+ "Usalo solo su bersagli quasi caduti.";
			case SILENZIO:
				return "Impedisce al bersaglio di fare magie per un po'. Non si lancia su chi è già silenziato o è immune.";
			case INCANTESIMO_DI_DANNO:
				return "Uno dei sette incantesimi elementali che può lanciare, scelto con un criterio. Con \"solo se più forte\" si lancia solo se rende più "
						+ "dell'attacco con le armi.";
			case INCANTESIMO_DEL_REPERTORIO:
				return "Uno degli incantesimi del repertorio di un mostro, scelto con un criterio.";
			case DARDO_ARCANO:
				return "Il dardo arcano di maghi ed elfi: non costa pergamene, solo magia.";
			case RESURREZIONE:
				return "Riporta in vita un compagno caduto. Serve la pergamena nella scorta e la magia. Funziona solo se ci sono compagni caduti.";
			case ALBA_SACRA:
				return "L'Alba sacra dei sacerdoti toglie gli stati negativi a tutto il gruppo. Viene lanciato solo se qualcuno ne ha uno.";
			case CANTO_CURATIVO:
				return "Il canto del bardo che cura un compagno ferito.";
			case CANTO_IPNOTICO:
				return "Il canto del bardo che spaventa o confonde gli avversari.";
			case URLO_DELLA_BANSHEE:
				return "L'urlo che spaventa e confonde gli avversari, con più probabilità del canto ipnotico. Il bardo lo impara durante il gioco.";
			case CANTO_DI_VALORE:
				return "Il canto del bardo che rafforza coraggio e precisione di tutto il gruppo. Si canta solo se qualcuno non è già ispirato.";
			case POZIONE_SALUTE:
				return "Beve una pozione della Salute dalla scorta del gruppo. Funziona solo se la magia non è piena.";
			case POZIONE_SALUTE_GRANDE:
				return "Beve una pozione della Salute grande, che ridà molta più salute e ne aumenta il livello massimo. Funziona solo se la magia non è piena.";
			case POZIONE_MAGIA:
				return "Beve una pozione della Magia dalla scorta del gruppo. Funziona solo se la magia non è piena.";
			case POZIONE_MAGIA_GRANDE:
				return "Beve una pozione della Magia grande, che ridà molta più magia e ne aumenta il livello massimo. Funziona solo se la magia non è piena.";
			default:
				return "Prende un'arma del gruppo che dia un certo tipo di danno. Conta come l'azione del turno.";
		}
	}
}
