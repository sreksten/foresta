package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.tipi.TipoAttributo;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Come un personaggio si presenta a parole: il nome (proprio, o quello della specie con l'articolo, la preposizione e il numero che lo distingue
 * dagli altri dello stesso gruppo) e la descrizione che ne dà la scheda. Lo tiene {@link PersonaggioBase}, che ne inoltra i metodi di
 * {@link Personaggio}; gli articoli e i nomi della specie li danno le classi concrete.
 */
final class NomeEDescrizione {

	private final PersonaggioBase p;

	NomeEDescrizione(PersonaggioBase personaggio) {
		this.p = personaggio;
	}

	private PersonaggioMD md() {
		return p.getModelloDati();
	}

	String getDescrizione() {
		StringBuilder sb = new StringBuilder();
		if (md().getNome() != null) {
			sb.append(md().getNome());
		} else {
			String s = p.getADS();
			sb.append(Character.toUpperCase(s.charAt(0)));
			sb.append(s.substring(1));
			sb.append(p.getNomeSingolare());
		}
		sb.append(' ');
		int salute = p.getSalute();
		if (salute < 20) {
			sb.append("è molto debole");
		} else if (salute < 40) {
			sb.append("è debole");
		} else if (salute < 80) {
			sb.append("non è molto in salute");
		} else if (salute < 100) {
			sb.append("è in salute");
		} else if (salute < 200) {
			sb.append("è assolutamente in salute");
		} else if (salute < 400) {
			sb.append("è davvero in salute");
		} else {
			sb.append("è san").append(p.getLetteraFinaleAttributo()).append(" come un pesce");
		}
		sb.append(", ");
		int coraggio = p.get(TipoAttributo.CORAGGIO);
		if (coraggio < 30) {
			sb.append("non ha molto coraggio");
		} else if (coraggio < 60) {
			sb.append("ha coraggio");
		} else {
			sb.append("ha coraggio da vendere");
		}
		sb.append(", nei combattimenti ");
		int valore = p.get(TipoAttributo.VALORE);
		if (valore < 30) {
			sb.append("non e' che se la cavi egregiamente");
		} else if (valore < 60) {
			sb.append("se la cava bene");
		} else {
			sb.append("puo' arrecare gravi danni");
		}
		sb.append(" e ");
		int carisma = (int) md().get(TipoAttributo.CARISMA);
		if (carisma < 3) {
			sb.append("non ha molto carisma");
		} else if (carisma < 6) {
			sb.append("ha abbastanza carisma");
		} else {
			sb.append("ha molto carisma");
		}
		sb.append('.');
		Collection<ArtefattoMD> artefatti = md().getArtefatti();
		if (!artefatti.isEmpty()) {
			sb.append(' ');
			sb.append(p.getPronome());
			sb.append(' ');
			sb.append(artefatti.stream()
					.map(a -> a.getTipo().getUtilizzo() + ' ' + a.getNomeCompleto())
					.collect(Collectors.joining(", ")));
			sb.append('.');
		}
		return sb.toString();
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
			return includiPreposizioneArticolata ? "di " + nome : nome;
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
