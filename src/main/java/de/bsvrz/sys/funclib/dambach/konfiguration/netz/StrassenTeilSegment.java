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

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;

public class StrassenTeilSegment
{
  /**
   * Das zum Straßenteilsegment gehörende Systemobjekt
   */
  private SystemObject m_systemObject;

  /**
   * Länge des Straßenteilsegments
   */
  private long m_laenge = 0;
  
  /**
   * Anzahl Fahrstreifen
   */
  private int m_anzahlFahrstreifen = 0;
  
  /**
   * Steigung/Gefälle
   */
  private String m_steigungGefaelle = null;
  
  /**
   * Offset innethalb des Straßensegments
   */
  private long m_offset = 0;
  
  /**
   * Datenmodell des DaV
   */
  private DataModel m_dataModel;

  /**
   * Konstruktor der Klasse
   * @param dataModel Datenmodell des DaV
   * @param object Systemobjekt vom Typ 'typ.straßenTeilSegment'
   */
  public StrassenTeilSegment (DataModel dataModel, SystemObject object)
  {
    m_systemObject = object;
    
    m_dataModel = dataModel;
    
    AttributeGroup atg  = m_dataModel.getAttributeGroup("atg.straßenTeilSegment");
    
    if (atg != null)
    {
      Data confData = object.getConfigurationData(atg);
    
      if (confData != null)
      {
        m_laenge              = confData.getUnscaledValue("Länge").longValue() / 100;
        m_anzahlFahrstreifen = confData.getUnscaledValue("AnzahlFahrStreifen").intValue();
        m_steigungGefaelle    = confData.getScaledValue  ("SteigungGefälle" ).getValueText();
      }
    }
  }

  /**
   * @return liefert die Variable systemObject zurück
   */
  public SystemObject getSystemObject()
  {
    return m_systemObject;
  }
  
  /**
   * Methode liefert die Pid des Straßenteilsegments
   * @return Pid des Straßenteilsegments
   */
  public String getPid ()
  {
    if (m_systemObject != null)
      return m_systemObject.getPid();
    
    return null;
  }

  /**
   * @return liefert die Variable anzahlFahrstreifen zurück
   */
  public int getAnzahlFahrstreifen()
  {
    return m_anzahlFahrstreifen;
  }

  /**
   * @return liefert die Variable länge zurück
   */
  public long getLaenge()
  {
    return m_laenge;
  }

  /**
   * @return liefert die Variable steigungGefälle zurück
   */
  public String getSteigungGefaelle()
  {
    return m_steigungGefaelle;
  }

  /**
   * @return liefert die Variable offset zurück
   */
  public long getOffset()
  {
    return m_offset;
  }

  /**
   * @param offset setzt die Variable offset
   */
  public void setOffset( long offset )
  {
    m_offset = offset;
  }
}
