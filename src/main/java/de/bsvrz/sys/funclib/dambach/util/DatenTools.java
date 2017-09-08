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

package de.bsvrz.sys.funclib.dambach.util;

import java.util.Iterator;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.Data.Array;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.DoubleAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.StringAttributeType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.TimeAttributeType;

/**
 * Methoden zur Bearbeitung von Datensätzen.
 *  
 * @author Dambach Werke GmbH
 * @author Stefan Sans
 * @version $Revision: 1.1 $ / $Date: 2008/09/29 11:20:38 $ / ($Author: Sans $)
 */
public class DatenTools 
{
  /**
   * Fertigt eine tiefe Kopie der Daten, die in quelle enthalten sind, in ziel an.
   * Die Daten von quelle werden rekursiv durchlaufen, es werden alle Attributlisten,
   * Arrays (werden vor dem Kopieren in Ziel auf die selbe Länge gesetzt wie in quelle)
   * und einfache Attribute vom Typ Text, Ganzahl unskaliert, Ganzzahl skaliert, Zeit
   * und Referenz unterstützt.
   * ziel muss jeweils die exakt gleiche Datenstruktur wie in quelle auf demselben Level
   * enthalten, die Struktur muss vollständig vorhanden sein (wie z. B., wenn mit
   * {@link de.bsvrz.dav.daf.main.ClientDavInterface#createData(AttributeGroup)} erzeugt.
   * 
   * @param quelle der Datensatz, dessen Daten kopiert werden sollen
   * @param ziel der Datensatz, in den die Daten kopiert werden. Er muss die Struktur von 
   * Quelle enthalten
   * 
   * @throws IllegalArgumentException wenn Attributtypen oder die Namen eines (Sub-)Datums
   * auf einem bestimmten Level nicht gleich sind oder ein in quelle vorhandenes Attribut
   * in Ziel auf demselben Level nicht vorhanden ist
   * @throws UnsupportedOperationExceptuion wenn ein (Sub-)Datum nicht Attributliste, Array
   * oder einfaches Datum ist, bzw. wenn bei einfachem Datum der Typ nicht unterstützt wird
   * (s. o.)
   */
  public static void tiefeKopie (Data quelle, Data ziel)
  {
    /*
     * Attributtypen müssen gleich sein auf jedem Level. 
     * Auf ATG-Level sind beide Attributtypen null!
     */
    
    if ((quelle.getAttributeType() == null && ziel.getAttributeType() != null) ||
        (quelle.getAttributeType() != null && ziel.getAttributeType() == null) ||
        (quelle.getAttributeType() != null && ziel.getAttributeType() != null &&
        !quelle.getAttributeType().equals(ziel.getAttributeType())))
    {
      /* Attribut-Typen auf diesem Sub-Level sind unterschiedlich */ 
      throw new IllegalArgumentException("Attributtypen des Datums \"" + 
          quelle.getName() + "\" sind nicht gleich für Quelle und Ziel");
    }  
    
    /*
     * Namen müssen gleich sein auf jedem Level. 
     * Ausnahme: wenn beide Attributtypen null sind (auf ATG-Level), wird nicht geprüft
     */
    if (quelle.getAttributeType() != null && ziel.getAttributeType() != null  &&
        !quelle.getName().equals(ziel.getName()))
    {
      /* Item/Attribut-Namen auf diesem Sub-Level sind unterschiedlich */
      throw new IllegalArgumentException("Attributnamen des Datums \"" + 
          quelle.getName() + "\" sind nicht gleich für Quelle und Ziel");
    }  
  
    if (quelle.isList())
    {
      Iterator it = quelle.iterator();
      while (it.hasNext())
      {
        Data subQuelle = (Data)it.next();
        Data subZiel = ziel.getItem(subQuelle.getName());
        
        if (subZiel == null)
        {
          /* Ziel-Attribut mit diesem  Namen existiert nicht */
          throw new IllegalArgumentException("Ziel-Datensatz hat kein Sub-Datum \"" + 
              subQuelle.getName() + "\"");
        }
        tiefeKopie(subQuelle, subZiel);
      }  
    }
    else if (quelle.isArray())
    {
      Array arrQuelle = quelle.asArray();
      Array arrZiel = ziel.asArray();
      
      arrZiel.setLength(arrQuelle.getLength());
      
      for(int i = 0; i < arrQuelle.getLength(); i += 1)
      {
        Data subSource = arrQuelle.getItem(i);
        Data subTarget = arrZiel.getItem(i);
        tiefeKopie(subSource, subTarget);
      }
    }
    else if (quelle.isPlain())
    {
      if (quelle.getAttributeType() instanceof StringAttributeType)
      {
        String value = quelle.asTextValue().getText();
        ziel.asTextValue().setText(value);
      }
      else if (quelle.getAttributeType() instanceof IntegerAttributeType)
      {
        long value = quelle.asUnscaledValue().longValue();
        ziel.asUnscaledValue().set(value);
      }
      else if (quelle.getAttributeType() instanceof DoubleAttributeType)
      {
        double value = quelle.asScaledValue().doubleValue();
        ziel.asScaledValue().set(value);
      }
      else if (quelle.getAttributeType() instanceof TimeAttributeType)
      {
        long value = quelle.asTimeValue().getMillis();
        ziel.asTimeValue().setMillis(value);
      }
      else if (quelle.getAttributeType() instanceof ReferenceAttributeType)
      {
         SystemObject value = quelle.asReferenceValue().getSystemObject();
         ziel.asReferenceValue().setSystemObject(value);
      }
      else
      {  
        /* Nicht unterstützter Attributtyp */
        throw new UnsupportedOperationException("Operation nicht definiert für Datum \"" + 
          quelle.getName() + "\" und Attributtyp" + quelle.getAttributeType()); 
      }  
    }  
    else
    {
      /* Nicht unterstützte Struktur */
      throw new UnsupportedOperationException("Operation nur für Attributlisten, Arrays und einfache Datentypen definiert");
    }
  }
  
}
