/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ettrema.ldap;

/**
 *
 * @author brad
 */
public class NotCondition implements Condition {

	protected final Condition condition;

	protected NotCondition(Condition condition) {
		this.condition = condition;
	}

	public boolean isEmpty() {
		return condition.isEmpty();
	}

	public boolean isMatch(LdapContact contact) {
		return !condition.isMatch(contact);
	}

//	public void appendTo(StringBuilder buffer) {
//		buffer.append("(Not ");
//		condition.appendTo(buffer);
//		buffer.append(')');
//	}
}