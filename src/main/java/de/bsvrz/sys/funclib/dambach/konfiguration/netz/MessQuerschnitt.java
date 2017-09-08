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

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Klasse realisiert einen Messquerschnitt
 * @author Dambach Werke GmbH
 */
public class MessQuerschnitt implements Comparable<MessQuerschnitt>
{
  /**
   * dazugehörendes Systemobjekt vom Typ 'typ.messQuerschnittAllgemein'
   */
	private SystemObject m_systemObjekt;
  
  /**
   * Offset des Messquerschnitts
   */
	private long         m_offset;

  /**
   * Linienreferenz des Messquerschnitts
   */
  private StrassenSegment m_referenz;

  /**
   * Datenmodell des DaV
   */
  private DataModel m_dataModel;

  /**
   * Typ des Messquerschnitts
   */
  private String m_typ;

  /**
   * Konstruktor der Klasse
   * @param connection Verbindung zum DaV
   * @param objekt Systemobjekt vom Typ 'typ.messQuerschnittAllgemein'
   */
  public MessQuerschnitt (ClientDavInterface connection, SystemObject objekt)
  {
    m_dataModel = connection.getDataModel();
    
    m_systemObjekt = objekt;
    
    AttributeGroup atg = m_dataModel.getAttributeGroup("atg.punktLiegtAufLinienObjekt");
    
    Data data = objekt.getConfigurationData (atg);

    if (data != null)
    {
      SystemObject linie = data.getReferenceValue("LinienReferenz").getSystemObject();
      if (linie != null)
      {
        m_referenz = Konfiguration.getInstanz().getStrassenSegment( linie.getPid() );
      }
      
      if (data.getScaledValue("Offset").isNumber())
      {
        m_offset = (long) data.getScaledValue("Offset").floatValue();
      }
    }

    AttributeGroup atg2 = m_dataModel.getAttributeGroup("atg.messQuerschnittAllgemein");
    
    Data data2 = objekt.getConfigurationData (atg2);

    if (data2 != null)
    {
      m_typ = data2.getTextValue( "Typ" ).getValueText();
    }
  }
  
  /**
   * Methode liefert das Systemobjekt des Messquerschnitts
   * @return Systemobjekt
   */
	public SystemObject getSystemObjekt()
	{
		return m_systemObjekt;
	}

  /**
   * Methode liefert den Offset des Messquerschnitts im Strassensegment
   * @return Offset
   */
	public long getOffset()
	{
		return m_offset;
	}

  /**
   * Methode liefert die Pid des zum Messquerschnitts gehörenden Systemobjekts
   * @return Pid, im Fehlerfall null
   */
  public String getPid()
  {
    if (m_systemObjekt != null)
  	  return m_systemObjekt.getPid();
    
    return null;
  }

  /**
   * Methode liefert die Pid des zum Messquerschnitts gehörenden NBA Messquerschnitts
   * @return Pid des NBA Messquerschnitts, im Fehlerfall null
   */
  public String getNbaPid()
  {
    if (getPid() != null)
      return Konfiguration.getInstanz().getNbaMqVonMq( getPid() );
    
    return null;
  }
  
  @Override
	public String toString()
	{
		StringBuffer string = new StringBuffer();
		
		string.append("MessQuerschnitt " +
				          " Pid " +  m_systemObjekt.getPid() +
				          " Offset " + m_offset);
		
		return string.toString();
	}
  
  /**
   * Methode vergleicht 2 Messquerschnitte bzgl. des Offsets miteinander
   * Wenn "this < argument" dann wird -1 zurückgegben
   * Wenn "this = argument" dann wird  0 zurückgegben
   * Wenn "this > argument" dann wird  1 zurückgegben
   * @param o Objekt mit dem verglichen werden soll
   * @return -1, 0, 1
   */
  
  public int compareTo( MessQuerschnitt o )
  {
    if (m_offset < o.getOffset())
      return -1;

    if (m_offset > o.getOffset())
      return 1;

    return 0;
  }

  /**
   * @return liefert die Variable referenz zurück
   */
  public StrassenSegment getReferenz()
  {
    return m_referenz;
  }

  /**
   * @return liefert die Variable typ zurück
   */
  public String getTyp()
  {
    return m_typ;
  }	

  /**
   * Methode prüft ob es sich bei dem Messquerschnitt um eine Einfahrt handelt
   * @return Einfahrt: true, sonst false
   */
  public boolean isEinfahrt ()
  {
    return m_typ.equals( "Einfahrt" );
  }
  
  /**
   * Methode prüft ob es sich bei dem Messquerschnitt um eine Ausfahrt handelt
   * @return Ausfahrt: true, sonst false
   */
  public boolean isAusfahrt ()
  {
    return m_typ.equals( "Ausfahrt" );
  }

  /**
   * Methode prüft ob es sich bei dem Messquerschnitt um eine Hauptfahrbahn handelt
   * @return Hauptfahrbahn: true, sonst false
   */
  public boolean isHauptFahrbahn ()
  {
    return m_typ.equals( "HauptFahrbahn" );
  }
  
  /**
   * Methode prüft ob es sich bei dem Messquerschnitt um eine Nebenfahrbahn handelt
   * @return Nebenfahrbahn: true, sonst false
   */
  public boolean isNebenFahrbahn ()
  {
    return m_typ.equals( "NebenFahrbahn" );
  }
 
  /**
   * Methode prüft ob es sich bei dem Messquerschnitt um eine sonstige Fahrbahn handelt
   * @return sonstige Fahrbahn: true, sonst false
   */
  public boolean isSonstigeFahrbahn ()
  {
    return m_typ.equals( "SonstigeFahrbahn" );
  }
}
