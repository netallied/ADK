/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml.internal.aml.persistence;

class AMLLocationInFileImpl implements AMLLocationInFile {
	private int lineNumber;
	private int columnNumber;

	AMLLocationInFileImpl() {
		this(-1,-1);
	}

	AMLLocationInFileImpl(int lineNumber, int columnNumber) {
		super();
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public int getColumnNumber() {
		return columnNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + columnNumber;
		result = prime * result + lineNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AMLLocationInFileImpl other = (AMLLocationInFileImpl) obj;
		if (columnNumber != other.columnNumber) {
			return false;
		}
		if (lineNumber != other.lineNumber) {
			return false;
		}
		return true;
	}

}