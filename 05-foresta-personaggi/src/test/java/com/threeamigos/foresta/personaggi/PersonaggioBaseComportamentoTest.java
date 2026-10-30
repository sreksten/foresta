package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.TipoApproccio;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le regole comuni di PersonaggioBase che non stanno in un collaboratore: identità, immunità, tempo nel gruppo, offerte, armi impugnate, difesa
 * dell'equipaggiamento e poco altro.
 */
class PersonaggioBaseComportamentoTest {

    @BeforeEach
    void prepara() {
        DatiDiProva.nuovaPartita();
    }

    private static Artefatto genera(TipoArtefatto tipo, int livello) {
        return new GeneratoreArtefattiTabelle(new Random(11)).generaArtefatto(tipo, livello);
    }

    @Test
    void duePersonaggiSonoLoStessoSeHannoLoStessoModelloDati() {
        PersonaggioBase a = new Guerriero("Arsenio", 3);
        PersonaggioBase b = new Guerriero("Brunilde", 3);
        Personaggio ancoraA = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        ancoraA.setModelloDati(a.getModelloDati());
        assertEquals(a, a);
        assertEquals(a, ancoraA);
        assertEquals(a.hashCode(), ancoraA.hashCode());
        assertNotEquals(a, b);
        assertNotEquals(a, "Arsenio");
        assertNotEquals(a, null);
        assertTrue(a.getQuantitaMassima() >= 0);
    }

    @Test
    void ilGuerrieroFuriosoNonSiConfondeNeSiSpaventa() {
        Personaggio g = new Guerriero("Arsenio", 3);
        assertFalse(g.isImmuneAEffetto(TipoEffettoDiStato.CONFUSO));
        g.addEffettoDiStato(TipoEffettoDiStato.BERSERK, 2, 0);
        assertTrue(g.isImmuneAEffetto(TipoEffettoDiStato.CONFUSO));
        assertTrue(g.isImmuneAEffetto(TipoEffettoDiStato.SPAVENTATO));
        assertFalse(g.isImmuneAEffetto(TipoEffettoDiStato.AVVELENATO));
        // Un incantesimo non ha immunità di istanza
        assertFalse(g.isImmuneAIncantesimo(TipoIncantesimo.FUOCO));
    }

    @Test
    void chiEOspiteDelGiocatoreStaDallaSuaParte() {
        Personaggio ospite = FabbricaPersonaggi.crea(TipoPersonaggio.UOMO, 1);
        Personaggio estraneo = FabbricaPersonaggi.crea(TipoPersonaggio.UOMO, 1);
        GruppoGiocatore.getIstanza().aggiungiOspite(ospite);
        assertTrue(ospite.appartieneAlGruppoDelGiocatore());
        assertFalse(estraneo.appartieneAlGruppoDelGiocatore());
        Personaggio membro = new Guerriero("Arsenio", 3);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(membro);
        assertTrue(membro.appartieneAlGruppoDelGiocatore());
    }

    @Test
    void ilPoteremagicoModificaIDanniDegliIncantesimi() {
        Personaggio mago = new Mago("Merlino", 3);
        assertEquals(100.0d, mago.getPotereMagico(), 1e-9);
        assertEquals(50.0d, mago.getModificaDanniMagia(50.0d), 1e-9);
        assertTrue(mago.getBersagli() >= 1);
    }

    @Test
    void sipuoFormulareConAbbastanzaMagiaEPNonSeSilenziati() {
        Personaggio mago = new Mago("Merlino", 5);
        mago.addMagia(100000);
        assertTrue(mago.puoFormulare(TipoIncantesimo.FUOCO));
        mago.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 2, 0);
        assertFalse(mago.puoFormulare(TipoIncantesimo.FUOCO));
        mago.rimuoviEffettoDiStato(TipoEffettoDiStato.SILENZIATO);
        mago.subMagia(mago.getMagia());
        assertFalse(mago.puoFormulare(TipoIncantesimo.FUOCO));
    }

    @Test
    void ilTempoNelGruppoScendeSoloPerChiEATempoEVivo() {
        Personaggio aiuto = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 3);
        assertFalse(aiuto.isATempo());
        assertEquals(0, aiuto.decrementaTempo());
        aiuto.setMotivoNelGruppo(TipoMotivoNelGruppo.AIUTO_GRATUITO);
        assertTrue(aiuto.isATempo());
        aiuto.setTempo(2);
        assertEquals(1, aiuto.decrementaTempo());
        assertEquals(0, aiuto.decrementaTempo());
        aiuto.setTempo(5);
        aiuto.muore("prova");
        assertEquals(5, aiuto.decrementaTempo());
        assertEquals(TipoMotivoNelGruppo.AIUTO_GRATUITO, aiuto.getMotivoNelGruppo());
    }

    @Test
    void leOfferteSiPropongonoSoloSeLeSaFareELeDiAiutoSoloAGruppoNonPieno() {
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 3);
        for (int i = 0; i < 30; i++) {
            TipoOfferta offerta = goblin.getOfferta(TipoApproccio.CORRUZIONE);
            assertTrue(offerta == TipoOfferta.AIUTO_MERCENARIO || offerta == TipoOfferta.MAPPA_FORESTA, String.valueOf(offerta));
        }
        // Un amichevole propone cose di amicizia solo se l'approccio è l'amicizia
        Personaggio centauro = FabbricaPersonaggi.crea(TipoPersonaggio.CENTAURO, 3);
        assertNotNull(centauro.getOfferta(TipoApproccio.AMICIZIA));
        assertNotNull(centauro.getOfferta(TipoApproccio.CORRUZIONE));
        // A gruppo pieno le offerte di aiuto spariscono: al goblin non resta che la mappa
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        for (int i = 0; i < CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_TOTALE; i++) {
            gruppo.aggiungiPersonaggioSenzaNotificare(new Guerriero("Eroe" + i, 1));
        }
        for (int i = 0; i < 30; i++) {
            assertEquals(TipoOfferta.MAPPA_FORESTA, goblin.getOfferta(TipoApproccio.CORRUZIONE));
        }
        // Chi non sa fare offerte non ne propone
        Personaggio guerriero = new Guerriero("Arsenio", 3);
        assertNull(guerriero.getOfferta(TipoApproccio.CORRUZIONE));
    }

    @Test
    void sipuoAggiungereUnaQuantitaSenzaLimite() {
        PersonaggioBase p = new Guerriero("Arsenio", 3);
        assertEquals(5, p.limitaEntroMassimi(10, PersonaggioMD.SENZA_LIMITE, 5));
        assertEquals(5, p.limitaEntroMassimi(10, 20, 5));
        assertEquals(3, p.limitaEntroMassimi(10, 13, 5));
        assertEquals(0, p.limitaEntroMassimi(15, 13, 5));
    }

    @Test
    void leArmiImpugnateSiTrovanoPerSlot() {
        Personaggio ladro = new Ladro("Arsenio", 5);
        assertTrue(ladro.getArmaEquipaggiata() instanceof ArmaNaturale);
        assertFalse(ladro.getArmaSecondaria().isPresent());
        Artefatto spada = genera(TipoArtefatto.SPADA, 1);
        ladro.addArtefatto(spada);
        assertFalse(ladro.getArmaEquipaggiata() instanceof ArmaNaturale);
        Artefatto seconda = genera(TipoArtefatto.SPADA, 1);
        ladro.addArtefatto(seconda);
        assertTrue(ladro.getArmaSecondaria().isPresent());
    }

    @Test
    void ilModelloDatiSostituitoRicalcolaIPropriSecondari() {
        PersonaggioBase p = new Guerriero("Arsenio", 5);
        PersonaggioMD altro = new Guerriero("Altro", 5).getModelloDati();
        p.setModelloDati(altro);
        assertSame(altro, p.getModelloDati());
        // Il ricalcolo statico non pubblica niente: serve al caricamento
        PersonaggioBase.ricalcolaAttributiSecondari(altro, p);
        assertEquals(TipoPersonaggio.GUERRIERO, p.getClasse());
    }

    @Test
    void laParataEResistenzaMagicaCrescono_conLEquipaggiamentoEIlGuardiaApertaLaRiduce() {
        Personaggio guerriero = new Guerriero("Arsenio", 10);
        int parata = guerriero.get(TipoAttributo.PARATA);
        int resistenza = guerriero.get(TipoAttributo.RESISTENZA_MAGICA);
        for (TipoArtefatto tipo : new TipoArtefatto[] {TipoArtefatto.SCUDO, TipoArtefatto.ELMO, TipoArtefatto.MASCHERA, TipoArtefatto.ARMATURA,
                TipoArtefatto.SCHINIERI, TipoArtefatto.VESTE, TipoArtefatto.ANELLO}) {
            guerriero.addArtefatto(genera(tipo, 3));
        }
        assertTrue(guerriero.get(TipoAttributo.PARATA) > parata);
        assertTrue(guerriero.get(TipoAttributo.RESISTENZA_MAGICA) > resistenza);
        // Con uno scudo raro la resistenza magica è maggiore che con uno comune
        Personaggio comune = new Guerriero("Comune", 10);
        Artefatto scudoComune = genera(TipoArtefatto.SCUDO, 5);
        scudoComune.getModelloDati().setRarita(TipoRaritaArtefatto.COMUNE);
        comune.addArtefatto(scudoComune);
        Personaggio raro = new Guerriero("Raro", 10);
        Artefatto scudoRaro = genera(TipoArtefatto.SCUDO, 5);
        scudoRaro.getModelloDati().setRarita(TipoRaritaArtefatto.RARO);
        raro.addArtefatto(scudoRaro);
        assertTrue(raro.get(TipoAttributo.RESISTENZA_MAGICA) >= comune.get(TipoAttributo.RESISTENZA_MAGICA));
        // Una spada a due mani lascia la guardia aperta
        Personaggio due = new Guerriero("Due", 10);
        int paratProva = due.get(TipoAttributo.PARATA);
        due.addArtefatto(genera(TipoArtefatto.SPADONE, 3));
        assertTrue(due.get(TipoAttributo.PARATA) < paratProva + 1);
    }

    @Test
    void unaClasseOUnIstanzaPossonoEssereImmuniAUnIncantesimo() {
        Personaggio speciale = new Goblin(3) {
            @Override
            protected boolean isImmuneAIncantesimoPerIstanza(TipoIncantesimo tipoIncantesimo) {
                return tipoIncantesimo == TipoIncantesimo.GELO;
            }
        };
        assertTrue(speciale.isImmuneAIncantesimo(TipoIncantesimo.GELO));
        assertFalse(speciale.isImmuneAIncantesimo(TipoIncantesimo.FUOCO));
        assertTrue(FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1).isImmuneAIncantesimo(TipoIncantesimo.MORTE));
    }

    @Test
    void chiHaSoloProtezioniImpugnaLArmaNaturale() {
        Personaggio guerriero = new Guerriero("Arsenio", 5);
        guerriero.addArtefatto(genera(TipoArtefatto.SCUDO, 2));
        assertTrue(guerriero.getArmaEquipaggiata() instanceof ArmaNaturale);
        assertFalse(guerriero.getArmaSecondaria().isPresent());
    }

    @Test
    void duearmiLasciano_laGuardiaAperta() {
        Personaggio ladro = new Ladro("Arsenio", 10);
        ladro.addArtefatto(genera(TipoArtefatto.SPADA, 2));
        int conUnaSola = ladro.get(TipoAttributo.PARATA);
        ladro.addArtefatto(genera(TipoArtefatto.SPADA, 2));
        assertTrue(ladro.getArmaSecondaria().isPresent());
        assertTrue(ladro.get(TipoAttributo.PARATA) <= conUnaSola);
    }

    @Test
    void ilGruppoPienoToglieLeOfferteDiAiutoAnchePerAmicizia() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        for (int i = 0; i < CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_TOTALE; i++) {
            gruppo.aggiungiPersonaggioSenzaNotificare(new Guerriero("Eroe" + i, 1));
        }
        for (TipoPersonaggio tipo : new TipoPersonaggio[] {TipoPersonaggio.CENTAURO, TipoPersonaggio.FOLLETTO, TipoPersonaggio.GIGANTE,
                TipoPersonaggio.EREMITA, TipoPersonaggio.MINOTAURO, TipoPersonaggio.HOBGOBLIN}) {
            Personaggio p = FabbricaPersonaggi.crea(tipo, 3);
            for (int i = 0; i < 40; i++) {
                for (TipoApproccio approccio : TipoApproccio.values()) {
                    TipoOfferta offerta = p.getOfferta(approccio);
                    assertTrue(offerta != TipoOfferta.AIUTO_GRATUITO && offerta != TipoOfferta.AIUTO_MERCENARIO, tipo + " " + offerta);
                }
            }
        }
    }

    @Test
    void ilGuerrieroAdunaManoSolaPortaUnaSolaArma() {
        Personaggio guerriero = new Guerriero("Arsenio", 5);
        guerriero.addArtefatto(genera(TipoArtefatto.SPADA, 1));
        guerriero.addArtefatto(genera(TipoArtefatto.SPADONE, 1));
        assertFalse(guerriero.getInventario().isEmpty());
    }

    @Test
    void chiNonPuoAgireELAtterratoCongelatoOStordito() {
        Personaggio p = new Guerriero("Arsenio", 5);
        assertFalse(p.isIncapaceDiAgire());
        for (TipoEffettoDiStato effetto : new TipoEffettoDiStato[] {TipoEffettoDiStato.ATTERRATO, TipoEffettoDiStato.CONGELATO, TipoEffettoDiStato.STORDITO}) {
            Personaggio altro = new Guerriero("Altro", 5);
            altro.addEffettoDiStato(effetto, 2, 0);
            assertTrue(altro.isIncapaceDiAgire(), effetto.name());
        }
        p.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 2, 0);
        assertFalse(p.isIncapaceDiAgire());
    }

    @Test
    void laGuerrieraFuriosaNonSiConfondeNeSiSpaventa() {
        Personaggio g = new Guerriera("Brunilde", 3);
        assertFalse(g.isImmuneAEffetto(TipoEffettoDiStato.CONFUSO));
        g.addEffettoDiStato(TipoEffettoDiStato.BERSERK, 2, 0);
        assertTrue(g.isImmuneAEffetto(TipoEffettoDiStato.CONFUSO));
        assertTrue(g.isImmuneAEffetto(TipoEffettoDiStato.SPAVENTATO));
        assertFalse(g.isImmuneAEffetto(TipoEffettoDiStato.AVVELENATO));
    }

    @Test
    void chiNonHaSaluteMassimaConservaLaFrazionePiena() {
        Personaggio p = new Guerriero("Arsenio", 1);
        p.addModificatore(new com.threeamigos.foresta.modellodati.ModificatoreAttributoMD(TipoAttributo.SALUTE,
                com.threeamigos.foresta.tipi.TipoModificatore.QUANTITA_ASSOLUTA, 0));
        p.impostaLivelloDopoReclutamento(4);
        assertEquals(4, p.getLivello());
    }

    @Test
    void ladotazioneNonSiDaSeNonSiPuoEquipaggiare() {
        Personaggio debole = new Guerriero("Arsenio", 1);
        debole.addModificatore(new com.threeamigos.foresta.modellodati.ModificatoreAttributoMD(TipoAttributo.CARICO_MASSIMO,
                com.threeamigos.foresta.tipi.TipoModificatore.QUANTITA_ASSOLUTA, 0));
        EquipaggiamentoIniziale.equipaggia(debole, 3);
        assertTrue(debole.getInventario().isEmpty());
    }
}
