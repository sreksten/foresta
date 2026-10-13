package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.modellodati.EffettoDiStatoMD;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.PersonaggioBase;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Caratterizzazione di CalcolatoreCombattimento.calcolaDannoRisultante per le interazioni fra tipi di danno e stati del
 * difensore (vedi TipoInterazioneConEffettiDiStato): per ogni difensore (un guerriero, un goblin e un mago), ogni stato (da solo e a coppie) e ogni tipo di
 * danno, con i dadi a seme fisso, si scrive tutto quello che il calcolo produce (danno, interazioni, stati aggiunti e
 * tolti, bandiere) e se ne confronta l'impronta con quella registrata. Serve a poter riscrivere il calcolo senza cambiarne
 * il comportamento; se lo si cambia di proposito, l'impronta si rigenera.
 * <p>
 * Per vedere il testo intero: {@code mvn test -Dtest=InterazioniConGliStatiCaratterizzazioneTest -Dforesta.caratterizzazione=/percorso/file}.
 */
class InterazioniConGliStatiCaratterizzazioneTest {

    /**
     * L'impronta SHA-256 del testo prodotto dal calcolo. Rispetto a come era prima del riordino della catena delle interazioni
     * (e del danno vero, diventato la debolezza di classe) ci sono due differenze volute: un difensore insieme SPAVENTATO e
     * CONFUSO colpito da danno VUOTO subisce la Follia Cosmica (prima no, perché contava solo lo SPAVENTATO; 2 righe su 8316);
     * il danno vero dei non morti non è più fra le interazioni ma nel campo {@code classe} (SACRO=2.0), con lo stesso
     * effetto sul danno.
     */
    private static final String IMPRONTA_ATTESA = "eb1a9b6ae6dfcfbaea53462ea77d503512aca5f90426526c39ee0d0b8281e1ea";

    private static final TipoEffettoDiStato[] STATI = {
            TipoEffettoDiStato.BAGNATO, TipoEffettoDiStato.BRUCIATO, TipoEffettoDiStato.AVVELENATO, TipoEffettoDiStato.SANGUINAMENTO,
            TipoEffettoDiStato.CONGELATO, TipoEffettoDiStato.MALEDETTO, TipoEffettoDiStato.INFETTATO, TipoEffettoDiStato.STORDITO,
            TipoEffettoDiStato.ATTERRATO, TipoEffettoDiStato.RALLENTATO, TipoEffettoDiStato.IMMOBILIZZATO, TipoEffettoDiStato.SPAVENTATO,
            TipoEffettoDiStato.CONFUSO, TipoEffettoDiStato.ACCECATO, TipoEffettoDiStato.ASSORDATO, TipoEffettoDiStato.SILENZIATO,
            TipoEffettoDiStato.MENTE_FRATTURATA};
    // Classi senza debolezze né resistenze nella tabella (TipoPersonaggio), così un ritocco della tabella non cambia l'impronta
    private static final TipoPersonaggio[] DIFENSORI = {TipoPersonaggio.GUERRIERO, TipoPersonaggio.GOBLIN, TipoPersonaggio.MAGO};

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
    }

    @AfterEach
    void ripristina() {
        Dado.ripristina();
    }

    private static Arma arma(TipoDanno tipo) {
        return new Arma() {
            @Override public int getDanni() { return 20; }
            @Override public int getLivello() { return 5; }
            @Override public TipoDanno getTipoDanno() { return tipo; }
            @Override public boolean isIncantata() { return false; }
            @Override public Collection<IncantamentoMD> getIncantamenti() { return new ArrayList<>(); }
        };
    }

    private static void fissaPrimari(Personaggio personaggio) {
        for (TipoAttributo attributo : new TipoAttributo[] {TipoAttributo.FORZA, TipoAttributo.DESTREZZA, TipoAttributo.COSTITUZIONE,
                TipoAttributo.INTELLIGENZA, TipoAttributo.SAGGEZZA, TipoAttributo.CARISMA, TipoAttributo.FORTUNA}) {
            ((PersonaggioBase) personaggio).addModificatore(new ModificatoreAttributoMD(attributo, TipoModificatore.QUANTITA_ASSOLUTA, 12));
        }
    }

    private static String descrivi(Collection<?> elementi) {
        return elementi.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    private static String descriviEffetti(Collection<EffettoDiStatoMD> effetti) {
        return effetti.stream().map(e -> e.getTipoEffettoDiStato() + ":" + e.getDurata() + ":" + e.getDanniNelTempo())
                .collect(Collectors.joining(","));
    }

    static String testoDelCalcolo() {
        StringBuilder testo = new StringBuilder();
        List<TipoEffettoDiStato[]> combinazioni = new ArrayList<>();
        combinazioni.add(new TipoEffettoDiStato[0]);
        for (int i = 0; i < STATI.length; i++) {
            combinazioni.add(new TipoEffettoDiStato[] {STATI[i]});
            for (int j = i + 1; j < STATI.length; j++) {
                combinazioni.add(new TipoEffettoDiStato[] {STATI[i], STATI[j]});
            }
        }
        int seme = 1;
        for (TipoPersonaggio tipoDifensore : DIFENSORI) {
            for (TipoEffettoDiStato[] stati : combinazioni) {
                for (TipoDanno tipoDanno : TipoDanno.values()) {
                    Personaggio attaccante = FabbricaPersonaggi.crea(TipoPersonaggio.LADRO, 5);
                    Personaggio difensore = FabbricaPersonaggi.crea(tipoDifensore, 5);
                    // Le statistiche dei personaggi seguono il seme, ma qui si fissano comunque, così il test non dipende da come si tirano
                    fissaPrimari(attaccante);
                    fissaPrimari(difensore);
                    for (TipoEffettoDiStato stato : stati) {
                        difensore.addEffettoDiStato(stato, 3, 0);
                    }
                    Dado.impostaSeme(seme++);
                    DannoRisultante r = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma(tipoDanno));
                    testo.append(tipoDifensore).append('|').append(Arrays.toString(stati)).append('|').append(tipoDanno)
                            .append(" -> danno=").append(r.getDanno())
                            .append(" interazioni=[").append(descrivi(r.getInterazioni())).append(']')
                            .append(" classe=").append(r.getReazioniDiClasse())
                            .append(" aggiunti=[").append(descriviEffetti(r.getEffettiDiStatoDaAggiungere())).append(']')
                            .append(" tolti=[").append(descrivi(r.getEffettiDiStatoDaRimuovere())).append(']')
                            .append(" grazia=").append(r.isColpoDiGrazia())
                            .append(" cura=").append(r.getCuraAdArea())
                            .append(" sifone=").append(r.getSifoneVitale())
                            .append(" diluizione=").append(r.isDiluizioneEmaticaAttiva())
                            .append(" collasso=").append(r.isCollassoEntropicoAttivo())
                            .append(" mietitura=").append(r.isMietituraAttiva())
                            .append('\n');
                }
            }
        }
        return testo.toString();
    }

    private static String impronta(String testo) throws NoSuchAlgorithmException {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(testo.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Test
    void ilCalcoloDelleInterazioniNonCambia() throws IOException, NoSuchAlgorithmException {
        String testo = testoDelCalcolo();
        String percorso = System.getProperty("foresta.caratterizzazione");
        if (percorso != null) {
            Files.write(Paths.get(percorso), testo.getBytes(StandardCharsets.UTF_8));
        }
        assertEquals(IMPRONTA_ATTESA, impronta(testo), "righe: " + testo.split("\n").length);
    }
}
