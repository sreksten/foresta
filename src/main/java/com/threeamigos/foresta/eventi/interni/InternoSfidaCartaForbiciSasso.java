package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;

/**
 * Lo stato della sfida a carta, forbici e sasso (vedi carta_forbici_sasso.md): la sfida comincia ({@link #apre}) o
 * una mano è stata giocata ({@link #mano}). La UI mostra il riquadro con le due mani e il punteggio; all'ultima mano
 * lo lascia vedere un attimo e lo dissolve, perché non copra la mappa.
 */
public class InternoSfidaCartaForbiciSasso extends EventoBase {

    private final MossaCartaForbiciSasso mossaDelGiocatore;
    private final MossaCartaForbiciSasso mossaDellAvversario;
    private final int punteggioDelGiocatore;
    private final int punteggioDellAvversario;
    private final boolean finale;

    private InternoSfidaCartaForbiciSasso(MossaCartaForbiciSasso mossaDelGiocatore,
                                          MossaCartaForbiciSasso mossaDellAvversario, int punteggioDelGiocatore,
                                          int punteggioDellAvversario, boolean finale) {
        super(TipoEvento.INTERNO_SFIDA_CARTA_FORBICI_SASSO);
        this.mossaDelGiocatore = mossaDelGiocatore;
        this.mossaDellAvversario = mossaDellAvversario;
        this.punteggioDelGiocatore = punteggioDelGiocatore;
        this.punteggioDellAvversario = punteggioDellAvversario;
        this.finale = finale;
    }

    /**
     * La sfida comincia: entrambi con il sasso in mano, zero a zero.
     */
    public static InternoSfidaCartaForbiciSasso apre() {
        return new InternoSfidaCartaForbiciSasso(MossaCartaForbiciSasso.SASSO, MossaCartaForbiciSasso.SASSO, 0, 0, false);
    }

    /**
     * Una mano è stata giocata: le due mosse e il punteggio dopo la mano; {@code finale} se con questa mano la sfida è
     * finita, e il riquadro può dissolversi.
     */
    public static InternoSfidaCartaForbiciSasso mano(MossaCartaForbiciSasso mossaDelGiocatore,
                                                     MossaCartaForbiciSasso mossaDellAvversario, int punteggioDelGiocatore,
                                                     int punteggioDellAvversario, boolean finale) {
        return new InternoSfidaCartaForbiciSasso(mossaDelGiocatore, mossaDellAvversario, punteggioDelGiocatore,
                punteggioDellAvversario, finale);
    }

    public MossaCartaForbiciSasso getMossaDelGiocatore() {
        return mossaDelGiocatore;
    }

    public MossaCartaForbiciSasso getMossaDellAvversario() {
        return mossaDellAvversario;
    }

    public int getPunteggioDelGiocatore() {
        return punteggioDelGiocatore;
    }

    public boolean isFinale() {
        return finale;
    }

    public int getPunteggioDellAvversario() {
        return punteggioDellAvversario;
    }
}
