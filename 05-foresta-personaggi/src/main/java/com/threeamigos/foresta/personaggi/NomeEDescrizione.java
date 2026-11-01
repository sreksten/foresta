package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.RegoleDellaLingua;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.TipoAttributo;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Come un personaggio si presenta a parole: il nome (proprio, o quello della specie con l'articolo, la preposizione e il numero che lo distingue
 * dagli altri dello stesso gruppo) e la descrizione che ne dà la scheda. Lo tiene {@link PersonaggioBase}, che ne inoltra i metodi di
 * {@link Personaggio}; gli articoli e i nomi della specie vengono dai file dei testi della classe e dalle regole della lingua, le frasi da
 * {@code descrizione_personaggio_<lingua>.properties}.
 */
final class NomeEDescrizione {

	private static final String FILE_DEI_TESTI = "descrizione_personaggio";

	private final PersonaggioBase p;

	NomeEDescrizione(PersonaggioBase personaggio) {
		this.p = personaggio;
	}

	private PersonaggioMD md() {
		return p.getModelloDati();
	}

	String getDescrizione() {
		RegoleDellaLingua lingua = Lingue.corrente();
		String chi = md().getNome() != null ? md().getNome() : lingua.inizialeMaiuscola(p.getADS() + p.getNomeSingolare());
		String salute = testo(livelloDellaSalute(p.getSalute()));
		int coraggio = p.get(TipoAttributo.CORAGGIO);
		int valore = p.get(TipoAttributo.VALORE);
		int carisma = (int) md().get(TipoAttributo.CARISMA);
		StringBuilder sb = new StringBuilder(testo("FRASE", chi,
				salute,
				testo(coraggio < 30 ? "CORAGGIO.BASSO" : coraggio < 60 ? "CORAGGIO.MEDIO" : "CORAGGIO.ALTO"),
				testo(valore < 30 ? "VALORE.BASSO" : valore < 60 ? "VALORE.MEDIO" : "VALORE.ALTO"),
				testo(carisma < 3 ? "CARISMA.BASSO" : carisma < 6 ? "CARISMA.MEDIO" : "CARISMA.ALTO")));
		Collection<ArtefattoMD> artefatti = md().getArtefatti();
		if (!artefatti.isEmpty()) {
			sb.append(' ').append(testo("ARTEFATTI", p.getPronome(), artefatti.stream()
					.map(a -> a.getTipo().getUtilizzo() + ' ' + a.getNomeCompleto())
					.collect(Collectors.joining(", "))));
		}
		return sb.toString();
	}

	// La chiave della salute, per fasce
	private String livelloDellaSalute(int salute) {
		if (salute < 20) {
			return "SALUTE.MOLTO_DEBOLE";
		} else if (salute < 40) {
			return "SALUTE.DEBOLE";
		} else if (salute < 80) {
			return "SALUTE.POCA";
		} else if (salute < 100) {
			return "SALUTE.BUONA";
		} else if (salute < 200) {
			return "SALUTE.OTTIMA";
		} else if (salute < 400) {
			return "SALUTE.DAVVERO_BUONA";
		}
		// Il «sano come un pesce» si accorda al sesso
		return p.getSesso() == Personaggio.Sesso.MASCHIO ? "SALUTE.COME_UN_PESCE.M" : "SALUTE.COME_UN_PESCE.F";
	}

	// Le parole stanno in descrizione_personaggio_<lingua>.properties
	private static String testo(String chiave, Object... valori) {
		return Testi.formatta(FILE_DEI_TESTI, chiave, valori);
	}

	String getNome(Personaggio.OpzioniGetNome... opzioni) {
		boolean includiArticoloDeterminativoSingolare = false;
		boolean includiArticoloIndeterminativoSingolare = false;
		boolean includiPreposizioneArticolata = false;
		boolean inizialeMaiuscola = false;
		for (Personaggio.OpzioniGetNome opzione : opzioni) {
			if (opzione == Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) {
				includiArticoloDeterminativoSingolare = true;
			} else if (opzione == Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_INDETERMINATIVO_SINGOLARE) {
				includiArticoloIndeterminativoSingolare = true;
			} else if (opzione == Personaggio.OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA) {
				includiPreposizioneArticolata = true;
			} else {
				inizialeMaiuscola = true;
			}
		}
		String nome = md().getNome();
		if (nome != null) {
			return includiPreposizioneArticolata ? Lingue.corrente().conPreposizione(Preposizione.DI, nome) : nome;
		} else if (!p.isPNG()) {
			throw new IllegalStateException("Personaggio giocante senza nome!");
		}
		StringBuilder sb = new StringBuilder();
		if (includiArticoloDeterminativoSingolare) {
			sb.append(p.getADS());
		} else if (includiArticoloIndeterminativoSingolare) {
			sb.append(p.getAIS());
		} else if (includiPreposizioneArticolata) {
			sb.append(p.getDeS());
		}
		sb.append(p.getNomeSingolare());
		sb.append(suffissoOrdinale());
		if (inizialeMaiuscola) {
			sb.replace(0, 1, sb.substring(0, 1).toUpperCase(Locale.ROOT));
		}
		return sb.toString();
	}

	/**
	 * Il numero che distingue un avversario dagli altri della stessa specie nel suo gruppo ("Viverna 2"): la sua posizione nel gruppo
	 * (vedi GruppoAvversario.aggiungiPersonaggio; conta per specie), e solo se ce n'è più d'uno con lo stesso nome; altrimenti niente. Non cambia quando
	 * qualcuno cade, così un mostro si riconosce per tutto lo scontro.
	 */
	String suffissoOrdinale() {
		if (p.getOrdinale() <= 0) {
			return "";
		}
		GruppoAvversario avversari = GruppoAvversario.getIstanza();
		if (!avversari.contiene(p)) {
			return "";
		}
		return avversari.contaDellaStessaSpecie(p) > 1 ? " " + p.getOrdinale() : "";
	}
}
