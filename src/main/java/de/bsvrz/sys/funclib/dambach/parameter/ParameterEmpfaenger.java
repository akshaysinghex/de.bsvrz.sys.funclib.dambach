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
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.dambach.util.DatenTools;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Empfangen und Versenden eines Parameterdatensatzes.
 * Empfängt einen Datensatz einer Quell-ATG und kopiert ihn in einen Datensatz
 * einer Ziel-ATG. Der Datensatz der Ziel-ATG wird mit
 * {@link de.bsvrz.dav.daf.main.ClientDavInterface#createData(AttributeGroup)}
 * erzeugt und muss die Struktur des Quelldatensatzes exakt enthalten. muss, aber eine unterschiedliche ATG haben
 * Die Empfangs-Anmeldung -und Abmeldung erfolgt im Konstruktor.
 * 
 * @author Dambach Werke GmbH
 * @author Stefan Sans
 * @version $Revision: 1.1 $ / $Date: 2008/09/29 11:20:38 $ / ($Author: Sans $)
 */
public class ParameterEmpfaenger implements ClientReceiverInterface
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
   * Datenbeschreibung der Daten, die aus der Parametrierung gelesen werden
   */
  final DataDescription datenBeschreibungQuell;
  
  /**
   * Datenbeschreibung unter der die Daten zurückgegeben werden
   */
  final DataDescription datenBeschreibungZiel;
    
  /**
   * Ist auf true gesetzt, solange auf update gewartet wird
   */
  private boolean _warten = false;
  
  /** 
   * Empfangene Daten
   */
  private ResultData _data = null;
  
  public ParameterEmpfaenger(ClientDavInterface con, SystemObject obj, 
      String atgQuellPid, DataDescription datenBeschreibungZiel)
  {
    connection = con;
    davObjekt = obj;
    
    AttributeGroup atg = connection.getDataModel().getAttributeGroup(atgQuellPid);
    Aspect asp = connection.getDataModel().getAspect("asp.parameterSoll");
    datenBeschreibungQuell = new DataDescription(atg, asp);
    
    this.datenBeschreibungZiel = datenBeschreibungZiel; 
    
    connection.subscribeReceiver(this, davObjekt, datenBeschreibungQuell, 
        ReceiveOptions.normal(), ReceiverRole.receiver());
    _debug.finest(datenBeschreibungQuell + " zum Empfang angemeldet für " + davObjekt);
    
    _debug.finer("Warte max. " + TIMEOUT + "ms auf Update Parameter-Daten von " + 
        atg.getPid() +  " für " + davObjekt.getPid());
    synchronized(this)
    {
      if (getData() == null)
      {  
        _warten = true;
        try
        {
          wait(TIMEOUT);
        }
        catch(InterruptedException e)
        {
          // tue nichts
        }
        _warten = false;
      }  
    }
    
    if (getData() == null)
    {
      _debug.warning("Keine Parameter-Daten empfangen von " + 
          atg.getPid() +  " für " + davObjekt.getPid());
    }
    
    connection.unsubscribeReceiver(this, davObjekt, datenBeschreibungQuell);
    _debug.finest("Empfang abgemeldet von " + datenBeschreibungQuell + " für " + davObjekt);
  }
  
  /**
   * @deprecated
   * Nur zur Rückwartskompatibilität erhalten, Ziel-Aspekt wird hier (fälschlicherweise?)
   * auf asp.parameterSoll gesetzt
   */
  public ParameterEmpfaenger(ClientDavInterface con, SystemObject obj, 
      String atgQuellPid, String atgZielPid)
  {
    this(con, obj, atgQuellPid, 
        new DataDescription(con.getDataModel().getAttributeGroup(atgZielPid),
        con.getDataModel().getAspect("asp.parameterSoll")));
  }
   
  /**
   * @return die empfangenen Daten
   */
  public ResultData getData()
  {
    return _data;
  }
  
  /*
   * (Kein Javadoc)
   * @see de.bsvrz.dav.daf.main.ClientReceiverInterface#update(de.bsvrz.dav.daf.main.ResultData[])
   */
  public void update(ResultData[] resultData)
  {
    for (ResultData data: resultData)
    {
      Aspect asp = data.getDataDescription().getAspect();
      AttributeGroup atg = data.getDataDescription().getAttributeGroup();
      
      if (atg.equals(datenBeschreibungQuell.getAttributeGroup()) &&
          asp.equals(datenBeschreibungQuell.getAspect())) 
      {
        if (data.hasData())
        {
            Data datenSatz = connection.createData(datenBeschreibungZiel.getAttributeGroup());
            DatenTools.tiefeKopie(data.getData(), datenSatz);
            _data = new ResultData(davObjekt, datenBeschreibungZiel, data.getDataTime(), datenSatz);
            _debug.fine("Bestehende Parameter-Daten empfangen von " + 
                atg.getPid() +  " für " + davObjekt.getPid());
        }
        else
        {
          _data = new ResultData(davObjekt, datenBeschreibungZiel, data.getDataTime(), null);          
        }
      }
    }
    if (_warten)
    {
      synchronized(this)
      {
        notify();
      }
    }
  }
  
}