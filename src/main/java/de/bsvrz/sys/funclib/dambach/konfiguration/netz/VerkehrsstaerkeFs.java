package de.bsvrz.sys.funclib.dambach.konfiguration.netz;

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
 * <code>VerkehrsstaerkeFs</code> bestimmt die Verkehrsstärke auf einem Fahrstreifen.
 * 
 * @author Dambach Werke GmbH
 * @version $Revision: 1.2 $ / $Date: 2008/10/31 12:48:00 $ / ($Author: Sans $)
 */

public class VerkehrsstaerkeFs implements ClientReceiverInterface
{
  /**
   * Debug-Logger für Logging-Ausgaben
   */
  private final static Debug _debug = Debug.getLogger();
  
  /**
   * Die Datenverteiler-Verbindung
   */
  private ClientDavInterface _connection;
  
  /**
   * Der referenzierte Fahrstreifen
   */
  private SystemObject _fahrStreifen;
  
  /**
   * Die Datenbeschreibung zur Datenverteiler-Kommunikatiuon
   */
  private DataDescription _datenBeschreibung;
  
  /**
   * Die Verkehrsstärke QB auf dem referenzierten Fahrstreifen in Fzg/h
   */
  private long _qb = -1;
  
  /**
   * Die Verkehrsstärke QKfz auf dem referenzierten Fahrstreifen in Fzg/s
   */
  private long _qkfz = -1;
     
  /**
   * Konstruiert eine neue Instanz vom Typ <code>VerkehrsstaerkeFs</code>.
   * Der übergebene Fahrstreifen wird zum Empfang seiner aktuelllen Verkehrsstärke
   * beim Datenverteiler angemeldet.
   *  
   * @param connection die Datenverteiler-Verbindung
   * @param fsPid die PID des referenzierten Fahrstreifens
   */
  public VerkehrsstaerkeFs(ClientDavInterface connection, String fsPid)
  {
    _connection = connection;
    _fahrStreifen = connection.getDataModel().getObject(fsPid);
    
    AttributeGroup atg = connection.getDataModel().getAttributeGroup("atg.verkehrsDatenKurzZeitFs");
    Aspect asp = connection.getDataModel().getAspect("asp.analyse");
    _datenBeschreibung = new DataDescription(atg, asp);
    
    connection.subscribeReceiver(this, _fahrStreifen, _datenBeschreibung, 
        ReceiveOptions.normal(), ReceiverRole.receiver());
  }
  
  /**
   * @deprecated
   * Bestimmt die aktuelle Verkehrsstärke QB für den referenzierten Fahrstreifen
   *  
   * @return die Verkehrsstärke QB in Fzg/h
   */
  public long getVerkehrsstaerke()
  {
    return _qb;
  }
  
  /**
   * Bestimmt die aktuelle Verkehrsstärke QB für den referenzierten Fahrstreifen
   *  
   * @return die Verkehrsstärke QB in Fzg/h
   */
  public long getVerkehrsstaerkeQB()
  {
    return _qb;
  }
  
  /**
   * Bestimmt ob der Wert der aktuellen Verkehrsstärke QB plausibel ist
   *  
   * @return true, wenn die aktuelle Verkehrsstärke QB plausibel ist, false sonst
   */
  public boolean isQBPlausibel()
  {
    return (_qb >= 0);
  }
  
  /**
   * Bestimmt die aktuelle Verkehrsstärke QKfz für den referenzierten Fahrstreifen
   *  
   * @return die Verkehrsstärke QKfz in Fzg/h
   */
  public long getVerkehrsstaerkeQKfz()
  {
    return _qkfz;
  }
  
  /**
   * Bestimmt ob der Wert der aktuellen Verkehrsstärke QKfz plausibel ist
   *  
   * @return true, wenn die aktuelle Verkehrsstärke QKfz plausibel ist, false sonst
   */
  public boolean isQKfzPlausibel()
  {
    return (_qkfz >= 0);
  }
  
  /**
   * Methode mit der eine Instanz dieser Klasse veranlasst wird, ihre Objekte am DaV abzumelden
   * und die angemeldeten Listener abzumelden.
   */
  public void dispose ()
  {
    // Fahrstreifen abmelden 
    _connection.unsubscribeReceiver( this, _fahrStreifen, _datenBeschreibung);
  }
  
  public void update(ResultData[] resultData)
  {
    for (ResultData data : resultData)
    {
      Aspect asp = data.getDataDescription().getAspect();
      AttributeGroup atg = data.getDataDescription().getAttributeGroup();
      
      if (atg.getPid().equals(_datenBeschreibung.getAttributeGroup().getPid()) && 
          asp.getPid().equals(_datenBeschreibung.getAspect().getPid()))
      {
        long qbAlt = _qb;
        long qkfzAlt = _qb;
        if (data.hasData())
        {
          Data datenSatz = data.getData().getItem("qB");
          
          _qb = datenSatz.getUnscaledValue("Wert").longValue();
          
          datenSatz = data.getData().getItem("qKfz");
          
          _qkfz = datenSatz.getUnscaledValue("Wert").longValue();
         
        }
        else
        {
          _qb = -1;
          _qkfz = -1;
        }
        if (qbAlt != _qb)
        {
          _debug.config("Verkehrstärke qB ändert sich von " + qbAlt + " auf " +
              _qb + "Fzg/h für " + _fahrStreifen);
        }
        if (qkfzAlt != _qkfz)
        {
          _debug.config("Verkehrstärke qKfz ändert sich von " + qbAlt + " auf " +
              _qkfz + "Fzg/s für " + _fahrStreifen);
        }
      }
    }
  }
  
}
