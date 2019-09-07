/*
 * Copyright (C) 2009 Kalle Kivimaa.
 * Use GPL to distribute and modify.
 */
package fi.kivimaa.patu;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Perusluokka suomalaisen pankkien EDI-viestinnän dekryptaamiseen.
 */
public class Tietue {
    public static final String TILIOTE_PERUSTIETUE = "00";
    public static final String TAPAHTUMA_PERUSTIETUE = "10";
    public static final String TAPAHTUMA_LISATIETUE = "11";
    public static final String VIESTITIETO = "30";
    public static final String SALDOTIETUE = "40";
    
    public static DateFormat format = new SimpleDateFormat( "yyMMdd" );
    public static DateFormat timeFormat = new SimpleDateFormat( "yyMMddHHmm" );
    
    String aineistoTunnus;
    String tietueTunnus;
    int tietueenPituus;

    /**
     * Tehdasmetodi, joka osaa parsia annetusta lukijasta
     * oikeanlaisen tietueen ulos.
     */
    public static Tietue getInstance( BufferedReader reader ) throws IOException, ParseException {
        String line = reader.readLine();
        if( line == null ) return null;
        String tietuetunnus = line.substring(1,3).trim();
        if( tietuetunnus.equals( TILIOTE_PERUSTIETUE ) ) return new Perustietue( line );
        if( tietuetunnus.equals( TAPAHTUMA_PERUSTIETUE ) ) {
            Tapahtuma tapahtuma = new Tapahtuma( line );
            boolean newDetail = true;
            while( newDetail ) {
                reader.mark( 1000 );
                line = reader.readLine();
                tietuetunnus = line.substring(1,3);
                if( tietuetunnus.equals( TAPAHTUMA_LISATIETUE ) ) {
                    Lisatietue lisatietue = new Lisatietue( line );
                    tapahtuma.lisatietueet.add( lisatietue );
                } else if( tietuetunnus.equals( VIESTITIETO ) ) {
                    Viestitieto viesti = new Viestitieto( line );
                    tapahtuma.viestitieto = viesti;
                } else if( tietuetunnus.equals( TAPAHTUMA_PERUSTIETUE ) ) {
                    Tapahtuma t = new Tapahtuma( line );
                    if( t.tasotunnus != null && t.tasotunnus.length() > 0 && ! " ".equals( t.tasotunnus ) ) {
                        tapahtuma.alitapahtumat.add( t );
                    } else {
                        reader.reset();
                        newDetail = false;
                    }
                } else {
                    reader.reset();
                    newDetail = false;
                }
            }
            return tapahtuma;
        }
        return new Tietue( line );
    }
    
    public Tietue( String line ) {
        aineistoTunnus = line.substring(0,1).trim();
        tietueTunnus = line.substring(1,3).trim();
        tietueenPituus = Integer.parseInt( line.substring( 3, 6 ) );
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( "Aineistotunnus: " ).append( aineistoTunnus ).append( "\n" );
        sb.append( "Tietuetunnus: " ).append( tietueTunnus ).append( "\n" );
        sb.append( "Tietueen pituus: " ).append( tietueenPituus ).append( "\n" );
        return sb.toString();
    }
}

