/*
 * Copyright (C) 2008 Kalle Kivimaa.
 * Use GPL to distribute and modify.
 */
package fi.kivimaa.patu;

public class Lisatietue extends Tietue {
    public static final int VIITETIETO = 6;
    int lisatiedonTyyppi;
    String lisatieto;
    String numerodata;

    public Lisatietue( String line ) {
        super(line);
        lisatiedonTyyppi = Integer.parseInt( line.substring(6, 8) );
        numerodata = line.substring(8, 43).trim();
        lisatieto = line.substring(43).trim();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( super.toString() );
        sb.append( "Tyyppi: " ).append( lisatiedonTyyppi ).append( "\n" );
        sb.append( "Numerodata: " ).append( numerodata ).append( "\n" );
        sb.append( "Lisätieto: " ).append( lisatieto ).append( "\n" );
        return sb.toString();
    }
}
