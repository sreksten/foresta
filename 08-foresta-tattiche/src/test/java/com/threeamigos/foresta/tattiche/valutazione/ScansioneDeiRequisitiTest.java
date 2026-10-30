package com.threeamigos.foresta.tattiche.valutazione;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tattiche.ContestoTattico;
import com.threeamigos.foresta.tattiche.DatiDiProvaDelleTattiche;
import com.threeamigos.foresta.tattiche.modifica.BozzaTattica;
import com.threeamigos.foresta.tipi.CriterioIncantesimo;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ogni azione in ogni situazione: chi decide e chi è il bersaglio cambiano a ogni giro, come le scorte, la salute e gli stati. Per ogni
 * combinazione il requisito risponde "si può" o dice perché no, senza rompersi.
 */
class ScansioneDeiRequisitiTest {

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

    private static Personaggio pronto(TipoPersonaggio classe) {
        Personaggio personaggio = FabbricaPersonaggi.crea(classe, 5);
        personaggio.addMagia(personaggio.getMagiaMassima());
        personaggio.addSalute(personaggio.getSaluteMassima());
        return personaggio;
    }

    private static void ferisci(Personaggio p) {
        p.subSalute(p.getSalute() / 2, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
    }

    private static void abbatti(Personaggio p) {
        p.subSalute(p.getSalute(), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
    }

    private static List<AzioneMD> azioni() {
        List<AzioneMD> azioni = new ArrayList<>();
        for (TipoAzione tipo : TipoAzione.values()) {
            AzioneMD base = BozzaTattica.azionePredefinita(tipo);
            azioni.add(base);
            if (BozzaTattica.haSoloSePiuForte(base)) {
                azioni.add(BozzaTattica.commutaSoloSePiuForte(base));
            }
            if (tipo == TipoAzione.INCANTESIMO_DI_DANNO || tipo == TipoAzione.INCANTESIMO_DEL_REPERTORIO) {
                for (CriterioIncantesimo criterio : CriterioIncantesimo.values()) {
                    azioni.add(tipo == TipoAzione.INCANTESIMO_DI_DANNO ? AzioneMD.incantesimoDiDanno(criterio, false)
                            : AzioneMD.incantesimoDelRepertorio(criterio, true));
                }
            }
        }
        return azioni;
    }

    private static void provaTutto(Gruppo miei, Gruppo mostri) {
        List<Personaggio> tutti = new ArrayList<>(miei.getPersonaggi());
        tutti.addAll(mostri.getPersonaggi());
        List<Personaggio> bersagli = new ArrayList<>(tutti);
        bersagli.add(null);
        for (AzioneMD azione : azioni()) {
            for (Personaggio attore : tutti) {
                // Il lato dell'attore lo decide il contesto: chi non sta nel gruppo del giocatore è un mostro
                ContestoTattico contesto = ContestoTattico.per(attore, miei, mostri);
                for (Personaggio bersaglio : bersagli) {
                    String motivo = Requisiti.motivoDelNo(azione, attore, bersaglio, contesto);
                    if (motivo != null) {
                        assertFalse(motivo.isEmpty());
                    }
                }
            }
        }
    }

    private Personaggio mago, guerriero, sacerdote, chierico, druido, bardo, goblin, scheletro, spettro, elfo;
    private Gruppo miei, mostri;

    private void mondo() {
        com.threeamigos.foresta.strumenti.logger.Logger.impostaDestinazione(com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog.NESSUNA);
        DatiDiProvaDelleTattiche.nuovaPartita();
        com.threeamigos.foresta.modellodati.CantiImparatiMD canti = new com.threeamigos.foresta.modellodati.CantiImparatiMD();
        com.threeamigos.foresta.canti.CantiImparati.setFornitoreDiDati(() -> canti);
        for (TipoCanto canto : TipoCanto.values()) {
            com.threeamigos.foresta.canti.CantiImparati.impara(canto);
        }
        StatoDelCombattimento.getIstanza().reimposta();
        mago = pronto(TipoPersonaggio.MAGO);
        guerriero = pronto(TipoPersonaggio.GUERRIERO);
        sacerdote = pronto(TipoPersonaggio.SACERDOTE);
        chierico = pronto(TipoPersonaggio.CHIERICO);
        druido = pronto(TipoPersonaggio.DRUIDO);
        bardo = pronto(TipoPersonaggio.BARDO);
        elfo = pronto(TipoPersonaggio.ELFO);
        goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        scheletro = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        spettro = FabbricaPersonaggi.crea(TipoPersonaggio.SPETTRO, 3);
        miei = gruppo(true, mago, guerriero, sacerdote, chierico, druido, bardo, elfo);
        mostri = gruppo(false, goblin, scheletro, spettro);
    }

    private void risorse(int quante) {
        for (TipoIncantesimo classe : TipoIncantesimo.values()) {
            miei.addIncantesimi(classe, quante);
        }
        miei.addPozioniSalute(quante);
        miei.addPozioniSaluteGrande(quante);
        miei.addPozioniMagia(quante);
        miei.addPozioniMagiaGrande(quante);
    }

    @Test
    void conTutteLeRisorseELaSaluteAlMassimo() {
        mondo();
        risorse(2);
        provaTutto(miei, mostri);
    }

    @Test
    void senzaRisorseEConTuttiFeriti() {
        mondo();
        for (Personaggio p : Arrays.asList(mago, guerriero, sacerdote, chierico, druido, bardo, elfo, goblin, scheletro, spettro)) {
            ferisci(p);
        }
        for (Personaggio p : Arrays.asList(mago, sacerdote, chierico, druido, bardo, elfo)) {
            p.subMagia(p.getMagia());
        }
        provaTutto(miei, mostri);
    }

    @Test
    void conRisorseFeritiESilenziati() {
        mondo();
        risorse(1);
        for (Personaggio p : Arrays.asList(mago, guerriero, sacerdote, chierico, druido, bardo, elfo, goblin, scheletro, spettro)) {
            ferisci(p);
            p.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        }
        for (Personaggio p : Arrays.asList(goblin, scheletro, spettro)) {
            p.addEffettoDiStato(TipoEffettoDiStato.SPAVENTATO, 3, 0);
            p.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 3, 0);
        }
        provaTutto(miei, mostri);
    }

    @Test
    void conAvversariEAlleatiCaduti() {
        mondo();
        risorse(1);
        abbatti(goblin);
        abbatti(scheletro);
        abbatti(spettro);
        abbatti(guerriero);
        guerriero.setInPanchina(false);
        chierico.setInPanchina(true);
        provaTutto(miei, mostri);
    }

    @Test
    void conAlleatiIspiratiERigeneranti() {
        mondo();
        risorse(1);
        for (Personaggio p : Arrays.asList(mago, guerriero, sacerdote, chierico, druido, bardo, elfo)) {
            p.addEffettoDiStato(TipoEffettoDiStato.ISPIRATO, 3, 0);
            p.addEffettoDiStato(TipoEffettoDiStato.RIGENERAZIONE, 3, 1);
            ferisci(p);
        }
        goblin.addEffettoDiStato(TipoEffettoDiStato.SANGUINAMENTO, 3, 1);
        provaTutto(miei, mostri);
    }

    @Test
    void conUnNonMortoNelGruppoDelGiocatore() {
        mondo();
        risorse(1);
        Gruppo conNonMorto = gruppo(true, mago, guerriero, sacerdote, chierico, druido, bardo, scheletro);
        ferisci(scheletro);
        provaTutto(conNonMorto, gruppo(false, goblin, spettro));
    }

    private static Personaggio primoCon(java.util.function.Predicate<Personaggio> condizione) {
        for (TipoPersonaggio classe : TipoPersonaggio.values()) {
            Personaggio p;
            try {
                p = FabbricaPersonaggi.crea(classe, 3);
            } catch (RuntimeException e) {
                continue;
            }
            if (condizione.test(p)) {
                return p;
            }
        }
        return null;
    }

    @Test
    void conAvversariImmuniAlDardoAllaLuceEAiCanti() {
        mondo();
        risorse(1);
        Personaggio immuneArcano = primoCon(p -> p.isImmuneATipoDanno(com.threeamigos.foresta.tipi.TipoDanno.ARCANO));
        Personaggio immuneSacro = primoCon(p -> p.isImmuneATipoDanno(com.threeamigos.foresta.tipi.TipoDanno.SACRO));
        // Nessuna classe è immune a tutti e due, ma un guerriero in berserk sì
        Personaggio immuneAgliStati = pronto(TipoPersonaggio.GUERRIERO);
        immuneAgliStati.addEffettoDiStato(TipoEffettoDiStato.BERSERK, 3, 0);
        assertTrue(immuneAgliStati.isImmuneAEffetto(TipoEffettoDiStato.SPAVENTATO));
        assertTrue(immuneAgliStati.isImmuneAEffetto(TipoEffettoDiStato.CONFUSO));
        assertNotNull(immuneArcano);
        assertNotNull(immuneSacro);
        assertNotNull(immuneAgliStati);
        provaTutto(miei, gruppo(false, immuneArcano, immuneSacro, immuneAgliStati));
        provaTutto(miei, gruppo(false, immuneAgliStati));
        // il canto contro soli immuni non si fa
        assertNotNull(Requisiti.motivoDelNo(AzioneMD.di(TipoAzione.CANTO_IPNOTICO), bardo, null,
                ContestoTattico.per(bardo, miei, gruppo(false, immuneAgliStati))));
    }

    @Test
    void unMostroMagicoLanciaDalRepertorioSenzaPergamene() {
        mondo();
        Personaggio magico = primoCon(p -> p.isMagico() && !p.getRepertorioIncantesimi().isEmpty() && p.getMagiaMassima() > 0
                && p.getClasse() != TipoPersonaggio.MAGO);
        assertNotNull(magico);
        magico.addMagia(magico.getMagiaMassima());
        Gruppo magici = gruppo(false, magico, goblin);
        provaTutto(miei, magici);
        TipoIncantesimo classe = null;
        for (TipoIncantesimo candidata : magico.getRepertorioIncantesimi()) {
            if (magico.puoFormulare(candidata)) {
                classe = candidata;
            }
        }
        assertNotNull(classe, "il mostro magico ha la magia per almeno un incantesimo del suo repertorio");
        assertTrue(Requisiti.puoLanciare(magico, classe, ContestoTattico.per(magico, miei, magici)));
    }

    @Test
    void conUnGruppoVeroDelGiocatoreConArmiEUnNonMorto() {
        DatiDiProvaDelleTattiche.nuovaPartita();
        com.threeamigos.foresta.gruppi.GruppoGiocatore vero = com.threeamigos.foresta.gruppi.GruppoGiocatore.getIstanza();
        com.threeamigos.foresta.modellodati.CantiImparatiMD canti = new com.threeamigos.foresta.modellodati.CantiImparatiMD();
        com.threeamigos.foresta.canti.CantiImparati.setFornitoreDiDati(() -> canti);
        StatoDelCombattimento.getIstanza().reimposta();
        Personaggio guerrieroVero = new com.threeamigos.foresta.personaggi.Guerriero("Arsenio", 5);
        Personaggio druidoVero = new com.threeamigos.foresta.personaggi.Druido("Quercia", 5);
        Personaggio chiericoVero = new com.threeamigos.foresta.personaggi.Chierico("Pio", 5);
        for (Personaggio p : Arrays.asList(guerrieroVero, druidoVero, chiericoVero)) {
            p.addMagia(p.getMagiaMassima());
            p.addSalute(p.getSaluteMassima());
        }
        Personaggio scheletroVero = FabbricaPersonaggi.crea(TipoPersonaggio.SCHELETRO, 1);
        for (Personaggio p : Arrays.asList(guerrieroVero, druidoVero, chiericoVero)) {
            p.getModelloDati().setPng(false);
        }
        vero.aggiungiPersonaggio(guerrieroVero);
        vero.aggiungiPersonaggio(druidoVero);
        vero.aggiungiPersonaggio(chiericoVero);
        vero.aggiungiPersonaggio(scheletroVero);
        com.threeamigos.foresta.artefatti.GeneratoreArtefatti generatore = new com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle(new java.util.Random(3));
        for (com.threeamigos.foresta.tipi.TipoArtefatto tipo : new com.threeamigos.foresta.tipi.TipoArtefatto[] {
                com.threeamigos.foresta.tipi.TipoArtefatto.SPADA, com.threeamigos.foresta.tipi.TipoArtefatto.MAZZA,
                com.threeamigos.foresta.tipi.TipoArtefatto.ASCIA, com.threeamigos.foresta.tipi.TipoArtefatto.LANCIA}) {
            for (int i = 0; i < 5; i++) {
                vero.addArtefatto(generatore.generaArtefatto(tipo, 5));
            }
        }
        for (TipoIncantesimo classe : TipoIncantesimo.values()) {
            vero.addIncantesimi(classe, 2);
        }
        vero.addPozioniSalute(1);
        vero.addPozioniSaluteGrande(1);
        ferisci(druidoVero);
        ferisci(scheletroVero);
        Gruppo nemici = gruppo(false, FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1), FabbricaPersonaggi.crea(TipoPersonaggio.SPETTRO, 3));
        provaTutto(vero, nemici);
        // il cambio d'arma trova l'arma giusta per ogni tipo di danno
        int possibili = 0;
        for (com.threeamigos.foresta.tipi.TipoDanno danno : com.threeamigos.foresta.tipi.TipoDanno.values()) {
            if (Requisiti.motivoDelNo(AzioneMD.cambiaArma(danno), guerrieroVero, null, ContestoTattico.per(guerrieroVero, vero, nemici)) == null) {
                possibili++;
            }
        }
        assertTrue(possibili >= 1);
    }

    @Test
    void conLaPelleDiCortecciaGiaSuTutti() {
        mondo();
        risorse(1);
        new com.threeamigos.foresta.abilita.PelleDiCorteccia(druido).usa(druido, miei, mostri);
        provaTutto(miei, mostri);
        assertNotNull(Requisiti.motivoDelNo(AzioneMD.di(TipoAzione.PELLE_DI_CORTECCIA), druido, null, ContestoTattico.per(druido, miei, mostri)));
    }

    @Test
    void conUnaCadutaInPanchina() {
        mondo();
        risorse(1);
        abbatti(chierico);
        chierico.setInPanchina(true);
        abbatti(elfo);
        abbatti(bardo);
        provaTutto(miei, mostri);
        assertNotNull(Requisiti.motivoDelNo(AzioneMD.di(TipoAzione.RESURREZIONE), sacerdote, chierico, ContestoTattico.per(sacerdote, miei, mostri)));
    }

    @Test
    void iPrimiDelleSquadreSonoCaduti() {
        mondo();
        risorse(1);
        abbatti(mago);
        abbatti(goblin);
        provaTutto(miei, mostri);
        // anche il secondo, ma non l'ultimo
        abbatti(guerriero);
        abbatti(scheletro);
        provaTutto(miei, mostri);
        for (Personaggio p : Arrays.asList(sacerdote, chierico, druido, bardo, elfo)) {
            p.addEffettoDiStato(TipoEffettoDiStato.ISPIRATO, 3, 0);
        }
        provaTutto(miei, mostri);
    }

    @Test
    void unMostroCheNonSiDiceMagicoNonLanciaDalRepertorio() {
        mondo();
        Personaggio strano = new com.threeamigos.foresta.personaggi.Mago(3) {
            @Override
            public boolean isMagico() {
                return false;
            }
        };
        strano.addMagia(strano.getMagiaMassima());
        assertTrue(strano.puoFormulare(TipoIncantesimo.FUOCO), "ha la magia per lanciare");
        Gruppo gruppoDelMostro = gruppo(false, strano);
        assertFalse(Requisiti.puoLanciare(strano, TipoIncantesimo.FUOCO, ContestoTattico.per(strano, miei, gruppoDelMostro)));
    }
}
