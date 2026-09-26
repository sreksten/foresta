package com.threeamigos.foresta.motore.modellodati;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoArtefatto {

    SPADA(SupertipoArtefatto.ARMA, SlotArtefatto.MANO_PRINCIPALE, TipoDanno.TAGLIENTE,"Spada", "impugna"),
    MAZZA(SupertipoArtefatto.ARMA, SlotArtefatto.MANO_PRINCIPALE, TipoDanno.CONTUNDENTE, "Mazza", "brandisce"),
    ASCIA(SupertipoArtefatto.ARMA, SlotArtefatto.MANO_PRINCIPALE, TipoDanno.TAGLIENTE, "Ascia", "impugna"),
    // Arma a due mani. TODO per ora riusa la grafica della spada
    SPADONE(SupertipoArtefatto.ARMA, SlotArtefatto.ENTRAMBE_LE_MANI, TipoDanno.TAGLIENTE, "Spadone", "impugna"),
    LANCIA(SupertipoArtefatto.ARMA, SlotArtefatto.MANO_PRINCIPALE, TipoDanno.PERFORANTE, "Lancia", "impugna"),

    BASTONE_MAGICO(SupertipoArtefatto.ARMA, SlotArtefatto.MANO_PRINCIPALE, TipoDanno.CONTUNDENTE, "Bastone magico", "impugna"),
    /**
     * FIXME se si passa ARMA occorre passare il tipo di danno, ma questo dipenderebbe un po' dall'incantesimo...
     * Ad ogni modo potrebbe essere considerato un'arma secondaria.
     */
    LIBRO_MAGICO(SupertipoArtefatto.POTENZIAMENTO_POTERE_MAGICO, SlotArtefatto.MANO_SECONDARIA, "Libro magico", "porta"),

    SCUDO(SupertipoArtefatto.SCUDO, SlotArtefatto.MANO_SECONDARIA, "Scudo", "porta"),

    ELMO(SupertipoArtefatto.ELMO, SlotArtefatto.TESTA, "Elmo", "indossa"),

    ARMATURA(SupertipoArtefatto.ARMATURA, SlotArtefatto.CORPO, "Armatura", "indossa"),
    VESTE(SupertipoArtefatto.ARMATURA, SlotArtefatto.CORPO, "Veste", "indossa"),

    ANELLO(SupertipoArtefatto.ALTRO, SlotArtefatto.ACCESSORIO, "Anello", "indossa"),
    TALISMANO(SupertipoArtefatto.ALTRO, SlotArtefatto.ACCESSORIO, "Talismano", "possiede"),
    NINNOLO(SupertipoArtefatto.ALTRO, SlotArtefatto.ACCESSORIO, "Ninnolo", "ha con se"),

    INCANTAMENTO(SupertipoArtefatto.INCANTAMENTO, SlotArtefatto.NUCLEO, "Pergamena", "porta con se");

    private final SupertipoArtefatto supertipo;
    private final SlotArtefatto slotArtefatto;
    private final TipoDanno tipoDanno;
    private final String descrizione;
    private final String utilizzo;

    TipoArtefatto(SupertipoArtefatto supertipo, SlotArtefatto slotArtefatto, TipoDanno tipoDanno,
                  String descrizione, String utilizzo) {
        if (supertipo != SupertipoArtefatto.ARMA) {
            throw new IllegalArgumentException("Il Supertipo per l'artefatto deve essere ARMA");
        }
        this.supertipo = supertipo;
        this.slotArtefatto = slotArtefatto;
        this.tipoDanno = tipoDanno;
        this.descrizione = descrizione;
        this.utilizzo = utilizzo;
    }

    TipoArtefatto(SupertipoArtefatto supertipo, SlotArtefatto slotArtefatto,
                  String descrizione, String utilizzo) {
        if (supertipo == SupertipoArtefatto.ARMA) {
            throw new IllegalArgumentException("Se il Supertipo per l'artefatto è ARMA occorre passare anche il TipoDanno");
        }
        this.supertipo = supertipo;
        this.slotArtefatto = slotArtefatto;
        this.tipoDanno = null;
        this.descrizione = descrizione;
        this.utilizzo = utilizzo;
    }

    public SupertipoArtefatto getSupertipo() {
        return supertipo;
    }

    public SlotArtefatto getSlotArtefatto() {
        return slotArtefatto;
    }

    public TipoDanno getTipoDanno() {
        return tipoDanno;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getUtilizzo() {
        return utilizzo;
    }

}
