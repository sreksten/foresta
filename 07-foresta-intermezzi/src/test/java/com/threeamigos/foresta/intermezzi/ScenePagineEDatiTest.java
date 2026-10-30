package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.eventi.interni.InternoCreazioneFumetto;
import com.threeamigos.foresta.eventi.interni.InternoFumettoSuccessivo;
import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.GruppoGiocatoreMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Animazione;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRipetizione;
import com.threeamigos.foresta.tipi.TipoStiramento;
import com.threeamigos.foresta.tipi.Verso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I mattoni delle scene: battute, immagini, pagine ed elementi con i loro tempi, e le scene già pronte (in città, in locanda, nel negozio, fra
 * compagni) con il gruppo del momento.
 */
class ScenePagineEDatiTest {

    private static final double DELTA = 1e-9;

    @BeforeEach
    void preparaIlGruppo() {
        GruppoGiocatoreMD dati = new GruppoGiocatoreMD();
        GruppoGiocatore.setFornitoreDiDati(() -> dati);
        GruppoGiocatore.azzeraIstanza();
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(new Guerriero("Arsenio", 3));
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(new Mago("Merlino", 3));
    }

    // --- Battute

    @Test
    void laBattutaHaChiParlaIlTestoEUnaDurata() {
        assertThrows(IllegalArgumentException.class, () -> BattutaIntermezzo.di(null, "x"));
        assertThrows(IllegalArgumentException.class, () -> BattutaIntermezzo.di("", "x"));
        assertThrows(IllegalArgumentException.class, () -> BattutaIntermezzo.di("a", null));
        assertThrows(IllegalArgumentException.class, () -> BattutaIntermezzo.di("a", ""));
        BattutaIntermezzo breve = BattutaIntermezzo.di("a", "Ciao");
        assertEquals("a", breve.getIdElemento());
        assertEquals("Ciao", breve.getTesto());
        assertTrue(Double.isNaN(breve.getPuntoX()) && Double.isNaN(breve.getPuntoY()));
        assertFalse(breve.hasInizio());
        assertEquals(BattutaIntermezzo.DURATA_MINIMA, breve.getDurata(), DELTA);
        char[] lunga = new char[500];
        Arrays.fill(lunga, 'x');
        assertEquals(BattutaIntermezzo.DURATA_MASSIMA, BattutaIntermezzo.di("a", new String(lunga)).getDurata(), DELTA);
        BattutaIntermezzo media = BattutaIntermezzo.di("a", "Una frase di media lunghezza, dai.");
        assertEquals(BattutaIntermezzo.SECONDI_BASE + BattutaIntermezzo.SECONDI_PER_CARATTERE * media.getTesto().length(), media.getDurata(), DELTA);
        assertEquals(7.5, breve.perSecondi(7.5).getDurata(), DELTA);
        assertEquals(2.0, breve.daSecondo(2.0).getInizio(), DELTA);
        assertTrue(breve.hasInizio());
    }

    @Test
    void laNuvolaStaInUnPuntoOCentrataMaNonTutteEDue() {
        BattutaIntermezzo daPunto = BattutaIntermezzo.daPunto(0.3, 0.4, "Qui");
        assertNull(daPunto.getIdElemento());
        assertEquals(0.3, daPunto.getPuntoX(), DELTA);
        assertEquals(0.4, daPunto.getPuntoY(), DELTA);
        assertFalse(daPunto.hasPosizioneNuvola());
        assertFalse(daPunto.hasLarghezza());
        daPunto.nuvolaA(0.1, 0.2).conLarghezza(0.5);
        assertTrue(daPunto.hasPosizioneNuvola());
        assertTrue(daPunto.hasLarghezza());
        assertEquals(0.1, daPunto.getNuvolaX(), DELTA);
        assertEquals(0.2, daPunto.getNuvolaY(), DELTA);
        assertEquals(0.5, daPunto.getLarghezza(), DELTA);
        assertFalse(daPunto.isCentrataOrizzontalmente());
        assertThrows(IllegalStateException.class, daPunto::centrataOrizzontalmente);
        BattutaIntermezzo centrata = BattutaIntermezzo.di("a", "Centro").centrataOrizzontalmente();
        assertTrue(centrata.isCentrataOrizzontalmente());
        assertThrows(IllegalStateException.class, () -> centrata.nuvolaA(0, 0));
        BattutaProgrammata programmata = new BattutaProgrammata(centrata, 1, 3);
        assertSame(centrata, programmata.getBattuta());
        assertEquals(1, programmata.getInizio(), DELTA);
        assertEquals(3, programmata.getFine(), DELTA);
        assertTrue(programmata.isVisibileAl(1));
        assertFalse(programmata.isVisibileAl(0.5));
        assertFalse(programmata.isVisibileAl(3));
    }

    // --- Immagini

    @Test
    void leImmaginiDiUnaPagina() {
        ImmagineIntermezzo personaggio = ImmagineIntermezzo.personaggio(TipoPersonaggio.MAGO);
        assertEquals(ImmagineIntermezzo.Tipo.PERSONAGGIO, personaggio.getTipo());
        assertEquals(TipoPersonaggio.MAGO, personaggio.getClassePersonaggio());
        assertEquals("personaggio MAGO", personaggio.toString());
        assertThrows(NullPointerException.class, () -> ImmagineIntermezzo.personaggio(null));
        ImmagineIntermezzo locazione = ImmagineIntermezzo.locazione(TipoLocazione.BOSCO);
        assertEquals(TipoLocazione.BOSCO, locazione.getTipoLocazione());
        assertEquals("locazione BOSCO", locazione.toString());
        assertThrows(NullPointerException.class, () -> ImmagineIntermezzo.locazione(null));
        ImmagineIntermezzo risorsa = ImmagineIntermezzo.risorsa("img/x.gif");
        assertEquals("img/x.gif", risorsa.getRisorsa());
        assertEquals("risorsa img/x.gif", risorsa.toString());
        assertThrows(IllegalArgumentException.class, () -> ImmagineIntermezzo.risorsa(null));
        assertThrows(IllegalArgumentException.class, () -> ImmagineIntermezzo.risorsa(""));
        ImmagineIntermezzo animazione = ImmagineIntermezzo.animazione(Animazione.FUOCO_DA_CAMPO);
        assertEquals(Animazione.FUOCO_DA_CAMPO, animazione.getAnimazione());
        assertEquals("animazione FUOCO_DA_CAMPO", animazione.toString());
        assertThrows(NullPointerException.class, () -> ImmagineIntermezzo.animazione(null));
    }

    @Test
    void unoSpriteSheetSiAnimaConOSenzaSequenza() {
        ImmagineIntermezzo senza = ImmagineIntermezzo.spriteSheet("s.gif", 2, 2, 4.0);
        assertEquals(2, senza.getColonne());
        assertEquals(2, senza.getRighe());
        assertEquals(0, senza.getFotogrammaAl(0));
        assertEquals(1, senza.getFotogrammaAl(0.3));
        assertEquals(0, senza.getFotogrammaAl(1.0), "ricomincia dopo quattro fotogrammi");
        assertEquals(0, senza.getFotogrammaAl(-5));
        assertEquals("sprite sheet s.gif 2x2", senza.toString());
        ImmagineIntermezzo con = ImmagineIntermezzo.spriteSheet("s.gif", 2, 2, 2.0, 3, 1);
        assertEquals(3, con.getFotogrammaAl(0));
        assertEquals(1, con.getFotogrammaAl(0.5));
        assertEquals(3, con.getFotogrammaAl(1.0));
        assertEquals("sprite sheet s.gif 2x2 [3, 1]", con.toString());
        assertThrows(IllegalArgumentException.class, () -> ImmagineIntermezzo.spriteSheet("s.gif", 0, 2, 1.0));
        assertThrows(IllegalArgumentException.class, () -> ImmagineIntermezzo.spriteSheet("s.gif", 2, 0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> ImmagineIntermezzo.spriteSheet("s.gif", 2, 2, 0.0));
        assertThrows(IllegalArgumentException.class, () -> ImmagineIntermezzo.spriteSheet("s.gif", 2, 2, 1.0, 4));
        assertThrows(IllegalArgumentException.class, () -> ImmagineIntermezzo.spriteSheet("s.gif", 2, 2, 1.0, -1));
    }

    // --- Elementi e tappe

    @Test
    void unElementoHaScalaOpacitaBoccaEDurata() {
        assertThrows(IllegalArgumentException.class, () -> ElementoIntermezzo.personaggio(null, TipoPersonaggio.MAGO, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> ElementoIntermezzo.personaggio("", TipoPersonaggio.MAGO, 0, 0));
        ElementoIntermezzo elemento = ElementoIntermezzo.personaggio("a", TipoPersonaggio.MAGO, 0.2, 0.3)
                .conScala(2.0).conOpacita(0.5).guarda(Verso.SINISTRA).conBocca(0.4, -0.1);
        assertEquals("a", elemento.getId());
        assertEquals(ImmagineIntermezzo.Tipo.PERSONAGGIO, elemento.getImmagine().getTipo());
        assertEquals(0.4, elemento.getBoccaX(), DELTA);
        assertEquals(-0.1, elemento.getBoccaY(), DELTA);
        StatoElemento stato = elemento.getStatoAl(5);
        assertEquals(2.0, stato.getScala(), DELTA);
        assertEquals(0.5, stato.getOpacita(), DELTA);
        assertEquals(Verso.SINISTRA, stato.getVerso());
        assertEquals(0, elemento.getDurata(), DELTA);
        // Una tappa con scala e opacità
        ElementoIntermezzo che = ElementoIntermezzo.di("b", ImmagineIntermezzo.risorsa("x.gif"), 0.0, 0.0)
                .poi(Tappa.inSecondi(2).verso(1.0, 1.0).conScala(3).conOpacita(0).guarda(Verso.DESTRA));
        StatoElemento metà = che.getStatoAl(1);
        assertEquals(0.5, metà.getX(), DELTA);
        assertEquals(2.0, metà.getScala(), DELTA);
        assertEquals(0.5, metà.getOpacita(), DELTA);
        assertEquals(2, che.getDurata(), DELTA);
        assertEquals(Verso.DESTRA, che.getStatoAl(10).getVerso());
        assertThrows(IllegalArgumentException.class, () -> Tappa.inSecondi(-1));
        // Ripetuto, dura per sempre
        che.ripeti(TipoRipetizione.CICLICA);
        assertTrue(Double.isInfinite(che.getDurata()));
        // Senza durata una tappa istantanea dà lo stato finale
        ElementoIntermezzo istantaneo = ElementoIntermezzo.di("c", ImmagineIntermezzo.risorsa("x.gif"), 0.0, 0.0).poi(Tappa.inSecondi(0).verso(0.9, 0.9));
        assertEquals(0.9, istantaneo.getStatoAl(0).getX(), DELTA);
        istantaneo.ripeti(TipoRipetizione.AVANTI_E_INDIETRO);
        assertEquals(0, istantaneo.getDurata(), DELTA);
        // Una tappa lunga zero fra due altre
        ElementoIntermezzo con = ElementoIntermezzo.di("d", ImmagineIntermezzo.risorsa("x.gif"), 0.0, 0.0)
                .poi(Tappa.inSecondi(0).verso(0.5, 0.5)).poi(Tappa.inSecondi(1).verso(1, 1));
        assertEquals(0.75, con.getStatoAl(0.5).getX(), DELTA);
        assertEquals(0.5, con.getStatoAl(0).getX(), DELTA);
    }

    // --- Pagine

    @Test
    void laPaginaTienePiuElementiMaUnoPerOgniNome() {
        PaginaIntermezzo vuota = new PaginaIntermezzo();
        assertNull(vuota.getTesto());
        assertNull(vuota.getSfondo());
        assertEquals(TipoStiramento.NESSUNO, vuota.getTipoStiramentoSfondo());
        assertFalse(vuota.isRitaglioSuSfondo());
        assertFalse(vuota.hasDurata());
        assertTrue(vuota.getElementi().isEmpty());
        assertEquals(0, vuota.getDurataContenuto(), DELTA);
        assertEquals(0, vuota.getSecondiPrimaDiAvanzare(0), DELTA);
        assertEquals(8, vuota.getSecondiPrimaDiAvanzare(8), DELTA);
        PaginaIntermezzo pagina = new PaginaIntermezzo("Testo")
                .conSfondo(ImmagineIntermezzo.locazione(TipoLocazione.BOSCO), TipoStiramento.values()[TipoStiramento.values().length - 1])
                .conRitaglioSuSfondo()
                .conElemento(ElementoIntermezzo.personaggio("a", TipoPersonaggio.MAGO, 0.5, 0.5).poi(Tappa.inSecondi(20).verso(0.9, 0.9)))
                .conElemento(ElementoIntermezzo.di("fuoco", ImmagineIntermezzo.animazione(Animazione.FUOCO_DA_CAMPO), 0.5, 0.5)
                        .poi(Tappa.inSecondi(1)).ripeti(TipoRipetizione.CICLICA));
        assertEquals("Testo", pagina.getTesto());
        assertTrue(pagina.isRitaglioSuSfondo());
        assertEquals(2, pagina.getElementi().size());
        assertNotNull(pagina.getElemento("a"));
        assertNull(pagina.getElemento("z"));
        assertThrows(IllegalArgumentException.class, () -> pagina.conElemento(ElementoIntermezzo.personaggio("a", TipoPersonaggio.MAGO, 0, 0)));
        assertThrows(IllegalArgumentException.class, () -> pagina.conBattuta(BattutaIntermezzo.di("assente", "x")));
        pagina.conBattuta(BattutaIntermezzo.daPunto(0.1, 0.1, "Da un punto"));
        pagina.conBattuta(BattutaIntermezzo.di("a", "Una"));
        assertEquals(2, pagina.getBattuteProgrammate().size());
        assertEquals(20, pagina.getDurataContenuto(), DELTA, "l'elemento infinito non conta");
        assertEquals(20, pagina.getSecondiPrimaDiAvanzare(8), DELTA);
        assertEquals(8, pagina.perSecondi(8).getSecondiPrimaDiAvanzare(5) , DELTA);
        assertTrue(pagina.hasDurata());
        assertEquals(8, pagina.getDurata(), DELTA);
        assertThrows(IllegalArgumentException.class, () -> pagina.conRivelazioneDiArtefatto(null, 1, "assente"));
        assertNull(pagina.getRivelazioneArtefatto());
        pagina.conRivelazioneDiArtefatto(null, 7, "a");
        assertEquals(7, pagina.getLivelloMondoRivelazione());
        assertEquals("a", pagina.getIdDestinazioneRivelazione());
    }

    @Test
    void gliEventiDegliIntermezzi() {
        PaginaIntermezzo pagina = new PaginaIntermezzo("x");
        NotificaPaginaIntermezzo notifica = new NotificaPaginaIntermezzo(pagina, 2, 5);
        assertSame(pagina, notifica.getPagina());
        assertEquals(2, notifica.getNumeroPagina());
        assertEquals(5, notifica.getTotalePagine());
        assertEquals("frase", new InternoCreazioneFumetto("frase").getFrase());
        assertEquals(1.5, new InternoFumettoSuccessivo(1.5).getSecondi(), DELTA);
    }

    // --- Scene

    @Test
    void laScenaInCittaConTuttiIMandanti() {
        ScenaInCitta[] scene = {ScenaInCitta.conMandante(), ScenaInCitta.conBorgomastro(), ScenaInCitta.conMandantessa(), ScenaInCitta.conLocandiere(),
                ScenaInCitta.conArmaiolo(), ScenaInCitta.conAlchimista(), ScenaInCitta.conCapitano(), ScenaInCitta.conMoglieDelBardo(),
                ScenaInCitta.con(TipoPersonaggio.EREMITA)};
        for (ScenaInCitta scena : scene) {
            scena.parlaIlMandante("Buongiorno").parlaIlCapo("Salve");
            assertEquals(1, scena.getPagine().size());
        }
        ScenaInCitta conOspite = ScenaInCitta.conMandante();
        assertThrows(IllegalStateException.class, () -> conOspite.parlaLOspite("Io?"));
        GruppoGiocatore.getIstanza().aggiungiOspite(FabbricaPersonaggi.crea(TipoPersonaggio.DONNA, 1));
        GruppoGiocatore.getIstanza().aggiungiOspite(FabbricaPersonaggi.crea(TipoPersonaggio.UOMO, 1));
        ScenaInCitta scena = ScenaInCitta.conMandante();
        scena.parlaIlMandante("Chi è?").parlaLOspite("Io sono l'ospite").parlaIlCapo("Piacere");
        PaginaIntermezzo pagina = scena.getPagine().get(0);
        assertNotNull(pagina.getElemento(ScenaNegozio.ID_OSPITE));
        assertNotNull(pagina.getElemento(ScenaNegozio.ID_OSPITE + 1));
        // Gli ospiti morti non entrano in scena
        Personaggio morto = FabbricaPersonaggi.crea(TipoPersonaggio.UOMO, 1);
        GruppoGiocatore.getIstanza().aggiungiOspite(morto);
        morto.muore("prova");
        assertNull(ScenaInCitta.conMandante().parlaIlMandante("x").getPagine().get(0).getElemento(ScenaNegozio.ID_OSPITE + 2));
    }

    @Test
    void laScenaInLocandaConLocandiereBardoESacerdote() {
        ScenaInLocanda.conLocandiere().parlaIlLocandiere("Benvenuti").parlaIlCapo("Grazie");
        assertEquals(1, ScenaInLocanda.conBardo().parlaIlBardo("C'era una volta").parlaIlCapo("Bello").getPagine().size());
        assertEquals(1, ScenaInLocanda.conSacerdote(TipoPersonaggio.SACERDOTE).parlaIlLocandiere("Pace").getPagine().size());
        assertEquals(1, ScenaInLocanda.conSacerdote(TipoPersonaggio.SACERDOTESSA).parlaIlLocandiere("Pace").getPagine().size());
        assertThrows(IllegalArgumentException.class, () -> ScenaInLocanda.conSacerdote(TipoPersonaggio.MAGO));
    }

    @Test
    void laScenaFraCompagniPerOgniNumeroDiPersonaggi() {
        for (int quanti = 1; quanti <= 6; quanti++) {
            List<Personaggio> personaggi = new ArrayList<>();
            for (int i = 0; i < quanti; i++) {
                personaggi.add(new Guerriero("Eroe" + i, 1));
            }
            ScenaFraCompagni accampamento = ScenaFraCompagni.allAccampamento("Si accampano", personaggi).conLuna("luna.gif").parlaIlPrimo("Uno");
            ScenaFraCompagni locale = ScenaFraCompagni.in(TipoLocazione.BOSCO, "Nel bosco", personaggi).parlaIlPrimo("Due");
            if (quanti > 1) {
                accampamento.parlaIlSecondo("Tre");
                locale.parlaIlSecondo("Quattro");
            } else {
                assertThrows(IllegalStateException.class, () -> locale.parlaIlSecondo("Nessuno"));
            }
            assertEquals(1, accampamento.getPagine().size());
            assertEquals(1, locale.getPagine().size());
            // I personaggi in scena sono al più cinque, più la luna e il fuoco
            assertEquals(Math.min(quanti, 5) + 2, accampamento.getPagine().get(0).getElementi().size());
        }
        assertThrows(IllegalArgumentException.class, () -> ScenaFraCompagni.in(TipoLocazione.BOSCO, "Vuoto", new ArrayList<>()));
    }

    @Test
    void ilNegozioConIlGruppoIniziaAParlareDopoChiEntra() {
        ScenaNegozio negozio = com.threeamigos.foresta.tipi.NegozioInScena.ARMAIOLO.entraIlGruppo()
                .parlaIlNegoziante("Salve").parlaIlCapo("Salve a voi");
        PaginaIntermezzo pagina = negozio.getPagine().get(0);
        List<BattutaProgrammata> battute = pagina.getBattuteProgrammate();
        assertTrue(battute.get(0).getInizio() > 0, "la prima battuta aspetta la fine dell'ingresso");
        assertEquals(com.threeamigos.foresta.tipi.NegozioInScena.ARMAIOLO.getIdNegoziante(), "armaiolo");
        PaginaIntermezzo conCapo = com.threeamigos.foresta.tipi.NegozioInScena.LOCANDA.paginaConCapo("capo", TipoPersonaggio.MAGO);
        assertNotNull(conCapo.getElemento("capo"));
        assertNotNull(conCapo.getElemento("foreground"));
        assertEquals("locandieri", com.threeamigos.foresta.tipi.NegozioInScena.LOCANDA.getNegozianti());
        assertEquals(23, com.threeamigos.foresta.tipi.NegozioInScena.LOCANDA.getOraDiChiusura());
        // Il gruppo vivo da solo, con un morto che non entra
        GruppoGiocatore.getIstanza().getPersonaggio(1).muore("prova");
        assertNotNull(com.threeamigos.foresta.tipi.NegozioInScena.ALCHIMISTA.entraIlGruppo().parlaIlCapo("Io solo").getPagine().get(0).getElemento("personaggio0"));
    }
}
