package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tattiche.Decisione;
import com.threeamigos.foresta.tattiche.EsecutoreTattiche;
import com.threeamigos.foresta.tattiche.RegistroTattiche;
import com.threeamigos.foresta.tattiche.TatticheDeiPersonaggi;

import java.util.ArrayList;
import java.util.List;

/**
 * Il round a coppie del gioco (vedi tattiche_di_combattimento_automatiche.md §3 e {@link StatoDelCombattimento}) per le simulazioni che
 * lavorano su liste di personaggi e non sulla partita vera: i due gruppi agiscono a turno secondo il <b>piano del round</b>, e a fine
 * round passa un turno di effetti di stato.
 * <ul>
 * <li>A inizio round si contano i personaggi in grado di agire (<i>n</i> i miei, <i>m</i> gli avversari) e il piano è di max(n, m)
 * coppie: alla coppia <i>k</i> agisce il mio <i>k</i>-esimo (se <i>k</i> &lt; <i>n</i>) e poi l'avversario <i>k</i>-esimo (se
 * <i>k</i> &lt; <i>m</i>). Il piano non si ricalcola se qualcuno cade: il posto di chi non c'è più si salta.</li>
 * <li>Chi agisce lo decide il round-robin di ogni gruppo: dopo l'ultimo che ha agito, il prossimo vivo (il puntatore non si azzera fra un
 * round e l'altro).</li>
 * <li>Chi è atterrato, congelato o stordito salta il turno, ma il suo posto nel piano è usato.</li>
 * <li>Non c'è una "risposta": l'avversario che tocca agisce con la sua tattica, la stessa del gioco (la sua o quella base della classe,
 * {@link com.threeamigos.foresta.tattiche.RegistroTattiche}), eseguita da {@link EsecutoreTattiche}: sceglie da sé il bersaglio (prima il Mago, poi
 * il Sacerdote, l'Elfo, il Bardo, altrimenti uno a caso) e se lanciare un incantesimo.</li>
 * <li>Se muore il capo (il primo dei miei) la partita è persa; se cade l'ultimo avversario lo scontro è vinto e nessuno agisce più.</li>
 * </ul>
 * Chi decide che cosa fa un mio personaggio quando tocca a lui è un {@link Decisore}.
 */
final class RoundASimulato {

    /**
     * Che cosa fa un mio personaggio quando il piano tocca a lui
     */
    interface Decisore {
        void agisce(Personaggio chi, RoundASimulato round);

        /**
         * Dopo ogni azione, dei miei o degli avversari (per esempio per assegnare l'esperienza di chi è caduto)
         */
        default void dopoOgniAzione(RoundASimulato round) {
        }
    }

    enum Esito {
        IN_CORSO, VITTORIA, SCONFITTA
    }

    private final List<Personaggio> miei;
    private final List<Personaggio> avversari;
    private final Gruppo gruppoDeiMiei = new Gruppo() {
        @Override
        public boolean isGruppoGiocatore() {
            return true;
        }
    };
    private final Gruppo gruppoDegliAvversari = new Gruppo() {
        @Override
        public boolean isGruppoGiocatore() {
            return false;
        }
    };
    private final EsecutoreTattiche esecutore;
    private Personaggio ultimoMio;
    private Personaggio ultimoAvversario;
    private int round = 0;
    private int azioni = 0;

    /**
     * @param miei      i personaggi del giocatore; il primo è il capo
     * @param avversari gli avversari
     */
    RoundASimulato(List<Personaggio> miei, List<Personaggio> avversari) {
        this.miei = miei;
        this.avversari = avversari;
        // Il primo che entra è il capo
        miei.forEach(gruppoDeiMiei::aggiungiPersonaggio);
        avversari.forEach(gruppoDegliAvversari::aggiungiPersonaggio);
        // Uno scontro nuovo: la memoria dello scontro (tentativi falliti, immunità scoperte, voci usate) riparte da zero
        StatoDelCombattimento.getIstanza().reimposta();
        esecutore = new EsecutoreTattiche(gruppoDeiMiei, gruppoDegliAvversari, StatoDelCombattimento.getIstanza(), (uccisore, vittima) -> {
        });
    }

    /**
     * I round cominciati finora
     */
    int getRound() {
        return round;
    }

    /**
     * Le azioni compiute finora, dei miei e degli avversari (anche quelle di chi ha saltato il turno non si contano)
     */
    int getAzioni() {
        return azioni;
    }

    /**
     * Per i decisori che fanno agire i miei con una tattica, come il gioco: la stessa esecuzione degli avversari
     *
     * @return la decisione presa, o null se non c'era niente da fare
     */
    Decisione agisciConLaTattica(Personaggio chi, TatticaMD tattica) {
        return esecutore.agisci(chi, tattica);
    }

    List<Personaggio> mieiVivi() {
        return vivi(miei);
    }

    List<Personaggio> avversariVivi() {
        return vivi(avversari);
    }

    Personaggio primoAvversarioVivo() {
        for (Personaggio p : avversari) {
            if (!p.isFuoriCombattimento()) {
                return p;
            }
        }
        return null;
    }

    Esito esito() {
        if (!miei.get(0).isVivo() || mieiVivi().isEmpty()) {
            return Esito.SCONFITTA;
        }
        return avversariVivi().isEmpty() ? Esito.VITTORIA : Esito.IN_CORSO;
    }

    /**
     * Esegue un round intero (o fino a che lo scontro finisce)
     */
    Esito eseguiRound(Decisore decisore) {
        round++;
        allineaIlRoundDelloStato();
        int n = mieiVivi().size();
        int m = avversariVivi().size();
        for (int k = 0; k < Math.max(n, m); k++) {
            if (k < n) {
                Esito esito = agisceUnoDeiMiei(decisore);
                if (esito != Esito.IN_CORSO) {
                    return esito;
                }
            }
            if (k < m) {
                Esito esito = agisceUnAvversario(decisore);
                if (esito != Esito.IN_CORSO) {
                    return esito;
                }
            }
        }
        return trascorriTurnoEffettiDiStato();
    }

    /**
     * Il numero del round dello stato del combattimento è quello che leggono le condizioni delle tattiche (ROUND, ROUND_DALL_ULTIMO_USO) e che si
     * ricorda per le voci usate. Nel gioco lo fa avanzare la lista dei turni (StatoDelCombattimento.registraAzione), che qui non c'è perché il giro lo
     * fa questa classe: lo si porta al passo con il proprio contatore, altrimenti resta a 1 e quelle condizioni non scattano mai.
     */
    private void allineaIlRoundDelloStato() {
        try {
            java.lang.reflect.Field campo = StatoDelCombattimento.class.getDeclaredField("round");
            campo.setAccessible(true);
            campo.setInt(StatoDelCombattimento.getIstanza(), round);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("StatoDelCombattimento non ha più il campo round", e);
        }
    }

    private Esito agisceUnoDeiMiei(Decisore decisore) {
        Personaggio chi = prossimo(miei, ultimoMio);
        if (chi == null) {
            return esito();
        }
        ultimoMio = chi;
        if (!isIncapaceDiAgire(chi)) {
            azioni++;
            decisore.agisce(chi, this);
            decisore.dopoOgniAzione(this);
        }
        return esito();
    }

    private Esito agisceUnAvversario(Decisore decisore) {
        Personaggio chi = prossimo(avversari, ultimoAvversario);
        if (chi == null) {
            return esito();
        }
        ultimoAvversario = chi;
        if (!isIncapaceDiAgire(chi) && !mieiVivi().isEmpty()) {
            azioni++;
            esecutore.agisci(chi, TatticheDeiPersonaggi.di(chi) != null ? TatticheDeiPersonaggi.di(chi) : RegistroTattiche.perClasse(chi.getClasse()));
            decisore.dopoOgniAzione(this);
        }
        return esito();
    }

    /**
     * A fine round: i danni e la durata degli effetti di stato, la durata dei modificatori a turni (LocazioneBase.trascorriTurnoEffettiDiStato)
     */
    private Esito trascorriTurnoEffettiDiStato() {
        for (Personaggio p : mieiVivi()) {
            p.applicaDanniDaEffettiDiStato();
            p.riduciEffettiDiStato();
            p.riduciModificatori(UnitaDurataModificatore.TURNI);
        }
        if (!miei.get(0).isVivo()) {
            return Esito.SCONFITTA;
        }
        for (Personaggio p : avversariVivi()) {
            p.applicaDanniDaEffettiDiStato();
            p.riduciEffettiDiStato();
            p.riduciModificatori(UnitaDurataModificatore.TURNI);
        }
        return esito();
    }

    private static Personaggio prossimo(List<Personaggio> gruppo, Personaggio ultimo) {
        int partenza = gruppo.indexOf(ultimo);
        for (int i = 1; i <= gruppo.size(); i++) {
            Personaggio candidato = gruppo.get((partenza + i + gruppo.size()) % gruppo.size());
            if (!candidato.isFuoriCombattimento()) {
                return candidato;
            }
        }
        return null;
    }

    private static boolean isIncapaceDiAgire(Personaggio p) {
        return p.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO) || p.hasEffettoDiStato(TipoEffettoDiStato.CONGELATO)
                || p.hasEffettoDiStato(TipoEffettoDiStato.STORDITO);
    }

    private static List<Personaggio> vivi(List<Personaggio> personaggi) {
        List<Personaggio> vivi = new ArrayList<>();
        for (Personaggio p : personaggi) {
            if (!p.isFuoriCombattimento()) {
                vivi.add(p);
            }
        }
        return vivi;
    }
}
