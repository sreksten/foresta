package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAzioneInCombattimento;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoDanno;
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
 * Ogni azione che una decisione può chiedere, eseguita dall'esecutore: il valutatore ha già scelto, qui si fa quello che ha scelto.
 */
class EsecutoreTatticheAzioniTest {

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

    private final List<String> uccisioni = new ArrayList<>();
    private final List<InternoAzioneInCombattimento> annunci = new ArrayList<>();
    private Personaggio goblin;
    private Personaggio strega;
    private Gruppo mostri;

    @BeforeEach
    void prepara() {
        DatiDiProvaDelleTattiche.nuovaPartita();
        StatoDelCombattimento.getIstanza().reimposta();
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        BusEventi.iscriviti(InternoAzioneInCombattimento.class, annunci::add);
        goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        strega = FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 1);
        mostri = gruppo(false, goblin, strega);
    }

    @AfterEach
    void ripristina() {
        BusEventi.impostaConsegna(null);
        BusEventi.azzera();
        Dado.ripristina();
    }

    private static Decisione decisione(TipoAzione tipo, TipoIncantesimo incantesimo, Personaggio bersaglio) {
        return new Decisione(0, AzioneMD.di(tipo), tipo, incantesimo, bersaglio, Collections.emptyList());
    }

    private EsecutoreTattiche esecutoreDi(Gruppo miei) {
        return new EsecutoreTattiche(miei, mostri, StatoDelCombattimento.getIstanza(), (uccisore, vittima) -> uccisioni.add(uccisore.getNome() + ">" + vittima.getNome()));
    }

    private static Personaggio pronto(TipoPersonaggio classe) {
        Personaggio personaggio = FabbricaPersonaggi.crea(classe, 5);
        personaggio.addMagia(personaggio.getMagiaMassima());
        return personaggio;
    }

    private static void feriscilo(Personaggio personaggio) {
        personaggio.subSalute(personaggio.getSalute() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
    }

    @Test
    void leAbilitaInnateDelMagoEDelSacerdoteSiUsanoSuiNemici() {
        Personaggio mago = pronto(TipoPersonaggio.MAGO);
        Personaggio sacerdote = pronto(TipoPersonaggio.SACERDOTE);
        EsecutoreTattiche esecutore = esecutoreDi(gruppo(true, mago, sacerdote));

        esecutore.esegui(mago, decisione(TipoAzione.DARDO_ARCANO, null, goblin));
        esecutore.esegui(sacerdote, decisione(TipoAzione.LUCE_DIVINA, null, goblin));
        esecutore.esegui(sacerdote, decisione(TipoAzione.ALBA_SACRA, null, null));

        assertFalse(annunci.isEmpty(), "ogni azione che parte si annuncia");
    }

    @Test
    void leAbilitaDelChiericoEDelDruido() {
        Personaggio chierico = pronto(TipoPersonaggio.CHIERICO);
        Personaggio druido = pronto(TipoPersonaggio.DRUIDO);
        feriscilo(chierico);
        EsecutoreTattiche esecutore = esecutoreDi(gruppo(true, chierico, druido));

        esecutore.esegui(chierico, decisione(TipoAzione.SCACCIARE_I_NON_MORTI, null, null));
        esecutore.esegui(chierico, decisione(TipoAzione.IMPOSIZIONE_DELLE_MANI, null, chierico));
        esecutore.esegui(druido, decisione(TipoAzione.RADICI, null, null));
        esecutore.esegui(druido, decisione(TipoAzione.SCIAME, null, null));
        esecutore.esegui(druido, decisione(TipoAzione.PELLE_DI_CORTECCIA, null, null));
        esecutore.esegui(druido, decisione(TipoAzione.RIGENERAZIONE, null, chierico));

        assertFalse(annunci.isEmpty());
    }

    @Test
    void iCantiDelBardo() {
        Personaggio bardo = pronto(TipoPersonaggio.BARDO);
        Personaggio guerriero = pronto(TipoPersonaggio.GUERRIERO);
        feriscilo(guerriero);
        EsecutoreTattiche esecutore = esecutoreDi(gruppo(true, bardo, guerriero));

        esecutore.esegui(bardo, decisione(TipoAzione.CANTO_CURATIVO, null, guerriero));
        esecutore.esegui(bardo, decisione(TipoAzione.CANTO_IPNOTICO, null, null));
        esecutore.esegui(bardo, decisione(TipoAzione.URLO_DELLA_BANSHEE, null, null));
        esecutore.esegui(bardo, decisione(TipoAzione.CANTO_DI_VALORE, null, null));

        assertFalse(annunci.isEmpty());
    }

    @Test
    void laResurrezioneRiportaInVitaEConsumaLaPergamena() {
        Personaggio sacerdote = pronto(TipoPersonaggio.SACERDOTE);
        Personaggio guerriero = pronto(TipoPersonaggio.GUERRIERO);
        guerriero.subSalute(guerriero.getSalute(), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
        Gruppo miei = gruppo(true, sacerdote, guerriero);
        miei.addIncantesimi(TipoIncantesimo.RESURREZIONE, 1);

        esecutoreDi(miei).esegui(sacerdote, decisione(TipoAzione.RESURREZIONE, null, guerriero));

        assertEquals(0, miei.getIncantesimi(TipoIncantesimo.RESURREZIONE));
        assertFalse(annunci.isEmpty());
    }

    @Test
    void lePozioniSiBevonoDalleScorteDelGruppoERicordanoLAlleato() {
        Personaggio guerriero = pronto(TipoPersonaggio.GUERRIERO);
        Personaggio mago = pronto(TipoPersonaggio.MAGO);
        feriscilo(guerriero);
        mago.subMagia(mago.getMagia() / 2);
        Gruppo miei = gruppo(true, guerriero, mago);
        miei.addPozioniSalute(1);
        miei.addPozioniSaluteGrande(1);
        miei.addPozioniMagia(1);
        miei.addPozioniMagiaGrande(1);
        EsecutoreTattiche esecutore = esecutoreDi(miei);

        esecutore.esegui(guerriero, decisione(TipoAzione.POZIONE_SALUTE, null, guerriero));
        esecutore.esegui(guerriero, decisione(TipoAzione.POZIONE_SALUTE_GRANDE, null, guerriero));
        esecutore.esegui(guerriero, decisione(TipoAzione.POZIONE_MAGIA, null, mago));
        esecutore.esegui(guerriero, decisione(TipoAzione.POZIONE_MAGIA_GRANDE, null, mago));

        assertEquals(0, miei.getPozioniSalute());
        assertEquals(0, miei.getPozioniSaluteGrande());
        assertEquals(0, miei.getPozioniMagia());
        assertEquals(0, miei.getPozioniMagiaGrande());
    }

    @Test
    void unIncantesimoDiPiuBersagliColpisceIlPrincipaleEIVivi() {
        Personaggio mago = pronto(TipoPersonaggio.MAGO);
        Gruppo miei = gruppo(true, mago);
        miei.addIncantesimi(TipoIncantesimo.FUOCO, 1);
        goblin.subSalute(goblin.getSalute() - 1, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);

        esecutoreDi(miei).esegui(mago, decisione(TipoAzione.FUOCO, TipoIncantesimo.FUOCO, goblin));

        assertEquals(0, miei.getIncantesimi(TipoIncantesimo.FUOCO));
        assertFalse(annunci.isEmpty());
    }

    @Test
    void unIncantesimoSingoloColpisceSoloIlBersaglio() {
        Personaggio mago = pronto(TipoPersonaggio.MAGO);
        Gruppo miei = gruppo(true, mago);
        miei.addIncantesimi(TipoIncantesimo.MORTE, 1);

        esecutoreDi(miei).esegui(mago, decisione(TipoAzione.MORTE, TipoIncantesimo.MORTE, goblin));

        assertEquals(0, miei.getIncantesimi(TipoIncantesimo.MORTE));
        assertFalse(annunci.isEmpty());
    }

    @Test
    void ilCambioDArmaDiUnGruppoCheNonEQuelloDelGiocatoreNonFaNiente() {
        Personaggio guerriero = pronto(TipoPersonaggio.GUERRIERO);

        esecutoreDi(gruppo(true, guerriero)).esegui(guerriero, new Decisione(0, AzioneMD.cambiaArma(TipoDanno.CONTUNDENTE), TipoAzione.CAMBIA_ARMA,
                null, null, Collections.emptyList()));

        assertTrue(annunci.isEmpty());
    }

    @Test
    void ilCambioDArmaSceglieLaPrimaArmaDelTipoVoluto() {
        GruppoGiocatore miei = new GruppoGiocatore();
        Personaggio guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 5);
        miei.aggiungiPersonaggio(guerriero);
        Artefatto mazza = artefatto(TipoArtefatto.MAZZA, 5);
        miei.addArtefatto(mazza);

        esecutoreDi(miei).esegui(guerriero, new Decisione(0, AzioneMD.cambiaArma(TipoDanno.CONTUNDENTE), TipoAzione.CAMBIA_ARMA, null, null,
                Collections.emptyList()));

        assertEquals(1, annunci.size());
        assertNull(annunci.get(0).getBersaglio(), "chi cambia arma non attacca nessuno");
    }

    @Test
    void senzaUnArmaDelTipoVolutoIlCambioNonFaNiente() {
        GruppoGiocatore miei = new GruppoGiocatore();
        Personaggio guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 5);
        miei.aggiungiPersonaggio(guerriero);

        esecutoreDi(miei).esegui(guerriero, new Decisione(0, AzioneMD.cambiaArma(TipoDanno.SACRO), TipoAzione.CAMBIA_ARMA, null, null,
                Collections.emptyList()));

        assertTrue(annunci.isEmpty());
    }

    private static Artefatto artefatto(TipoArtefatto tipo, int livello) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(livello);
        md.setPeso(0.1);
        return Artefatto.di(md);
    }
}
