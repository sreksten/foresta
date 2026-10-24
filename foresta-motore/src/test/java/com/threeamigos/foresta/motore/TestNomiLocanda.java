package com.threeamigos.foresta.motore;

import java.util.List;
import com.threeamigos.foresta.testi.ProduttoreDiLocandeCasuale;
import com.threeamigos.foresta.testi.ProduttoriDiTesti;

public class TestNomiLocanda extends TestGrammatiche {

       public static void main(String[] args) throws Exception {
           ProduttoriDiTesti.resetProduzioni();
            List<ProduttoreDiLocandeCasuale.DatiLocanda> datiLocanda = ProduttoreDiLocandeCasuale.getDatiLocanda(15);

            for (ProduttoreDiLocandeCasuale.DatiLocanda datiLocandaCorrente : datiLocanda) {
            	System.out.println(datiLocandaCorrente.getNome());
                System.out.println(datiLocandaCorrente.getDialogo());
                System.out.println(datiLocandaCorrente.getRecensione());
                System.out.println("------------------------------");
            }
        }

}
