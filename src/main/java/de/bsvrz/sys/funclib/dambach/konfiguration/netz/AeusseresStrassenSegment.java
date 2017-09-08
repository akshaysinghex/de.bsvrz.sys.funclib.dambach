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

/**
 * Klasse realisiert ein äußeres Straßensegment
 * @author Dambach Werke GmbH
 */
public class AeusseresStrassenSegment extends StrassenSegment
{
  /**
   * zum Straßensegment gehörender Startknoten  
   */
  private StrassenKnoten m_vonKnoten;

  /**
   * zum Straßensegment gehörender Zielknoten
   */
  private StrassenKnoten m_nachKnoten;
  
  /**
   * zum Straßensegment gehörende TMC Richtung
   */
  private String m_tmcRichtung;
  
  /**
   * zum Straßensegment gehörende Strasse
   */
  private String m_strasse;
  
  /**
   * Datenmodell des DaV
   */
  private DataModel m_dataModel;

  /**
   * Konstruktor der Klasse
   * @param dataModel Datenmodell
   * @param objekt äußeres Straßensegment
   */
  public AeusseresStrassenSegment (DataModel dataModel, SystemObject objekt)
  {
    super (dataModel, objekt);
    
    m_dataModel = dataModel;
    
    AttributeGroup atg  = m_dataModel.getAttributeGroup("atg.äußeresStraßenSegment");
    
    if (atg != null)
    {
      Data confData = objekt.getConfigurationData(atg);
    
      if (confData != null)
      {
        SystemObject o1 = confData.getReferenceValue("vonKnoten").getSystemObject();
        if (o1 != null)
          m_vonKnoten = Konfiguration.getInstanz().getStrassenKnoten( o1.getPid() );

        SystemObject o2 = confData.getReferenceValue("nachKnoten").getSystemObject();
        if (o2 != null)
          m_nachKnoten = Konfiguration.getInstanz().getStrassenKnoten( o2.getPid() );
        
        m_tmcRichtung = confData.getScaledValue("TmcRichtung").getValueText();
        
        m_strasse = getStrassenNummer( objekt );
      }
    }
  }
  
  /**
   * Methode bestimmt den Strassennamen zu einem Straßensegment.
   * @param strassenSegment Straßensegment 
   * @return String mit der Nummer der Straße
   */
  private String getStrassenNummer(SystemObject strassenSegment)
  {
    String nr = null;
    
    if (strassenSegment == null)
      return nr;
 
    AttributeGroup atg  = m_dataModel.getAttributeGroup("atg.straßenSegment");

    Data confDataSS = strassenSegment.getConfigurationData(atg);
    
    if (confDataSS != null)
    {
      String strasse = confDataSS.getReferenceValue("gehörtZuStraße").getValueText();
      if (strasse != null)
      {
        SystemObject soStr = m_dataModel.getObject(strasse); 
        if (soStr != null)
        {
          Data confDataStr = soStr.getConfigurationData(m_dataModel.getAttributeGroup("atg.straße"));
          if (confDataStr != null)
          {
            nr = confDataStr.getTextValue("Nummer").getValueText();
          }
        }
      }
    }
    
    return nr;
  }

  /**
   * @return liefert die Variable nachKnoten zurück
   */
  public StrassenKnoten getNachKnoten()
  {
    return m_nachKnoten;
  }

  /**
   * @return liefert die Variable strasse zurück
   */
  public String getStrasse()
  {
    return m_strasse;
  }

  /**
   * @return liefert die Variable tmcRichtung zurück
   */
  public String getTmcRichtung()
  {
    return m_tmcRichtung;
  }

  /**
   * @return liefert die Variable vonKnoten zurück
   */
  public StrassenKnoten getVonKnoten()
  {
    return m_vonKnoten;
  }
  
  /**
   * Methode prüft, ob zwei äussere Straßensegmente auf der selben Straße liegen
   * @param segment zu prüfendes Straßensegment
   * @return selbe Straße: true, sonst false
   */
  public boolean liegtAufSelberStrasse (AeusseresStrassenSegment segment)
  {
    if (segment != null)
    {
      if (m_tmcRichtung.equals(segment.getTmcRichtung()) && m_strasse.equals( segment.getStrasse()))
        return true;
    }
    
    return false;
  }

  /**
   * Methode liefert bei äußeren Straßensegmenten die Pid des Systemobjekts, erweitert um die TMC-Richtung und die Nummer der Strasse. 
   * @return erweiterte Pid, im Fehlerfall null
   */
  @Override
  public String getPidRichtungStrasse ()
  {
    if (super.getSystemObject() != null)
      return super.getPid() + " (" + m_strasse + "/" + m_tmcRichtung + ")"; 
    
    return null;
  }
  
  /**
   * Methode prüft ob es sich bei dem Straßensegment um ein äußeres Straßensegment handelt.
   * @return bei äußerem Straßensegment: true, sonst: false
   */
  @Override
  public boolean isAeusseresStrassenSegment ()
  {
    return true;
  }

  /**
   * Methode prüft das Straßensegmenten in einem Autobahnkreuz oder Autobahndreieck beginnt.
   * @return Autobahnkreuz, Autobahndreick: true, sonst false
   */
  @Override
  public boolean beginntInAutobahnKreuzOderDreieck ()
  {
    if (m_vonKnoten != null)
      return (m_vonKnoten.isAutobahnKreuz() || m_vonKnoten.isAutobahnDreieck());

    return false;
  }

  /**
   * Methode prüft das Straßensegmenten in einem Autobahnkreuz oder Autobahndreieck endet.
   * @return Autobahnkreuz, Autobahndreick: true, sonst false
   */
  @Override
  public boolean endetInAutobahnKreuzOderDreieck ()
  {
    if (m_nachKnoten != null)
      return (m_nachKnoten.isAutobahnKreuz() || m_nachKnoten.isAutobahnDreieck());

    return false;
  }
  
  /**
   * Methode prüft ob das Straßensegmenten am Autobahnanfang beginnt.
   * @return Autobahnanfang: true, sonst false
   */
  @Override
  public boolean beginntAmAutobahnAnfang ()
  {
    if (m_vonKnoten != null)
      return (m_vonKnoten.isAutobahnEnde());
    
    return false;  
  }

  /**
   * Methode prüft ob das Straßensegmenten am Autobahnende endet. 
   * @return Autobahnende: true, sonst false 
   */
  @Override
  public boolean endetAmAutobahnEnde ()
  {
    if (m_nachKnoten != null)
      return m_nachKnoten.isAutobahnEnde();
    
    return false;  
  }
}
