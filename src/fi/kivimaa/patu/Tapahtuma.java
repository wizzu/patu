/*
 * Copyright (C) 2008 Kalle Kivimaa.
 * Use GPL to distribute and modify.
 */
package fi.kivimaa.patu;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Tapahtuma extends Tietue {
    
    int tapahtumanNumero;
    String arkistointitunnus;
    Date kirjauspaiva;
    Date arvopaiva;
    Date maksupaiva;
    int tapahtumatunnus;
    String kirjausselitekoodi;
    String kirjausseliteteksti;
    double tapahtumanRahamaara;
    String kuittikoodi;
    String valitystapa;
    String saajaMaksaja;
    String saajanLahde;
    String saajanTili;
    String saajanTiliMuuttunut;
    long viitenumero;
    String lomakkeenNumero;
    String tasotunnus;
    Viestitieto viestitieto;
    List<Tapahtuma> alitapahtumat = new ArrayList<Tapahtuma>();
    List<Lisatietue> lisatietueet = new ArrayList<Lisatietue>();
    
    public Tapahtuma( String line ) throws ParseException {
        super( line );
        tapahtumanNumero = Integer.parseInt( line.substring(6,12) );
        arkistointitunnus = line.substring(12,30).trim();
        kirjauspaiva = format.parse( line.substring(30,36).trim() );
        arvopaiva = format.parse( line.substring(36,42).trim() );
        maksupaiva = format.parse( line.substring(42,48).trim() );
        tapahtumatunnus = Integer.parseInt( line.substring(48,49) );
        kirjausselitekoodi = line.substring(49,52).trim();
        kirjausseliteteksti = line.substring(52,87).trim();
        tapahtumanRahamaara = Double.parseDouble( line.substring(87,106) ) / 100;
        kuittikoodi = line.substring(106,107).trim();
        valitystapa = line.substring(107,108).trim();
        saajaMaksaja = line.substring(108,143).trim();
        saajanLahde = line.substring(143,144).trim();
        saajanTili = line.substring(144,158).trim();
        saajanTiliMuuttunut = line.substring(158,159).trim();
        try {
            viitenumero = Long.parseLong( line.substring(159,179).trim() );
        } catch( NumberFormatException nfe ) {
            viitenumero = 0;
        }
        lomakkeenNumero = line.substring(179,187).trim();
        tasotunnus = line.substring(187,188).trim();
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( super.toString() );
        sb.append( "Tapahtuman numero: " ).append( tapahtumanNumero ).append( "\n" );
        sb.append( "Arkistointitunnus: " ).append( arkistointitunnus ).append( "\n" );
        sb.append( "Kirjauspäivä: " ).append( kirjauspaiva ).append( "\n" );
        sb.append( "Arvopäivä: " ).append( arvopaiva ).append( "\n" );
        sb.append( "Maksupäivä: " ).append( maksupaiva ).append( "\n" );
        sb.append( "Tapahtumatunnus: " ).append( tapahtumatunnus ).append( "\n" );
        sb.append( "Kirjausselite: " ).append( kirjausselitekoodi ).append( " " ).append( kirjausseliteteksti ).append( "\n" );
        sb.append( "Tapahtuman rahamäärä: " ).append( tapahtumanRahamaara ).append( "\n" );
        sb.append( "Kuittikoodi: " ).append( kuittikoodi ).append( "\n" );
        sb.append( "Välitystapa: " ).append( valitystapa ).append( "\n" );
        sb.append( "Saaja/Maksaja: " ).append( saajaMaksaja ).append( "\n" );
        sb.append( "Saajan lähde: " ).append( saajanLahde ).append( "\n" );
        sb.append( "Saajan tili: " ).append( saajanTili ).append( "\n" );
        sb.append( "Saajan tili muuttunut: " ).append( saajanTiliMuuttunut ).append( "\n" );
        sb.append( "Viite: " ).append( viitenumero ).append( "\n" );
        sb.append( "Lomakkeen nro: " ).append( lomakkeenNumero ).append( "\n" );
        sb.append( "Tasotunnus: " ).append( tasotunnus ).append( "\n" );
        for( Tapahtuma t : alitapahtumat ) {
            sb.append( t.toString() );
        }
        for( Lisatietue t : lisatietueet ) {
            sb.append( t.toString() );
        }
        if( viestitieto != null ) {
            sb.append( viestitieto.toString() );
        }
        return sb.toString();
    }
}

