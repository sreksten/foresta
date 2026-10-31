package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.SpadaNormalmenteNascosta;
import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.artefatti.leggendari.RegoleSetLeggendari;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoSpadaNascostaImpugnata;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoSlotArtefatto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Il peso che un personaggio porta e il suo equipaggiamento: carico e carico massimo, cosa può prendere ed equipaggiare, i set leggendari
 * completi, e l'aggiunta e la rimozione degli artefatti (con il ricalcolo di quello che ne dipende). Lo tiene {@link PersonaggioBase}, che
 * ne inoltra i metodi di {@link Personaggio}; i dati stanno nel {@link PersonaggioMD} del personaggio, che qui si rilegge a ogni uso.
 */
final class CaricoEInventario {

	private final PersonaggioBase p;

	CaricoEInventario(PersonaggioBase personaggio) {
		this.p = personaggio;
	}

	private PersonaggioMD md() {
		return p.getModelloDati();
	}

	double getCarico() {
		return md().getArtefatti().stream().mapToDouble(ArtefattoMD::getPeso).sum();
	}

	Optional<RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento> puoEquipaggiare(Artefatto artefatto) {
		RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento esitoControlloRichiestaEquipaggiamento = RegoleEquipaggiamento.valuta(p, artefatto.getModelloDati());
		if (esitoControlloRichiestaEquipaggiamento.getMotivo() != null) {
			return Optional.of(esitoControlloRichiestaEquipaggiamento);
		}
		return Optional.empty();
	}

	boolean isEquipaggiabile(VistaArtefatto artefatto) {
		return !puoEquipaggiare(Artefatto.da(artefatto)).isPresent();
	}

	List<String> getDescrizioniSetCompleti() {
		return RegoleSetLeggendari.setCompleti(md().getArtefatti()).stream()
				.map(RegoleSetLeggendari::descrizioneSetCompleto)
				.collect(Collectors.toList());
	}

	boolean puoPrendere(double quantita) {
		return quantita <= AttributiSecondari.calcolaCaricoMassimo(md(), p) - getCarico();
	}

	int getCaricoMassimo() {
		return (int) AttributiSecondari.calcolaCaricoMassimo(md(), p);
	}

	List<Artefatto> getInventario() {
		return md().getArtefatti().stream().map(Artefatto::di).collect(Collectors.toList());
	}

	/**
	 * Aggiunge l'artefatto all'equipaggiamento e ne imposta lo slot occupato. I controlli
	 * ({@link #puoEquipaggiare}) spettano a chi chiama: se l'artefatto non passerebbe le regole,
	 * occupa lo slot del suo tipo.
	 */
	void addArtefatto(Artefatto a) {
		ArtefattoMD artefattoMD = a.getModelloDati();
		TipoSlotArtefatto slot = RegoleEquipaggiamento.valuta(p, artefattoMD).getSlot();
		artefattoMD.setSlotEquipaggiamento(slot != null ? slot : artefattoMD.getTipo().getSlotArtefatto());
		md().aggiungiArtefatto(artefattoMD);
		if (a instanceof SpadaNormalmenteNascosta) {
			BusEventi.pubblica(new InternoSpadaNascostaImpugnata());
		}
		// I modificatori dell'artefatto sui primari cambiano anche i secondari che ne derivano
		p.ricalcolaAttributiSecondari();
		limitaAiMassimi();
	}

	void removeArtefatto(Artefatto a) {
		md().togliArtefatto(a.getModelloDati());
		a.getModelloDati().setSlotEquipaggiamento(null);
		p.ricalcolaAttributiSecondari();
		limitaAiMassimi();
	}

	/**
	 * Togliendo un artefatto che dava SALUTE o Magia massima, i valori attuali possono superare i nuovi massimi: si
	 * riportano subito entro il limite (voluto: chi toglie l'artefatto ne perde il vantaggio).
	 */
	void limitaAiMassimi() {
		int salute = (int) md().get(TipoAttributo.SALUTE);
		int saluteMassima = p.getSaluteMassima();
		if (salute > saluteMassima) {
			md().setSalute(saluteMassima);
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.SALUTE, salute, saluteMassima));
		}
		int magia = (int) md().get(TipoAttributo.MAGIA);
		int magiaMassima = p.getMagiaMassima();
		if (magia > magiaMassima) {
			md().setMagia(magiaMassima);
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.MAGIA, magia, magiaMassima));
		}
	}
}
