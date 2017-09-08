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
 * Klasse realisiet ein inneres Straßenteilsegment
 * @author Dambach Werke GmbH
 */
public class InneresStrassenSegment extends StrassenSegment
{
  /**
   * zum Straßensegment gehörendes Vorgängersegment
   */
  private AeusseresStrassenSegment m_vonStrassenSegment;

  /**
   * zum Straßensegment gehörendes Nachfolgersegment
   */
  private AeusseresStrassenSegment m_nachStrassenSegment;
  
  /**
   * Konstruktor der Klasse
   * @param dataModel Datenmodell
   * @param objekt Objekt vom Typ "typ.inneresStraßenSegment"
   */
  public InneresStrassenSegment (DataModel dataModel, SystemObject objekt)
  {
    super (dataModel, objekt);
    
    AttributeGroup atg  = dataModel.getAttributeGroup("atg.inneresStraßenSegment");
    
    if (atg != null)
    {
      Data confData = objekt.getConfigurationData(atg);
    
      if (confData != null)
      {
        SystemObject s1 = confData.getReferenceValue ("nachStraßenSegment").getSystemObject();
        if (s1 == null)
          m_nachStrassenSegment = null;
        else
          m_nachStrassenSegment = Konfiguration.getInstanz().getAeusseresStrassenSegment( s1.getPid() );
        
        SystemObject s2 = confData.getReferenceValue ("vonStraßenSegment").getSystemObject();
        if (s2 == null)
          m_vonStrassenSegment = null;
        else
          m_vonStrassenSegment = Konfiguration.getInstanz().getAeusseresStrassenSegment( s2.getPid() );
      }
    }
  }

  /**
   * @return liefert die Variable nachStrassenSegment zurück
   */
  public AeusseresStrassenSegment getNachStrassenSegment()
  {
    return m_nachStrassenSegment;
  }

  /**
   * @return liefert die Variable vonStrassenSegmemt zurück
   */
  public AeusseresStrassenSegment getVonStrassenSegmemt()
  {
    return m_vonStrassenSegment;
  }
  
  /**
   * Methode prüft ob es sich bei dem Straßensegment um ein inneres Straßensegment handelt.
   * @return bei innerem Straßensegment: true, sonst: false
   */
  @Override
  public boolean isInneresStrassenSegment ()
  {
    return true;
  }

  /**
   * Methode prüft das Straßensegmenten in einem Autobahnkreuz oder Autobahndreieck liegt.
   * @return Autobahnkreuz, Autobahndreick: true, sonst false
   */
  @Override
  public boolean beginntInAutobahnKreuzOderDreieck ()
  {
    // Ein inneres Straßensegment hat keinen direkten Bezug zum Straßenknoten, daher erfolgt der
    // Zugriff über die durch das innere Straßensegment verbundenen äußeren Straßenknoten
    
    if (m_nachStrassenSegment != null)
      return m_nachStrassenSegment.beginntInAutobahnKreuzOderDreieck();
    
    if (m_vonStrassenSegment != null)
      return m_vonStrassenSegment.endetInAutobahnKreuzOderDreieck();
    
    return false;
  }

  /**
   * Methode prüft das Straßensegmenten in einem Autobahnkreuz oder Autobahndreieck endet.
   * @return Autobahnkreuz, Autobahndreick: true, sonst false
   */
  @Override
  public boolean endetInAutobahnKreuzOderDreieck ()
  {
    // Da ein inneres Straßensegment innerhalb eines Straßenknotens liegt, braucht nicht zwischen
    // beginnt und endet unterschieden werden.
    
    return beginntInAutobahnKreuzOderDreieck();
  }
  
  /**
   * Methode prüft ob der Straßenknoten in dem das Straßensegment liegt ein Autobahnanfang ist.
   * @return Autobahnanfang: true, sonst false
   */
  @Override
  public boolean beginntAmAutobahnAnfang ()
  {
    // Ein inneres Straßensegment hat keinen direkten Bezug zum Straßenknoten, daher erfolgt der
    // Zugriff über die durch das innere Straßensegment verbundenen äußeren Straßenknoten
    
    if (m_nachStrassenSegment != null)
      return m_nachStrassenSegment.beginntAmAutobahnAnfang();
    
    return false;
  }

  /**
   * Methode prüft ob der Straßenknoten in dem das Straßensegment liegt ein Autobahnende ist.
   * @return Autobahnende true, sonst false
   */
  @Override
  public boolean endetAmAutobahnEnde ()
  {
    // Ein inneres Straßensegment hat keinen direkten Bezug zum Straßenknoten, daher erfolgt der
    // Zugriff über die durch das innere Straßensegment verbundenen äußeren Straßenknoten
    
    if (m_vonStrassenSegment != null)
      return m_vonStrassenSegment.endetAmAutobahnEnde();
    
    return false;  
  }

}
