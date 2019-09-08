/*
 * Copyright (C) 2008 Kalle Kivimaa.
 * Use GPL to distribute and modify.
 */
package fi.kivimaa.patu;

import java.text.ParseException;
import java.util.Date;

public class Perustietue extends Tietue {
    String versionumero;
    String tilinumero;
    String tiliotteenNumero;
    String vuosi;
    Date tilioteKausiAlku;
    Date tilioteKausiLoppu;
    Date muodostamisAika;
    String asiakasTunnus;
    Date alkusaldonPaivays;
    double tiliotteenAlkusaldo;
    int tiliotteenTietueidenLukumaara;
    String tilinValuutanTunnus;
    String tilinNimi;
    double tilinLimiitti;
    String tilinomistajanNimi;
    String yhteydenottotieto1;
    String yhteydenottotieto2;
    String pankkikohtaistaTietoa;
    String IBAN;
    String BIC;

    public Perustietue( String line ) throws ParseException {
        super(line);
        versionumero = line.substring(6, 9).trim();
        tilinumero = line.substring(9, 23).trim();
        tiliotteenNumero = line.substring(23, 26).trim();
        vuosi = line.substring(26, 28).trim();
        tilioteKausiAlku = format.parse( line.substring(26, 32).trim() );
        tilioteKausiLoppu = format.parse( line.substring(32, 38).trim() );
        muodostamisAika = timeFormat.parse( line.substring(38, 48).trim() );
        asiakasTunnus = line.substring(48, 65).trim();
        alkusaldonPaivays = format.parse( line.substring(65, 71).trim() );
        tiliotteenAlkusaldo = Double.parseDouble( line.substring(71, 90) ) / 100.0D;
        tiliotteenTietueidenLukumaara = Integer.parseInt( line.substring(90, 96) );
        tilinValuutanTunnus = line.substring(96, 99).trim();
        tilinNimi = line.substring(99, 129).trim();
        tilinLimiitti = Double.parseDouble( line.substring(129, 147) ) / 100.0D;
        tilinomistajanNimi = line.substring(147, 182).trim();
        yhteydenottotieto1 = line.substring(182, 222).trim();
        yhteydenottotieto2 = line.substring(222, 262).trim();
        pankkikohtaistaTietoa = line.substring(262, 292).trim();
        IBAN = line.substring(292, 310).trim();
        BIC = line.substring(310, 321).trim();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( super.toString() );
        sb.append( "Versionumero: " ).append( versionumero ).append( "\n" );
        sb.append( "Tilinumero: " ).append( tilinumero ).append( "\n" );
        sb.append( "Numero: " ).append( tiliotteenNumero ).append( "\n" );
        sb.append( "Kausi: " ).append( tilioteKausiAlku ).append( " - " ).append( tilioteKausiLoppu ).append( "\n" );
        sb.append( "Muodostamisaika: " ).append( muodostamisAika ).append( "\n" );
        sb.append( "Asiakastunnus: " ).append( asiakasTunnus ).append( "\n" );
        sb.append( "Alkusaldon päiväys: " ).append( alkusaldonPaivays ).append( "\n" );
        sb.append( "Alkusaldo: " ).append( tiliotteenAlkusaldo ).append( "\n" );
        sb.append( "Tietueiden lkm: " ).append( tiliotteenTietueidenLukumaara ).append( "\n" );
        sb.append( "Valuutta: " ).append( tilinValuutanTunnus ).append( "\n" );
        sb.append( "Tilin nimi: " ).append( tilinNimi ).append( "\n" );
        sb.append( "Limiitti: " ).append( tilinLimiitti ).append( "\n" );
        sb.append( "Tilinomistaja: " ).append( tilinomistajanNimi ).append( "\n" );
        sb.append( "Yhteydenotto 1: " ).append( yhteydenottotieto1 ).append( "\n" );
        sb.append( "Yhteydenotto 2: " ).append( yhteydenottotieto2 ).append( "\n" );
        sb.append( "Pankkikohtaista: " ).append( pankkikohtaistaTietoa ).append( "\n" );
        sb.append( "IBAN: " ).append( IBAN ).append( "\n" );
        sb.append( "BIC: " ).append( BIC ).append( "\n" );
        return sb.toString();
    }
}
