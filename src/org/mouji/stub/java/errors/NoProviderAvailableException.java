package org.mouji.stub.java.errors;

import org.mouji.common.info.ServiceInfo;

public class NoProviderAvailableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoProviderAvailableException(ServiceInfo<?> service) {
		super("Tried to fetch a provider info from NameServer but NameServer did not return any for service: "
				+ service);
	}

}
