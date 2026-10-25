package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.modellodati.CoordinateMD;

import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.abilita.FabbricaAbilita;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.abilita.AbilitaInnate;
import com.threeamigos.foresta.tipi.CostantiAbilitaInnate;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Sacerdote;
import com.threeamigos.foresta.personaggi.Sacerdotessa;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sacerdote e Sacerdotessa: giocanti come Mago e Maga, ma con l'alba sacra al posto del dardo arcano, un incantesimo
 * innato che non consuma pergamene.
 */
class SacerdoteTest {

    @BeforeEach
    void preparaModello() {
        com.threeamigos.foresta.modellodati.ModelloDati.setIstanza(new com.threeamigos.foresta.modellodati.ModelloDati());
    }

    @Test
    void sonoClassiGiocantiMaschioEFemmina() {
        Personaggio sacerdote = new Sacerdote("Elia", 1);
        Personaggio sacerdotessa = new Sacerdotessa("Giuditta", 1);
        assertEquals(TipoPersonaggio.SACERDOTE, sacerdote.getClasse());
        assertEquals(TipoPersonaggio.SACERDOTESSA, sacerdotessa.getClasse());
        assertEquals(Personaggio.Sesso.MASCHIO, sacerdote.getSesso());
        assertEquals(Personaggio.Sesso.FEMMINA, sacerdotessa.getSesso());
        assertEquals("Sacerdote", sacerdote.getNomeSingolare());
        assertEquals("Sacerdotesse", sacerdotessa.getNomePlurale());
        assertTrue(sacerdote.isMagico());
    }

    @Test
    void nonHannoIlDardoArcano() {
        assertFalse(AbilitaInnate.conosciutaDa(TipoPersonaggio.SACERDOTE, TipoAbilita.DARDO_ARCANO));
        assertFalse(AbilitaInnate.conosciutaDa(TipoPersonaggio.SACERDOTESSA, TipoAbilita.DARDO_ARCANO));
        assertFalse(AbilitaInnate.conosciutaDa(TipoPersonaggio.MAGO, TipoAbilita.ALBA_SACRA));
        assertFalse(AbilitaInnate.conosciutaDa(TipoPersonaggio.ELFA, TipoAbilita.ALBA_SACRA));
        assertTrue(AbilitaInnate.conosciutaDa(TipoPersonaggio.SACERDOTE, TipoAbilita.ALBA_SACRA));
        assertTrue(AbilitaInnate.conosciutaDa(TipoPersonaggio.SACERDOTESSA, TipoAbilita.ALBA_SACRA));
    }

    @Test
    void serveAbbastanzaMagiaEBisognaPoterParlare() {
        Personaggio sacerdote = new Sacerdote("Elia", 1);
        assertTrue(AbilitaInnate.puoUsare(sacerdote, TipoAbilita.ALBA_SACRA));
        sacerdote.subMagia(sacerdote.getMagia() - (CostantiAbilitaInnate.ABILITA_ALBA_SACRA_COSTO_LANCIO - 1));
        assertFalse(AbilitaInnate.puoUsare(sacerdote, TipoAbilita.ALBA_SACRA));

        Personaggio muta = new Sacerdotessa("Giuditta", 1);
        assertTrue(AbilitaInnate.puoUsare(muta, TipoAbilita.ALBA_SACRA));
        muta.addEffettoDiStato(TipoEffettoDiStato.SILENZIATO, 3, 0);
        assertFalse(AbilitaInnate.puoUsare(muta, TipoAbilita.ALBA_SACRA));

        assertFalse(AbilitaInnate.puoUsare(new Mago("Merlino", 1), TipoAbilita.ALBA_SACRA));
    }

    @Test
    void siScelgonoDallaSchermataDellaClasse() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(7)) {
            partita.comando(Comando.PERGAMENA);
            partita.testo("Elia");
            partita.comando(Comando.MASCHIO);
            partita.assertComandoDisponibile(Comando.SACERDOTE);
            partita.assertComandoDisponibile(Comando.MAGO);
            partita.comando(Comando.SACERDOTE);
            assertEquals(TipoPersonaggio.SACERDOTE, partita.gruppo().getCapo().getClasse());
        }
        try (PartitaDiTest partita = PartitaDiTest.nuova(7)) {
            partita.comando(Comando.PERGAMENA);
            partita.testo("Giuditta");
            partita.comando(Comando.FEMMINA);
            partita.assertComandoDisponibile(Comando.SACERDOTESSA);
            partita.comando(Comando.SACERDOTESSA);
            assertEquals(TipoPersonaggio.SACERDOTESSA, partita.gruppo().getCapo().getClasse());
        }
    }

    @Test
    void laInvocaSoloSeServeEAncheFuoriDagliScontri() {
        Personaggio sacerdote = new Sacerdote("Elia", 5);
        Personaggio compagno = new Guerriero("Wendy", 5);
        Gruppo gruppo = new Gruppo() {
            @Override
            public boolean isGruppoGiocatore() {
                return true;
            }
        };
        gruppo.aggiungiPersonaggio(sacerdote);
        gruppo.aggiungiPersonaggio(compagno);
        GruppoAvversario nessuno = GruppoAvversario.getIstanza();
        nessuno.reimposta();

        // Senza stati da togliere non serve
        assertFalse(AbilitaInnate.abilitaUtili(sacerdote, gruppo, nessuno).contains(TipoAbilita.ALBA_SACRA));

        // Con uno stato negativo serve, anche senza avversari: e non ci sono pergamene di mezzo
        compagno.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 3, 0);
        assertTrue(AbilitaInnate.abilitaUtili(sacerdote, gruppo, nessuno).contains(TipoAbilita.ALBA_SACRA));
        int magiaPrima = sacerdote.getMagia();
        assertTrue(FabbricaAbilita.crea(TipoAbilita.ALBA_SACRA, sacerdote).usa(sacerdote, gruppo, nessuno));
        assertEquals(magiaPrima - CostantiAbilitaInnate.ABILITA_ALBA_SACRA_COSTO_LANCIO, sacerdote.getMagia());
        assertFalse(compagno.hasEffettoDiStato(TipoEffettoDiStato.AVVELENATO));
        assertFalse(AbilitaInnate.abilitaUtili(sacerdote, gruppo, nessuno).contains(TipoAbilita.ALBA_SACRA), "ora non serve più");
    }

    @Test
    void iSacerdotiDaReclutareSiIncontrano() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
            partita.iniziaCon("Elia", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            // I compagni da reclutare, sistemati nelle città e nelle locande, ci sono tutti e due...
            java.util.Set<String> nomi = new java.util.TreeSet<>();
            while (RegistroPersonaggi.getNumeroPersonaggiDisponibili() > 0) {
                nomi.add(RegistroPersonaggi.getPersonaggioDisponibile().getNomeProprio().orElse("?"));
            }
            // ...salvo quelli già sistemati nella Foresta: li cerchiamo lì
            for (com.threeamigos.foresta.modellodati.CoordinateMD coordinate : com.threeamigos.foresta.modellodati.ModelloDati.getIstanza()
                    .getRegistroPersonaggiMD().getUbicazioniPersonaggi()) {
                nomi.add(RegistroPersonaggi.getPersonaggioInLocazione(coordinate).getNomeProprio().orElse("?"));
            }
            // I loro nomi vengono da nomi.txt (NOME_SACERDOTE e NOME_SACERDOTESSA)
            assertTrue(nomi.stream().anyMatch(n -> n.startsWith("Fra' ")), nomi.toString());
            assertTrue(nomi.stream().anyMatch(n -> n.startsWith("Suor ") || n.startsWith("Sorella ")), nomi.toString());
        }
    }

    @Test
    void ilNomeConSpaziEApostrofoSopravviveAlSalvataggio() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(12)) {
            partita.iniziaCon("Elia", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            partita.gruppo().aggiungiPersonaggio(new Sacerdote("Fra' Stornato", 1));
            partita.gruppo().aggiungiPersonaggio(new Sacerdotessa("Sorella Intronata", 1));
            assertTrue(partita.salva(Comando.NUMERO_2));
            assertTrue(partita.leggi(Comando.NUMERO_2));
            java.util.List<String> nomi = new java.util.ArrayList<>();
            for (Personaggio personaggio : partita.gruppo().getPersonaggi()) {
                nomi.add(personaggio.getNomeProprio().orElse("?") + ":" + personaggio.getClasse());
            }
            assertTrue(nomi.contains("Fra' Stornato:SACERDOTE"), nomi.toString());
            assertTrue(nomi.contains("Sorella Intronata:SACERDOTESSA"), nomi.toString());
        }
    }

    @Test
    void ilSacerdoteSedutoInLocandaEUnPersonaggioDelSuoTipo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(13)) {
            partita.iniziaCon("Elia", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            for (TipoPersonaggio classe : new TipoPersonaggio[]{TipoPersonaggio.SACERDOTE, TipoPersonaggio.SACERDOTESSA}) {
                com.threeamigos.foresta.intermezzi.ImmagineIntermezzo immagine = com.threeamigos.foresta.intermezzi.ScenaInLocanda
                        .conSacerdote(classe).getPagine().get(0).getElemento("locandiere").getImmagine();
                assertEquals(com.threeamigos.foresta.intermezzi.ImmagineIntermezzo.Tipo.PERSONAGGIO, immagine.getTipo());
                assertEquals(classe, immagine.getClassePersonaggio());
            }
            // Entrambi guardano verso sinistra, da dove arriva il gruppo (se serve, la UI specchia l'immagine)
            for (TipoPersonaggio classe : new TipoPersonaggio[]{TipoPersonaggio.SACERDOTE, TipoPersonaggio.SACERDOTESSA}) {
                assertEquals(com.threeamigos.foresta.tipi.Verso.SINISTRA, com.threeamigos.foresta.intermezzi.ScenaInLocanda
                        .conSacerdote(classe).getPagine().get(0).getElemento("locandiere").getStatoAl(0).getVerso());
            }
            assertThrows(IllegalArgumentException.class,
                    () -> com.threeamigos.foresta.intermezzi.ScenaInLocanda.conSacerdote(TipoPersonaggio.MAGO));
        }
    }

    @Test
    void rimuovendoTuttiGliEffettiOgniNotificaDiceQualeEffettoEFinito() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(14)) {
            partita.iniziaCon("Elia", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            partita.eventi().ascolta(com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneEffettoDiStatoPersonaggio.class);
            Personaggio sacerdote = partita.gruppo().getCapo();
            sacerdote.addEffettoDiStato(TipoEffettoDiStato.BRUCIATO, 3, 0);
            sacerdote.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO, 3, 0);
            partita.eventi().svuota();
            // Come fa l'alba sacra: toglie tutto, e la UI deve sapere cosa
            sacerdote.rimuoviTuttiGliEffettiDiStato();
            java.util.Set<TipoEffettoDiStato> rimossi = new java.util.HashSet<>();
            for (com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneEffettoDiStatoPersonaggio evento
                    : partita.eventi().tutti(com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneEffettoDiStatoPersonaggio.class)) {
                assertNotNull(evento.getEffetto(), "l'evento di rimozione non dice quale effetto");
                rimossi.add(evento.getEffetto());
            }
            assertEquals(new java.util.HashSet<>(java.util.Arrays.asList(TipoEffettoDiStato.BRUCIATO, TipoEffettoDiStato.AVVELENATO)), rimossi);
        }
    }
}
