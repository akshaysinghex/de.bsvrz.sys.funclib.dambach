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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ConfigurationObjectType;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Klasse die Methoden zur Verfügung stellt, die die Objekte ermittelt die für die Ermittlung der
 * Stauprognose benötigt werden.
 * @author Dambach Werke GmbH
 */
public class Konfiguration
{
  /**
   * Comment for <code>m_debug</code>
   */
  private Debug m_debug;

  /**
   * DebugFilter Möglichkeiten
   */
  private boolean _debug = false;
  
  /**
   * Datenmodell
   */
  private DataModel m_dataModel;

  /**   * Verbindung zum DaV
   */
  private ClientDavInterface m_connection;

  /**
   * Hashmap mit Instanzen der einzelnen Staßenknoten (Key = Pid, Value = Instanz)
   */
  private HashMap<String, StrassenKnoten> m_strassenKnoten =  new HashMap<String, StrassenKnoten>();

  /**
   * Hashmap mit Instanzen der einzelnen äußeren Straßensegmente (Key = Pid, Value = Instanz)
   */
  private HashMap<String, AeusseresStrassenSegment> m_aeusseresStrassenSegment = new HashMap<String, AeusseresStrassenSegment>();
  
  /**
   * Hashmap mit Instanzen der einzelnen inneren Staßensegmente (Key = Pid, Value = Instanz)
   */
  private HashMap<String, InneresStrassenSegment> m_inneresStrassenSegment = new HashMap<String, InneresStrassenSegment>();

  /**
   * Hashmap mit Instanzen der einzelnen Messquerschnitte (Key = Pid, Value = Instanz)
   */
  private HashMap<String, MessQuerschnitt> m_messQuerschnitt = new HashMap<String, MessQuerschnitt>();
  

  /**
   * Zuordnungstabelle MessQuerschnitt zu NBA MessQuerschnitt.
   * (Key = PID des MessQuerschnitt, Value = die Pid des NbaMessQuerschnitts) 
   */
  private static HashMap<String, String> m_zuordnungMqZuNbaMq = new HashMap<String, String>();


  /**
   * Methode zum Lesen der einzigen Instanz der Klasse
   * @return einzige Instanz der Klasse
   */
  public static Konfiguration getInstanz()
  {
    return Inner.INSTANCE;
  }

  /**
   * Innere Klasse zum Sicherstellen, dass wirklich nur eine Instanz der Klasse
   * gebildet wird
   * @author Dambach Werke GmbH
   */
  public static class Inner
  {
    private static Konfiguration INSTANCE = new Konfiguration();
  }
  
  /**
   * Konstruktor der Klasse
   */  
  private Konfiguration ()
  {
  }
  
  /**
   * Methode die das eigentliche Bestimmen der Objekte auslöst. Methode benötigt die Verbindung zum DaV
   * @param connection Verbindung zum DaV
   * @param netz zu betrachtendes Netz
   * @param kbMessQuerschnitte Konfigurationsbereich(e) der zu betrachtenden Messquerschnitte
   */
  public void bestimmeObjekte (ClientDavInterface connection, String netz, String kbMessQuerschnitte)
  {
    m_debug = Debug.getLogger();

    System.out.println("Objekte bestimmen für Netz: " + netz);
    
    m_connection = connection;
    
    m_dataModel = m_connection.getDataModel();

    System.out.println("Bestimme äußere Straßensegmente");

    bestimmeAeussereStrassenSegmente (netz);
    
//    System.out.println("Bestimme Straßenknoten");
//    
//    bestimmeStrassenKnoten();

    System.out.println("Bestimme innere Straßensegmente");

    bestimmeInnereStrassenSegmente();    

    System.out.println("Bestimme Messquerschnitte");

    bestimmeMessQuerschnitte(kbMessQuerschnitte);

    System.out.println("Bestimme NBA Messquerschnitte");
    
    bestimmeNbaMessQuerschnitte();

    if (_debug)
      ausgabeStrassenKnoten();

    if (_debug)
      ausgabeAeussereStrassenSegmente();

    if (_debug)
      ausgabeInnereStrassenSegmente();
    
    System.out.println("Anzahl äußerer Strassensegmente: " + m_aeusseresStrassenSegment.size());
    System.out.println("Anzahl innerer Strassensegmente: " + m_inneresStrassenSegment.size());
    System.out.println("Anzahl Staßenknoten            : " + m_strassenKnoten.size());
    System.out.println("Anzahl Messquerschnitt         : " + m_messQuerschnitt.size());
  }
  
  /**
   * Methode zum Bestimmen aller Objekte vom Typ "typ.straßenKnoten"
   */
  /* z. Z. nicht verwndet
  private void bestimmeStrassenKnoten ()
  {
    List<SystemObject> strassenKnoten = bestimmeObjekte( "typ.straßenKnoten" );
    
    Iterator it = strassenKnoten.iterator();
    while (it.hasNext())
    {
      SystemObject objekt = (SystemObject) it.next();
      
      m_strassenKnoten.put( objekt.getPid(), new StrassenKnoten (m_connection, objekt) );
    }
  }
  */

  /**
   * Methode zum Bestimmen aller Objekte com Typ "typ.äußeresStraßenSegment" im übergebenen Netz. Zusätzlich werden die dazugehörenden
   * Strassenknoten angelegt und eine Zuordnung der Strassensegmente zu den Strassenknoten vorgenommen
   */
  private void bestimmeAeussereStrassenSegmente (String netz)
  {
    List<SystemObject> aeussereStrassenSegmente = new ArrayList<SystemObject> ();
    
    ConfigurationObject co = (ConfigurationObject)m_dataModel.getObject (netz);

    if (co == null)
      return;
      
    List liste = co.getNonMutableSet("NetzBestandTeile").getElements();

    System.out.println("Bestimme Objekte --> " + co.getPid());
    
    for (int i = 0; i < liste.size(); i++)
    {
      ConfigurationObject coNetz = (ConfigurationObject)liste.get(i);
      /*
      if (!coNetz.getType().isOfType("typ.äußeresStraßenSegment"))
      {
        List liste2 = coNetz.getNonMutableSet("NetzBestandTeile").getElements();
          
        for (int j = 0; j < liste2.size(); j++)
        {
          ConfigurationObject coNetz2 = (ConfigurationObject)liste2.get(j);

          if (!coNetz2.getType().getPid().equals("typ.äußeresStraßenSegment"))
          {
            System.out.println("Kein ÄußeresStrassenSegment gefunden!");
          }
          else
          {
            if (!aeussereStrassenSegmente.contains(coNetz2))
              aeussereStrassenSegmente.add(coNetz2);
          }       
        }     
      }
      */
      /* Rekursive Lösung ab 17.4.2008 - ANFANG - */
      if (coNetz.isOfType("typ.äußeresStraßenSegment"))
      {
        if (!aeussereStrassenSegmente.contains(coNetz))
        {  
          aeussereStrassenSegmente.add(coNetz);
        }  
      }
      else if (coNetz.isOfType("typ.netz"))
      {
        bestimmeAeussereStrassenSegmente(coNetz.getPid());
      }
      else
      {
        System.out.println("Objekt " + coNetz + " mit unerwartetem Typ " +
            coNetz.getType() + " gefunden in der Menge \"NetzBestandTeile\" von " +
            co);
      }
      /* Rekursive Lösung ab 17.4.2008 - ENDE - */
    }

//    äußereStraßenSegmente = bestimmeObjekte( "typ.äußeresStraßenSegment" );  // Test mit allen äüßeren Straßensegmenten
    
    Iterator it = aeussereStrassenSegmente.iterator();
    while (it.hasNext())
    {
      SystemObject objekt = (SystemObject) it.next();
      
      AeusseresStrassenSegment strassenSegment = new AeusseresStrassenSegment (m_dataModel, objekt);
      strassenSegment.initialisiere();
      
      m_aeusseresStrassenSegment.put( objekt.getPid(), strassenSegment );
      
      StrassenKnoten vonKnoten  = strassenSegment.getVonKnoten(); 
      StrassenKnoten nachKnoten = strassenSegment.getNachKnoten();
      
      if (vonKnoten != null)
        vonKnoten.addAbgehendesAeusseresStrassenSegment (strassenSegment);
      
      if (nachKnoten != null)
        nachKnoten.addHinfuehrendesAeusseresStrassenSegment(strassenSegment);
    }
  }

  /**
   * Methode zum Bestimmen aller Objekte com Typ "typ.inneresStraßenSegment". Zusätzlich wird eine
   * Zuordnung der Strassensegmente zu den Strassenknoten vorgenommen
   */
  private void bestimmeInnereStrassenSegmente ()
  {
    List<SystemObject> innereStrassenSegmente = bestimmeObjekte( "typ.inneresStraßenSegment" );
    
    Iterator it = innereStrassenSegmente.iterator();
    while (it.hasNext())
    {
      SystemObject objekt = (SystemObject) it.next();
      
      InneresStrassenSegment strassenSegment = new InneresStrassenSegment (m_dataModel, objekt);
      
      m_inneresStrassenSegment.put( objekt.getPid(), strassenSegment );
      
      AeusseresStrassenSegment vonStrassenSegment  = strassenSegment.getVonStrassenSegmemt(); 
      AeusseresStrassenSegment nachStrassenSegment = strassenSegment.getNachStrassenSegment();

      if ((vonStrassenSegment) == null && (nachStrassenSegment == null))
      {
        m_inneresStrassenSegment.remove( objekt.getPid() );        
      }
      else
      {
        strassenSegment.initialisiere();
        
        if (vonStrassenSegment != null)
        {
          strassenSegment.setVorgaengerAufStrasse ( vonStrassenSegment  );
          
          StrassenKnoten sk = m_strassenKnoten.get( vonStrassenSegment.getNachKnoten().getPid());
          if (sk != null)
            sk.addInneresStrassenSegment (strassenSegment);
            
          if (vonStrassenSegment.liegtAufSelberStrasse( nachStrassenSegment ))
            vonStrassenSegment.addNachfolgerAufStrasse( strassenSegment );
          else
            vonStrassenSegment.addNachfolger( strassenSegment );
        }

        if (nachStrassenSegment != null)
        {
          strassenSegment.setNachfolgerAufStrase( nachStrassenSegment );
         
          StrassenKnoten sk = m_strassenKnoten.get( nachStrassenSegment.getVonKnoten().getPid());
          if (sk != null)
            sk.addInneresStrassenSegment (strassenSegment);
          
          if (nachStrassenSegment.liegtAufSelberStrasse( vonStrassenSegment ))
            nachStrassenSegment.addVorgaengerAufStrasse( strassenSegment );
          else
            nachStrassenSegment.addVorgaenger( strassenSegment );
        }
      }      
    }
  }

  /**
   * Methode liefert einen Straßenknoten zu einer Pid, existiert der Stasßenknoten noch nicht
   * und handlet es sich bei dem Objekt das zu der Pid gehört um ein Objekt vom Typ 'typ.straßenKnoten',
   * so wird ein neuer StraßenKnoten angelegt.
   * @param pid Pid des Straßenknotens
   * @return Instanz des Straßenknotens, im Fehlerfall null
   */
  public StrassenKnoten getStrassenKnoten (String pid)
  {
    if (m_strassenKnoten.containsKey( pid ))
      return m_strassenKnoten.get( pid );
    else
    {
      ConfigurationObject objekt = (ConfigurationObject)m_dataModel.getObject (pid);
      if (objekt != null)
      {
        if (objekt.isOfType( "typ.straßenKnoten" ))
        {
          StrassenKnoten sk = new StrassenKnoten (m_connection, objekt);
          m_strassenKnoten.put( sk.getPid(), sk );
          return sk;
        }
      }
    }
    
    return null;
  }

  /**
   * Methode liefert ein äußeres Straßensegment
   * @param pid Pid des Straßensegments
   * @return Instanz des Straßensegments, im Fehlerfall null
   */
  public AeusseresStrassenSegment getAeusseresStrassenSegment (String pid)
  {
    if (m_aeusseresStrassenSegment.containsKey( pid ))
      return m_aeusseresStrassenSegment.get( pid );
    
    return null;
  }

  /**
   * Methode liefert ein inneres Straßensegment
   * @param pid Pid des Straßensegments
   * @return Instanz des Straßensegments, im Fehlerfall null
   */
  public InneresStrassenSegment getInneresStrassenSegment (String pid)
  {
    if (m_inneresStrassenSegment.containsKey( pid ))
      return m_inneresStrassenSegment.get( pid );
    return null;
  }

  /**
   * Methode bestimmt die vorhandenen Messquerschnitte und weist den einzelnen Straßensegmente (innere und äußere) die Messquerschnitte
   * zu, die auf den jeweiligen Strassensegmenten liegen (geordnet nach Offset).
   * @param kbMessQuerschnitte Konfigurationsbereiche der zu verwendenden Messquerschnitte mit ":" getrennt. Leerer String: alle Konfigurationsbereiche
   */
  private void bestimmeMessQuerschnitte(String kbMessQuerschnitte)
  {
//    List<SystemObject> elemente = bestimmeObjekte( "typ.messQuerschnittAllgemein" );

    List<SystemObject> elemente = objektListeErstellen(m_dataModel, "typ.messQuerschnittAllgemein", kbMessQuerschnitte);

    for (int i = 0; i < elemente.size(); i++)
    {
      ConfigurationObject co = (ConfigurationObject)elemente.get(i);
      
      MessQuerschnitt mq = new MessQuerschnitt (m_connection, co);
      // m_messQuerschnitt.put( co.getPid(), mq );
      
      StrassenSegment linie = mq.getReferenz();
      
      if (linie != null)
      {
        m_messQuerschnitt.put( co.getPid(), mq );
        linie.addMessQuerschnitt (mq);
      }
       
    }
  }

  /**
   * Methode ermittelt, ob es sich bei der übergebenen Pid um die Pid eines inneren Straßensegments handelt
   * @param pid zu prüfende Pid
   * @return true: Pid gehört zu einem inneren Straßensegment, sonst false
   */
  private boolean isInneresSrtassenSegment( String pid )
  {
    return m_inneresStrassenSegment.containsKey( pid ); 
  }

  /**
   * Methode ermittelt, ob es sich bei der übergebenen Pid um die Pid eines äußeren Straßensegments handelt
   * @param pid zu prüfende Pid
   * @return true: Pid gehört zu einem äußeren Straßensegment, sonst false
   */
  private boolean isAeusseresStrassenSegment( String pid )
  {
    return m_aeusseresStrassenSegment.containsKey( pid );
  }

  /**
   * Methode liefert zu einem Strassensegment die dazugehörenden Messquerschnitte
   * @param pidStrassenSegment Pid des Strassensegments
   * @return Vector mit Messquerschnitten
   */
  public Vector<MessQuerschnitt> getMessQuerschnitteVonStrassenSegment (String pidStrassenSegment)
  {
    if (isAeusseresStrassenSegment( pidStrassenSegment ))
    {
      if (m_aeusseresStrassenSegment.containsKey( pidStrassenSegment ))
        return m_aeusseresStrassenSegment.get( pidStrassenSegment ).getMessquerschnitte();
    }
    
    if (isInneresSrtassenSegment( pidStrassenSegment ))
    {
      if (m_inneresStrassenSegment.containsKey( pidStrassenSegment ))
        return m_inneresStrassenSegment.get( pidStrassenSegment ).getMessquerschnitte();
    }
    
    return null;
  }

  /**
   * Methode zum Bestimmen der Objekte, die zu einer bestimmten PID gehören (z.B. typ.de)
   * @param objPid Pid der Objekttypen
   * @return Liste mit den Systemobjekten 
   */
  private List<SystemObject> bestimmeObjekte (String objPid)
  {
    System.out.println("bestimmeObjekte --> " + objPid);

    List<SystemObject> objekte = new ArrayList<SystemObject> ();

    // Systemobjekt erzeugen

    SystemObjectType typeSysObj = null;

    typeSysObj = m_connection.getDataModel().getType(objPid);
    
    if (typeSysObj == null)
      return objekte;
      
    if (typeSysObj.isConfigurating())
      typeSysObj = (ConfigurationObjectType) typeSysObj;
    else
      typeSysObj = (DynamicObjectType) typeSysObj;
    
    // Elemente dieses Systemobjekts einlesen
    
    List typobjekte = typeSysObj.getElements();
    
    Iterator iterator_Typen = typobjekte.iterator();

    while (iterator_Typen.hasNext()) 
    {
      SystemObject sysObj = (SystemObject) iterator_Typen.next();
      
      objekte.add ( sysObj );
      
//      System.out.println(sysObj.getPid());
    }

    return objekte;
  }

  /**
   * Methode zur Ausgabe der äußeren Staßensegmente (für Testzwecke)
   */
  private void ausgabeAeussereStrassenSegmente ()
  {
    Iterator it = m_aeusseresStrassenSegment.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry) it.next();
      
      AeusseresStrassenSegment s = (AeusseresStrassenSegment) entry.getValue();
      
//      if (!DebugFilter.debugSS( s.getPid() ))
//        continue;
      
      System.out.println("--------------------------------------");
      System.out.println(s.getPidRichtungStrasse());

      String buffer = null;

      long l = 0;
      
      buffer = "Straßenteilsegmente:   ";
      Vector<StrassenTeilSegment> pidsts  = s.getStrassenTeilSegmente();
      Iterator<StrassenTeilSegment> itvsts = pidsts.iterator();
      while (itvsts.hasNext())
      {
        StrassenTeilSegment sts = itvsts.next();
        buffer += " " + sts.getPid() + " (" + sts.getOffset() + "/" + sts.getLaenge() + ")";
        l += sts.getLaenge();
      }
      System.out.println(buffer);
      
      System.out.println("Länge: " + s.getLaenge() + " (" + l + ")");
      
      buffer = "Vorgänger auf Straße:  ";
      Vector<StrassenSegment> pids  = s.getVorgaengerAufStrasse();
      Iterator itv = pids.iterator();
      while (itv.hasNext())
      {
        StrassenSegment s1 = (StrassenSegment) itv.next();
        buffer += " " + s1.getPidRichtungStrasse() + "(" + s1.getLaenge() + ")";
      }
      System.out.println(buffer);
      
      if (s.getKuerzestenVorgaengerAufStrasse() != null)
        System.out.println("kürzester Vorgänger:   " + s.getKuerzestenVorgaengerAufStrasse().getPid());
      
      buffer = "sonstige Vorgänger:    ";
      pids  = s.getVorgaenger();
      itv = pids.iterator();
      while (itv.hasNext())
        buffer += " " + ((StrassenSegment) itv.next()).getPidRichtungStrasse();
      System.out.println(buffer);
      
      buffer = "Nachfolger auf Straße: ";
      pids  = s.getNachfolgerAufStrasse();
      itv = pids.iterator();
      while (itv.hasNext())
        buffer += " " + ((StrassenSegment) itv.next()).getPidRichtungStrasse();
      System.out.println(buffer);
      
      buffer = "sonstige Nachfolger:   ";
      pids  = s.getNachfolger();
      itv = pids.iterator();
      while (itv.hasNext())
        buffer += " " + ((StrassenSegment) itv.next()).getPidRichtungStrasse();
      System.out.println(buffer);
      
      buffer = "Einfahrten:            ";
      pids  = s.getEinfahrendeStassenSegmente();
      itv = pids.iterator();
      while (itv.hasNext())
        buffer += " " + ((StrassenSegment) itv.next()).getPidRichtungStrasse();
      System.out.println(buffer);

      buffer = "Einfahrten (MQ):       ";
      Vector v1 = s.getEinfahrendeMessQuerschnitte();
      itv = v1.iterator();
      while (itv.hasNext())
        buffer += " " + ((MessQuerschnitt) itv.next()).getPid();
      System.out.println(buffer);

      buffer = "Ausfahrten:            ";
      pids  = s.getAusfahrendeStrassenSegmente();
      itv = pids.iterator();
      while (itv.hasNext())
        buffer += " " + ((StrassenSegment) itv.next()).getPidRichtungStrasse();
      System.out.println(buffer);

      buffer = "Ausfahrten (MQ):       ";
      Vector v2 = s.getAusfahrendeMessQuerschnitte();
      itv = v2.iterator();
      while (itv.hasNext())
        buffer += " " + ((MessQuerschnitt) itv.next()).getPid();
      System.out.println(buffer);

      buffer = "Messquerschnitte:      ";
      Vector v = s.getMessquerschnitte();
      itv = v.iterator();
      while (itv.hasNext())
      {
        MessQuerschnitt m = (MessQuerschnitt) itv.next();
        buffer += " " + m.getPid() + "(" + m.getOffset() + ") ";
      }
      System.out.println(buffer);
    }
  }

  /**
   * Methode zur Ausgabe der inneren Staßensegmente (für Testzwecke)
   */
  private void ausgabeInnereStrassenSegmente ()
  {
    Iterator it = m_inneresStrassenSegment.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry) it.next();
      
      InneresStrassenSegment s = (InneresStrassenSegment) entry.getValue();

//      if (!DebugFilter.debugSS( s.getPid() ))
//        continue;

      System.out.println("--------------------------------------");
      
      String buffer = s.getPid();
      
      if (s.isEinfahrt())
        buffer += " (Einfahrt)";
      
      if (s.isAusfahrt())
        buffer += " (Ausfahrt)";
      
      System.out.println(buffer);

      long l = 0;
      
      buffer = "Straßenteilsegmente:   ";
      Vector<StrassenTeilSegment> pidsts  = s.getStrassenTeilSegmente();
      Iterator<StrassenTeilSegment> itvsts = pidsts.iterator();
      while (itvsts.hasNext())
      {
        StrassenTeilSegment sts = itvsts.next();
        buffer += " " + sts.getPid() + " (" + sts.getOffset() + "/" + sts.getLaenge() + ")";
        l += sts.getLaenge();
      }
      System.out.println(buffer);
      
      System.out.println("Länge: " + s.getLaenge() + " (" + l + ")");
      
      buffer = "Vorgänger auf Straße:  ";
      Vector<StrassenSegment> pids  = s.getVorgaengerAufStrasse();
      Iterator itv = pids.iterator();
      while (itv.hasNext())
        buffer += " " + ((StrassenSegment) itv.next()).getPid();
      System.out.println(buffer);
      
      buffer = "Nachfolger auf Straße: ";
      pids  = s.getNachfolgerAufStrasse();
      itv = pids.iterator();
      while (itv.hasNext())
        buffer += " " + ((StrassenSegment) itv.next()).getPid();
      System.out.println(buffer);
      
      buffer = "Messquerschnitte:      ";
      Vector v = s.getMessquerschnitte();
      itv = v.iterator();
      while (itv.hasNext())
      {
        MessQuerschnitt m = (MessQuerschnitt) itv.next();
        buffer += " " + m.getPid() + "(" + m.getOffset() + ") ";
      }
      System.out.println(buffer);
    
    }
  }

  /**
   * Methode zum Ausgeben der StassenKnoten (für Testzwecke)
   */  
  private void ausgabeStrassenKnoten ()
  {
    Iterator it = m_strassenKnoten.entrySet().iterator();
    
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry) it.next();
      
      StrassenKnoten sk = (StrassenKnoten) entry.getValue();
      
      System.out.println("--------------------------------------");
      System.out.println(sk.getPid() + " (" + sk.getTyp() + ")");

      System.out.println("Interne Strassensegmente:  ");
      List<InneresStrassenSegment> segmente  = sk.getInnereStrassenSegmente();
      
      Iterator itv = segmente.iterator();
      while (itv.hasNext())
      {
        InneresStrassenSegment iss = (InneresStrassenSegment) itv.next();
//        System.out.println (iss.getSystemObject().getPid() + " Von: " + iss.getVonStrassenSegmemt() + " Nach: " + iss.getNachStrassenSegment());
        System.out.println (iss.getSystemObject().getPid());
      }
    }
  }
  
  /**
   * Methode liefert ein Strassensegment zurück.
   * @param pid Pid des Strassensegments
   * @return Instanz des Strassensegments, im Fehlerfall null
   */
  public StrassenSegment getStrassenSegment (String pid)
  {
    if (isAeusseresStrassenSegment( pid ))
      return m_aeusseresStrassenSegment.get( pid );
    
    if (isInneresSrtassenSegment( pid ))
      return m_inneresStrassenSegment.get( pid );
    
    return null;
  }


  /**
   * Methode ermittelt die NBA-Messquerschnitte
   */
  private void bestimmeNbaMessQuerschnitte()
  {
    List<SystemObject> elemente = bestimmeObjekte( "typ.nbaMessQuerschnitt" );
    
    for (int i = 0; i < elemente.size(); i++)
    {
      ConfigurationObject co = (ConfigurationObject)elemente.get(i);

      Data confData = co.getConfigurationData(m_dataModel.getAttributeGroup("atg.nbaMessQuerschnitt"));

      if (confData == null)
        continue;
      
      String ref = confData.getReferenceValue("MessQuerschnittReal").getValueText();
      
      if (ref != null)
      {
//        System.out.println("Mq: " + ref + " nbaMQ: " + co.getPid());
        
        m_zuordnungMqZuNbaMq.put(ref, co.getPid());
      }
    }
  }
  
  /**
   * Methode bestimmt zu einem Messquerschnitt der mit der Pid vom Typ "typ.messQuerschnitt" die dazugehörende
   * Pid vom Typ "typ.nbaMessQuerschnitt"
   * @param  pidMq Pid vom Typ "typ.messQuerschnitt"
   * @return Pid vom Typ "typ.nbaMessQuerschnitt"
   */
  public String getNbaMqVonMq (String pidMq)
  {
    if (m_zuordnungMqZuNbaMq.containsKey( pidMq ))
      return m_zuordnungMqZuNbaMq.get(pidMq);
    
    return null;
  }

  /**
   * Methode bestimmt zu einem Messquerschnitt der mit der Pid vom Typ "typ.nbaMessQuerschnitt" die dazugehörende
   * Pid vom Typ "typ.messQuerschnitt"
   * @param  pidNbaMq Pid vom Typ "typ.nbaMessQuerschnitt"
   * @return Pid vom Typ "typ.messQuerschnitt"
   */
  public String getMqVonNbaMq (String pidNbaMq)
  {
    Iterator it = m_zuordnungMqZuNbaMq.entrySet().iterator();
    
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry) it.next();
      
      String _pidMq    = (String) entry.getKey();
      String _pidNbaMq = (String) entry.getValue();
      
      if (pidNbaMq.equals( _pidNbaMq ))
        return _pidMq;
    }
    
    return null;
  }

  /**
   * Methode liefert alle Messquerschnitte (ty.messQuerschnitt)
   * @return Liste mit den Messquerschnitten
   */
  public List<SystemObject> getMessQuerschnitte ()
  {
    List<SystemObject> l = new ArrayList<SystemObject> ();

    Iterator it = m_messQuerschnitt.entrySet().iterator();
    
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry) it.next();
      
//      String pid         = (String) entry.getKey();
      MessQuerschnitt mq = (MessQuerschnitt) entry.getValue();

      l.add( mq.getSystemObjekt() );
    }
    
    return l;
  }
  
  /**
   * Methode zur Erstellung einer Liste von Objekten bestimmter Konfigurationsbereiche.
   * Es können die Pids der Konfigurationsbereichen getrennt durch ':' übergeben werden.
   * @param dataModel Datenmodell
   * @param pidObjektTyp Pid des gesuchten ObjektTyps
   * @param konfigurationsBereiche Pids der Konfigurationsbereiche getrennt durch ';'.
   * @throws IllegalArgumentException 
   */
  public List<SystemObject> objektListeErstellen( DataModel dataModel, String pidObjektTyp,  String konfigurationsBereiche) throws IllegalArgumentException
  {
    List<SystemObject> objekte = new ArrayList<SystemObject> ();
    
    String[] argumente = null;
    
    List<SystemObject> alleObjekteTyp = null;
    
    if (dataModel.getType(pidObjektTyp) == null)
    {
      throw new IllegalArgumentException ("Gesuchter Objekttyp nicht vorhanden " + pidObjektTyp);
    }
    
    // keine Argumente übergeben, dann alle Konfigurationsbereiche verwenden   

    if (konfigurationsBereiche.length() == 0)
    {
      if (_debug)
        System.out.println("Kein Argument übergeben, alle Konfigurationsbereiche werden verwendet.");
      
      SystemObjectType sotKonfigurationsbereiche = dataModel.getType("typ.konfigurationsBereich");
      
      List listKonfigurationsbereiche = sotKonfigurationsbereiche.getElements();
      
      argumente = new String[listKonfigurationsbereiche.size()];
      
      for (int i = 0; i < listKonfigurationsbereiche.size(); i++)
      {
        ConfigurationObject konfigurationsbereich = (ConfigurationObject)listKonfigurationsbereiche.get(i);
        
        argumente[i] = konfigurationsbereich.getPid();
      }
    }

    // vorhandene Argumente übernehmen   

    else
    {
      if (konfigurationsBereiche.length() != 0)
      {
        if (_debug)
          System.out.println("Argument '" + konfigurationsBereiche + " wird verwendet.");

        argumente = konfigurationsBereiche.split(":");
      }
    }
    
    // Einzelobjekte bestimmen   
    if (konfigurationsBereiche.length() == 0)
    {  
      System.out.println("Konfigurationsbereich: <alle>");
    }
        
    if (argumente != null)
    {
      for (int i = 0; i < argumente.length; i++)
      {
        if (konfigurationsBereiche.length() != 0)
        {  
          System.out.println("Konfigurationsbereich: " + argumente [i]);
        }
        SystemObject so = dataModel.getObject(argumente[i]);
        
        if (so != null)
        {
          if (so.isOfType (pidObjektTyp))
          {
            m_debug.finer("Einzelobjekt gefunden " + so.getPid());
            
            if (!objekte.contains(so))
            {
              objekte.add(so);

              m_debug.finest("MessQuerschnitt in Liste eingefügt " + so.getPid());
            }
            else
            {
              m_debug.finest("MessQuerschnitt schon in Liste vorhanden " + so.getPid());
            }
          }
          else
          {
            if (so.isOfType("typ.konfigurationsBereich"))
            {
              m_debug.finer("Konfigurationsbereich gefunden " + so.getPid());
              
              if (alleObjekteTyp == null)
              {
                alleObjekteTyp = dataModel.getType(pidObjektTyp).getElements();
              }
              
              for (int j = 0; j < alleObjekteTyp.size(); j++)
              {
                SystemObject so1 = alleObjekteTyp.get(j);
              
                if (!so1.getConfigurationArea().equals(so))
                {
                  continue;
                }
                
                if (!objekte.contains(so1))
                {
                  objekte.add(so1);

                  m_debug.finest("MessQuerschnitt in Liste eingefügt " + so1.getPid() + " " + so1.getConfigurationArea().getPid());
                }
                else
                {
                  m_debug.finest("MessQuerschnitt schon in Liste vorhanden " + so1.getPid());
                }
              }
            }
            else
            {
              // Fehlermeldung, falscher Typ
              
              m_debug.error("Falscher Typ gefunden " + so.getPid() + " " + so.getType().getPid());
            }
          }
        }
        else
        {
          // Fehlermeldung, Objekt nicht in Konfiguration

          m_debug.config("Objekt nicht in Konfiguration " + argumente[i]);
        }
      }
    }
    
    return objekte;
  }
  
  /**
   * Methode liefert alle äußeren Straßensegmente
   * @return Vektor mit äüßeren Straßensegmenten
   */
  public Vector<AeusseresStrassenSegment> getAeussereStrassenSegmente ()
  {
    Vector<AeusseresStrassenSegment> v = new Vector<AeusseresStrassenSegment>();  
  
    Iterator it = m_aeusseresStrassenSegment.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry) it.next();
      
      AeusseresStrassenSegment s = (AeusseresStrassenSegment) entry.getValue();
      
      v.add( s );
    }
    
    return v;
  }
  
  /**
   * Methode liefert alle inneren Straßensegmente
   * @return Vektor mit inneren Straßensegmenten
   */
  public Vector<InneresStrassenSegment> getInnereStrassenSegmente ()
  {
    Vector<InneresStrassenSegment> v = new Vector<InneresStrassenSegment>();  
  
    Iterator it = m_inneresStrassenSegment.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry entry = (Map.Entry) it.next();
      
      InneresStrassenSegment s = (InneresStrassenSegment) entry.getValue();
      
      v.add( s );
    }
    
    return v;
  }

  /**
   * Methode liefert alle Straßenteilsegmente
   * @return Straßensegmente
   */
  public Vector<StrassenTeilSegment> getStrassenTeilSegmente ()
  {
    Vector<StrassenTeilSegment> v = new Vector<StrassenTeilSegment>();

    // äußere Straßensegmente
    
    Iterator<AeusseresStrassenSegment> itÄ = getAeussereStrassenSegmente().iterator();
    while (itÄ.hasNext())
    {
      AeusseresStrassenSegment s = itÄ.next();
      
      Iterator<StrassenTeilSegment> itSts = s.getStrassenTeilSegmente().iterator();
      while (itSts.hasNext())
      {
        v.add ( itSts.next() );
      }
    }

    // innere Straßensegmente
    
    Iterator<InneresStrassenSegment> itI = getInnereStrassenSegmente().iterator();
    while (itI.hasNext())
    {
      InneresStrassenSegment s = itI.next();
      
      Iterator<StrassenTeilSegment> itSts = s.getStrassenTeilSegmente().iterator();
      while (itSts.hasNext())
      {
        v.add ( itSts.next() );
      }
    }

    return v;
  }
}
