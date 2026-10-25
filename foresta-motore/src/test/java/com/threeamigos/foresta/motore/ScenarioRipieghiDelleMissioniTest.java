package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.eventi.notifiche.NotificaAggiornamentoStatoMissione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.IlNegozioDellArmaiolo;
import com.threeamigos.foresta.missioni.incarichi.LOggettoSmarrito;
import com.threeamigos.foresta.mappa.CoordinateMD;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli incarichi quando la mappa non offre il posto che cercano (niente rovine, né boschi o paludi da trasformare), la
 * copia di un incarico ripetibile che annuncia la partenza e la missione che si completa due volte.
 */
class ScenarioRipieghiDelleMissioniTest {

    private static final String FEDE = "F;fede nuziale;il mugnaio;ROVINE;15;Al ritorno la fede non c'era più.;E se se ne accorge?;Meglio di no.;Eccola!";

    /**
     * Rade al suolo la foresta: niente rovine, boschi e paludi, solo radure.
     */
    private static void sgombraLaMappa() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                TipoLocazione tipo = Foresta.getLocazione(x, y);
                if (tipo == TipoLocazione.ROVINE || tipo == TipoLocazione.BOSCO || tipo == TipoLocazione.PALUDE) {
                    CostruttoreDellaForesta.costruisciLocazione(new CoordinateMD(x, y), TipoLocazione.RADURA);
                }
            }
        }
    }

    private static <T extends Missione> T trova(Class<T> tipo) {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).map(tipo::cast).findFirst()
                .orElseThrow(AssertionError::new);
    }

    @Test
    void senzaNeancheUnBoscoLOggettoSmarritoSiPerdeInCitta() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(461)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            sgombraLaMappa();
            LOggettoSmarrito smarrito = trova(LOggettoSmarrito.class);
            smarrito.aggiungiProprieta("PARAMETRO_" + LOggettoSmarrito.SMARRITO, FEDE);
            smarrito.controllaPreLocazione();
            smarrito.segnaIntermezzoPassoMostrato("INCARICO");
            smarrito.controllaInLocazione();

            assertTrue(smarrito.isAttiva());
            CoordinateMD citta = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA);
            assertEquals(citta, smarrito.getPosto());
            assertNotNull(smarrito.getCasella());
            assertTrue(smarrito.getDescrizione().startsWith("Cerca la fede nuziale"), smarrito.getDescrizione());
        }
    }

    @Test
    void senzaPostoPerLeRovineLaBottegaNonSiOffreESenzaRovineLeCostruisceDovunque() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(462)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            IlNegozioDellArmaiolo armaiolo = trova(IlNegozioDellArmaiolo.class);
            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(armaiolo.getCittaAssegnata()));
            sgombraLaMappa();

            armaiolo.controllaPreLocazione();
            assertEquals("INCARICO", armaiolo.getPassoCorrente(), "nessun posto: l'incarico non si offre, senza eccezioni");

            // Basta un bosco, ovunque nella mappa, anche fuori dal quadrante della città
            CoordinateMD citta = Foresta.getCoordinateLocazioneUnica(armaiolo.getCittaAssegnata());
            CoordinateMD lontano = null;
            for (int x = 0; x < Foresta.getDimensioneX() && lontano == null; x++) {
                for (int y = 0; y < Foresta.getDimensioneY() && lontano == null; y++) {
                    CoordinateMD c = new CoordinateMD(x, y);
                    if (Foresta.getLocazione(c) == TipoLocazione.RADURA && !c.equals(citta)
                            && Quadrante.di(c) != Quadrante.di(citta)) {
                        lontano = c;
                    }
                }
            }
            assertNotNull(lontano);
            CostruttoreDellaForesta.costruisciLocazione(lontano, TipoLocazione.BOSCO);
            armaiolo.controllaPreLocazione();
            assertEquals("ACCETTAZIONE", armaiolo.getPassoCorrente());
            assertEquals(lontano, armaiolo.getCovo());
            assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(lontano));
        }
    }

    @Test
    void laCopiaDiUnIncaricoRipetibileAnnunciaLaNuovaMissione() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(463)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            LOggettoSmarrito originale = trova(LOggettoSmarrito.class);
            originale.fallisciMissione();
            LOggettoSmarrito copia = RegistroMissioni.getTutteLeMissioni().stream().filter(LOggettoSmarrito.class::isInstance)
                    .map(LOggettoSmarrito.class::cast).filter(m -> m != originale).findFirst().orElseThrow(AssertionError::new);
            assertNotEquals(originale.getId(), copia.getId());
            partita.eventi().ascolta(NotificaAggiornamentoStatoMissione.class);
            partita.eventi().svuota();

            copia.attivaMissione();

            assertTrue(partita.eventi().tutti(NotificaAggiornamentoStatoMissione.class).stream()
                    .anyMatch(n -> "NUOVA MISSIONE".equals(n.getEtichetta())));
        }
    }

    @Test
    void unaMissioneCompletaDueVolteNonRipagaNeRiannuncia() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(464)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            LOggettoSmarrito smarrito = trova(LOggettoSmarrito.class);
            partita.eventi().ascolta(NotificaAggiornamentoStatoMissione.class);
            partita.eventi().svuota();

            smarrito.completaMissione();
            long annunci = partita.eventi().tutti(NotificaAggiornamentoStatoMissione.class).stream()
                    .filter(n -> "MISSIONE COMPLETATA".equals(n.getEtichetta())).count();
            int completate = RegistroMissioni.getMissioniCompletate().size();
            long copie = RegistroMissioni.getTutteLeMissioni().stream().filter(LOggettoSmarrito.class::isInstance).count();
            smarrito.completaMissione();

            assertEquals(1, annunci);
            assertEquals(annunci, partita.eventi().tutti(NotificaAggiornamentoStatoMissione.class).stream()
                    .filter(n -> "MISSIONE COMPLETATA".equals(n.getEtichetta())).count());
            assertEquals(completate, RegistroMissioni.getMissioniCompletate().size());
            assertEquals(copie, RegistroMissioni.getTutteLeMissioni().stream().filter(LOggettoSmarrito.class::isInstance).count());
        }
    }
}
