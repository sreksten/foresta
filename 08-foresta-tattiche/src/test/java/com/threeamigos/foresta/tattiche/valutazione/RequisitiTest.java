package com.threeamigos.foresta.tattiche.valutazione;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tattiche.ContestoTattico;
import com.threeamigos.foresta.tattiche.DatiDiProvaDelleTattiche;
import com.threeamigos.foresta.tipi.CostantiTattiche;
import com.threeamigos.foresta.tipi.EsitoAzione;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Quando un'azione si può fare: per ognuna il motivo del no, o null se si può fare.
 */
class RequisitiTest {

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

    private Personaggio mago;
    private Personaggio guerriero;
    private Personaggio sacerdote;
    private Personaggio chierico;
    private Personaggio druido;
    private Personaggio bardo;
    private Personaggio goblin;
    private Personaggio scheletro;
    private Gruppo miei;
    private Gruppo mostri;

    @BeforeEach
    void prepara() {
        DatiDiProvaDelleTattiche.nuovaPartita();
        com.threeamigos.foresta.modellodati.CantiImparatiMD canti = new com.threeamigos.foresta.modellodati.CantiImparatiMD();
        com.threeamigos.foresta.canti.CantiImparati.setFornitoreDiDati(() -> canti);
        for (com.threeamigos.foresta.tipi.TipoCanto canto : com.threeamigos.foresta.tipi.TipoCanto.values()) {
            com.threeamigos.foresta.canti.CantiImparati.impara(canto);
        }
        StatoDelCombattimento.getIstanza().reimposta();
        mago = pronto(TipoPersonaggio.MAGO);
        guerriero = pronto(TipoPersonaggio.GUERRIERO);
        sacerdote = pronto(TipoPersonaggio.SACERDOTE);
        chierico = pronto(TipoPersonaggio.CHIERICO);
        druido = pronto(TipoPersonaggio.DRUIDO);
        bardo = pronto(TipoPersonaggio.BARDO);
        goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        miei = gruppo(true, mago, guerriero, sacerdote, chierico, druido, bardo);
        mostri = gruppo(false, goblin, scheletro);
    }

    private static Personaggio pronto(TipoPersonaggio classe) {
        Personaggio personaggio = FabbricaPersonaggi.crea(classe, 5);
        personaggio.addMagia(personaggio.getMagiaMassima());
        personaggio.addSalute(personaggio.getSaluteMassima());
        return personaggio;
    }

    private static void feriscilo(Personaggio personaggio) {
        personaggio.subSalute(personaggio.getSalute() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
    }

    private static void abbattilo(Personaggio personaggio) {
        personaggio.subSalute(personaggio.getSalute(), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
    }

    private String no(TipoAzione tipo, Personaggio attore, Personaggio bersaglio) {
        return no(AzioneMD.di(tipo), attore, bersaglio);
    }

    private String no(AzioneMD azione, Personaggio attore, Personaggio bersaglio) {
        return Requisiti.motivoDelNo(azione, attore, bersaglio, ContestoTattico.per(attore, miei, mostri));
    }

    private static void assertSi(String motivo) {
        assertNull(motivo, motivo);
    }

    private static void assertNo(String motivo, String contiene) {
        assertNotNull(motivo, "doveva dire di no (" + contiene + ")");
        assertTrue(motivo.contains(contiene), motivo);
    }

    @Test
    void unaImmunitaScopertaOTroppiFallimentiFermanoLAzione() {
        miei.addIncantesimi(TipoIncantesimo.FUOCO, 1);
        miei.addIncantesimi(TipoIncantesimo.GELO, 1);
        assertSi(no(TipoAzione.FUOCO, mago, goblin));
        StatoDelCombattimento.getIstanza().registraEsitoAzione(mago, goblin, TipoAzione.FUOCO, EsitoAzione.IMMUNE);
        assertNo(no(TipoAzione.FUOCO, mago, goblin), "è immune a");

        for (int i = 0; i < CostantiTattiche.MAX_TENTATIVI_FALLITI_PER_AZIONE; i++) {
            StatoDelCombattimento.getIstanza().registraEsitoAzione(mago, scheletro, TipoAzione.GELO, EsitoAzione.MANCATA);
        }
        assertNo(no(TipoAzione.GELO, mago, scheletro), "è già fallita");

        // L'attacco fisico non si abbandona mai
        for (int i = 0; i < CostantiTattiche.MAX_TENTATIVI_FALLITI_PER_AZIONE + 1; i++) {
            StatoDelCombattimento.getIstanza().registraEsitoAzione(guerriero, goblin, TipoAzione.ATTACCO_FISICO, EsitoAzione.MANCATA);
        }
        assertSi(no(TipoAzione.ATTACCO_FISICO, guerriero, goblin));
    }

    @Test
    void unNonMortoNelGruppoDelGiocatoreNonSiCura() {
        miei.addPozioniSalute(1);
        feriscilo(scheletro);
        assertSi(no(TipoAzione.POZIONE_SALUTE, guerriero, scheletro));
        // Un non morto è curabile solo se non è nel gruppo del giocatore
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(scheletro);
        assertNo(no(TipoAzione.POZIONE_SALUTE, guerriero, scheletro), "non morto");
    }

    @Test
    void lAttaccoFisicoServeUnBersaglioInCombattimento() {
        assertNo(no(TipoAzione.ATTACCO_FISICO, guerriero, null), "nessun bersaglio");
        abbattilo(goblin);
        assertNo(no(TipoAzione.ATTACCO_FISICO, guerriero, goblin), "nessun bersaglio");
        assertSi(no(TipoAzione.ATTACCO_FISICO, guerriero, scheletro));
    }

    @Test
    void gliIncantesimiDiDannoChiedonoQualcosaDaLanciare() {
        assertNo(no(AzioneMD.incantesimoDiDanno(com.threeamigos.foresta.tipi.CriterioIncantesimo.PIU_POTENTE, false), mago, goblin), "nessun incantesimo");
        miei.addIncantesimi(TipoIncantesimo.FUOCO, 1);
        assertSi(no(AzioneMD.incantesimoDiDanno(com.threeamigos.foresta.tipi.CriterioIncantesimo.PIU_POTENTE, false), mago, goblin));
    }

    @Test
    void ilDardoArcanoELaLuceDivinaSonoDiChiLeConosce() {
        assertNo(no(TipoAzione.DARDO_ARCANO, guerriero, goblin), "dardo arcano");
        assertSi(no(TipoAzione.DARDO_ARCANO, mago, goblin));
        assertNo(no(TipoAzione.DARDO_ARCANO, mago, null), "nessun bersaglio");

        assertNo(no(TipoAzione.LUCE_DIVINA, guerriero, goblin), "luce divina");
        assertSi(no(TipoAzione.LUCE_DIVINA, sacerdote, scheletro));
    }

    @Test
    void unBersaglioImmuneAlTipoDiDannoNonSiColpisce() {
        Personaggio immune = null;
        TipoDanno tipo = null;
        for (TipoPersonaggio classe : TipoPersonaggio.values()) {
            if (!classe.isAvversario()) {
                continue;
            }
            Personaggio candidato = FabbricaPersonaggi.crea(classe, 1);
            for (TipoDanno danno : TipoDanno.values()) {
                if (candidato.isImmuneATipoDanno(danno) && (danno == TipoDanno.ARCANO || danno == TipoDanno.SACRO)) {
                    immune = candidato;
                    tipo = danno;
                }
            }
        }
        Personaggio bersaglio = immune;
        TipoAzione azione = tipo == TipoDanno.ARCANO ? TipoAzione.DARDO_ARCANO : TipoAzione.LUCE_DIVINA;
        Personaggio attore = tipo == TipoDanno.ARCANO ? mago : sacerdote;
        assertNotNull(bersaglio, "serve una classe immune all'arcano o al sacro");
        assertNo(no(azione, attore, bersaglio), "è immune a");
    }

    @Test
    void conUnArmaMoltoForteLeArmieRendonoDiPiuDelLoroIncantesimoODelDardo() {
        GruppoGiocatore giocatore = new GruppoGiocatore();
        Personaggio elfo = FabbricaPersonaggi.crea(TipoPersonaggio.ELFO, 5);
        elfo.addMagia(elfo.getMagiaMassima());
        giocatore.aggiungiPersonaggio(elfo);
        Artefatto spada = arma(TipoArtefatto.SPADA);
        spada.getModelloDati().setDanni(500);
        giocatore.addArtefatto(spada);
        giocatore.equipaggia(elfo, spada);
        giocatore.addIncantesimi(TipoIncantesimo.FUOCO, 1);
        ContestoTattico contesto = ContestoTattico.per(elfo, giocatore, mostri);

        assertNo(Requisiti.motivoDelNo(AzioneMD.di(TipoAzione.DARDO_ARCANO).soloSePiuForteDellAttacco(), elfo, goblin, contesto), "attacco con le armi");
        assertNo(Requisiti.motivoDelNo(AzioneMD.di(TipoAzione.FUOCO).soloSePiuForteDellAttacco(), elfo, goblin, contesto), "attacco con le armi");
        // Senza la condizione si lancia lo stesso
        assertSi(Requisiti.motivoDelNo(AzioneMD.di(TipoAzione.DARDO_ARCANO), elfo, goblin, contesto));
        assertSi(Requisiti.motivoDelNo(AzioneMD.di(TipoAzione.FUOCO), elfo, goblin, contesto));
    }

    @Test
    void chiEImmuneAlTipoDiDannoDiUnIncantesimoNonLoSubisce() {
        Personaggio bersaglio = null;
        TipoIncantesimo incantesimo = null;
        for (TipoPersonaggio classe : TipoPersonaggio.values()) {
            if (!classe.isAvversario() || bersaglio != null) {
                continue;
            }
            Personaggio candidato = FabbricaPersonaggi.crea(classe, 1);
            for (TipoIncantesimo tipo : TipoIncantesimo.values()) {
                if (tipo.getTipoDanno() != null && candidato.isImmuneATipoDanno(tipo.getTipoDanno()) && !candidato.isImmuneAIncantesimo(tipo)
                        && AzioneMD.di(TipoAzione.di(tipo)) != null && tipo != TipoIncantesimo.RESURREZIONE) {
                    bersaglio = candidato;
                    incantesimo = tipo;
                    break;
                }
            }
        }
        assertNotNull(bersaglio, "serve un avversario immune al tipo di danno di un incantesimo ma non all'incantesimo");
        miei.addIncantesimi(incantesimo, 1);
        assertNo(no(TipoAzione.di(incantesimo), mago, bersaglio), "è immune a");
    }

    @Test
    void laResurrezioneServePergamenaEUnCaduto() {
        assertNo(no(TipoAzione.RESURREZIONE, sacerdote, guerriero), "manca la pergamena");
        miei.addIncantesimi(TipoIncantesimo.RESURREZIONE, 1);
        assertNo(no(TipoAzione.RESURREZIONE, sacerdote, guerriero), "nessun caduto");
        assertNo(no(TipoAzione.RESURREZIONE, sacerdote, null), "nessun caduto");
        abbattilo(guerriero);
        assertSi(no(TipoAzione.RESURREZIONE, sacerdote, guerriero));
    }

    @Test
    void lAlbaSacraTogliGliStatiNegativi() {
        assertNo(no(TipoAzione.ALBA_SACRA, guerriero, null), "Alba sacra");
        assertNo(no(TipoAzione.ALBA_SACRA, sacerdote, null), "nessun alleato");
        guerriero.addEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO, 3, 1);
        assertSi(no(TipoAzione.ALBA_SACRA, sacerdote, null));
    }

    @Test
    void iCantiDelBardo() {
        assertNo(no(TipoAzione.CANTO_CURATIVO, guerriero, mago), "canto curativo");
        assertNo(no(TipoAzione.CANTO_CURATIVO, bardo, guerriero), "non è ferito");
        feriscilo(guerriero);
        assertSi(no(TipoAzione.CANTO_CURATIVO, bardo, guerriero));
        assertNo(no(TipoAzione.CANTO_CURATIVO, bardo, null), "non è ferito");

        assertNo(no(TipoAzione.CANTO_IPNOTICO, guerriero, null), "canto");
        assertSi(no(TipoAzione.CANTO_IPNOTICO, bardo, null));
        assertNo(no(TipoAzione.URLO_DELLA_BANSHEE, guerriero, null), "canto");
        assertSi(no(TipoAzione.URLO_DELLA_BANSHEE, bardo, null));
        goblin.addEffettoDiStato(TipoEffettoDiStato.SPAVENTATO, 3, 0);
        scheletro.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 3, 0);
        assertNo(no(TipoAzione.CANTO_IPNOTICO, bardo, null), "già spaventati o confusi");

        assertNo(no(TipoAzione.CANTO_DI_VALORE, guerriero, null), "canto di valore");
        assertSi(no(TipoAzione.CANTO_DI_VALORE, bardo, null));
        for (Personaggio alleato : miei.getPersonaggi()) {
            alleato.addEffettoDiStato(TipoEffettoDiStato.ISPIRATO, 3, 0);
        }
        assertNo(no(TipoAzione.CANTO_DI_VALORE, bardo, null), "già ispirati");
    }

    @Test
    void leAbilitaDelChiericoEDelDruido() {
        assertNo(no(TipoAzione.SCACCIARE_I_NON_MORTI, guerriero, null), "scacciare");
        Personaggio spaventabile = null;
        for (TipoPersonaggio classe : TipoPersonaggio.values()) {
            if (classe.isAvversario()) {
                Personaggio candidato = FabbricaPersonaggi.crea(classe, 1);
                if (com.threeamigos.foresta.abilita.AbilitaInnate.isDaScacciare(candidato)) {
                    spaventabile = candidato;
                    break;
                }
            }
        }
        assertNotNull(spaventabile, "serve un non morto che si possa spaventare");
        mostri = gruppo(false, goblin, spaventabile);
        assertSi(no(TipoAzione.SCACCIARE_I_NON_MORTI, chierico, null));
        abbattilo(spaventabile);
        assertNo(no(TipoAzione.SCACCIARE_I_NON_MORTI, chierico, null), "nessun non morto");

        assertNo(no(TipoAzione.IMPOSIZIONE_DELLE_MANI, guerriero, mago), "imposizione");
        assertNo(no(TipoAzione.IMPOSIZIONE_DELLE_MANI, chierico, mago), "non è ferito");
        feriscilo(mago);
        assertSi(no(TipoAzione.IMPOSIZIONE_DELLE_MANI, chierico, mago));

        assertNo(no(TipoAzione.RADICI, guerriero, null), "abilità");
        assertSi(no(TipoAzione.RADICI, druido, null));
        assertSi(no(TipoAzione.SCIAME, druido, null));
        abbattilo(goblin);
        assertNo(no(TipoAzione.RADICI, druido, null), "nessun avversario");

        assertNo(no(TipoAzione.PELLE_DI_CORTECCIA, guerriero, null), "pelle di corteccia");
        assertSi(no(TipoAzione.PELLE_DI_CORTECCIA, druido, null));

        assertNo(no(TipoAzione.RIGENERAZIONE, guerriero, mago), "rigenerazione");
        assertSi(no(TipoAzione.RIGENERAZIONE, druido, mago));
        mago.addEffettoDiStato(TipoEffettoDiStato.RIGENERAZIONE, 3, 1);
        assertNo(no(TipoAzione.RIGENERAZIONE, druido, mago), "si rigenera già");
    }

    @Test
    void lePozioniServonoScorteEUnBersaglioCheNeHaBisogno() {
        assertNo(no(TipoAzione.POZIONE_SALUTE, guerriero, guerriero), "non ha pozioni della Salute");
        assertNo(no(TipoAzione.POZIONE_SALUTE_GRANDE, guerriero, guerriero), "grandi");
        assertNo(no(TipoAzione.POZIONE_MAGIA, guerriero, mago), "non ha pozioni della Magia");
        assertNo(no(TipoAzione.POZIONE_MAGIA_GRANDE, guerriero, mago), "grandi");

        miei.addPozioniSalute(1);
        miei.addPozioniSaluteGrande(1);
        miei.addPozioniMagia(1);
        miei.addPozioniMagiaGrande(1);
        assertNo(no(TipoAzione.POZIONE_SALUTE, guerriero, guerriero), "non è ferito");
        assertNo(no(TipoAzione.POZIONE_SALUTE_GRANDE, guerriero, guerriero), "non è ferito");
        assertNo(no(TipoAzione.POZIONE_MAGIA, guerriero, mago), "MAGIA piena");
        assertNo(no(TipoAzione.POZIONE_MAGIA_GRANDE, guerriero, mago), "MAGIA piena");

        feriscilo(guerriero);
        mago.subMagia(mago.getMagia() / 2);
        assertSi(no(TipoAzione.POZIONE_SALUTE, guerriero, guerriero));
        assertSi(no(TipoAzione.POZIONE_SALUTE_GRANDE, guerriero, guerriero));
        assertSi(no(TipoAzione.POZIONE_MAGIA, guerriero, mago));
        assertSi(no(TipoAzione.POZIONE_MAGIA_GRANDE, guerriero, mago));
    }

    @Test
    void ilCambioDArmaServeUnGruppoDelGiocatoreConUnArmaAdatta() {
        AzioneMD cambia = AzioneMD.cambiaArma(TipoDanno.CONTUNDENTE);
        assertNo(no(cambia, guerriero, null), "solo i personaggi del giocatore");

        GruppoGiocatore giocatore = new GruppoGiocatore();
        Personaggio capo = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 5);
        giocatore.aggiungiPersonaggio(capo);
        assertNo(Requisiti.motivoDelNo(cambia, capo, null, ContestoTattico.per(capo, giocatore, mostri)), "nessuna arma");

        giocatore.addArtefatto(arma(TipoArtefatto.MAZZA));
        assertSi(Requisiti.motivoDelNo(cambia, capo, null, ContestoTattico.per(capo, giocatore, mostri)));
    }

    private static Artefatto arma(TipoArtefatto tipo) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(5);
        md.setPeso(0.1);
        return Artefatto.di(md);
    }

    @Test
    void gliIncantesimiDiUnaClassePrecisa() {
        assertNo(no(TipoAzione.FUOCO, mago, goblin), "manca la pergamena");
        miei.addIncantesimi(TipoIncantesimo.FUOCO, 2);
        miei.addIncantesimi(TipoIncantesimo.SILENZIO, 2);
        miei.addIncantesimi(TipoIncantesimo.MORTE, 2);

        assertSi(no(TipoAzione.FUOCO, mago, goblin));
        assertNo(no(TipoAzione.FUOCO, mago, null), "nessun bersaglio");
        abbattilo(goblin);
        assertNo(no(TipoAzione.FUOCO, mago, goblin), "nessun bersaglio");

        assertSi(no(TipoAzione.SILENZIO, mago, scheletro));
        scheletro.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        assertNo(no(TipoAzione.SILENZIO, mago, scheletro), "silenziato");
    }

    @Test
    void chiEImmuneAUnIncantesimoNonLoSubisce() {
        miei.addIncantesimi(TipoIncantesimo.FUOCO, 1);
        Personaggio gargoyle = FabbricaPersonaggi.crea(TipoPersonaggio.GARGOYLE, 1);
        assertTrue(gargoyle.isImmuneAIncantesimo(TipoIncantesimo.FUOCO), "il gargoyle è immune a tutti gli incantesimi");
        assertNo(no(TipoAzione.FUOCO, mago, gargoyle), "immune");
    }

    @Test
    void unMostroMagicoLanciaIlSuoRepertorioSenzaPergamene() {
        Personaggio strega = FabbricaPersonaggi.crea(TipoPersonaggio.STREGA, 5);
        strega.addMagia(strega.getMagiaMassima());
        Gruppo mostriConLaStrega = gruppo(false, strega);
        TipoIncantesimo suo = strega.getRepertorioIncantesimi().iterator().next();
        TipoAzione azione = TipoAzione.di(suo);
        assertNotNull(azione);
        assertSi(Requisiti.motivoDelNo(AzioneMD.di(azione), strega, mago, ContestoTattico.per(strega, miei, mostriConLaStrega)));
    }
}
