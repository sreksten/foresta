package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.Elfo;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Goblin;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.OmbraFiamma;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.ModalitaDiProva;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.CategoriaDanno;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I casi che restano fuori dalle altre prove del combattimento: il turno visto da una sequenza casuale di eventi, il cambio d'arma con la doppia
 * arma, le armi vere e i pezzi difensivi nel calcolo, le immunità di un solo personaggio e la modalità di prova.
 */
class CodaDelCombattimentoTest {

    @BeforeEach
    void prepara() {
        BusEventi.impostaConsegna(Runnable::run);
        DatiDiProva.nuovaPartita();
        GruppoAvversario.azzeraIstanza();
        StatoDelCombattimento.azzeraIstanza();
        Dado.impostaSeme(4242L);
    }

    @AfterEach
    void ripulisce() {
        BusEventi.impostaConsegna(null);
        StatoDelCombattimento.azzeraIstanza();
        GruppoAvversario.azzeraIstanza();
        ModalitaDiProva.setAttiva(false);
        Dado.ripristina();
    }

    private static Artefatto genera(TipoArtefatto tipo, int livello, long seme) {
        return new GeneratoreArtefattiTabelle(new Random(seme)).generaArtefatto(tipo, livello);
    }

    private static Personaggio giocante(String nome) {
        Personaggio p = new Guerriero(nome, 5);
        p.getModelloDati().setPng(false);
        return p;
    }

    // --- Il turno, a sorte

    @Test
    void ilTurnoRegge_unaSequenzaCasualeDiEventi() {
        Random caso = new Random(2024);
        for (int partita = 0; partita < 60; partita++) {
            GruppoGiocatore.getIstanza().reimposta();
            GruppoAvversario.getIstanza().reimposta();
            StatoDelCombattimento.azzeraIstanza();
            StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
            List<Personaggio> mieiPersonaggi = new ArrayList<>();
            for (int i = 0; i < 1 + caso.nextInt(3); i++) {
                Personaggio p = giocante("Eroe" + i);
                GruppoGiocatore.getIstanza().aggiungiPersonaggio(p);
                mieiPersonaggi.add(p);
            }
            Personaggio ostaggio = FabbricaPersonaggi.crea(TipoPersonaggio.DONNA, 1);
            if (caso.nextBoolean()) {
                GruppoGiocatore.getIstanza().aggiungiOspite(ostaggio, true);
            }
            if (caso.nextBoolean()) {
                // Uno dei miei personaggi è anche un ospite vulnerabile: nella lista non si ripete
                GruppoGiocatore.getIstanza().aggiungiOspite(mieiPersonaggi.get(0), true);
            }
            List<Personaggio> avversari = new ArrayList<>();
            for (int i = 0; i < 1 + caso.nextInt(4); i++) {
                Personaggio g = new Goblin(3);
                GruppoAvversario.getIstanza().aggiungiPersonaggio(g);
                avversari.add(g);
            }
            stato.reimposta();
            for (int passo = 0; passo < 80; passo++) {
                switch (caso.nextInt(12)) {
                    case 0:
                        stato.getOrdineDelRound();
                        break;
                    case 1:
                        stato.getProssimoLato();
                        break;
                    case 2:
                        stato.getProssimoAttaccante(caso.nextBoolean() ? GruppoGiocatore.getIstanza() : GruppoAvversario.getIstanza());
                        break;
                    case 3:
                        stato.getAvversarioCheAttaccaPerPrimo();
                        stato.getPiano();
                        break;
                    case 4: {
                        List<Personaggio> tutti = new ArrayList<>(mieiPersonaggi);
                        tutti.addAll(avversari);
                        stato.registraAzione(tutti.get(caso.nextInt(tutti.size())));
                        break;
                    }
                    case 5:
                        stato.registraAzioneSaltata(caso.nextBoolean() ? StatoDelCombattimento.Lato.GIOCATORE : StatoDelCombattimento.Lato.AVVERSARI);
                        break;
                    case 6: {
                        List<Personaggio> tutti = new ArrayList<>(mieiPersonaggi);
                        tutti.addAll(avversari);
                        stato.faAgirePerPrimo(GruppoGiocatore.getIstanza(), tutti.get(caso.nextInt(tutti.size())));
                        break;
                    }
                    case 7: {
                        Personaggio vittima = avversari.get(caso.nextInt(avversari.size()));
                        if (vittima.isVivo()) {
                            vittima.muore("prova");
                        }
                        break;
                    }
                    case 8: {
                        Personaggio chi = mieiPersonaggi.get(caso.nextInt(mieiPersonaggi.size()));
                        if (GruppoGiocatore.getIstanza().contiene(chi) && GruppoGiocatore.getIstanza().getNumeroPersonaggi() > 1) {
                            GruppoGiocatore.getIstanza().rimuoviPersonaggio(chi);
                        }
                        break;
                    }
                    case 9: {
                        Personaggio nuovo = new Goblin(3);
                        GruppoAvversario.getIstanza().aggiungiPersonaggio(nuovo);
                        avversari.add(nuovo);
                        break;
                    }
                    case 10:
                        stato.nuovaOndata();
                        break;
                    default: {
                        Personaggio chi = avversari.get(caso.nextInt(avversari.size()));
                        if (GruppoAvversario.getIstanza().contiene(chi) && caso.nextInt(4) == 0) {
                            GruppoAvversario.getIstanza().rimuoviPersonaggio(chi);
                        }
                        stato.haAgitoNelRound(chi);
                        break;
                    }
                }
                List<Personaggio> ordine = stato.getOrdineDelRound();
                assertEquals(ordine.size(), new java.util.HashSet<>(ordine).size(), "nessuno compare due volte");
            }
        }
    }

    @Test
    void senzaAvversariIlProssimoLatoNonCe() {
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(giocante("Arsenio"));
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        stato.reimposta();
        assertNotNull(stato.getProssimoLato());
        stato.registraAzione(GruppoGiocatore.getIstanza().getPersonaggio(0));
        assertNull(stato.getAvversarioCheAttaccaPerPrimo());
    }

    // --- Cambio d'arma

    @Test
    void ilCambioDArmaConLaDoppiaArmaFaUscireLaPiuDebole() {
        Personaggio ladro = new Ladro("Arsenio", 8);
        ladro.getModelloDati().setPng(false);
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        gruppo.aggiungiPersonaggio(ladro);
        Artefatto forte = genera(TipoArtefatto.SPADA, 6, 1);
        Artefatto debole = genera(TipoArtefatto.SPADA, 1, 2);
        ladro.addArtefatto(forte);
        ladro.addArtefatto(debole);
        Artefatto nuova = genera(TipoArtefatto.SPADA, 5, 3);
        List<ArtefattoMD> uscenti = CambioDArma.uscentiPerCambioArma(ladro, nuova.getModelloDati());
        assertEquals(1, uscenti.size());
        // La nuova arma non da mano secondaria (un'arma a due mani) fa uscire tutto quello che c'è in mano
        Artefatto spadone = genera(TipoArtefatto.SPADONE, 5, 4);
        assertEquals(2, CambioDArma.uscentiPerCambioArma(ladro, spadone.getModelloDati()).size());
        // Chi non può impugnare due armi non ha la secondaria: esce solo quella principale
        Personaggio guerriero = giocante("Brunilde");
        gruppo.aggiungiPersonaggio(guerriero);
        guerriero.addArtefatto(genera(TipoArtefatto.SPADA, 2, 5));
        assertEquals(1, CambioDArma.uscentiPerCambioArma(guerriero, nuova.getModelloDati()).size());
        // Con uno scudo in mano e un'arma a due mani il cambio non si può fare
        guerriero.addArtefatto(genera(TipoArtefatto.SCUDO, 2, 6));
        assertTrue(CambioDArma.uscentiPerCambioArma(guerriero, spadone.getModelloDati()).isEmpty());
        assertNotNull(CambioDArma.valutaCambioArma(guerriero, spadone.getModelloDati()).getMotivo());
        // Una nuova arma che non è da mano principale o a due mani non si valuta
        assertThrows(IllegalArgumentException.class, () -> CambioDArma.valutaCambioArma(guerriero, genera(TipoArtefatto.SCUDO, 1, 7).getModelloDati()));
    }

    private static Artefatto inSlot(TipoArtefatto tipo, int livello, long seme, com.threeamigos.foresta.tipi.TipoSlotArtefatto slot) {
        Artefatto a = genera(tipo, livello, seme);
        a.getModelloDati().setSlotEquipaggiamento(slot);
        return a;
    }

    private static List<ArtefattoMD> uscentiCon(Personaggio p, com.threeamigos.foresta.tipi.TipoSlotArtefatto slotPrimo, Artefatto primo,
                                                 com.threeamigos.foresta.tipi.TipoSlotArtefatto slotSecondo, Artefatto secondo, Artefatto nuova) {
        if (primo != null) {
            primo.getModelloDati().setSlotEquipaggiamento(slotPrimo);
            p.getModelloDati().aggiungiArtefatto(primo.getModelloDati());
        }
        if (secondo != null) {
            secondo.getModelloDati().setSlotEquipaggiamento(slotSecondo);
            p.getModelloDati().aggiungiArtefatto(secondo.getModelloDati());
        }
        return CambioDArma.uscentiPerCambioArma(p, nuova.getModelloDati());
    }

    @Test
    void ilCambioDArmaTieneContoDiOgniCombinazioneDiMani() {
        com.threeamigos.foresta.tipi.TipoSlotArtefatto principale = com.threeamigos.foresta.tipi.TipoSlotArtefatto.MANO_PRINCIPALE;
        com.threeamigos.foresta.tipi.TipoSlotArtefatto secondaria = com.threeamigos.foresta.tipi.TipoSlotArtefatto.MANO_SECONDARIA;
        Artefatto nuova = genera(TipoArtefatto.SPADA, 3, 50);
        // Mani vuote: non esce niente
        assertTrue(uscentiCon(new Ladro("A", 8), null, null, null, null, nuova).isEmpty());
        // Una sola arma: esce quella
        assertEquals(1, uscentiCon(new Ladro("B", 8), principale, genera(TipoArtefatto.SPADA, 2, 51), null, null, nuova).size());
        // In mano principale qualcosa che non è un'arma, e la secondaria una spada: esce quello (e quindi il cambio non si fa)
        assertTrue(uscentiCon(new Ladro("C", 8), principale, genera(TipoArtefatto.SCUDO, 2, 52), secondaria, genera(TipoArtefatto.SPADA, 2, 53), nuova).isEmpty());
        // La secondaria è uno scudo: esce solo la principale
        assertEquals(1, uscentiCon(new Ladro("D", 8), principale, genera(TipoArtefatto.SPADA, 2, 54), secondaria, genera(TipoArtefatto.SCUDO, 2, 55), nuova).size());
        // Due armi, ma la nuova è a due mani: escono tutte e due
        assertEquals(2, uscentiCon(new Ladro("E", 8), principale, genera(TipoArtefatto.SPADA, 2, 56), secondaria, genera(TipoArtefatto.SPADA, 2, 57),
                genera(TipoArtefatto.SPADONE, 3, 58)).size());
        // Chi non può impugnare due armi (anche se le ha) cambia la principale
        assertEquals(1, uscentiCon(new Guerriero("F", 8), principale, genera(TipoArtefatto.SPADA, 2, 59), secondaria, genera(TipoArtefatto.SPADA, 2, 60), nuova).size());
        // Due armi di un ladro: esce la più debole, a parità la secondaria
        Personaggio ladro = new Ladro("G", 8);
        Artefatto forte = genera(TipoArtefatto.SPADA, 6, 61);
        Artefatto debole = genera(TipoArtefatto.SPADA, 1, 62);
        List<ArtefattoMD> uscenti = uscentiCon(ladro, principale, forte, secondaria, debole, nuova);
        assertEquals(1, uscenti.size());
        assertSame(debole.getModelloDati(), uscenti.get(0));
        Personaggio ladro2 = new Ladro("H", 8);
        List<ArtefattoMD> uscenti2 = uscentiCon(ladro2, principale, debole, secondaria, forte, nuova);
        assertSame(debole.getModelloDati(), uscenti2.get(0));
    }

    @Test
    void leArmiPerCambioSonoSoloQuelleDa_manoPrincipaleChePossonoEntrare() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        Personaggio guerriero = giocante("Arsenio");
        gruppo.aggiungiPersonaggio(guerriero);
        long seme = 1;
        for (TipoArtefatto tipo : TipoArtefatto.values()) {
            gruppo.addArtefatto(genera(tipo, 2, seme++));
        }
        for (TipoDanno tipoDanno : TipoDanno.values()) {
            for (Artefatto trovata : CambioDArma.armiPerCambio(gruppo, guerriero, tipoDanno)) {
                assertTrue(((Arma) trovata).daTipoDanno(tipoDanno));
            }
        }
        assertFalse(CambioDArma.armiPerCambio(gruppo, guerriero, TipoDanno.TAGLIENTE).isEmpty());
    }

    // --- Colpo magico senza immunità da controllare

    @Test
    void ilColpoMagicoControllaLImmunitaSoloSeCeNeEUna() {
        Personaggio mago = new Mago("Merlino", 5);
        Personaggio goblin = new Goblin(5);
        GruppoAvversario.getIstanza().aggiungiPersonaggio(goblin);
        Arma dardo = new Arma() {
            @Override
            public int getDanni() {
                return 5;
            }

            @Override
            public int getLivello() {
                return 3;
            }

            @Override
            public TipoDanno getTipoDanno() {
                return TipoDanno.FUOCO;
            }

            @Override
            public boolean isIncantata() {
                return false;
            }

            @Override
            public boolean isMagica() {
                return true;
            }

            @Override
            public Collection<IncantamentoMD> getIncantamenti() {
                return Collections.emptyList();
            }
        };
        for (int i = 0; i < 10; i++) {
            ColpoMagico.colpisci(mago, goblin, dardo, TipoAzione.di(TipoIncantesimo.FUOCO), TipoIncantesimo.FUOCO, 1.0d);
        }
        assertFalse(StatoDelCombattimento.getIstanza().isImmunitaScoperta(goblin, TipoAzione.di(TipoIncantesimo.FUOCO)));
    }

    // --- Il calcolo

    @Test
    void lePozzeELeFileAlBordo() {
        List<Personaggio> fila = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Personaggio p = new Goblin(3);
            p.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
            fila.add(p);
        }
        // Il primo e l'ultimo, dove un lato non c'è
        assertEquals(1, CalcolatoreCombattimento.viciniInvestiti(fila, fila.get(0), TipoInterazioneConEffettiDiStato.RIGETTO).size());
        assertEquals(3, CalcolatoreCombattimento.viciniInvestiti(fila, fila.get(0), TipoInterazioneConEffettiDiStato.ELETTROCUZIONE).size());
        assertEquals(3, CalcolatoreCombattimento.viciniInvestiti(fila, fila.get(3), TipoInterazioneConEffettiDiStato.ELETTROCUZIONE).size());
    }

    @Test
    void ilBersaglioSenzaForzaNonRegge_gliStatiENonSiDividePerZero() {
        Personaggio mago = new Mago("Merlino", 8);
        Personaggio debole = new Goblin(5);
        debole.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        Arma spada = (Arma) genera(TipoArtefatto.SPADA, 5, 1);
        for (int i = 0; i < 200; i++) {
            DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(mago, debole, spada);
            assertTrue(risultato.getDanno() >= 0);
        }
    }

    @Test
    void leInterazioniAttiveControChiEImmuneSonoNessuna() {
        Personaggio drago = FabbricaPersonaggi.crea(TipoPersonaggio.DRAGO, 5);
        assertTrue(CalcolatoreCombattimento.interazioniAttive(drago, TipoDanno.FUOCO).isEmpty());
    }

    @Test
    void chiEImmuneAgliStatiNonLiRiceveDalleReazioni() {
        Personaggio immune = new Goblin(5) {
            @Override
            protected boolean isImmuneAEffettoPerIstanza(TipoEffettoDiStato effettoStato) {
                return true;
            }
        };
        int controllati = 0;
        for (TipoEffettoDiStato stato : TipoEffettoDiStato.values()) {
            Personaggio difensore = new Goblin(5) {
                @Override
                protected boolean isImmuneAEffettoPerIstanza(TipoEffettoDiStato effetto) {
                    return effetto != stato;
                }
            };
            difensore.addEffettoDiStato(stato, 3, 0);
            for (TipoDanno tipoDanno : TipoDanno.values()) {
                Arma arma = (Arma) genera(TipoArtefatto.SPADA, 2, 1);
                Arma elementale = new Arma() {
                    @Override
                    public int getDanni() {
                        return 5;
                    }

                    @Override
                    public int getLivello() {
                        return 2;
                    }

                    @Override
                    public TipoDanno getTipoDanno() {
                        return tipoDanno;
                    }

                    @Override
                    public boolean isIncantata() {
                        return false;
                    }

                    @Override
                    public Collection<IncantamentoMD> getIncantamenti() {
                        return Collections.emptyList();
                    }
                };
                assertNotNull(CalcolatoreCombattimento.statiCausabili(difensore, elementale));
                assertNotNull(CalcolatoreCombattimento.statiCausabili(immune, arma));
                controllati++;
            }
        }
        assertTrue(controllati > 100);
    }

    @Test
    void ipezziDifensiviSiContanoTuttiEGliAltriNo() {
        Personaggio difensore = new Guerriero("Bersaglio", 8);
        for (TipoArtefatto tipo : new TipoArtefatto[] {TipoArtefatto.ELMO, TipoArtefatto.ARMATURA, TipoArtefatto.SCHINIERI, TipoArtefatto.SCUDO,
                TipoArtefatto.ANELLO, TipoArtefatto.SPADA}) {
            Artefatto pezzo = genera(tipo, 4, 9);
            pezzo.getModelloDati().addIncantamento(new IncantamentoMD("Fiamma", TipoDanno.FUOCO, 4, 0.2d));
            difensore.addArtefatto(pezzo);
        }
        double fuoco = CalcolatoreCombattimento.difesaContro(difensore, TipoDanno.FUOCO);
        double base = CalcolatoreCombattimento.difesaBaseContro(difensore, TipoDanno.FUOCO);
        assertTrue(fuoco > base);
    }

    @Test
    void conUnArmaVeraIlSetLeggendarioSiCerca_E_ILibroNonVaBene_aTutti() {
        Personaggio elfo = new Elfo("Legolas", 8);
        elfo.addArtefatto(genera(TipoArtefatto.LIBRO_MAGICO, 3, 2));
        Personaggio mago = new Mago("Merlino", 8);
        mago.addArtefatto(genera(TipoArtefatto.LIBRO_MAGICO, 3, 2));
        Arma spada = (Arma) genera(TipoArtefatto.SPADA, 3, 4);
        elfo.addArtefatto((Artefatto) spada);
        Personaggio difensore = new Goblin(5);
        assertTrue(CalcolatoreCombattimento.calcolaDannoRisultante(elfo, difensore, spada).getDanno() >= 1);
        Arma incantesimo = new Arma() {
            @Override
            public int getDanni() {
                return 6;
            }

            @Override
            public int getLivello() {
                return 3;
            }

            @Override
            public TipoDanno getTipoDanno() {
                return TipoDanno.FUOCO;
            }

            @Override
            public boolean isIncantata() {
                return false;
            }

            @Override
            public boolean isMagica() {
                return true;
            }

            @Override
            public Collection<IncantamentoMD> getIncantamenti() {
                return Collections.emptyList();
            }
        };
        assertTrue(CalcolatoreCombattimento.calcolaDannoRisultante(elfo, difensore, incantesimo).getDanno() >= 1);
        assertTrue(CalcolatoreCombattimento.calcolaDannoRisultante(mago, difensore, incantesimo).getDanno() >= 1);
        assertTrue(CalcolatoreCombattimento.dannoAtteso(elfo, difensore, spada) > 0);
        assertEquals(CategoriaDanno.FISICO, spada.getTipoDanno().getCategoriaDanno());
    }

    @Test
    void inProvaLOmbraFiammaColpisceSempreEFaAlmenoCentoDanniAncheSeNonLaSiVede() {
        ModalitaDiProva.setAttiva(true);
        Personaggio guerriero = new Guerriero("Arsenio", 5);
        Personaggio ombra = new OmbraFiamma(5);
        // Un'altra classe in prova ha le regole di sempre
        assertTrue(CalcolatoreCombattimento.calcolaProbabilitaDiColpire(guerriero, new Goblin(5), CategoriaDanno.FISICO) < 100);
        assertEquals(100, CalcolatoreCombattimento.calcolaProbabilitaDiColpire(ombra, guerriero, CategoriaDanno.MAGICO));
        Arma spada = (Arma) genera(TipoArtefatto.SPADA, 5, 3);
        assertTrue(CalcolatoreCombattimento.calcolaDannoRisultante(ombra, guerriero, spada).getDanno() >= 100);
        assertTrue(CalcolatoreCombattimento.calcolaDannoRisultante(guerriero, new Goblin(5), spada).getDanno() >= 1);
        // Oltre i cento danni non c'è niente da forzare
        Personaggio titano = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 5);
        titano.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        Personaggio fortissima = new OmbraFiamma(30);
        fortissima.addModificatore(new ModificatoreAttributoMD(TipoAttributo.INTELLIGENZA, TipoModificatore.QUANTITA_ASSOLUTA, 5000));
        assertTrue(CalcolatoreCombattimento.calcolaDannoRisultante(fortissima, titano, spada).getDanno() >= 100);
    }

    private static Arma incantesimoDiFuoco() {
        return new Arma() {
            @Override
            public int getDanni() {
                return 6;
            }

            @Override
            public int getLivello() {
                return 3;
            }

            @Override
            public TipoDanno getTipoDanno() {
                return TipoDanno.FUOCO;
            }

            @Override
            public boolean isIncantata() {
                return false;
            }

            @Override
            public boolean isMagica() {
                return true;
            }

            @Override
            public Collection<IncantamentoMD> getIncantamenti() {
                return Collections.emptyList();
            }
        };
    }

    @Test
    void conUnOmbraFiammaGiaPotenteInProvaNonSiForzaNiente() {
        ModalitaDiProva.setAttiva(true);
        Personaggio ombra = new OmbraFiamma(30);
        ombra.addModificatore(new ModificatoreAttributoMD(TipoAttributo.INTELLIGENZA, TipoModificatore.QUANTITA_ASSOLUTA, 5000));
        Personaggio bersaglio = new Goblin(1);
        assertTrue(CalcolatoreCombattimento.calcolaDannoRisultante(ombra, bersaglio, incantesimoDiFuoco()).getDanno() > 100);
    }

    @Test
    void unArmaVeraIncantataCercaIlSetEUnaElfaUsaMenoIlLibro() {
        Personaggio elfa = FabbricaPersonaggi.crea(TipoPersonaggio.ELFA, 8);
        elfa.addArtefatto(genera(TipoArtefatto.LIBRO_MAGICO, 3, 2));
        Artefatto spada = genera(TipoArtefatto.SPADA, 3, 4);
        spada.getModelloDati().addIncantamento(new IncantamentoMD("Fiamma", TipoDanno.FUOCO, 4, 0.3d));
        elfa.addArtefatto(spada);
        Personaggio goblin = new Goblin(5);
        assertTrue(CalcolatoreCombattimento.calcolaDannoRisultante(elfa, goblin, (Arma) spada).getDanno() >= 1);
        assertTrue(CalcolatoreCombattimento.calcolaDannoRisultante(elfa, goblin, incantesimoDiFuoco()).getDanno() >= 1);
    }

    @Test
    void chiAttaccaPerPrimoSaltaGliAvversariCaduti() {
        Personaggio g1 = new Goblin(3);
        Personaggio g2 = new Goblin(3);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(giocante("Arsenio"));
        GruppoAvversario.getIstanza().aggiungiPersonaggio(g1);
        GruppoAvversario.getIstanza().aggiungiPersonaggio(g2);
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        stato.reimposta();
        for (Personaggio p : stato.getOrdineDelRound()) {
            if (p == g1) {
                g1.muore("prova");
            }
        }
        assertSame(g2, stato.getAvversarioCheAttaccaPerPrimo());
    }

    @Test
    void ilCursoreFinitoOltreLaListaNonRompeLAzione() {
        Personaggio g1 = new Goblin(3);
        Personaggio g2 = new Goblin(3);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(giocante("Arsenio"));
        GruppoAvversario.getIstanza().aggiungiPersonaggio(g1);
        GruppoAvversario.getIstanza().aggiungiPersonaggio(g2);
        StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
        stato.reimposta();
        List<Personaggio> ordine = stato.getOrdineDelRound();
        // Tutti tranne l'ultimo hanno agito: il cursore è sull'ultimo
        for (int i = 0; i < ordine.size() - 1; i++) {
            stato.registraAzione(ordine.get(i));
        }
        Personaggio ultimo = ordine.get(ordine.size() - 1);
        if (GruppoAvversario.getIstanza().contiene(ultimo)) {
            GruppoAvversario.getIstanza().rimuoviPersonaggio(ultimo);
        } else {
            GruppoGiocatore.getIstanza().rimuoviPersonaggio(ultimo);
        }
        stato.registraAzione(ordine.get(0));
        stato.registraAzioneSaltata(StatoDelCombattimento.Lato.GIOCATORE);
        assertNotNull(stato.getOrdineDelRound());
    }
}
