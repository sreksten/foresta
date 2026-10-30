package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAzioneInCombattimento;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tattiche.valutazione.Requisiti;
import com.threeamigos.foresta.tipi.PortataAzione;
import com.threeamigos.foresta.tipi.PortataIncantesimo;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli incantesimi GLOBALI nelle tattiche: nessun bersaglio, un requisito che guarda se servono, un esecutore che li lancia sul gruppo giusto.
 */
class IncantesimiGlobaliNelleTatticheTest {

    private static Gruppo gruppo(boolean delGiocatore, Personaggio... membri) {
        return new Gruppo() {
            {
                personaggi.addAll(Arrays.asList(membri));
                capo = membri[0];
            }

            @Override
            public boolean isGruppoGiocatore() {
                return delGiocatore;
            }
        };
    }

    private final List<InternoAzioneInCombattimento> annunci = new ArrayList<>();
    private final List<String> uccisioni = new ArrayList<>();
    private Personaggio mago;
    private Personaggio guerriero;
    private Personaggio goblin;
    private Gruppo miei;
    private Gruppo mostri;

    @BeforeEach
    void prepara() {
        DatiDiProvaDelleTattiche.nuovaPartita();
        StatoDelCombattimento.getIstanza().reimposta();
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        BusEventi.iscriviti(InternoAzioneInCombattimento.class, annunci::add);
        Dado.impostaSeme(3L);
        mago = FabbricaPersonaggi.crea(TipoPersonaggio.MAGO, 5);
        mago.addMagia(mago.getMagiaMassima());
        guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 5);
        goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        miei = gruppo(true, mago, guerriero);
        mostri = gruppo(false, goblin, FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 5));
    }

    @AfterEach
    void ripristina() {
        BusEventi.impostaConsegna(null);
        BusEventi.azzera();
        Dado.ripristina();
    }

    private static List<TipoIncantesimo> globali() {
        List<TipoIncantesimo> globali = new ArrayList<>();
        for (TipoIncantesimo classe : TipoIncantesimo.values()) {
            if (classe.getPortata() == PortataIncantesimo.GLOBALE) {
                globali.add(classe);
            }
        }
        return globali;
    }

    @Test
    void sonoAzioniSenzaBersaglioCheNonSiScelgonoPerCriterio() {
        assertFalse(globali().isEmpty());
        for (TipoIncantesimo classe : globali()) {
            TipoAzione azione = TipoAzione.di(classe);
            assertNotNull(azione, classe.name());
            PortataAzione portata = PortataAzione.di(azione);
            assertTrue(portata == PortataAzione.GRUPPO_AVVERSARI || portata == PortataAzione.GRUPPO_ALLEATI, classe.name());
            // Non sono "di danno" per la scelta dell'incantesimo: non hanno un bersaglio su cui confrontarsi con le armi
            assertFalse(AzioneMD.di(azione).isSoloSePiuForteDellAttacco(), classe.name());
        }
    }

    @Test
    void servePergamenaEMagia() {
        for (TipoIncantesimo classe : globali()) {
            String senza = Requisiti.motivoDelNo(AzioneMD.di(TipoAzione.di(classe)), mago, null, ContestoTattico.per(mago, miei, mostri));
            assertNotNull(senza, classe.name());
            assertTrue(senza.contains("manca la pergamena"), senza);
            miei.addIncantesimi(classe, 1);
            String con = Requisiti.motivoDelNo(AzioneMD.di(TipoAzione.di(classe)), mago, null, ContestoTattico.per(mago, miei, mostri));
            assertNull(con, classe.name() + ": " + con);
        }
    }

    @Test
    void sePerNessunoCambiaQualcosaNonSiLancia() {
        // Il campo è vuoto: chi fosse da colpire o da aiutare non c'è
        Gruppo vuoti = gruppo(false, goblin);
        goblin.subSalute(goblin.getSalute(), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        guerriero.subSalute(guerriero.getSalute(), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        Gruppo soloCaduti = gruppo(true, mago, guerriero);
        for (TipoIncantesimo classe : globali()) {
            soloCaduti.addIncantesimi(classe, 1);
            String motivo = Requisiti.motivoDelNo(AzioneMD.di(TipoAzione.di(classe)), mago, null, ContestoTattico.per(mago, soloCaduti, vuoti));
            if (classe.getCategoriaIncantesimo() == com.threeamigos.foresta.tipi.CategoriaIncantesimo.MALEFICO) {
                assertNotNull(motivo, classe.name());
                assertTrue(motivo.contains("non cambierebbe niente"), motivo);
            }
        }
    }

    @Test
    void siLancianoSenzaBersaglioEConsumanoLaPergamena() {
        for (TipoIncantesimo classe : globali()) {
            miei.addIncantesimi(classe, 1);
            int magiaPrima = mago.getMagia();
            annunci.clear();
            new EsecutoreTattiche(miei, mostri, StatoDelCombattimento.getIstanza(), (uccisore, vittima) -> uccisioni.add(vittima.getNome()))
                    .esegui(mago, new Decisione(0, AzioneMD.di(TipoAzione.di(classe)), TipoAzione.di(classe), classe, null, Collections.emptyList()));
            assertEquals(0, miei.getIncantesimi(classe), classe.name());
            assertEquals(1, annunci.size(), classe.name());
            assertNull(annunci.get(0).getBersaglio(), classe.name());
            assertEquals(magiaPrima - classe.getCostoLancio(), mago.getMagia(), classe.name());
        }
    }
}
