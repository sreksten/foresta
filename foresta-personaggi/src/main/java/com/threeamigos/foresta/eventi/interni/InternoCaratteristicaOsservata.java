package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.CaratteristicaDiClasse;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * In uno scontro si è visto come una classe reagisce a un tipo di danno, a un incantesimo o a un effetto di stato: un colpo di quel tipo è andato a
 * segno, o l'incantesimo o l'effetto è stato respinto perché il bersaglio è immune. Il bestiario se ne ricorda, se la classe ha davvero quella
 * caratteristica (vedi RegistroBestiario).
 */
public class InternoCaratteristicaOsservata extends EventoBase {

    private final TipoPersonaggio classe;
    private final CaratteristicaDiClasse.Genere genere;
    private final Enum<?> valore;

    /**
     * @param classe la classe del bersaglio
     * @param genere se è un tipo di danno, un incantesimo o un effetto di stato
     * @param valore il {@code TipoDanno}, il {@code TipoIncantesimo} o il {@code TipoEffettoDiStato}
     */
    public InternoCaratteristicaOsservata(TipoPersonaggio classe, CaratteristicaDiClasse.Genere genere, Enum<?> valore) {
        super(TipoEvento.INTERNO_CARATTERISTICA_OSSERVATA);
        this.classe = classe;
        this.genere = genere;
        this.valore = valore;
    }

    public TipoPersonaggio getClasse() {
        return classe;
    }

    public CaratteristicaDiClasse.Genere getGenere() {
        return genere;
    }

    public Enum<?> getValore() {
        return valore;
    }

}
