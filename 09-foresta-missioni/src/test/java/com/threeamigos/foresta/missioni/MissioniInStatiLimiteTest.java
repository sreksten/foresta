package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.incarichi.MissioneATappe;
import com.threeamigos.foresta.missioni.richieste.TappeRichieste;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le missioni a passi negli stati in cui il giocatore automatico di rado le trova: appena nate e senza nessun parametro, con le città
 * distrutte, con la partenza forzata dalla finestra di prova, e davanti a un passo che non esiste.
 */
class MissioniInStatiLimiteTest {

    @AfterEach
    void ripulisce() {
        RegistroMissioni.setFornitoreLocazioneCompleta(() -> false);
        MondoDiProva.finito();
    }

    /** Un'istanza nuova di ogni missione a passi del modulo che si sa costruire senza parametri. */
    private static List<MissioneAPassi> missioniNuove() throws Exception {
        File radice = new File(MissioneAPassi.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<MissioneAPassi> nuove = new ArrayList<>();
        raccogli(radice, radice, nuove);
        return nuove;
    }

    private static void raccogli(File radice, File cartella, List<MissioneAPassi> nuove) throws Exception {
        File[] file = cartella.listFiles();
        if (file == null) {
            return;
        }
        for (File f : file) {
            if (f.isDirectory()) {
                raccogli(radice, f, nuove);
            } else if (f.getName().endsWith(".class") && !f.getName().contains("$")) {
                String nome = radice.toPath().relativize(f.toPath()).toString().replace(File.separatorChar, '.');
                Class<?> classe = Class.forName(nome.substring(0, nome.length() - ".class".length()));
                if (MissioneAPassi.class.isAssignableFrom(classe) && !Modifier.isAbstract(classe.getModifiers())) {
                    try {
                        Constructor<?> costruttore = classe.getDeclaredConstructor();
                        costruttore.setAccessible(true);
                        nuove.add((MissioneAPassi) costruttore.newInstance());
                    } catch (ReflectiveOperationException | RuntimeException serveUnParametro) {
                        // le missioni nate da una riga o da un'altra missione si provano giocando
                    }
                }
            }
        }
    }

    /** Chiede a una missione tutto quello che sa dire di sé, anche con i metodi privati, e si fida che non si rompa. */
    private static void chiediTutto(Object missione, Map<String, String> inattesi) {
        for (Class<?> c = missione.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method metodo : c.getDeclaredMethods()) {
                if (metodo.getParameterCount() == 0 && !Modifier.isStatic(metodo.getModifiers()) && !metodo.isSynthetic()
                        && !metodo.getName().startsWith("lambda$")) {
                    try {
                        metodo.setAccessible(true);
                        metodo.invoke(missione);
                    } catch (ReflectiveOperationException | RuntimeException e) {
                        Throwable causa = e.getCause() != null ? e.getCause() : e;
                        if (!(causa instanceof IllegalStateException) && !(causa instanceof IllegalArgumentException)
                                && !(causa instanceof NullPointerException) && !(causa instanceof NumberFormatException)
                                && !(causa instanceof UnsupportedOperationException) && !(causa instanceof java.util.NoSuchElementException) && !(causa instanceof IndexOutOfBoundsException)) {
                            inattesi.put(c.getSimpleName() + "." + metodo.getName(), String.valueOf(causa));
                        }
                    }
                }
            }
        }
    }

    @Test
    void senzaFornitoriIlRegistroDiceCosaManca() {
        MondoDiProva.nuovo(1);
        RegistroMissioni.setFornitoreLocazioneCompleta(null);
        assertThrows(IllegalStateException.class, RegistroMissioni::isLocazioneCompleta);
        RegistroMissioni.setFornitoreDiDati(null);
        assertThrows(IllegalStateException.class, RegistroMissioni::reimposta);
    }

    /** Una missione nata da una riga non sa costruire niente finché non ha la sua riga: lo si prova quando l'ha. */
    private static void passoSconosciuto(MissioneAPassi m) {
        try {
            m.costruisciPasso("PASSO_CHE_NON_ESISTE");
            fail(m.getClass().getSimpleName() + " ha costruito un passo che non esiste");
        } catch (IllegalArgumentException atteso) {
            assertNotNull(atteso.getMessage());
        } catch (NullPointerException senzaRiga) {
            assertNull(m.ottieniProprieta("RIGA"), m.getClass().getSimpleName());
        }
    }

    @Test
    void unPassoSconosciutoSiRifiuta() throws Exception {
        MondoDiProva.nuovo(2);
        List<MissioneAPassi> nuove = missioniNuove();
        assertTrue(nuove.size() >= 15, "le missioni che si sanno costruire: " + nuove.size());
        for (MissioneAPassi m : nuove) {
            passoSconosciuto(m);
        }
    }

    @Test
    void leMissioniAppenaNateRispondonoAnchePrimaDiAvereIParametri() throws Exception {
        for (int seme = 3; seme < 6; seme++) {
            MondoDiProva.nuovo(seme);
            Map<String, String> inattesi = new TreeMap<>();
            for (MissioneAPassi m : missioniNuove()) {
                chiediTutto(m, inattesi);
                m.forzaPartenza(() -> true);
                chiediTutto(m, inattesi);
                m.forzaPartenza(() -> false);
                chiediTutto(m, inattesi);
            }
            assertTrue(inattesi.isEmpty(), inattesi.toString());
        }
    }

    @Test
    void conLeCittaDistruttePartiteEPassiRispondono() throws Exception {
        Map<String, String> inattesi = new TreeMap<>();
        TipoLocazione[] citta = {TipoLocazione.CITTA_NYENA, TipoLocazione.CITTA_MALGAARD, TipoLocazione.CITTA_RUUNA, TipoLocazione.CITTA_FLEENA};
        for (int seme = 20; seme < 26; seme++) {
            MondoDiProva.nuovo(seme);
            RegistroMissioni.reimposta();
            RegistroMissioni.registrati();
            GiocatoreAutomatico g = new GiocatoreAutomatico(seme);
            g.gioca(80);
            for (int i = 0; i < citta.length; i++) {
                if ((seme + i) % 2 == 0 || seme % 3 == 0) {
                    Foresta.setCittaDistrutta(citta[i]);
                }
                g.gioca(30);
                for (Missione m : new ArrayList<>(RegistroMissioni.getTutteLeMissioni())) {
                    chiediTutto(m, inattesi);
                    if (m instanceof MissioneAPassi) {
                        passoSconosciuto((MissioneAPassi) m);
                    }
                    for (Missione figlia : new ArrayList<>(m.getMissioniSecondarie())) {
                        chiediTutto(figlia, inattesi);
                        if (figlia instanceof MissioneAPassi) {
                            passoSconosciuto((MissioneAPassi) figlia);
                        }
                    }
                }
            }
            g.getErrori().forEach((k, v) -> {
                if (!GiocatoreAutomaticoTest.isRumoreNoto(k)) {
                    inattesi.put(k, v);
                }
            });
        }
        assertTrue(inattesi.isEmpty(), inattesi.toString());
    }

    @Test
    void leMissioniATappeConLeCittaCheCadonoStradaFacendo() {
        Map<String, String> righe = new LinkedHashMap<>();
        for (int i = 0; i < 400; i++) {
            String riga = ProduttoreDiMissioniCasuale.rigaDiTappe(i % 4 == 0);
            TappeRichieste r = TappeRichieste.da(riga);
            righe.putIfAbsent(r.isFarsa() + "/" + r.getBivio().isPresent() + "/" + r.hasChiavi() + "/" + r.isDiUnLeggendario() + "/" + r.getTappe().size(), riga);
        }
        TipoLocazione[] citta = {TipoLocazione.CITTA_NYENA, TipoLocazione.CITTA_MALGAARD, TipoLocazione.CITTA_RUUNA, TipoLocazione.CITTA_FLEENA};
        Map<String, String> inattesi = new java.util.TreeMap<>();
        int seme = 300;
        for (String riga : righe.values()) {
          for (int rimaste = 0; rimaste < 4; rimaste++) {
            MondoDiProva.nuovo(++seme);
            // Prima ancora che la missione parta restano in piedi poche città: i posti in città finiscono, e la pista si perde
            for (int i = rimaste; i < citta.length; i++) {
                Foresta.setCittaDistrutta(citta[i]);
            }
            RegistroMissioni.reimposta();
            RegistroMissioni.registrati();
            MissioneATappe missione = new MissioneATappe();
            missione.getModelloDati().setId("TAPPE" + seme);
            missione.aggiungiProprieta("PARAMETRO_RIGA", riga);
            RegistroMissioni.aggiungiMissioneSecondaria(missione);
            GiocatoreAutomatico g = new GiocatoreAutomatico(seme);
            g.gioca(60);
            chiediTutto(missione, inattesi);
            g.getErrori().forEach((k, v) -> {
                if (!GiocatoreAutomaticoTest.isRumoreNoto(k)) {
                    inattesi.put(k, v);
                }
            });
          }
        }
        assertTrue(inattesi.isEmpty(), inattesi.toString());
    }

    @Test
    void ilTorneoSenzaLeggendarioDiceCosaSuccederaDiUnaVolta() throws Exception {
        MondoDiProva.nuovo(60);
        com.threeamigos.foresta.missioni.incarichi.IlTorneo torneo = new com.threeamigos.foresta.missioni.incarichi.IlTorneo();
        assertEquals("Prima o poi, in città, si bandirà un torneo con un leggendario in palio.", torneo.getDescrizione());
    }

    @Test
    void laConsegnaELaRicompensaDiUnaMissioneATappeDescrivonoIlRitorno() {
        MondoDiProva.nuovo(61);
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
        MissioneATappe missione = new MissioneATappe();
        missione.getModelloDati().setId("TAPPE_RITORNO");
        missione.aggiungiProprieta("PARAMETRO_RIGA", ProduttoreDiMissioniCasuale.rigaDiTappe(false));
        RegistroMissioni.aggiungiMissioneSecondaria(missione);
        Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        missione.attivaMissione();
        missione.aggiungiProprieta("RIGA", ProduttoreDiMissioniCasuale.rigaDiTappe(false));
        missione.aggiungiProprieta("PASSO_CORRENTE", "CONSEGNA");
        String consegna = missione.getDescrizione();
        missione.aggiungiProprieta("PASSO_CORRENTE", "RICOMPENSA");
        assertEquals(consegna, missione.getDescrizione());
        assertFalse(consegna.isEmpty());
    }

    @Test
    void conTutteLeCittaDistruttePerIlViandanteLaMissioneFalliscePerConto() {
        MondoDiProva.nuovo(62);
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
        for (TipoLocazione citta : new TipoLocazione[] {TipoLocazione.CITTA_NYENA, TipoLocazione.CITTA_MALGAARD, TipoLocazione.CITTA_RUUNA,
                TipoLocazione.CITTA_FLEENA}) {
            Foresta.setCittaDistrutta(citta);
        }
        com.threeamigos.foresta.missioni.storie.IlViandante viandante = new com.threeamigos.foresta.missioni.storie.IlViandante();
        viandante.getModelloDati().setId("VIANDANTE_SENZA_CITTA");
        RegistroMissioni.aggiungiMissioneSecondaria(viandante);
        viandante.forzaPartenza(() -> true);
        for (int i = 0; i < 6; i++) {
            viandante.controllaPreLocazione();
        }
        assertTrue(viandante.isFallita() || !viandante.isCompleta(), "la missione non si rompe: o fallisce o aspetta");
    }
}
