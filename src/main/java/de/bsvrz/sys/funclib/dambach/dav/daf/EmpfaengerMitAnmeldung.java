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

package de.bsvrz.sys.funclib.dambach.dav.daf;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.config.SystemObject;
/**
 * Erweiterung des ClientReceiverInterface.<br>
 * Modelliert einen Empfänger für genau ein (Stellvertreter-) Systemobjekt. Sollte benutzt 
 * werden, wenn nur Empfängeranmeldung, keine explizite Abmeldung unterstützt werden soll 
 * 
 * @author Dambach Werke GmbH
 * @author Stefan Sans
 * @version $Revision: 1.1 $ / $Date: 2008/09/29 11:20:38 $ / ($Author: Sans $)
 */
public interface EmpfaengerMitAnmeldung extends ClientReceiverInterface
{
  /**
   * Bestimmt die Datenverteilertverbindung, über die die Empfängeranmeldung erfolgt
   * @return die Datenverteilertverbindung für die Empfängeranmeldung
   */
  public ClientDavInterface getConnection();
  
  
  /**
   * Bestimmt das Systemobjekt, für das die Empfängeranmeldugen für diese Instanz erfolgen
   * @return das singuläre Systemobjekt
   */
  public SystemObject getObjekt();
  
  /**
   * Anmelden aller Datenidentifikationen für diese Instanz und das zugeordnete Systemobjekt
   */
  public void anmelden();
  
}
