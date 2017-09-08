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

package de.bsvrz.sys.funclib.dambach.parameter;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.dambach.util.DatenTools;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Kopieren und Versenden eines Parameterdatensatzes.
 * Kopiert einen Datensatz in einen anderen Datensatz, der die Struktur des 
 * Quelldatensatzes exakt enthalten muss, aber eine unterschiedliche ATG haben
 * kann, und bietet Funktionalität zum Versenden der Daten.<br>
 * Das Senden der Daten unter asp.parameterVorgabe erfolgt nach dem Starten des 
 * Threads, der eine Instanz dieser Klasse verkörpert.
 * 
 * @author Dambach Werke GmbH
 * @author Stefan Sans
 * @version $Revision: 1.1 $ / $Date: 2008/09/29 11:20:38 $ / ($Author: Sans $)
 */
public class ParameterSender extends Thread implements ClientSenderInterface
{
  /**
   * Debug-Logger für Logging-Ausgaben
   */
  private final static Debug _debug = Debug.getLogger();
  
  /**
   * Zeitin ms, die max. auf Sendesteuerung gewartet wird
   */
  static final long TIMEOUT = 5000;
  
  /**
   * Die Datenverteilerverbindung
   */
  final ClientDavInterface connection;
  
  /**
   * Das DAV-Objekt, für das das Senden erfolgt
   */
  final SystemObject davObjekt;
  
  /**
   * Datenbeschreibung der Daten, die verarbeitet werden
   */
  final DataDescription datenBeschreibung;
  
  /**
   * Die Daten, die gesendet werden sollen
   */
  final ResultData daten;
  
  /**
   * Ist auf true gesetzt, solange auf Sendesteuerung gewartet wird
   */
  private boolean _warten = false;
    
  /**
   * Konstruiert eine neue Instanz vom Typ <code>ParameterSender</code>.
   * Die übergebenen Daten werden auf die angegebene Parameter-ATG kopiert
   * und nach starten des Threads unter dem Aspekt asp.parameterVorgabe
   * gesendet.
   * 
   * @param data die Daten, die gesendet werden sollen
   * @param atgPid die PID der Parameter-ATG, auf die die Daten kopiert werden sollen 
   */
  public ParameterSender(ClientDavInterface con, SystemObject obj, ResultData data,
      String atgPid)
  {
    super();
    
    connection = con;
    davObjekt = obj;
    
    AttributeGroup atg = connection.getDataModel().getAttributeGroup(atgPid);
    Aspect asp = connection.getDataModel().getAspect("asp.parameterVorgabe");
    datenBeschreibung = new DataDescription(atg, asp);
    
    Data datenSatz = connection.createData(atg);
    DatenTools.tiefeKopie(data.getData(), datenSatz);
        
    daten = new ResultData(davObjekt, datenBeschreibung, data.getDataTime(), datenSatz); 
  }
  
  @Override
  public void run()
  {
    boolean angemeldet = false;
    
    try
    {
      try
      {
        connection.subscribeSender(this, davObjekt, datenBeschreibung, SenderRole.sender());
        angemeldet = true;
      }
      catch(OneSubscriptionPerSendData e)
      {
        // angemeldet bleibt false
      }
      
      try
      {
        connection.sendData(daten);
        _debug.finer("Parameter erfolgreich aktualisiert mit " + 
            datenBeschreibung.getAttributeGroup() + " für " + davObjekt); 
      }
      catch(SendSubscriptionNotConfirmed e)
      {
        if (TIMEOUT > 0)
        {  
          _debug.finer("Warte max. " + TIMEOUT + "ms auf Sendesteuerung für " + davObjekt.getPid());
          synchronized(this)
          {
            _warten = true;
            try
            {
              wait(TIMEOUT);
            }
            catch(InterruptedException ex)
            {
              // tue nichts
            }
            _warten = false;
          }
          try
          {
            connection.sendData(daten);
          }
          catch (SendSubscriptionNotConfirmed ex)
          {
            _debug.warning("Keine Sendesteuerung für " + davObjekt.getPid(), ex);
          }
        }
        else
        {
          _debug.warning("Keine Sendesteuerung für " + davObjekt.getPid(), e);
        }
      }
      catch (DataNotSubscribedException e)
      {
        _debug.error("Keine Sendeanmeldung bzw. Sendeanmeldung ungültig für " + davObjekt.getPid(), e);
      }
    }
    finally
    {
      if (angemeldet)
      {  
        connection.unsubscribeSender(this, davObjekt, datenBeschreibung);
      }  
    }  
  }
  
  /*
   * (Kein Javadoc)
   * @see de.bsvrz.dav.daf.main.ClientSenderInterface#dataRequest(de.bsvrz.dav.daf.main.config.SystemObject, de.bsvrz.dav.daf.main.DataDescription, byte)
   */
  public void dataRequest(SystemObject object, DataDescription dataDescription, byte state)
  {
    _debug.finest("dataRequest() für " + davObjekt.getPid() + " - Sender state: " + state);
    if (_warten && state == ClientSenderInterface.START_SENDING)
    {
      synchronized(this)
      {
        notify();
      }
    }
  }

  /*
   * (Kein Javadoc)
   * @see de.bsvrz.dav.daf.main.ClientSenderInterface#isRequestSupported(de.bsvrz.dav.daf.main.config.SystemObject, de.bsvrz.dav.daf.main.DataDescription)
   */
  public boolean isRequestSupported(SystemObject so, DataDescription dd)
  {
    return true;
  }
  
}