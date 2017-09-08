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

import java.util.ArrayList;
import java.util.List;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Klasse zur Realisierung eines Straßenknotens
 * @author Dambach Werke GmbH
 */
public class StrassenKnoten
{
  /**
   * Zum Strassenknoten gehörendes Systemobjekt
   */
  private SystemObject m_systemObject;
  
  /**
   * Liste der äusseren Straßensegmente die von diesem Knoten abgehen
   */
  private List<AeusseresStrassenSegment> m_abgehendeAeussereStrassensegmente = new ArrayList<AeusseresStrassenSegment>();
 
  /**
   * Liste der äusseren Straßensegmente die zu diesem Knoten hinführen
   */
  private List<AeusseresStrassenSegment> m_hinfuehrendeAeussereStrassensegmente = new ArrayList<AeusseresStrassenSegment>();
  
  /**
   * Liste der inneren Straßensegmente dieses Knotens
   */
  private List<InneresStrassenSegment> m_innereStrassenSegmente = new ArrayList<InneresStrassenSegment>();
  
  /**
   * Typ des Straßenknotens
   */
  private String m_typ = null;
  
  /**
   * Konstruktor der Klasse
   * @param connection Verbindung zum DaV
   * @param systemObject Objekt vom Typ "typ.straßenKnoten"
   */
  public StrassenKnoten (ClientDavInterface connection, SystemObject systemObject)
  {
    m_systemObject = systemObject;
    
    DataModel dataModel = connection.getDataModel();

    AttributeGroup atg = dataModel.getAttributeGroup("atg.straßenKnoten");
        
    Data data = systemObject.getConfigurationData (atg);

    if (data != null)
      m_typ = data.getTextValue( "Typ" ).getValueText();
  }

  /**
   * Methode fügt ein abgehendes äußeres Straßensegment zum Knoten hinzu  
   * @param objekt äußeres Straßensegment
   */
  public void addAbgehendesAeusseresStrassenSegment( AeusseresStrassenSegment objekt )
  {
    if (!m_abgehendeAeussereStrassensegmente.contains( objekt ))
      m_abgehendeAeussereStrassensegmente.add( objekt );
  }

  /**
   * Methode fügt ein hinführendes äußeres Straßensegment zum Knoten hinzu  
   * @param objekt äußeres Straßensegment
   */
  public void addHinfuehrendesAeusseresStrassenSegment( AeusseresStrassenSegment objekt )
  {
    if (!m_hinfuehrendeAeussereStrassensegmente.contains( objekt ))
      m_hinfuehrendeAeussereStrassensegmente.add( objekt );
  }

  /**
   * Methode liefert eine Liste aller von diesem Knoten abgehender äußerer Straßensegmente
   * @return Liste mit äußeren Straßensegmenten
   */
  public List<AeusseresStrassenSegment> getAbgehendeAeussereStrassenSegmente ()
  {
    return m_abgehendeAeussereStrassensegmente;
  }
  
  /**
   * Methode liefert eine Liste aller an diesem Knoten hinführender äußerer Straßensegmente
   * @return Liste mit äußeren Straßensegmenten
   */
  public List<AeusseresStrassenSegment> getHinfuehrendeAeussereStrassenSegmente ()
  {
    return m_hinfuehrendeAeussereStrassensegmente;   
  }

  /**
   * Methode liefert alle inneren Staßensegmente dieses Knotens
   * @return Liste mit inneren Straßensegmenten
   */
  public List<InneresStrassenSegment> getInnereStrassenSegmente ()
  {
    return m_innereStrassenSegmente;
  }
  
  /**
   * @return liefert die Variable systemObject zurück
   */
  public SystemObject getSystemObject()
  {
    return m_systemObject;
  }

  /**
   * Methode fügt ein inneres Straßensegment dem Knoten hinzu
   * @param objekt inneres Straßensegment
   */
  public void addInneresStrassenSegment( InneresStrassenSegment objekt )
  {
    if (!m_innereStrassenSegmente.contains( objekt ))
      m_innereStrassenSegmente.add( objekt );
  }

  /**
   * @return liefert die Variable typ zurück
   */
  public String getTyp()
  {
    return m_typ;
  }

  /**
   * Methode prüft ob es sich bei dem Knoten um ein Autobahnkreuz handelt
   * @return Knoten ist Autobahnkreuz: true, sonst false
   */
  public boolean isAutobahnKreuz ()
  {
    return m_typ.equals( "AutobahnKreuz" );
  }
  
  /**
   * Methode prüft ob es sich bei dem Knoten um ein Autobahndreieck handelt
   * @return Knoten ist Autobahndreieck: true, sonst false
   */
  public boolean isAutobahnDreieck ()
  {
    return m_typ.equals( "AutobahnDreieck" );
  }

  /**
   * Methode prüft ob es sich bei dem Knoten um ein Autobahnende handelt
   * @return Knoten ist Autobahnende: true, sonst false
   */
  public boolean isAutobahnEnde ()
  {
    return m_typ.equals( "AutobahnEnde" );
  }
  
  /**
   * Methode prüft ob es sich bei dem Knoten um eine Autobahnanschlussstelle handelt
   * @return Knoten ist eine Autobahnanschlussstelle: true, sonst false
   */
  public boolean isAutobahnAnschlussStelle ()
  {
    return m_typ.equals( "AutobahnAnschlussStelle" );
  }
  
  /**
   * Methode prüft ob es sich bei dem Knoten um einen sonstigen Knoten handelt
   * @return Knoten ist ein sonstiger Knoten: true, sonst false
   */
  public boolean isSonstigerKnoten ()
  {
    return m_typ.equals( "SonstigerKnoten" );
  }
  
  /**
   * Methode liefert die Pid des Systemobjekts, das zu diesem Straßenknoten gehört
   * @return Pid im Fehlerfall null
   */
  public String getPid ()
  {
    if (m_systemObject != null)
      return m_systemObject.getPid();
    
    return null;
  }
}
