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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Klasse zur Realisierung eines Straßensegments, es enthält die Daten, die sowohl für die inneren wie auch
 * für die äußeren Straßensegmente verwendet werden
 * @author Dambach Werke GmbH
 */
public class StrassenSegment
{
  /**
   * Das zum Straßensegment gehörende Systemobjekt
   */
  private SystemObject m_systemObject;
  
  /**
   * Vorgänger des Staßensegments die nicht auf der selben Straße liegen
   */
  private Vector<StrassenSegment> m_vorgaenger = new Vector<StrassenSegment>(); 

  /**
   * Vorgänger des Staßensegments die auf der selben Straße liegen
   */
  private Vector<StrassenSegment> m_vorgaengerAufStrasse = new Vector<StrassenSegment>(); 

  /**
   * Nachfolger des Staßensegments die nicht auf der selben Straße liegen
   */
  private Vector<StrassenSegment> m_nachfolger = new Vector<StrassenSegment>(); 

  /**
   * Nachfolger des Staßensegments die auf der selben Straße liegen
   */
  private Vector<StrassenSegment> m_nachfolgerAufStrasse = new Vector<StrassenSegment>(); 
  
  /**
   * Zum Strassensegment gehörende Straßenteilsegmente
   */
  private Vector<StrassenTeilSegment> m_strassenTeilSegment = new Vector<StrassenTeilSegment>();

  /**
   * Zum Strassensegment gehörende Messquerschnitte
   */
  private Vector<MessQuerschnitt> m_messquerschnitte = new Vector<MessQuerschnitt>();

  /**
   * Datenmodell des DaV
   */
  private DataModel m_dataModel;

  /**
   * Länge des Segments
   */
  private long m_laenge = 0;
  
  /**
   * Konstruktor der Klasse
   * @param dataModel Datenmodell des DaV
   * @param objekt Systemobjekt vom Typ 'typ.straßenSegment'
   */
  public StrassenSegment (DataModel dataModel, SystemObject objekt)
  {
    m_systemObject = objekt;

    m_dataModel = dataModel;
  }  
  
  /**
   * Methode bestimme zu einem Straßensegment die Länge und die dazugehörenden Straßenteilsegmente.
   * Achtung: rechenintensiv, sollte nur bei den Segmenten aufgerufen werden die diese Informationen 
   * auch benötigten. Daher erfolgt dieser Aufruf nicht automatisch im Konstruktor der Klasse.
   */
  public void initialisiere ()
  {
    // Bestimmen der Länge des Straßensegments
    
    AttributeGroup atg  = m_dataModel.getAttributeGroup("atg.straßenSegment");
    
    if (atg != null)
    {
      Data confData = m_systemObject.getConfigurationData(atg);
    
      if (confData != null)
      {
        m_laenge = confData.getUnscaledValue("Länge").longValue() / 100;
      }
    }

    // Bestimmen der zum Straßensegment gehörenden Straßenteilsegmente
   
    atg  = m_dataModel.getAttributeGroup("atg.bestehtAusLinienObjekten");
    
    long offset = 0;
    
    if (atg != null)
    {
      Data confData = m_systemObject.getConfigurationData(atg);
    
      if (confData != null)
      {
        int anzahl = confData.getReferenceArray("LinienReferenz").getLength(); 
        
        for (int j = 0; j < anzahl; j++)
        {
          SystemObject objSts = confData.getReferenceArray("LinienReferenz").getReferenceValue(j).getSystemObject();
  
          if (objSts != null)
          {
            // Anlegen des Objekts und Bestimmen des Offsets des Strassenteilsegments innerhalb des
            // Strassensegments
            
            StrassenTeilSegment sts = new StrassenTeilSegment (m_dataModel, objSts);
            sts.setOffset( offset );
            
            offset += sts.getLaenge();
            
            m_strassenTeilSegment.add( sts );
          }
        }
      }
    }
  }

  /**
   * @return liefert die Variable vorgaenger zurück
   */
  public Vector<StrassenSegment> getVorgaenger()
  {
    Vector<StrassenSegment> v = new Vector<StrassenSegment>();
    Iterator<StrassenSegment> it = m_vorgaenger.iterator();
    while (it.hasNext())
      v.add( it.next() );
    
    return v;
  }

  /**
   * @param vorgaenger setzt die Variable vorgaenger
   */
  public void setVorgaenger( Vector<StrassenSegment> vorgaenger )
  {
    m_vorgaenger = vorgaenger;
  }

  /**
   * @return liefert die Variable nachfolger zurück
   */
  public Vector<StrassenSegment> getNachfolger()
  {
    Vector<StrassenSegment> v = new Vector<StrassenSegment>();
    Iterator<StrassenSegment> it = m_nachfolger.iterator();
    while (it.hasNext())
      v.add( it.next() );
    
    return v;
  }

  /**
   * @param nachfolger setzt die Variable nachfolger
   */
  public void setNachfolger( Vector<StrassenSegment> nachfolger )
  {
    m_nachfolger = nachfolger;
  }

  /**
   * @return liefert die Variable vorgängerAufStrasse zurück
   */
  public Vector<StrassenSegment> getVorgaengerAufStrasse()
  {
    Vector<StrassenSegment> v = new Vector<StrassenSegment>();
    Iterator<StrassenSegment> it = m_vorgaengerAufStrasse.iterator();
    while (it.hasNext())
      v.add( it.next() );
    
    return v;
  }

  /**
   * Methode liefert das Vorgängerstraßensegment auf der selben Straße zurück, das die
   * kürzeste Länge hat. Bei mehreren gleichen Längen wird das zuerst gefundene Segment
   * zurückgeliefert. 
   * @return kürzestes Straßenteilsegment, im Fehlerfall null
   */
  public StrassenSegment getKuerzestenVorgaengerAufStrasse()
  {
    StrassenSegment ks = null;
    
    Iterator<StrassenSegment> it = m_vorgaengerAufStrasse.iterator();
    while (it.hasNext())
    {
      StrassenSegment s = it.next();
      if (ks == null)
      {
        ks = s;
      }
      else
      {
        if (s.getLaenge() < ks.getLaenge())
          ks = s;
      }
    }
    
    return ks;
  }

  /**
   * @param vorgaengerAufStrasse setzt die Variable vorgaengerAufStrasse
   */
  public void setVorgaengerAufStrasse( Vector<StrassenSegment> vorgaengerAufStrasse )
  {
    m_vorgaengerAufStrasse = vorgaengerAufStrasse;
  }

  /**
   * @param vorgaengerAufStrasse setzt die Variable vorgaengerAufStrasse
   */
  public void setVorgaengerAufStrasse( StrassenSegment vorgaengerAufStrasse )
  {
    m_vorgaengerAufStrasse.clear();
    m_vorgaengerAufStrasse.add( vorgaengerAufStrasse );
  }

  /**
   * @return liefert die Variable systemObject zurück
   */
  public SystemObject getSystemObject()
  {
    return m_systemObject;
  }

  /**
   * @return liefert die Variable nachfolgerAufStraße zurück
   */
  public Vector<StrassenSegment> getNachfolgerAufStrasse()
  {
    Vector<StrassenSegment> v = new Vector<StrassenSegment>();
    Iterator<StrassenSegment> it = m_nachfolgerAufStrasse.iterator();
    while (it.hasNext())
      v.add( it.next() );
    
    return v;
  }

  /**
   * @param nachfolgerAufStrasse setzt die Variable nachfolgerAufStrasse
   */
  public void setNachfolgerAufStrasse( Vector<StrassenSegment> nachfolgerAufStrasse )
  {
    m_nachfolgerAufStrasse = nachfolgerAufStrasse;
  }
  
  /**
   * @param nachfolgerAufStrasse setzt die Variable nachfolgerAufStrasse
   */
  public void setNachfolgerAufStrase( StrassenSegment nachfolgerAufStrasse )
  {
    m_nachfolgerAufStrasse.clear();
    m_nachfolgerAufStrasse.add( nachfolgerAufStrasse );
  }

  /**
   * @return liefert die Variable messquerschnitte zurück
   */
  public Vector<MessQuerschnitt> getMessquerschnitte()
  {
    Vector<MessQuerschnitt> v = new Vector<MessQuerschnitt>();
    Iterator<MessQuerschnitt> it = m_messquerschnitte.iterator();
    while (it.hasNext())
      v.add( it.next() );
    
    return v;
  }

  /**
   * @param messquerschnitte setzt die Variable messquerschnitte
   */
  public void setMessquerschnitte( Vector<MessQuerschnitt> messquerschnitte )
  {
    m_messquerschnitte = messquerschnitte;
  }

  /**
   * Methode liefet innerhalb des Straßensegments den letzten Messquerschnitt, der vor einem übergebenen Offset liegt.
   * @param offset Offset innerhalb des Staßensegments 
   * @return letzter Messquerschnitt vor Offset, im Fehlerfall, bzw. wenn nicht gefunden 'null'
   */
  public MessQuerschnitt getMessQuerschnittVorOffset (long offset)
  {
    MessQuerschnitt mq = null;
    
    Iterator<MessQuerschnitt> it = m_messquerschnitte.iterator();
    while (it.hasNext())
    {
      MessQuerschnitt m = it.next();
      
      if (m.getOffset() < offset)
        mq = m;
    }
    
    return mq;
  }

  /**
   * Methode liefet innerhalb des Straßensegments den ersten Messquerschnitt, der in oder nach einem übergebenen Offset liegt.
   * @param offset Offset innerhalb des Staßensegments 
   * @return letzter Messquerschnitt nach Offset, im Fehlerfall, bzw. wenn nicht gefunden 'null'
   */
  public MessQuerschnitt getMessQuerschnittNachOffset (long offset)
  {
    Iterator<MessQuerschnitt> it = m_messquerschnitte.iterator();
    while (it.hasNext())
    {
      MessQuerschnitt m = it.next();
      if (m.getOffset() >= offset)
        return m;
    }
    
    return null;
  }

  /**
   * Methode liefert die Pid des Systemobjekts, das zu diesem Staßensegment gehört
   * @return Pid im Fehlerfall null
   */
  public String getPid ()
  {
    if (m_systemObject != null)
      return m_systemObject.getPid();
    
    return null;
  }
  
  /**
   * Methode liefert bei äußeren Straßensegmenten die Pid des Systemobjekts, erweitert um die TMC-Richtung und die Nummer der Strasse. 
   * Bei inneren Straßensegmenten wird nur die Pid zurückgegben.
   * @return erweiterte Pid, im Fehlerfall null
   */
  public String getPidRichtungStrasse ()
  {
    if (m_systemObject != null)
      return m_systemObject.getPid() ;
    
    return null;
  }

  /**
   * Methode prüft ob es sich bei dem Straßensegment um ein inneres Straßensegment handelt
   * (Methode wird in den abgeleiteten Klassen überschrieben).
   * @return bei innerem Straßensegment: true, sonst: false
   */
  public boolean isInneresStrassenSegment ()
  {
    return false;
  }

  /**
   * Methode prüft ob es sich bei dem Straßensegment um ein äußeres Straßensegment handelt.
   * (Methode wird in den abgeleiteten Klassen überschrieben).
   * @return bei äußerem Straßensegment: true, sonst: false
   */
  public boolean isAeusseresStrassenSegment ()
  {
    return false;
  }

  /**
   * Methode prüft bei äußeren Straßensegmenten ob das Straßensegment in einem Autobahnkreuz oder Autobahndreieck beginnt,
   * bei inneren Straßensegmenten ob der Straßenknoten in dem das Straßensegment liegt ein Autobahnkreuz oder Autobahndreieck ist.
   * (Methode wird in den abgeleiteten Klassen überschrieben).
   * @return Autobahnkreuz, Autobahndreick: true, sonst false
   */
  public boolean beginntInAutobahnKreuzOderDreieck ()
  {
    return false;
  }

  /**
   * Methode prüft bei äußeren Straßensegmenten ob das Straßensegment in einem Autobahnkreuz oder Autobahndreieck endet,
   * bei inneren Straßensegmenten ob der Straßenknoten in dem das Straßensegment liegt ein Autobahnkreuz oder Autobahndreieck ist.
   * (Methode wird in den abgeleiteten Klassen überschrieben).
   * @return Autobahnkreuz, Autobahndreick: true, sonst false
   */
  public boolean endetInAutobahnKreuzOderDreieck ()
  {
    return false;
  }

  /**
   * Methode prüft bei äußeren Straßensegmenten ob das Segment am Autobahnanfang beginnt. Bei inneren Straßensegmenten wird
   * geprüft, ob der Straßenknoten in dem das Straßensegment liegt ein Autobahnanfang ist.
   * @return Autobahnanfang: true, sonst false
   */
  public boolean beginntAmAutobahnAnfang ()
  {
    return false;  
  }

  /**
   * Methode prüft bei äußeren Straßensegmenten ob das Segment am Autobahnende endet. Bei inneren Straßensegmenten wird
   * geprüft, ob der Straßenknoten in dem das Straßensegment liegt ein Autobahnende ist.
   * @return Autobahnende: true, sonst false
   */
  public boolean endetAmAutobahnEnde ()
  {
    return false;  
  }

  /**
   * Methode prüft ob es sich bei dem Straßensegment um eine Einfahrt handelt. Diese Information wird dadurch gebildet,
   * dass geprüft wird ob es auf diesem Segment einen Messquerschnitt von Typ Einfahrt gibt. 
   * @return Einfahrt: true, sonst false
   */
  public boolean isEinfahrt ()
  {
    return (getMessQuerschnitteTypEinfahrt().size() > 0);
  }

  /**
   * Methode prüft ob es sich bei dem Straßensegment um eine Hauptfahrbahn handelt. Diese Information wird dadurch gebildet,
   * dass geprüft wird ob es auf diesem Segment einen Messquerschnitt von Typ Hauptfahrbahn gibt. 
   * @return Hauptfahrbahn: true, sonst false
   */
  public boolean isHauptFahrbahn ()
  {
    return (getMessQuerschnitteTypHauptFahrbahn().size() > 0);
  }

  /**
   * Methode prüft ob es sich bei dem Straßensegment um eine Nebenfahrbahn handelt. Diese Information wird dadurch gebildet,
   * dass geprüft wird ob es auf diesem Segment einen Messquerschnitt von Typ Nebenfahrbahn gibt. 
   * @return Nebenfahrbahn: true, sonst false
   */
  public boolean isNebenFahrbahn ()
  {
    return (getMessQuerschnitteTypNebenFahrbahn().size() > 0);
  }

  /**
   * Methode prüft ob es sich bei dem Straßensegment um eine sonstige Fahrbahn handelt. Diese Information wird dadurch gebildet,
   * dass geprüft wird ob es auf diesem Segment einen Messquerschnitt von Typ sonstige Fahrbahn gibt. 
   * @return sonstige Fahrbahn: true, sonst false
   */
  public boolean isSonstigeFahrbahn ()
  {
    return (getMessQuerschnitteTypSonstigeFahrbahn().size() > 0);
  }

  /**
   * Methode prüft ob es sich bei dem Straßensegment um eine Ausfahrt handelt. Diese Information wird dadurch gebildet,
   * dass geprüft wird ob es auf diesem Segment einen Messquerschnitt von Typ Ausfahrt gibt. 
   * @return Ausfahrt: true, sonst false
   */
  public boolean isAusfahrt ()
  {
    return (getMessQuerschnitteTypAusfahrt().size() > 0);
  }
  
  /**
   * Methode liefert die Straßensegmente, die als Einfahrt in dieses Straßensegment hineinführen (am Anfang des Straßensegments) 
   * @return List mit Einfahrten des Straßensegments
   */
  public Vector<StrassenSegment> getEinfahrendeStassenSegmente ()
  {
    Vector<StrassenSegment> einfahrten = new Vector<StrassenSegment>();
    
    Iterator it = m_vorgaenger.iterator();
    while (it.hasNext())
    {
      StrassenSegment s = (StrassenSegment) it.next();
      
      if (s.isEinfahrt())
        einfahrten.add( s );
    }
    
    it = m_vorgaengerAufStrasse.iterator();
    while (it.hasNext())
    {
      StrassenSegment s = (StrassenSegment) it.next();
      
      if (s.isEinfahrt())
        einfahrten.add( s );
    }
    
    return einfahrten;
  }
  
  /**
   * Methode liefert die Straßensegmente, die als Ausfahrt aus diesem Straßensegment hinausführen (am Ende des Straßensegments) 
   * @return List mit Einfahrten des Straßensegments
   */
  public Vector<StrassenSegment> getAusfahrendeStrassenSegmente ()
  {
    Vector<StrassenSegment> ausfahrten = new Vector<StrassenSegment>();
    
    Iterator it = m_nachfolger.iterator();
    while (it.hasNext())
    {
      StrassenSegment s = (StrassenSegment) it.next();
      
      if (s.isAusfahrt())
        ausfahrten.add( s );
    }
    
    it = m_nachfolgerAufStrasse.iterator();
    while (it.hasNext())
    {
      StrassenSegment s = (StrassenSegment) it.next();
      
      if (s.isAusfahrt())
        ausfahrten.add( s );
    }
    
    return ausfahrten;
  }
  
  /**
   * Methode liefert die Messquerschnitte, die in das Segment hineinführen (nicht die zu diesem Segment definierten 
   * Messquerschnitte die als Einfahrt definiert sind). 
   * @return Liste mit Messquerschnitten
   */
  public Vector<MessQuerschnitt> getEinfahrendeMessQuerschnitte ()
  { 
    Vector<MessQuerschnitt> messQuerschnitt = new Vector<MessQuerschnitt>();
    
    Iterator it = m_vorgaenger.iterator();
    while (it.hasNext())
    {
      StrassenSegment s = (StrassenSegment) it.next();
      
      if (s.isEinfahrt())
        messQuerschnitt.addAll( s.getMessQuerschnitteTypEinfahrt() );
    }
    
    it = m_vorgaengerAufStrasse.iterator();
    while (it.hasNext())
    {
      StrassenSegment s = (StrassenSegment) it.next();
      
      if (s.isEinfahrt())
        messQuerschnitt.addAll( s.getMessQuerschnitteTypEinfahrt() );
    }
    
    return messQuerschnitt;
  }

  /**
   * Methode liefert die Messquerschnitte, die die als Ausfahrt aus diesem Straßensegment hinausführen (nicht die zu diesem Segment definierten 
   * Messquerschnitte die als Ausfahrt definiert sind). 
   * @return Liste mit Messquerschnitten
   */
  public Vector<MessQuerschnitt> getAusfahrendeMessQuerschnitte ()
  { 
    Vector<MessQuerschnitt> messQuerschnitt = new Vector<MessQuerschnitt>();
    
    Iterator it = m_nachfolger.iterator();
    while (it.hasNext())
    {
      StrassenSegment s = (StrassenSegment) it.next();
      
      if (s.isAusfahrt())
        messQuerschnitt.addAll( s.getMessQuerschnitteTypAusfahrt() );
    }
    
    it = m_nachfolgerAufStrasse.iterator();
    while (it.hasNext())
    {
      StrassenSegment s = (StrassenSegment) it.next();
      
      if (s.isAusfahrt())
        messQuerschnitt.addAll( s.getMessQuerschnitteTypAusfahrt() );
    }
    
    return messQuerschnitt;
  }

  /**
   * Methode liefert die Messquerschnitt des Strassensegments, die in diesem Segment als Einfahrt definiert sind
   * @return Liste mit Messquerschnitten
   */
  public Vector<MessQuerschnitt> getMessQuerschnitteTypEinfahrt ()
  {
    Vector<MessQuerschnitt> messQuerschnitte = new Vector<MessQuerschnitt>();
    Iterator<MessQuerschnitt> it = m_messquerschnitte.iterator();
    while (it.hasNext())
    {
      MessQuerschnitt mq = it.next();
      if (mq.isEinfahrt())
        messQuerschnitte.add( mq );
    }
    
    return messQuerschnitte;
  }

  /**
   * Methode liefert die Messquerschnitt des Strassensegments, die in diesem Segment als Ausfahrt definiert sind
   * @return Liste mit Messquerschnitten
   */
  public Vector<MessQuerschnitt> getMessQuerschnitteTypAusfahrt ()
  {
    Vector<MessQuerschnitt> messQuerschnitte = new Vector<MessQuerschnitt>();
    Iterator<MessQuerschnitt> it = m_messquerschnitte.iterator();
    while (it.hasNext())
    {
      MessQuerschnitt mq = it.next();
      if (mq.isAusfahrt())
        messQuerschnitte.add( mq );
    }
    
    return messQuerschnitte;
  }

  /**
   * Methode liefert die Messquerschnitt des Strassensegments, die in diesem Segment als Hauptfahrbahn definiert sind
   * @return Liste mit Messquerschnitten
   */
  public Vector<MessQuerschnitt> getMessQuerschnitteTypHauptFahrbahn ()
  {
    Vector<MessQuerschnitt> messQuerschnitte = new Vector<MessQuerschnitt>();
    Iterator<MessQuerschnitt> it = m_messquerschnitte.iterator();
    while (it.hasNext())
    {
      MessQuerschnitt mq = it.next();
      if (mq.isHauptFahrbahn())
        messQuerschnitte.add( mq );
    }
    
    return messQuerschnitte;
  }

  /**
   * Methode liefert die Messquerschnitt des Strassensegments, die in diesem Segment als Nebenfahrbahn definiert sind
   * @return Liste mit Messquerschnitten
   */
  public Vector<MessQuerschnitt> getMessQuerschnitteTypNebenFahrbahn ()
  {
    Vector<MessQuerschnitt> messQuerschnitte = new Vector<MessQuerschnitt>();
    Iterator<MessQuerschnitt> it = m_messquerschnitte.iterator();
    while (it.hasNext())
    {
      MessQuerschnitt mq = it.next();
      if (mq.isNebenFahrbahn())
        messQuerschnitte.add( mq );
    }
    
    return messQuerschnitte;
  }

  /**
   * Methode liefert die Messquerschnitt des Strassensegments, die in diesem Segment als sonstige Fahrbahn definiert sind
   * @return Liste mit Messquerschnitten
   */
  public Vector<MessQuerschnitt> getMessQuerschnitteTypSonstigeFahrbahn ()
  {
    Vector<MessQuerschnitt> messQuerschnitte = new Vector<MessQuerschnitt>();
    Iterator<MessQuerschnitt> it = m_messquerschnitte.iterator();
    while (it.hasNext())
    {
      MessQuerschnitt mq = it.next();
      if (mq.isSonstigeFahrbahn())
        messQuerschnitte.add( mq );
    }
    
    return messQuerschnitte;
  }

  /**
   * Methode liefert den letzten Messquerschnitt (in Fahrtrichtung) auf diesem Strassensegment.
   * @return letzter Messquerschnitt, im Fehlerfall null
   */
  public MessQuerschnitt getLetzterMessQuerschnitt()
  {
    int anzahl = m_messquerschnitte.size();
    if (anzahl > 0)
    {
      return m_messquerschnitte.get( anzahl - 1 );
    }
    
    return null;
  }

  /**
   * Methode liefert den ersten Messquerschnitt (in Fahrtrichtung) auf diesem Strassensegment.
   * @return erster Messquerschnitt, im Fehlerfall null
   */
  public MessQuerschnitt getErsterMessQuerschnitt()
  {
    int anzahl = m_messquerschnitte.size();
    if (anzahl > 0)
    {
      return m_messquerschnitte.get( 0 );
    }
    
    return null;
  }

  /**
   * Methode fügt einen Messquerschnitt zum Strassensegment hinzu
   * @param messQuerschnitt
   */
  public void addMessQuerschnitt( MessQuerschnitt messQuerschnitt )
  {
    m_messquerschnitte.add( messQuerschnitt );
    
    Collections.sort (m_messquerschnitte, new MyComparator());
  }

  /**
   * Klasse zum vergleichen zweier Messquerschnitte anhand des Offsets
   * @author Dambach Werke GmbH
   */
  class MyComparator implements Comparator<MessQuerschnitt>
  {
    public int compare( MessQuerschnitt m1, MessQuerschnitt m2 )
    {
      if (m1.getOffset() < m2.getOffset())
        return 0;
      else
        return 1;
    }
  }

 
  /**
   * Methode liefert die Länge des Straßensegments zurück
   * @return Länge des Straßensegments
   */
  public long getLaenge()
  {
    return m_laenge;
  }
  
  /**
   * Methode liefert die Straßenteilsegmente eines Straßensegmente von einem bestimmten Offset ab,
   * bis zu einem bestimmten Abstand (relativ zu diesem Offset. Sollte die Summe von Offset plus Abstand 
   * grösser als die Länge des Straßensegments sein, so wird in den nachfolgenden Straßensegmenten 
   * weitergesucht. Methode ruft sich rekursiv selber auf.
   * @param offset Offset im Straßensegment (int Meter)
   * @param abstand Abstand relativ zum Offset (in Meter)
   * @return Liste mit den Straßenteilsegmenten
   */
  public Vector<StrassenTeilSegment> getStrassenTeilSegmente (long offset, long abstand)
  {
//    System.out.println(this.getPid() + " Länge = " + getLänge() + " Offset = " + offset + " Abstand = " + abstand);
    
    Vector<StrassenTeilSegment> v = new Vector<StrassenTeilSegment>();
    
    Iterator<StrassenTeilSegment> it = m_strassenTeilSegment.iterator();
    while (it.hasNext())
    {
      StrassenTeilSegment sts = it.next();
      
      long anfang = sts.getOffset();
      long ende   = anfang + sts.getLaenge();
      
      // Es werden alle Segmente gesucht deren Ende oder deren Anfang im gesuchten Bereich liegt.
      
      if ((ende > offset) && (anfang < (offset + abstand)))
      {
        if (!v.contains( sts ))
          v.add( sts );
//        System.out.println(" - " + sts.getPid() + " Offset: " + sts.getOffset() + " Länge: " + sts.getLänge());
      }
    }
    
    // Offset geht noch in das nächste Segment hinein
    
    long abstandNeu = abstand - (getLaenge() - offset);
    if ((offset + abstand) > getLaenge())
    {
      Iterator<StrassenSegment> itn = getNachfolgerAufStrasse().iterator();
      while (itn.hasNext())
      {
        StrassenSegment s = itn.next();

        Vector<StrassenTeilSegment> stsNachfolger = s.getStrassenTeilSegmente( 0, abstandNeu );
        v.addAll( stsNachfolger );
      }
    }
    
    return v;
  }
  
  /**
   * Methode liefert die Straßenteilsegmente eines Straßensegments.
   * @return Liste mit den Straßenteilsegmenten
   */
  public Vector<StrassenTeilSegment> getStrassenTeilSegmente ()
  {
    Vector<StrassenTeilSegment> v = new Vector<StrassenTeilSegment>();
    
    Iterator<StrassenTeilSegment> it = m_strassenTeilSegment.iterator();
    while (it.hasNext())
      v.add( it.next() );
    
    return v;
  }
  
  /**
   * Methode liefert die Straßenteilsegmente eines Straßensegments ab einem bestimmten Offset
   * @return Liste mit den Straßenteilsegmenten
   */
  public Vector<StrassenTeilSegment> getStrassenTeilSegmenteAbOffset (long offset)
  {
    Vector<StrassenTeilSegment> v = new Vector<StrassenTeilSegment>();
    
    Iterator<StrassenTeilSegment> it = m_strassenTeilSegment.iterator();
    while (it.hasNext())
    {
      StrassenTeilSegment sts = it.next();
      if ((sts.getOffset() + sts.getLaenge()) > offset)
      {
        if (!v.contains( sts ))
          v.add( sts );
      }
    }
    
    return v;
  }

  /**
   * Methode liefert die Straßenteilsegmente eines Straßensegments bis zu einem bestimmten Offset
   * @return Liste mit den Straßenteilsegmenten
   */
  public Vector<StrassenTeilSegment> getStrassenTeilSegmenteBisOffset (long offset)
  {
    Vector<StrassenTeilSegment> v = new Vector<StrassenTeilSegment>();
    
    Iterator<StrassenTeilSegment> it = m_strassenTeilSegment.iterator();
    while (it.hasNext())
    {
      StrassenTeilSegment sts = it.next();
      if (sts.getOffset() < offset)
      {
        if (!v.contains( sts ))
          v.add( sts );
      }
    }
    
    return v;
  }

  /**
   * Methode fügt einen Nachfolger zum Straßensegment hinzu
   * @param segment inneres Straßensegment
   */
  public void addNachfolger (StrassenSegment segment)
  {
    if (!m_nachfolger.contains( segment ))
      m_nachfolger.add( segment );
  }

  /**
   * Methode fügt einen Nachfolger auf der Straße zum Straßensegment hinzu
   * @param segment inneres Straßensegment
   */
  public void addNachfolgerAufStrasse (StrassenSegment segment)
  {
    if (!m_nachfolgerAufStrasse.contains( segment ))
      m_nachfolgerAufStrasse.add( segment );
  }

  /**
   * Methode fügt einen Vorgänger zum Straßensegment hinzu
   * @param segment inneres Straßensegment
   */
  public void addVorgaenger (StrassenSegment segment)
  {
    if (!m_vorgaenger.contains( segment ))
      m_vorgaenger.add( segment );
  }
  
  /**
   * Methode fügt einen Vorgänger auf der Straße zum Straßensegment hinzu
   * @param segment inneres Straßensegment
   */
  public void addVorgaengerAufStrasse (StrassenSegment segment)
  {
    if (!m_vorgaengerAufStrasse.contains( segment ))
      m_vorgaengerAufStrasse.add( segment );
  }
}
