package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il raffreddore dura sempre tre turni: non lo accorcia nessun attributo (gli altri stati elementali sì: la saggezza riduce la durata del bagnato).
 */
class DurataDelRaffreddoreTest {

	@BeforeEach
	void prepara() {
		DatiDiProva.nuovaPartita();
	}

	@Test
	void duraSempreTreTurniQualunqueSiaLaSaggezza() {
		Personaggio saggio = FabbricaPersonaggi.crea(TipoPersonaggio.MAGO, 1);
		saggio.addModificatore(new ModificatoreAttributoMD(TipoAttributo.SAGGEZZA, TipoModificatore.QUANTITA_ASSOLUTA, 400));
		Personaggio sciocco = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
		sciocco.addModificatore(new ModificatoreAttributoMD(TipoAttributo.SAGGEZZA, TipoModificatore.QUANTITA_ASSOLUTA, 1));

		assertEquals(CostantiPersonaggi.RAFFREDDATO_TURNI, CalcolatoreCombattimento.calcolaDurataStato(saggio, TipoEffettoDiStato.RAFFREDDATO));
		assertEquals(CostantiPersonaggi.RAFFREDDATO_TURNI, CalcolatoreCombattimento.calcolaDurataStato(sciocco, TipoEffettoDiStato.RAFFREDDATO));
		assertEquals(3, CostantiPersonaggi.RAFFREDDATO_TURNI);
		// Mentre un altro stato elementale, il bagnato, con tanta saggezza dura meno di quanto durerebbe a un altro
		assertTrue(CalcolatoreCombattimento.calcolaDurataStato(saggio, TipoEffettoDiStato.BAGNATO)
				< CalcolatoreCombattimento.calcolaDurataStato(sciocco, TipoEffettoDiStato.BAGNATO));
	}

	@Test
	void ilRaffreddoreNonImpedisceDiAgireDaSoloEDeVeEssereUnMale() {
		Personaggio raffreddato = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
		raffreddato.addEffettoDiStato(TipoEffettoDiStato.RAFFREDDATO, 3, 0);
		// Non è un blocco totale come lo stordimento: a decidere se si perde il turno è il tiro dello starnuto
		assertEquals(false, raffreddato.isIncapaceDiAgire());
		assertEquals(false, TipoEffettoDiStato.RAFFREDDATO.isBenefico());
	}

	private static com.threeamigos.foresta.artefatti.Arma armaDiAria() {
		return new com.threeamigos.foresta.artefatti.Arma() {
			@Override public int getDanni() { return 20; }
			@Override public int getLivello() { return 5; }
			@Override public com.threeamigos.foresta.tipi.TipoDanno getTipoDanno() { return com.threeamigos.foresta.tipi.TipoDanno.ARIA; }
			@Override public boolean isIncantata() { return false; }
			@Override public java.util.Collection<com.threeamigos.foresta.modellodati.IncantamentoMD> getIncantamenti() { return new java.util.ArrayList<>(); }
		};
	}

	private static Personaggio ariaSuUnBagnato(TipoPersonaggio classe) {
		Personaggio attaccante = FabbricaPersonaggi.crea(TipoPersonaggio.LADRO, 5);
		Personaggio bagnato = FabbricaPersonaggi.crea(classe, 5);
		bagnato.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
		DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, bagnato, armaDiAria());
		// Gli spettri e simili (aria e terra li attraversano) non sentono nemmeno il colpo: per loro la reazione non scatta
		if (!classe.isImmuneAIncantesimo(com.threeamigos.foresta.tipi.TipoIncantesimo.ARIA)) {
			assertTrue(risultato.getInterazioni().contains(com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato.RAFFREDDAMENTO),
					classe + ": la reazione scatta");
		}
		// Il colpo ha le sue conseguenze sugli stati, e chi le subisce le applica con le sue immunità
		ApplicazioneDelRisultato.applica(bagnato, risultato);
		return bagnato;
	}

	@Test
	void ilVentoSuUnBagnatoLoRaffreddaMaNonSeESiUnNonMorto() {
		// Un goblin bagnato colpito dall'aria prende il raffreddore (tre turni), e perde il bagnato
		Personaggio goblin = ariaSuUnBagnato(TipoPersonaggio.GOBLIN);
		assertTrue(goblin.hasEffettoDiStato(TipoEffettoDiStato.RAFFREDDATO));
		assertEquals(CostantiPersonaggi.RAFFREDDATO_TURNI, goblin.getEffettiDiStato().stream()
				.filter(e -> e.getTipoEffettoDiStato() == TipoEffettoDiStato.RAFFREDDATO).findFirst().get().getDurata());
		assertFalse(goblin.hasEffettoDiStato(TipoEffettoDiStato.BAGNATO));

		// Uno scheletro, uno spettro o il Lich no: i non morti non si raffreddano
		for (TipoPersonaggio nonMorto : new TipoPersonaggio[] {TipoPersonaggio.SCHELETRO, TipoPersonaggio.SPETTRO, TipoPersonaggio.LICH}) {
			assertFalse(ariaSuUnBagnato(nonMorto).hasEffettoDiStato(TipoEffettoDiStato.RAFFREDDATO), nonMorto.name());
		}
	}

	@Test
	void ilVentoAsciugaSempreChiELBagnatoAncheSeNonSiRaffredda() {
		// Un'arma d'aria toglie il bagnato a chiunque ne subisca il colpo, anche a chi non prende il raffreddore (i non morti) e a chi è immune all'aria (lo spettro)
		for (TipoPersonaggio classe : new TipoPersonaggio[] {TipoPersonaggio.GOBLIN, TipoPersonaggio.SCHELETRO, TipoPersonaggio.SPETTRO, TipoPersonaggio.GUERRIERO}) {
			Personaggio bagnato = FabbricaPersonaggi.crea(classe, 5);
			bagnato.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
			DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(FabbricaPersonaggi.crea(TipoPersonaggio.LADRO, 5), bagnato, armaDiAria());
			assertTrue(risultato.getEffettiDiStatoDaRimuovere().contains(TipoEffettoDiStato.BAGNATO), classe.name());
			ApplicazioneDelRisultato.applica(bagnato, risultato);
			assertFalse(bagnato.hasEffettoDiStato(TipoEffettoDiStato.BAGNATO), classe.name());
		}
	}
}
