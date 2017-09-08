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


/**
 * (Indirekte) Erweiterung des ClientReceiverInterface.<br>
 * Modelliert einen Empfänger für genau ein (Stellvertreter-) Systemobjekt. Sollte benutzt 
 * werden, wenn Empfängeranmeldung -und Abmeldung unterstützt werden soll
 * @see de.bsvrz.sys.funclib.dambach.dav.daf#EmpfaengerMitAnmeldung
 * 
 * @author Dambach Werke GmbH
 * @author Stefan Sans
 * @version $Revision: 1.1 $ / $Date: 2008/09/29 11:20:38 $ / ($Author: Sans $)
 */
public interface EmpfaengerMitAnAbmeldung extends EmpfaengerMitAnmeldung
{
  /**
   * Abmelden aller Datenidentifikationen für diese Instanz und das zugeordnete Systemobjekt
   */
  public void abmelden();
}
