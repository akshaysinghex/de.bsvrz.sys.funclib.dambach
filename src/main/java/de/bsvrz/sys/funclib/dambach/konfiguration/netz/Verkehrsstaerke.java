/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contact Information:
 * Dambach-Werke GmbH
 * Elektronische Leitsysteme
 * Fritz-Minhardt-Str. 1
 * 76456 Kuppenheim
 * Phone: +49-7222-402-0
 * Fax: +49-7222-402-200
 * mailto: info@els.dambach.de
 */

package de.bsvrz.sys.funclib.dambach.konfiguration.netz;

import java.util.Vector;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Klasse mit Methoden zum Bestimmen der aktuellen Verkehrsstärke eines Strassenteilsegments.
 * @author Dambach Werke GmbH
 */
public class Verkehrsstaerke implements ClientReceiverInterface
{
  /**
   * DebugFilter Möglichkeit
   */
  private boolean _debugAnmelde = false;

  private boolean _debug = false;

  /**
   * Comment for <code>debug</code>
   */
  private static final Debug m_debug = Debug.getLogger();
  
  /**
   * Verbindung zum DaV
   */
  private ClientDavInterface m_connection;
  
  /**
   * Definition der Attributgruppe
   */
  private String m_atg = "atg.verkehrsDatenKurzZeitMq";
  
  /**
   * benötigter Aspekt
   */
  private String m_asp = "asp.analyse";

  /**
   * Aktuelle Verkehrsstärke (QB)
   */
  long m_qb = -1;
  
  /**
   * Aktuelle Verkehrsstärke (QKfz)
   */
  long m_qkfz = -1;

  /**
   * Aktueller Güteindex QB
   */
  String m_qbGueteIndex = null;
  
  /**
   * Aktueller Güteindex QKfz
   */
  String m_qkfzGueteIndex = null;

  /**
   * Pid des Messquerschnitts
   */
  private String m_pidMessQuerschnitt = null;
  
  /**
   * Vector mit empfangenen Daten
   */
  private Vector<ResultData> m_resultInput = new Vector<ResultData>();

  /**
   * Konstruktor der Klasse. Konstruktor meldet sich beim DaV zum Empfang der Daten an
   * @param connection Verbindung zum DaV
   * @param pidMessQuerschnitt Pid eines Objekts vom Typ "typ.messQuerschnitt"
   */
  public Verkehrsstaerke (ClientDavInterface connection, String pidMessQuerschnitt)
  {
    m_connection = connection;
    
    m_pidMessQuerschnitt = pidMessQuerschnitt;
    
    anmeldeLeseDaV ( pidMessQuerschnitt, m_atg, m_asp, ReceiverRole.receiver());
  }
  
  /**
   * Update Methode des ClientReceiverInterface
   */
  public void update( ResultData[] results )
  {
    try 
    {
      for (int resultIndex = 0; resultIndex < results.length; ++resultIndex) 
      {
        ResultData result = results[resultIndex];
        
        synchronized (m_resultInput)
        {
          m_resultInput.add(result);
        }
      }
      
      verarbeiten();
    } 
    catch (Exception e) 
    {
      m_debug.error("" + e);
    }
  }
    
  /**
   * Methode zum eigentlichen Verarbeiten der empfangenen Daten des DaV. 
   */
  protected void verarbeiten() 
  {
    ResultData[] results = null;
    
    int anzahl;

    while (true)
    {
      anzahl = 0;

      // Daten aus Eingangsqueue holen      
      
      synchronized (m_resultInput)
      {
        if (m_resultInput.size() > 0)
        {
          anzahl = m_resultInput.size();
          
          results = new ResultData[anzahl];
          
          for (int i = 0; i < anzahl; i++)
            results[i] = m_resultInput.remove(0);
        }
      }
      
      if (anzahl == 0)
        break;
          
      // Daten aus Eingangsqueue bearbeiten      
      
      for (int i = 0; i < anzahl; i++)
      {
        ResultData dat = results[i];
    
        //----------------------------------------------------------------------
        // Auslesen der "atg.verkehrsDatenKurzZeitMq"
        //----------------------------------------------------------------------
  
        if (dat.getDataDescription().getAttributeGroup().getPid().equals(m_atg))
        {
          // Absender feststellen
          
          String absenderPid = dat.getObject().getPid();
  
          if (dat.hasData())
          {
            if (_debug)
              System.out.println("Daten für " + absenderPid + " empfangen");
            
            Data d = dat.getData().getItem( "QB" );
            
            m_qb  = d.getUnscaledValue("Wert").longValue();
  
            Data d1 = d.getItem( "Güte" );
            
            m_qbGueteIndex = d1.getTextValue( "Index" ).getValueText();
            
            d = dat.getData().getItem( "QKfz" );
            
            m_qkfz  = d.getUnscaledValue("Wert").longValue();
  
            d1 = d.getItem( "Güte" );
            
            m_qkfzGueteIndex = d1.getTextValue( "Index" ).getValueText();
            
            if (_debug)
              System.out.println("QB = " + m_qb + " GüteIndex = " + m_qbGueteIndex);
            
          } // if (dat.hasData())
          else
          {
            if (_debug)
              System.out.println("Keine Daten für " + absenderPid + " definiert");
  
            m_qb         = -1;
            m_qbGueteIndex = "";
          }
        }
        
      } // for (int i = 0; i < arg0.length; i++)
    }
  }

  /**
   * Methode die das Objekt mit der PID objPid beim Datenverteiler anmeldet zum
   * Lesen der Attibutgruppe atgPid unter dem Aspekt aspPid. 
   * @param objPid Pid des Objekts 
   * @param atgPid Attributgruppe die angemeldet werden soll
   * @param aspPid Apekt der angemeldet werden soll
   * @param role Rolle des Empfängers (siehe stauma.dav.clientside.ReceiveOptions)
   */
  private void anmeldeLeseDaV (String objPid, String atgPid, String aspPid, ReceiverRole role)
  {
    String buffer = "Anmelden am DaV (Lesen): " + objPid + " " + atgPid + "  " + aspPid;
    
    if (_debugAnmelde)
      System.out.println(buffer);
    
    ClientDavInterface verb = m_connection;

    AttributeGroup atg = verb.getDataModel().getAttributeGroup(atgPid);

    Aspect asp = verb.getDataModel().getAspect(aspPid);

    DataDescription dd = new DataDescription(atg, asp);

    SystemObject obj = m_connection.getDataModel().getObject(objPid); 

    m_connection.subscribeReceiver(this, obj, dd, ReceiveOptions.normal(), role);
  }

  /**
   * Methode die das Objekt mit der Pid objPid beim Datenverteiler abmeldet zum
   * Lesen der Attibutgruppe atgPid unter dem Aspekt aspPid.
   * @param objPid Pid des Objekts 
   * @param atgPid Attributgruppe die abgemeldet werden soll
   * @param aspPid Apekt der abgemeldet werden soll
   */
  private void abmeldeLeseDaV (String objPid, String atgPid, String aspPid)
  {
    String buffer = "Abmelden am DaV (Lesen): " + objPid + " " + atgPid + "  " + aspPid;

    if (_debugAnmelde)
      System.out.println(buffer);
    
    AttributeGroup atg = m_connection.getDataModel().getAttributeGroup(atgPid);
    Aspect asp = m_connection.getDataModel().getAspect(aspPid);

    DataDescription dd = new DataDescription(atg, asp);
      
    SystemObject obj = m_connection.getDataModel().getObject(objPid); 

    m_connection.unsubscribeReceiver( this, obj, dd);
  }

  /**
   * @deprecated
   * Methode liefert die aktuelle Verkehrsstärke QB des Messquerschnitts zurück.
   * @return aktuelle Verkehrsstärke QB in Fzg/h, falls nicht bestimmbar wird -1 zurückgeliefert.
   */
  public long getVerkehrsstaerke()
  {
    return m_qb;
  }

  /**
   * Methode liefert die aktuelle Verkehrsstärke QB des Messquerschnitts zurück.
   * @return aktuelle Verkehrsstärke QB in Fzg/h, falls nicht bestimmbar wird -1 zurückgeliefert.
   */
  public long getVerkehrsstaerkeQB()
  {
    return m_qb;
  }

  /**
   * Methode liefert die aktuelle Verkehrsstärke Qkfz des Messquerschnitts zurück.
   * @return aktuelle Verkehrsstärke QKfz in Fzg/h, falls nicht bestimmbar wird -1 zurückgeliefert.
   */
  public long getVerkehrsstaerkeQKfz()
  {
    return m_qkfz;
  }
  
  /**
   * Methode prüft ob der QB Wert plausibel ist
   * @return plausibel: true, sonst false
   */
  public boolean isQBPlausibel ()
  {
    return m_qb >= 0;
  }
  
  /**
   * Methode prüft ob der Qkfz Wert plausibel ist
   * @return plausibel: true, sonst false
   */
  public boolean isQkfzPlausibel ()
  {
    return m_qkfz >= 0;
  }
  
  //----------------------------------------------------------------------------------------------
  // Klasse aufräumen
  //----------------------------------------------------------------------------------------------

  /**
   * Methode mit der die Klasse veranlasst wird, ihre Objekte am DaV abzumelden un die angemeldeten
   * Listener abzumelden.
   */
  public void dispose ()
  {
    // Messquerschnitte abmelden 

    abmeldeLeseDaV ( m_pidMessQuerschnitt, m_atg, m_asp);
  }
}
