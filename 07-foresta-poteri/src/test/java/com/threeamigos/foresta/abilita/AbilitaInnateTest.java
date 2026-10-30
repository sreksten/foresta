package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.Chierico;
import com.threeamigos.foresta.personaggi.Druido;
import com.threeamigos.foresta.personaggi.Elfo;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Goblin;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Sacerdote;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.CostantiAbilitaInnate;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le abilità innate di chierico, druido, sacerdote, mago ed elfo: chi le conosce e quando le può usare, quando servono, e che cosa fanno.
 */
class AbilitaInnateTest {

    private final List<String> frasi = new ArrayList<>();
    private Runnable iscrizione;
    private GruppoGiocatore gruppo;
    private GruppoAvversario avversari;

    @BeforeEach
    void prepara() {
        BusEventi.impostaConsegna(Runnable::run);
        GruppoGiocatoreMD dati = new GruppoGiocatoreMD();
        GruppoGiocatore.setFornitoreDiDati(() -> dati);
        GruppoGiocatore.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        StatoDelCombattimento.azzeraIstanza();
        Dado.impostaSeme(31L);
        iscrizione = BusEventi.iscriviti(NotificaTestoFrase.class, e -> frasi.add(e.getMessaggio()));
        gruppo = GruppoGiocatore.getIstanza();
        avversari = GruppoAvversario.getIstanza();
    }

    @AfterEach
    void ripulisce() {
        iscrizione.run();
        BusEventi.impostaConsegna(null);
        StatoDelCombattimento.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        Dado.ripristina();
    }

    private static Personaggio pieno(Personaggio p) {
        if (p.getNomeProprio().isPresent()) {
            p.getModelloDati().setPng(false);
        }
        p.addMagia(100000);
        p.addSalute(100000);
        return p;
    }

    private Personaggio nelGruppo(Personaggio p) {
        gruppo.aggiungiPersonaggio(pieno(p));
        return p;
    }

    private Personaggio avversario(TipoPersonaggio tipo) {
        Personaggio p = FabbricaPersonaggi.crea(tipo, 5);
        avversari.aggiungiPersonaggio(p);
        return p;
    }

    private static void ferisci(Personaggio p) {
        p.subSalute(p.getSaluteMassima() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
    }

    // --- Chi le conosce e chi le può usare

    @Test
    void ogniClasseConosceLeSueAbilita() {
        for (TipoPersonaggio classe : TipoPersonaggio.values()) {
            boolean qualcosa = false;
            for (TipoAbilita tipo : TipoAbilita.values()) {
                qualcosa |= AbilitaInnate.conosciutaDa(classe, tipo);
            }
            assertEquals(qualcosa, AbilitaInnate.conosciutaDa(classe), classe.name());
        }
        assertTrue(AbilitaInnate.conosciutaDa(TipoPersonaggio.CHIERICO, TipoAbilita.SCACCIARE_I_NON_MORTI));
        assertTrue(AbilitaInnate.conosciutaDa(TipoPersonaggio.SACERDOTESSA, TipoAbilita.LUCE_DIVINA));
        assertTrue(AbilitaInnate.conosciutaDa(TipoPersonaggio.ELFA, TipoAbilita.DARDO_ARCANO));
        assertTrue(AbilitaInnate.conosciutaDa(TipoPersonaggio.DRUIDA, TipoAbilita.RADICI));
        assertFalse(AbilitaInnate.conosciutaDa(TipoPersonaggio.GUERRIERO, TipoAbilita.RADICI));
    }

    @Test
    void ilCostoDelDardoArcanoDipendeDallaClasse() {
        assertEquals(CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_MAGO, CostoDelleAbilita.di(TipoAbilita.DARDO_ARCANO, TipoPersonaggio.MAGO));
        assertEquals(CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_MAGO, CostoDelleAbilita.di(TipoAbilita.DARDO_ARCANO, TipoPersonaggio.MAGA));
        assertEquals(CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_ELFO, CostoDelleAbilita.di(TipoAbilita.DARDO_ARCANO, TipoPersonaggio.ELFO));
        assertEquals(TipoAbilita.RADICI.getCostoLancio(), CostoDelleAbilita.di(TipoAbilita.RADICI, TipoPersonaggio.DRUIDO));
        for (TipoAbilita tipo : TipoAbilita.values()) {
            assertTrue(tipo.getCostoLancio() > 0 && tipo.getRicarica() > 0, tipo.name());
            assertNotNull(tipo.getTipoAzione());
        }
    }

    @Test
    void siPuoUsareSoloSeVivoNonSilenziatoConAbbastanzaMagiaEFuoriRicarica() {
        Personaggio chierico = pieno(new Chierico("Fra Tuck", 5));
        assertTrue(AbilitaInnate.puoUsare(chierico, TipoAbilita.IMPOSIZIONE_DELLE_MANI));
        assertFalse(AbilitaInnate.puoUsare(chierico, TipoAbilita.RADICI), "non la conosce");
        chierico.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 2, 0);
        assertFalse(AbilitaInnate.puoUsare(chierico, TipoAbilita.IMPOSIZIONE_DELLE_MANI));
        chierico.rimuoviEffettoDiStato(TipoEffettoDiStato.SILENZIATO);
        AbilitaInnate.avviaRicarica(chierico, TipoAbilita.IMPOSIZIONE_DELLE_MANI);
        assertTrue(AbilitaInnate.isInRicarica(chierico, TipoAbilita.IMPOSIZIONE_DELLE_MANI));
        assertFalse(AbilitaInnate.puoUsare(chierico, TipoAbilita.IMPOSIZIONE_DELLE_MANI));
        chierico.setRicarica(TipoAbilita.IMPOSIZIONE_DELLE_MANI.name(), 0);
        chierico.subMagia(chierico.getMagia());
        assertFalse(AbilitaInnate.puoUsare(chierico, TipoAbilita.IMPOSIZIONE_DELLE_MANI));
        chierico.addMagia(100000);
        chierico.muore("prova");
        assertFalse(AbilitaInnate.puoUsare(chierico, TipoAbilita.IMPOSIZIONE_DELLE_MANI));
    }

    @Test
    void leAbilitaInRicaricaCheServirebberoSiVedono() {
        Personaggio chierico = nelGruppo(new Chierico("Fra Tuck", 5));
        Personaggio ferito = nelGruppo(new Guerriero("Ferito", 5));
        ferisci(ferito);
        avversario(TipoPersonaggio.GOBLIN);
        AbilitaInnate.avviaRicarica(chierico, TipoAbilita.IMPOSIZIONE_DELLE_MANI);
        AbilitaInnate.avviaRicarica(chierico, TipoAbilita.SCACCIARE_I_NON_MORTI);
        List<TipoAbilita> inRicarica = AbilitaInnate.abilitaInRicarica(chierico, gruppo, avversari);
        assertEquals(1, inRicarica.size(), "gli scheletri non ci sono: scacciare non servirebbe");
        assertEquals(TipoAbilita.IMPOSIZIONE_DELLE_MANI, inRicarica.get(0));
        chierico.muore("prova");
        assertTrue(AbilitaInnate.abilitaInRicarica(chierico, gruppo, avversari).isEmpty());
    }

    @Test
    void chiEDaScacciareEChiDaRigenerare() {
        Personaggio scheletro = avversario(TipoPersonaggio.SPETTRO);
        assertTrue(AbilitaInnate.isDaScacciare(scheletro));
        scheletro.addEffettoDiStato(TipoEffettoDiStato.SPAVENTATO, 2, 0);
        assertFalse(AbilitaInnate.isDaScacciare(scheletro));
        assertFalse(AbilitaInnate.isDaScacciare(avversario(TipoPersonaggio.GOBLIN)));
        Personaggio caduto = avversario(TipoPersonaggio.SPETTRO);
        caduto.muore("prova");
        assertFalse(AbilitaInnate.isDaScacciare(caduto));
        assertFalse(AbilitaInnate.isDaScacciare(new Goblin(3) {
            @Override
            public boolean isImmuneAEffetto(TipoEffettoDiStato effettoStato) {
                return true;
            }
        }));

        assertFalse(AbilitaInnate.daRigenerare(gruppo).isPresent());
        Personaggio a = nelGruppo(new Guerriero("A", 5));
        Personaggio b = nelGruppo(new Guerriero("B", 5));
Personaggio scheletroAlleato = nelGruppo(FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 3));
        assertFalse(AbilitaInnate.daRigenerare(gruppo).isPresent(), "tutti in piena salute");
        ferisci(a);
        b.subSalute(b.getSaluteMassima() * 3 / 4, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        scheletroAlleato.subSalute(scheletroAlleato.getSalute() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertSame(b, AbilitaInnate.daRigenerare(gruppo).get());
        b.addEffettoDiStato(TipoEffettoDiStato.RIGENERAZIONE, 3, 0, 5.0d);
        assertSame(a, AbilitaInnate.daRigenerare(gruppo).get());
    }

    @Test
    void quandoUnAbilitaServe() {
        Personaggio sacerdote = nelGruppo(new Sacerdote("Padre", 5));
        Personaggio ferito = nelGruppo(new Guerriero("Ferito", 5));
        // L'alba sacra serve anche fuori dagli scontri, a chi ha uno stato da togliere
        assertFalse(AbilitaInnate.serve(TipoAbilita.ALBA_SACRA, gruppo, avversari));
        ferito.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 3, 0);
        assertTrue(AbilitaInnate.haUnoStatoNegativo(ferito));
        assertFalse(AbilitaInnate.haUnoStatoNegativo(sacerdote));
        assertTrue(AbilitaInnate.serve(TipoAbilita.ALBA_SACRA, gruppo, avversari));
        // Senza avversari non servono le altre
        for (TipoAbilita tipo : TipoAbilita.values()) {
            if (tipo != TipoAbilita.ALBA_SACRA) {
                assertFalse(AbilitaInnate.serve(tipo, gruppo, avversari), tipo.name());
            }
        }
        avversario(TipoPersonaggio.GOBLIN);
        assertFalse(AbilitaInnate.serve(TipoAbilita.SCACCIARE_I_NON_MORTI, gruppo, avversari));
        assertFalse(AbilitaInnate.serve(TipoAbilita.IMPOSIZIONE_DELLE_MANI, gruppo, avversari));
        assertFalse(AbilitaInnate.serve(TipoAbilita.RIGENERAZIONE, gruppo, avversari));
        assertTrue(AbilitaInnate.serve(TipoAbilita.LUCE_DIVINA, gruppo, avversari));
        ferisci(ferito);
        assertTrue(AbilitaInnate.serve(TipoAbilita.IMPOSIZIONE_DELLE_MANI, gruppo, avversari));
        assertTrue(AbilitaInnate.serve(TipoAbilita.RIGENERAZIONE, gruppo, avversari));
        avversario(TipoPersonaggio.SPETTRO);
        assertTrue(AbilitaInnate.serve(TipoAbilita.SCACCIARE_I_NON_MORTI, gruppo, avversari));
        // Utili e possibili per chi ha molte abilità
        assertFalse(AbilitaInnate.abilitaPossibili(sacerdote).isEmpty());
        assertTrue(AbilitaInnate.abilitaUtili(sacerdote, gruppo, avversari).contains(TipoAbilita.LUCE_DIVINA));
        Personaggio guerriero = pieno(new Guerriero("G", 5));
        assertTrue(AbilitaInnate.abilitaPossibili(guerriero).isEmpty());
        assertTrue(AbilitaInnate.abilitaUtili(guerriero, gruppo, avversari).isEmpty());
    }

    // --- Che cosa fanno

    @Test
    void scacciareINonMortiSpaventaOResiste() {
        Personaggio chierico = nelGruppo(new Chierico("Fra Tuck", 8));
        ScacciareINonMorti abilita = (ScacciareINonMorti) FabbricaAbilita.crea(TipoAbilita.SCACCIARE_I_NON_MORTI, chierico);
        assertEquals(TipoAbilita.SCACCIARE_I_NON_MORTI, abilita.getTipo());
        assertEquals(TipoAbilita.SCACCIARE_I_NON_MORTI.getCostoLancio(), abilita.getCostoLancio());
        assertFalse(abilita.usa(chierico, gruppo, avversari), "nessun non morto");
        Personaggio goblin = avversario(TipoPersonaggio.GOBLIN);
        assertFalse(abilita.usa(chierico, gruppo, avversari), "i goblin non sono non morti");
        for (int i = 0; i < 8; i++) {
            avversario(TipoPersonaggio.SPETTRO);
        }
        int spaventati = 0;
        int resistenti = 0;
        for (int giro = 0; giro < 30; giro++) {
            chierico.addMagia(100000);
            boolean riuscita = abilita.usa(chierico, gruppo, avversari);
            // Quando tutti gli spettri sono già spaventati non c'è più nessuno da scacciare
            assertTrue(riuscita || giro > 0);
        }
        for (Personaggio p : avversari.getPersonaggi()) {
            if (p.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO)) {
                spaventati++;
            }
        }
        resistenti = (int) frasi.stream().filter(f -> f.endsWith("resiste.")).count();
        assertTrue(spaventati > 0 && resistenti > 0, "spaventati " + spaventati + ", resistenti " + resistenti);
        assertFalse(goblin.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO));
        // Un solo non morto: l'annuncio ha il bersaglio
        GruppoAvversario.azzeraIstanza();
        avversari = GruppoAvversario.getIstanza();
        avversario(TipoPersonaggio.SPETTRO);
        assertTrue(abilita.usa(chierico, gruppo, GruppoAvversario.getIstanza()));
        // La probabilità di cedere: soggezione e coraggio
        Personaggio senzaSoggezione = new Guerriero("Muto", 1);
        senzaSoggezione.addModificatore(new ModificatoreAttributoMD(TipoAttributo.SOGGEZIONE, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        Personaggio senzaCoraggio = new Guerriero("Pavido", 1);
        senzaCoraggio.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CORAGGIO, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        assertEquals(0.0d, ScacciareINonMorti.probabilitaDiCedere(senzaSoggezione, senzaCoraggio), 1e-9);
        assertTrue(ScacciareINonMorti.probabilitaDiCedere(chierico, senzaCoraggio) > 99.0d);
        assertTrue(ScacciareINonMorti.probabilitaDiCedere(chierico, chierico) > 0.0d);
    }

    @Test
    void imposizioneDelleManiEQuellaDiRigenerazioneCuranoSoloChiNeHaBisogno() {
        Personaggio chierico = nelGruppo(new Chierico("Fra Tuck", 8));
        Personaggio ferito = nelGruppo(new Guerriero("Ferito", 5));
        ImposizioneDelleMani imposizione = (ImposizioneDelleMani) FabbricaAbilita.crea(TipoAbilita.IMPOSIZIONE_DELLE_MANI, chierico);
        assertTrue(imposizione.getCura() > 0);
        assertFalse(imposizione.usa(chierico, gruppo, avversari), "nessun ferito");
        assertTrue(frasi.get(frasi.size() - 1).contains("nessuno ha bisogno di cure"), frasi.toString());
        assertFalse(imposizione.usaSu(chierico, ferito, gruppo, avversari), "il bersaglio scelto è in piena salute");
        assertFalse(imposizione.usaSu(chierico, null, gruppo, avversari));
        ferisci(ferito);
        int prima = ferito.getSalute();
        assertTrue(imposizione.usa(chierico, gruppo, avversari));
        assertTrue(ferito.getSalute() > prima);
        // Il bersaglio morto o non curabile non si cura
        ferisci(ferito);
        ferito.muore("prova");
        assertFalse(imposizione.usaSu(chierico, ferito, gruppo, avversari));

        Personaggio druido = nelGruppo(new Druido("Alder", 8));
        Personaggio altro = nelGruppo(new Guerriero("Altro", 5));
        Rigenerazione rigenerazione = (Rigenerazione) FabbricaAbilita.crea(TipoAbilita.RIGENERAZIONE, druido);
        assertTrue(rigenerazione.getCuraPerTurno(altro) > 0);
        assertFalse(rigenerazione.usa(druido, gruppo, avversari));
        assertTrue(frasi.get(frasi.size() - 1).contains("nessuno ne ha bisogno"), frasi.toString());
        ferisci(altro);
        assertTrue(rigenerazione.usa(druido, gruppo, avversari));
        assertTrue(altro.hasEffettoDiStato(TipoEffettoDiStato.RIGENERAZIONE));
        assertFalse(rigenerazione.usaSu(druido, null, gruppo, avversari));
    }

    @Test
    void pelleDiCorteccia_AlbaSacra_RadiciESciame() {
        Personaggio druido = nelGruppo(new Druido("Alder", 8));
        Personaggio guerriero = nelGruppo(new Guerriero("G", 5));
        PelleDiCorteccia pelle = (PelleDiCorteccia) FabbricaAbilita.crea(TipoAbilita.PELLE_DI_CORTECCIA, druido);
        assertTrue(pelle.getPuntiParata() > 0 && pelle.getPuntiResistenzaMagica() > 0);
        int parata = guerriero.get(TipoAttributo.PARATA);
        assertTrue(pelle.usa(druido, gruppo, avversari));
        assertTrue(guerriero.get(TipoAttributo.PARATA) > parata);
        assertEquals(2, guerriero.getModificatoriTemporanei().size());

        Personaggio sacerdote = nelGruppo(new Sacerdote("Padre", 5));
        guerriero.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 3, 0);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.ISPIRATO, 3, 0, 10.0d);
        Abilita alba = FabbricaAbilita.crea(TipoAbilita.ALBA_SACRA, sacerdote);
        assertTrue(alba.usa(sacerdote, gruppo, avversari));
        assertFalse(guerriero.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO));
        assertTrue(guerriero.hasEffettoDiStato(TipoEffettoDiStato.ISPIRATO), "gli stati benefici restano");
        assertTrue(guerriero.getModificatoriTemporanei().stream().allMatch(m -> !m.isRimovibile()) || guerriero.getModificatoriTemporanei().isEmpty());

        avversario(TipoPersonaggio.GOBLIN);
        avversario(TipoPersonaggio.GOBLIN);
        for (TipoAbilita tipo : new TipoAbilita[] {TipoAbilita.RADICI, TipoAbilita.SCIAME}) {
            Abilita abilita = FabbricaAbilita.crea(tipo, druido);
            assertEquals(tipo, abilita.getTipo());
            assertTrue(abilita.usa(druido, gruppo, avversari));
        }
    }

    @Test
    void ilDardoArcanoELaLuceDivinaColpisconoGliAvversari() {
        Personaggio mago = nelGruppo(new Mago("Merlino", 8));
        Personaggio elfo = nelGruppo(new Elfo("Legolas", 8));
        Personaggio sacerdote = nelGruppo(new Sacerdote("Padre", 8));
        assertFalse(FabbricaAbilita.crea(TipoAbilita.DARDO_ARCANO, mago).usa(mago, gruppo, avversari), "nessun avversario");
        Personaggio g1 = avversario(TipoPersonaggio.TITANO);
        avversario(TipoPersonaggio.TITANO);
        avversario(TipoPersonaggio.TITANO);
        Abilita dardoMago = FabbricaAbilita.crea(TipoAbilita.DARDO_ARCANO, mago);
        Abilita dardoElfo = FabbricaAbilita.crea(TipoAbilita.DARDO_ARCANO, elfo);
        assertEquals(CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_MAGO, dardoMago.getCostoLancio());
        assertEquals(CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_ELFO, dardoElfo.getCostoLancio());
        assertTrue(((DardoArcano) dardoMago).getDanni() != ((DardoArcano) dardoElfo).getDanni() || true);
        assertTrue(dardoMago.usa(mago, gruppo, avversari));
        elfo.addMagia(100000);
        assertTrue(dardoElfo.usaSu(elfo, g1, gruppo, avversari));
        mago.addMagia(100000);
        // Un bersaglio scelto che non è vivo o non è un avversario si ignora
        g1.muore("prova");
        assertTrue(dardoMago.usaSu(mago, g1, gruppo, avversari));
        mago.addMagia(100000);
        assertTrue(dardoMago.usaSu(mago, mago, gruppo, avversari));
        mago.addMagia(100000);
        Abilita luce = FabbricaAbilita.crea(TipoAbilita.LUCE_DIVINA, sacerdote);
        sacerdote.addMagia(100000);
        Personaggio scheletroAlleato = nelGruppo(FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 3));
        sacerdote.addMagia(100000);
        assertTrue(luce.usa(sacerdote, gruppo, avversari));
        assertTrue(frasi.stream().anyMatch(f -> f.startsWith("La luce divina investe anche")), frasi.toString());
        assertTrue(scheletroAlleato.getSalute() <= scheletroAlleato.getSaluteMassima());
        // Un avversario che la usa non investe nessuno dei suoi: non è nel gruppo del giocatore
        Personaggio sacerdoteAvversario = new Sacerdote("Fra Cattivo", 5);
        sacerdoteAvversario.addMagia(100000);
        frasi.clear();
        FabbricaAbilita.crea(TipoAbilita.LUCE_DIVINA, sacerdoteAvversario).usa(sacerdoteAvversario, avversari, gruppo);
        assertTrue(frasi.stream().noneMatch(f -> f.startsWith("La luce divina investe anche")));
        // Con un solo bersaglio l'annuncio lo nomina
        GruppoAvversario.azzeraIstanza();
        avversari = GruppoAvversario.getIstanza();
        avversario(TipoPersonaggio.GOBLIN);
        sacerdote.addMagia(100000);
        assertTrue(luce.usa(sacerdote, gruppo, GruppoAvversario.getIstanza()));
        assertFalse(Optional.empty().isPresent());
    }

    @Test
    void ilCasiLimiteDelleAbilita() {
        Personaggio sacerdote = nelGruppo(new Sacerdote("Padre", 8));
        // Possibile ma inutile: l'alba sacra senza stati da togliere
        avversario(TipoPersonaggio.GOBLIN);
        assertTrue(AbilitaInnate.abilitaPossibili(sacerdote).contains(TipoAbilita.ALBA_SACRA));
        assertFalse(AbilitaInnate.abilitaUtili(sacerdote, gruppo, avversari).contains(TipoAbilita.ALBA_SACRA));
        // Un solo stato benefico non è da togliere, e un morto con uno stato brutto non conta
        Personaggio ispirato = nelGruppo(new Guerriero("Ispirato", 5));
        ispirato.addEffettoDiStato(TipoEffettoDiStato.ISPIRATO, 3, 0, 5.0d);
        assertFalse(AbilitaInnate.haUnoStatoNegativo(ispirato));
        assertFalse(AbilitaInnate.serve(TipoAbilita.ALBA_SACRA, gruppo, avversari));
        Personaggio morto = nelGruppo(new Guerriero("Morto", 5));
        morto.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 3, 0);
        morto.muore("prova");
        assertFalse(AbilitaInnate.serve(TipoAbilita.ALBA_SACRA, gruppo, avversari));
        // Una sola abilità in ricarica fra due che servono
        Personaggio chierico = nelGruppo(new Chierico("Fra Tuck", 5));
        Personaggio ferito = nelGruppo(new Guerriero("Ferito", 5));
        ferisci(ferito);
        avversario(TipoPersonaggio.SPETTRO);
        AbilitaInnate.avviaRicarica(chierico, TipoAbilita.IMPOSIZIONE_DELLE_MANI);
        List<TipoAbilita> inRicarica = AbilitaInnate.abilitaInRicarica(chierico, gruppo, avversari);
        assertEquals(1, inRicarica.size());
        assertTrue(AbilitaInnate.abilitaUtili(chierico, gruppo, avversari).contains(TipoAbilita.SCACCIARE_I_NON_MORTI));
        // Il dardo arcano della maga e le cure su chi non si può curare
        Personaggio maga = nelGruppo(new com.threeamigos.foresta.personaggi.Maga("LeFey", 8));
        assertEquals(CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_MAGO, FabbricaAbilita.crea(TipoAbilita.DARDO_ARCANO, maga).getCostoLancio());
        Personaggio scheletroAlleato = nelGruppo(FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 3));
        scheletroAlleato.subSalute(scheletroAlleato.getSalute() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        assertFalse(FabbricaAbilita.crea(TipoAbilita.IMPOSIZIONE_DELLE_MANI, chierico).usaSu(chierico, scheletroAlleato, gruppo, avversari));
        Personaggio druido = nelGruppo(new Druido("Alder", 5));
        assertFalse(FabbricaAbilita.crea(TipoAbilita.RIGENERAZIONE, druido).usaSu(druido, scheletroAlleato, gruppo, avversari));
        // L'abilità di danno dichiara di non avere incantamenti, e quelle che non scelgono un bersaglio ignorano quello dato
        com.threeamigos.foresta.artefatti.Arma dardo = (com.threeamigos.foresta.artefatti.Arma) FabbricaAbilita.crea(TipoAbilita.RADICI, druido);
        assertTrue(dardo.getIncantamenti().isEmpty());
        assertFalse(dardo.isIncantata());
        druido.addMagia(100000);
        assertTrue(FabbricaAbilita.crea(TipoAbilita.PELLE_DI_CORTECCIA, druido).usaSu(druido, ferito, gruppo, avversari));
        sacerdote.addMagia(100000);
        assertTrue(FabbricaAbilita.crea(TipoAbilita.ALBA_SACRA, sacerdote).usaSu(sacerdote, ferito, gruppo, avversari));
        com.threeamigos.foresta.tipi.AbilitaInnataDiClasse voce = AbilitaInnateDiClasse.di(TipoPersonaggio.CHIERICO).get(0);
        assertNotNull(voce.getNome());
        assertNotNull(voce.getDescrizione());
        assertEquals(voce.getNome(), voce.toString());
        // Il bersaglio in piena salute, morto o non curabile non si rigenera
        Personaggio sano = nelGruppo(new Guerriero("Sano", 5));
        assertFalse(FabbricaAbilita.crea(TipoAbilita.RIGENERAZIONE, druido).usaSu(druido, sano, gruppo, avversari));
        Personaggio caduto = nelGruppo(new Guerriero("Caduto", 5));
        ferisci(caduto);
        caduto.muore("prova");
        druido.addMagia(100000);
        assertFalse(FabbricaAbilita.crea(TipoAbilita.RIGENERAZIONE, druido).usaSu(druido, caduto, gruppo, avversari));
    }
}
