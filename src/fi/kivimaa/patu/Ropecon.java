/*
 * Copyright (C) 2008 Kalle Kivimaa.
 * Use GPL to distribute and modify.
 */
package fi.kivimaa.patu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Ropecon {

    protected TiliTripletti m_pankkitili;

    protected String m_vuosi;

    protected String m_tiliote;

    protected Connection m_tietokantayhteys;

    protected PreparedStatement m_etsiTiliNumeronPerusteella;

    protected PreparedStatement m_etsiTiliNimenPerusteella;

    protected PreparedStatement m_etsiTiliSaajanPerusteella;

    protected PreparedStatement m_etsiTiliLaskunViitteella;

    protected PreparedStatement m_lisaaVientiRivi;

    protected PreparedStatement m_lisaaTapahtuma;

    protected PreparedStatement m_lisaaLaskunMaksu;

    protected PreparedStatement m_haeSeuraavaId;

    public static void main(String[] args) throws IOException, SQLException,
    ClassNotFoundException, ParseException {
        Class.forName( "org.postgresql.Driver" );
        Ropecon r = new Ropecon( args[0], args[1], args[2] );
        r.prosessoiSyote( System.in );
    }

    public Ropecon(String tietokantaUrl, String tietokantaKayttaja,
            String tietokantaSalasana) throws SQLException {
        m_tietokantayhteys = DriverManager.getConnection( tietokantaUrl, tietokantaKayttaja, tietokantaSalasana );
        m_etsiTiliLaskunViitteella = m_tietokantayhteys.prepareStatement( "select id from ar where invnumber=?" );
        m_etsiTiliNumeronPerusteella = m_tietokantayhteys.prepareStatement( "select id,description from chart where accno=?" );
        m_etsiTiliNimenPerusteella = m_tietokantayhteys.prepareStatement( "select id,description from chart where description=?" );
        m_etsiTiliSaajanPerusteella = m_tietokantayhteys.prepareStatement( "select chart.id,description from chart,decrypt_ref where chart.id=decrypt_ref.id and reference=?" );
        m_lisaaVientiRivi = m_tietokantayhteys.prepareStatement( "insert into acc_trans (trans_id,chart_id,amount,transdate,source,memo) values (?,?,?,?,?,?)" );
        m_lisaaTapahtuma = m_tietokantayhteys.prepareStatement( "insert into gl (id,reference,description,transdate,notes) values (?,?,?,?,?)" );
        m_lisaaLaskunMaksu = m_tietokantayhteys.prepareStatement( "update ar set paid=?, datepaid=? where id=?" );
        m_haeSeuraavaId = m_tietokantayhteys.prepareStatement( "select nextval('id')" );
    }

    protected List<TiliTripletti> etsiTilit(String selite, String viite,
            String osapuoli, double summa) throws SQLException {
        List<TiliTripletti> retList = etsiLaskunViitteella( viite, summa, selite );
        if( retList != null )
            return retList;

        // Ensisijaisesti haetaan saajan nimellä
        retList = etsiSaajanNimella( osapuoli, summa, selite );
        if( retList != null )
            return retList;

        // Jos saaja ei löydä, kokeillaan viitteellä
        retList = etsiSaajanViitteella( viite, summa, selite );
        if( retList != null )
            return retList;

        // Jos ei viitekään löydä, koetetaan purkaa selite auki TILINRO SUMMA
        // SELITE
        if( selite != null && selite.split( " " ).length > 1 ) {
            retList = new ArrayList<TiliTripletti>();
            for( TiliTripletti tili: puraSeliteAuki( selite, summa ) ) {
                retList.addAll( etsiNumerolla( tili.tili, tili.summa, tili.selite ) );
            }
            if( retList.size() > 0 )
                return retList;
        } else {
            // Viimeiseksi vielä kokeillaan, josko viite olisi suoraan
            // tilinumero
            retList = etsiNumerolla( viite, summa, selite );
            if( retList != null )
                return retList;
        }

        // Jos mikään ei onnaa, laitetaan selviteltäväksi
        retList = etsiNimella( "Selvittelytili", summa, selite );
        if( retList != null )
            return retList;

        throw new SQLException( "En löytänyt edes selvittelytiliä (!): "
                + selite + "/" + viite + "/" + osapuoli + "/" + summa );
    }

    protected void teeVienti(String lahde, Date paivays,
            List<TiliTripletti> tilit, String saaja)
            throws NumberFormatException, SQLException {
        long id = 0;

        if( tilit.size() == 1 && tilit.get( 0 ).tiliNimi == null ) {
            // Tämä on laskun maksu, ei GL-taulun vientiä
            id = Long.parseLong( tilit.get( 0 ).tili );
            m_lisaaLaskunMaksu.setLong( 3, id );
            m_lisaaLaskunMaksu.setDouble( 1, tilit.get( 0 ).summa );
            m_lisaaLaskunMaksu.setDate( 2, new java.sql.Date( paivays.getTime() ) );
            m_lisaaLaskunMaksu.execute();
        } else {
            // Generoidaan vienti GL-tauluun
            ResultSet rs = m_haeSeuraavaId.executeQuery();
            if( !rs.next() )
                throw new SQLException( "En kyennyt saamaan seuraavaa tapahtumanumeroa" );
            id = rs.getLong( 1 );

            m_lisaaTapahtuma.setLong( 1, id );
            m_lisaaTapahtuma.setString( 2, lahde );
            m_lisaaTapahtuma.setString( 3, saaja );
            m_lisaaTapahtuma.setDate( 4, new java.sql.Date( paivays.getTime() ) );
            m_lisaaTapahtuma.setNull( 5, Types.VARCHAR );
            m_lisaaTapahtuma.execute();
        }

        double kokonaisSumma = 0;

        // Luodaan osatapahtumat acc_transiin
        for( TiliTripletti tili: tilit ) {
            kokonaisSumma += tili.summa;
            luoRivi( id, tili.tili, tili.summa, paivays, lahde, tili.selite );
        }

        // Ja pankkitilille käänteinen tapahtuma
        luoRivi( id, m_pankkitili.tili, 0 - kokonaisSumma, paivays, lahde, saaja );
    }

    // Luetaan tietue kerrallaan, perustietueista otetaan tiliotteen tiedot
    // ja tapahtumatietueet viedään eteenpäin
    public void prosessoiSyote( InputStream is ) throws IOException, SQLException,
            ParseException {
        BufferedReader syoteLukija = new BufferedReader( new InputStreamReader( is ) );
        Tietue tietue = null;
        while( ( tietue = Tietue.getInstance( syoteLukija ) ) != null ) {
            if( tietue instanceof Tapahtuma ) {
                kasitteleTapahtuma( (Tapahtuma) tietue );
            } else if( tietue instanceof Perustietue ) {
                Perustietue p = (Perustietue) tietue;
                m_tiliote = p.tiliotteenNumero;
                m_vuosi = p.vuosi;

                System.out.println( "Tilinumero: " + p.tilinumero );
                m_etsiTiliNimenPerusteella.setString( 1, p.tilinumero );
                ResultSet rs = m_etsiTiliNimenPerusteella.executeQuery();
                if( rs.next() ) {
                    m_pankkitili = new TiliTripletti( rs.getInt( 1 ), p.tilinumero, 0, null );
                }
                rs.close();
            }
        }
    }

    public void kasitteleTapahtuma(Tapahtuma tapahtuma) throws SQLException {
        Date paivays = tapahtuma.kirjauspaiva;
        String saaja = tapahtuma.saajaMaksaja;
        double summa = tapahtuma.tapahtumanRahamaara;
        String viite = Long.toString( tapahtuma.viitenumero );
        if( "0".equals( viite ) )
            viite = "";
        String selite = null;
        if( tapahtuma.lisatietueet.size() > 0 ) {
            selite = tapahtuma.lisatietueet.get( 0 ).lisatieto;
        }
        List<TiliTripletti> tilit = etsiTilit( selite, viite, saaja, summa );
        for( TiliTripletti tili: tilit ) {
            if( tili.selite == null )
                tili.selite = selite;
            if( tili.selite == null )
                tili.selite = viite;
        }
        teeVienti( m_vuosi + "-" + m_tiliote + "-" + tapahtuma.tapahtumanNumero, paivays, tilit, saaja );
    }

    // Puretaan auki muodossa TILINUMERO SUMMA SELITE TILINUMERO SUMMA SELITE...
    // olevat selitteet
    protected List<TiliTripletti> puraSeliteAuki(String selite, double summa) {
        if( selite == null )
            return null;
        List<TiliTripletti> retList = new ArrayList<TiliTripletti>();
        try {
            String[] sanat = selite.split( " " );
            for( int i = 0; i < sanat.length; i++ ) {
                double osasumma = Double.NaN;
                int tili = Integer.parseInt( sanat[i] );
                try {
                    if( sanat[i + 1].indexOf( "," ) > -1 ) {
                        osasumma = NumberFormat.getInstance( new Locale( "fi", "FI" ) ).parse( sanat[i + 1] ).doubleValue();
                    } else {
                        osasumma = NumberFormat.getInstance( Locale.US ).parse( sanat[i + 1] ).doubleValue();
                    }
                    selite = sanat[i + 2];
                    for( int j = i + 3; j < sanat.length; j++ ) {
                        try {
                            Integer.parseInt( sanat[j] );
                            i = j - 1;
                            break;
                        } catch( NumberFormatException nfe ) {
                            selite = selite + " " + sanat[j];
                        }
                    }
                } catch( NumberFormatException nfe ) {
                    selite = selite.substring( selite.indexOf( " " ) + 1 );
                } catch( ParseException nfe ) {
                    selite = selite.substring( selite.indexOf( " " ) + 1 );
                }
                if( !Double.isNaN( osasumma ) ) {
                    retList.add( new TiliTripletti( tili, osasumma, selite ) );
                } else {
                    retList.add( new TiliTripletti( tili, summa, selite ) );
                    break;
                }
            }
        } catch( NumberFormatException nfe ) {
            // Ignored
        }
        return retList;
    }

    private void luoRivi(long id, String tili, double summa, Date paivays,
            String lahde, String saaja) throws SQLException {
        m_lisaaVientiRivi.setLong( 1, id );
        m_lisaaVientiRivi.setInt( 2, Integer.parseInt( tili ) );
        m_lisaaVientiRivi.setDouble( 3, summa );
        m_lisaaVientiRivi.setDate( 4, new java.sql.Date( paivays.getTime() ) );
        m_lisaaVientiRivi.setString( 5, lahde );
        m_lisaaVientiRivi.setString( 6, saaja );
        m_lisaaVientiRivi.execute();
    }

    private List<TiliTripletti> etsiLaskunViitteella(String viite,
            double summa, String selite) throws SQLException {
        List<TiliTripletti> retList = new ArrayList<TiliTripletti>();

        ResultSet rs = null;
        try {
            m_etsiTiliLaskunViitteella.setString( 1, viite );
            rs = m_etsiTiliLaskunViitteella.executeQuery();
            if( rs.next() ) {
                retList.add( new TiliTripletti( rs.getInt( 1 ), null, summa, selite ) );
                return retList;
            }
        } finally {
            if( rs != null )
                rs.close();
        }

        return null;
    }

    private List<TiliTripletti> etsiSaajanNimella(String osapuoli,
            double summa, String selite) throws SQLException {
        List<TiliTripletti> retList = new ArrayList<TiliTripletti>();

        ResultSet rs = null;
        try {
            m_etsiTiliSaajanPerusteella.setString( 1, osapuoli );
            rs = m_etsiTiliSaajanPerusteella.executeQuery();
            if( rs.next() ) {
                retList.add( new TiliTripletti( rs.getInt( 1 ), rs.getString( 2 ), summa, selite ) );
                return retList;
            }
        } finally {
            if( rs != null )
                rs.close();
        }

        return null;
    }

    private List<TiliTripletti> etsiSaajanViitteella(String viite,
            double summa, String selite) throws SQLException {
        List<TiliTripletti> retList = new ArrayList<TiliTripletti>();

        ResultSet rs = null;
        try {
            m_etsiTiliSaajanPerusteella.setString( 1, viite );
            rs = m_etsiTiliSaajanPerusteella.executeQuery();
            if( rs.next() ) {
                retList.add( new TiliTripletti( rs.getInt( 1 ), rs.getString( 2 ), summa, selite ) );
                return retList;
            }
        } finally {
            if( rs != null )
                rs.close();
        }

        return null;
    }

    private List<TiliTripletti> etsiNumerolla(String numero, double summa,
            String selite) throws SQLException {
        List<TiliTripletti> retList = new ArrayList<TiliTripletti>();

        ResultSet rs = null;
        try {
            m_etsiTiliNumeronPerusteella.setString( 1, numero );
            rs = m_etsiTiliNumeronPerusteella.executeQuery();
            if( rs.next() ) {
                retList.add( new TiliTripletti( rs.getInt( 1 ), rs.getString( 2 ), summa, selite ) );
                return retList;
            }
        } finally {
            if( rs != null )
                rs.close();
        }

        return null;
    }

    private List<TiliTripletti> etsiNimella(String nimi, double summa,
            String selite) throws SQLException {
        List<TiliTripletti> retList = new ArrayList<TiliTripletti>();

        ResultSet rs = null;
        try {
            m_etsiTiliNimenPerusteella.setString( 1, nimi );
            rs = m_etsiTiliNimenPerusteella.executeQuery();
            if( rs.next() ) {
                retList.add( new TiliTripletti( rs.getInt( 1 ), rs.getString( 2 ), summa, selite ) );
                return retList;
            }
        } finally {
            if( rs != null )
                rs.close();
        }
        return null;
    }

    public class TiliTripletti {
        String tili;

        String tiliNimi;

        double summa;

        String selite;

        public TiliTripletti(String t, double s, String se) {
            tili = t;
            summa = s;
            selite = se;
        }

        public TiliTripletti(int t, double s, String se) {
            tili = ( new Integer( t ) ).toString();
            summa = s;
            selite = se;
        }

        public TiliTripletti(int t, String n, double s, String se) {
            tili = ( new Integer( t ) ).toString();
            tiliNimi = n;
            summa = s;
            selite = se;
        }
    }
}
