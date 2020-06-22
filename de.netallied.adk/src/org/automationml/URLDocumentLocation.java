/*******************************************************************************
 * Copyright (c) 2019 NetAllied Systems GmbH, Ravensburg. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details. 
 *******************************************************************************/
package org.automationml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

public class URLDocumentLocation implements DocumentLocation {
	final URL url;

	public URLDocumentLocation(URL url) {
		super();
		this.url = url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		URLDocumentLocation other = (URLDocumentLocation) obj;
		if (url == null) {
			if (other.url != null) {
				return false;
			}
		} else if (!url.equals(other.url)) {
			return false;
		}
		return true;
	}

	public URL getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return url == null ? "" : url.toString();
	}

	@Override
	public OutputStream createOutputStream() throws Exception {
		return new FileOutputStream(new File(url.toURI()));
	}

	@Override
	public boolean isPersistent() {
		return getUrl().getProtocol().indexOf( "file" ) != -1 ;
	}

}