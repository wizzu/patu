/*
 * Copyright (C) 2008 Kalle Kivimaa.
 * Use GPL to distribute and modify.
 */
package fi.kivimaa.patu;

public class Lisatietue extends Tietue {
    int lisatiedonTyyppi;
    String lisatieto;
    
    public Lisatietue( String line ) {
        super(line);
        lisatiedonTyyppi = Integer.parseInt( line.substring(6,8) );
        lisatieto = line.substring(8).trim();
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( super.toString() );
        sb.append( "Tyyppi: " ).append( lisatiedonTyyppi ).append( "\n" );
        sb.append( "Lisätieto: " ).append( lisatieto ).append( "\n" );
        return sb.toString();
    }
}

