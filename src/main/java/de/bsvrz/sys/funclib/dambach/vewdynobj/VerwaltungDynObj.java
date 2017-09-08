package de.bsvrz.sys.funclib.dambach.vewdynobj;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataAndATGUsageInformation;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.debug.Debug;

 
/**
 * Klasse zum Verwalten (erzeugen, loeschen)dynamischer Objekte
 *
 * @version $Revision: 1.1 $ / $Date: 2009/02/06 10:47:28 $ / ($Author: Pittner $)
 *
 * @author Dambach-Werke GmbH
 * @author Timo Pittner
 *
 */
public class VerwaltungDynObj implements Verwaltung, ClientSenderInterface
{
  private Debug _debug;

  protected DataModel _dm;

  private ConfigurationArea _ca;

  private DynamicObjectType _dot;

  private ConfigurationObject _cal;

  private AttributeGroup _atg;

  private Aspect _asp;

  private SystemObject _systemObject;

  private Boolean flag;

  private ClientDavInterface _con;

  /**
   * @param dm
   *        das Datenmodell
   * @param ca
   *        der Konfigrationsbereich
   * @param dot
   *        der Objekttyp
   * @param cal
   *        das Konfigurationsobjekt
   * @param atg
   *        die Attributgruppe
   * @param asp
   *        der Aspekt
   */
  public VerwaltungDynObj(DataModel dm, ConfigurationArea ca, DynamicObjectType dot, ConfigurationObject cal,
      AttributeGroup atg, Aspect asp)
  {
    // TODO Auto-generated constructor stub
    this._dm = dm;
    this._ca = ca;
    this._dot = dot;
    this._cal = cal;
    this._atg = atg;
    this._asp = asp;
    _debug = Debug.getLogger();
  }

  /**
   * @param con
   * @param dm
   * @param ca
   * @param dot
   * @param cal
   * @param atg
   * @param asp
   */
  public VerwaltungDynObj(ClientDavInterface con, DataModel dm, ConfigurationArea ca, DynamicObjectType dot,
      ConfigurationObject cal, AttributeGroup atg, Aspect asp)
  {
    // TODO Auto-generated constructor stub
    this._con = con;
    this._dm = dm;
    this._ca = ca;
    this._dot = dot;
    this._cal = cal;
    this._atg = atg;
    this._asp = asp;
    _debug = Debug.getLogger();
  }

  /**
   * Fügt das dynamische Objekt der Menge hinzu
   * 
   * @param set
   *        die Pid der Menge
   * @return
   *        true, wenns funktioniert hat
   */
  public boolean hinzufuegeZuMenge(String set)
  {
    try
    {
      // TODO Auto-generated method stub
       _cal.getMutableSet(set).add(_systemObject);
 
    }
    catch (Exception e)
    {
      // TODO: handle exception
      e.getStackTrace();
    }
    return true;
  }

  /**
   * Loescht das dynamische Objekt 
   * 
   * @param pid
   *        die Pid
   * @return
   *        true, wenns funktioniert hat
   */
  private boolean loesche(String pid)
  {
    // TODO Auto-generated method stub
    SystemObject so = _dm.getObject(pid);

    if (so != null)
    {

      _debug.fine("Objekt " + so.getPid() + " vorhanden, loesche!");

      _systemObject = (DynamicObject)_dm.getObject(pid);

      if (_systemObject != null)
      {
        try
        {
          _systemObject.invalidate();

          _debug.fine("Objekt " + _systemObject.getPid() + " geloescht!");
        }
        catch (ConfigurationChangeException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

    }
    else
    {

      _debug.fine("Objekt " + pid + " nicht vorhanden");
      return false;

    }
    return true;
  }

  /**
   * Erzeugt das dynamische Objekt
   * 
   * @param pid
   *        die Pid
   * @param name
   *        der Name
   * @param data
   *        die konfigurierenden Daten
   * @return
   *        true, wenns funktioniert hat
   */
  private boolean erzeuge(String pid, String name, Data[] data)
  {
    // TODO Auto-generated method stub
    try
    {

      AttributeGroupUsage atgu = _atg.getAttributeGroupUsage(_asp);

      DataAndATGUsageInformation daaui = null;

      Collection<DataAndATGUsageInformation> col = new ArrayList<DataAndATGUsageInformation>();

      if (data != null)
      {

        for (Data d : data)
        {

          daaui = new DataAndATGUsageInformation(atgu, d);

          col.add(daaui);

        }

        _debug.fine(_dot.getPid() + " " + pid + " " + name + " " + daaui.getData() + " "
            + daaui.getAttributeGroupUsage());

        _systemObject = _ca.createDynamicObject(_dot, pid, name, col);

      }
      else{
        
        _debug.fine(_dot +":"+  pid +":"+  name);
        
        _systemObject = _ca.createDynamicObject(_dot, pid, name, null);
      }

    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  /* (non-Javadoc)
   * @see de.bsvrz.dav.daf.main.ClientSenderInterface#dataRequest(de.bsvrz.dav.daf.main.config.SystemObject, de.bsvrz.dav.daf.main.DataDescription, byte)
   */
  public void dataRequest(SystemObject arg0, DataDescription arg1, byte arg2)
  {
    // TODO Auto-generated method stub
    _debug.fine("Sender VewDynObj : Objekt " + arg0.getPid() + " dd " + arg1 + " state " + arg2);

    if (arg2 == 0)
    {
      flag = true;
      synchronized (this)
      {
        notify();
      }
    }

  }

  /* (non-Javadoc)
   * @see de.bsvrz.dav.daf.main.ClientSenderInterface#isRequestSupported(de.bsvrz.dav.daf.main.config.SystemObject, de.bsvrz.dav.daf.main.DataDescription)
   */
  public boolean isRequestSupported(SystemObject arg0, DataDescription arg1)
  {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * Parametriert ein dynamisches Objekt
   * 
   * @param dd
   *        die Datenbeschreibung
   * @param resultData
   *        die parametrirenden Datensätze, verpackt in einem ResultData
   * @return
   *        true, wenns funktioniert hat
   *        
   * @throws IllegalArgumentException
   */
  private boolean parametriere(DataDescription dd, ResultData resultData) throws IllegalArgumentException
  {
    // TODO Auto-generated method stub

    if (dd == null || resultData == null)
      throw new IllegalArgumentException();

    try
    {

      _debug.fine("send1 : " + resultData.getData());
      getConnection().sendData(resultData);

    }
    catch (DataNotSubscribedException e)
    {
      // TODO Auto-generated catch block
      // e.printStackTrace();

      try
      {
        _debug.fine("subscribe!");
        //getConnection().subscribeSender(this, resultData.getObject(), dd, SenderRole.sender());
        getConnection().subscribeSender(this, resultData.getObject(), dd, SenderRole.sender());

      }
      catch (OneSubscriptionPerSendData e1)
      {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      try
      {

        flag = false;

        synchronized (this)
        {

          while (!flag)
          {

            try
            {
              this.wait();
            }
            catch (InterruptedException e1)
            {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            }
          }
        }

        try
        {
          Thread.sleep(1);
        }
        catch (InterruptedException e1)
        {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

        _debug.fine("send2 : " + resultData.getData());
        getConnection().sendData(resultData);
      }
      catch (DataNotSubscribedException e1)
      {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      catch (SendSubscriptionNotConfirmed e1)
      {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
    catch (SendSubscriptionNotConfirmed e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return true;

  }

  /**
   * Fügt das dynamische Objekt der Menge hinzu
   * 
   * @param pid
   *        die Pid der Menge
   * @return
   *        true, wenns funktioniert hat
   */
  public boolean entferneVonMenge(String pid)
  {
    try
    {
      // TODO Auto-generated method stub
      //System.out.println(pid + " : " + _dynamicObject.getPid());
      
      _cal.getMutableSet(pid).remove(_systemObject);

    }
    catch (Exception e)
    {
      // TODO: handle exception
      e.getStackTrace();
    }
    return true;

  }

  
  /**
   * Holt das dynamischeObjekt der Verwaltungsinstanz
   * 
   * @return
   *      das dynamische Objekt
   */
  public SystemObject getDynamicObject()
  {
    return _systemObject;
  }

 
  /**
   * Setzt das dynamischeObjekt der Verwaltungsinstanz
   * 
   * @param dynamicObject
   */
  public void setDynamicObject(SystemObject dynamicObject)
  {
    _systemObject = dynamicObject;
  }

  /**
   * Holt die Datenverteilerverbindung
   * 
   * @return
   */
  public ClientDavInterface getConnection()
  {
    return _con;
  }

  /**
   * Setzt die Datenverteilerverbindung
   * 
   * @param connection
   */
  public void setConnection(ClientDavInterface connection)
  {
    _con = connection;
  }
  
  /* (non-Javadoc)
   * @see de.bsvrz.vew.ereigniskal.ereigniskal.verwaltung.Verwaltung#erzeuge(java.lang.String, java.lang.String, java.lang.String)
   */
  public void erzeuge(String pid, String name, String set)
  {
    
    
    if (erzeuge(pid, name, (Data[])null))
    //if (erzeuge(pid, name, new Data[1]))
    {

      if (set.length() > 0)
        hinzufuegeZuMenge(set);

    }
    else
    {

      throw new IllegalArgumentException();

    }
  }
  
  /* (non-Javadoc)
   * @see de.bsvrz.vew.ereigniskal.ereigniskal.verwaltung.Verwaltung#erzeuge(java.lang.String, java.lang.String, java.lang.String, de.bsvrz.dav.daf.main.Data[])
   */
  public void erzeuge(String pid, String name, String set, Data[] data)
  {
    
    if (erzeuge(pid, name, data))
    {

      if (set.length() > 0)
        hinzufuegeZuMenge(set);

    }
    else
    {

      throw new IllegalArgumentException();

    }
  }

  /* (non-Javadoc)
   * @see de.bsvrz.vew.ereigniskal.ereigniskal.verwaltung.Verwaltung#parametriere(java.lang.String, java.lang.String)
   */
  public void parametriere(String attribut, String definition)
  {

    Data data = _con.createData(_atg);

    if (attribut.length() > 0 && definition.length() > 0)
    {

      data.getItem(attribut).asTextValue().setText(definition);

    }
    else
    {

      throw new IllegalArgumentException();

    }

    DataDescription dd = new DataDescription(_atg, _asp);

    ResultData rd = new ResultData(getDynamicObject(), dd, new Date().getTime(), data);

    parametriere(dd, rd);

  }
 
  
  /* (non-Javadoc)
   * @see de.bsvrz.vew.ereigniskal.ereigniskal.verwaltung.Verwaltung#parametriere(de.bsvrz.dav.daf.main.Data[])
   */
  public void parametriere(Data[] data)
  {
    DataDescription dd = new DataDescription(_atg, _asp);
    
    for (Data d : data)
    {
      
      ResultData rd = new ResultData(getDynamicObject(), dd, new Date().getTime(), d);
      
      parametriere(dd, rd);
      
    }
    
    

  }

  /* (non-Javadoc)
   * @see de.bsvrz.vew.ereigniskal.ereigniskal.verwaltung.Verwaltung#loesche(java.lang.String, java.lang.String)
   */
  public void loesche(String pid, String set)
  {

    if (pid.length() > 0)
    {
      if (set.length() > 0)
        entferneVonMenge(set);

       loesche(pid);

    }
    else
      throw new IllegalArgumentException();

  }
  

}
