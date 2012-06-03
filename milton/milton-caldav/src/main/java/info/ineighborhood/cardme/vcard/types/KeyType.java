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

package info.ineighborhood.cardme.vcard.types;

import info.ineighborhood.cardme.util.StringUtil;
import info.ineighborhood.cardme.util.Util;
import info.ineighborhood.cardme.vcard.EncodingType;
import info.ineighborhood.cardme.vcard.VCardType;
import info.ineighborhood.cardme.vcard.features.KeyFeature;
import info.ineighborhood.cardme.vcard.types.media.KeyTextType;
import info.ineighborhood.cardme.vcard.types.parameters.ParameterTypeStyle;
import java.util.Arrays;

/**
 * Copyright (c) 2004, Neighborhood Technologies
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of Neighborhood Technologies nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * 
 * @author George El-Haddad
 * <br/>
 * Feb 4, 2010
 *
 */
public class KeyType extends Type implements KeyFeature {

	private byte[] keyBytes = null;
	private KeyTextType keyTextType = null;
	private boolean isSetCompression = false;
	
	public KeyType() {
		super(EncodingType.BINARY, ParameterTypeStyle.PARAMETER_VALUE_LIST);
	}
	
	public KeyType(byte[] keyBytes, EncodingType encodingType, KeyTextType keyTextType) {
		super(encodingType, ParameterTypeStyle.PARAMETER_VALUE_LIST);
		setKey(keyBytes);
		setKeyTextType(keyTextType);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public byte[] getKey()
	{
		return keyBytes;
	}

	/**
	 * {@inheritDoc}
	 */
	public KeyTextType getKeyTextType()
	{
		return keyTextType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setKeyTextType(KeyTextType keyTextType) {
		this.keyTextType = keyTextType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setKey(byte[] keyBytes) {
		this.keyBytes = keyBytes;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasKeyTextType()
	{
		return keyTextType != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearKey() {
		keyBytes = null;
		keyTextType = null;
		encodingType = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasKey()
	{
		return keyBytes != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setCompression(boolean compression) {
		isSetCompression = compression;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isSetCompression()
	{
		return isSetCompression;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isInline()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTypeString()
	{
		return VCardType.KEY.getType();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null) {
			if(obj instanceof KeyType) {
				if(this == obj || ((KeyType)obj).hashCode() == this.hashCode()) {
					return true;
				}
				else {
					return false;
				}
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return Util.generateHashCode(toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName());
		sb.append("[ ");
		if(encodingType != null) {
			sb.append(encodingType.getType());
			sb.append(",");
		}
		
		if(keyTextType != null) {
			sb.append(keyTextType.getTypeName());
			sb.append(",");
		}
		
		if(keyBytes != null) {
			sb.append(StringUtil.toHexString(keyBytes));
			sb.append(",");
		}

		if(super.id != null) {
			sb.append(super.id);
			sb.append(",");
		}
		
		sb.deleteCharAt(sb.length()-1);	//Remove last comma.
		sb.append(" ]");
		return sb.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public KeyFeature clone()
	{
		KeyType cloned = new KeyType();
		
		if(keyBytes != null) {
			byte[] clonedBytes = Arrays.copyOf(keyBytes, keyBytes.length);
			cloned.setKey(clonedBytes);
		}
		
		if(keyTextType != null) {
			cloned.setKeyTextType(keyTextType);
		}
		
		cloned.setParameterTypeStyle(getParameterTypeStyle());
		cloned.setEncodingType(getEncodingType());
		cloned.setID(getID());
		return clone();
	}
}
