/*
 * Copyright (C) 2008 Kalle Kivimaa.
 * Use GPL to distribute and modify.
 */
package fi.kivimaa.patu;

public class Viestitieto extends Tietue {
    int tapahtumanNumero;
    String arkistotunnus;
    String kirjauspaiva;
    String viesti;

    public Viestitieto( String line ) {
        super(line);
        tapahtumanNumero = Integer.parseInt( line.substring(6, 12) );
        arkistotunnus = line.substring(12, 30).trim();
        kirjauspaiva = line.substring(30, 36).trim();
        viesti = line.substring(36, 71).trim();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( super.toString() );
        sb.append( "Tapahtuman numero: " ).append( tapahtumanNumero ).append( "\n" );
        sb.append( "Arkistotunnus: " ).append( arkistotunnus ).append( "\n" );
        sb.append( "Kirjauspäivä: " ).append( kirjauspaiva ).append( "\n" );
        sb.append( "Viesti: " ).append( viesti ).append( "\n" );
        return sb.toString();
    }

}
