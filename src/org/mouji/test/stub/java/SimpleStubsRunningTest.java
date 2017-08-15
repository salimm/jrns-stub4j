package org.mouji.test.stub.java;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mouji.common.errors.ApplicationSpecificErrorException;
import org.mouji.common.errors.ExecuteInternalError;
import org.mouji.common.errors.InvalidArgsException;
import org.mouji.common.errors.ServiceNotSupportedException;
import org.mouji.common.info.SerializationFormat;
import org.mouji.common.info.SerializedObject;
import org.mouji.common.info.ServiceInfo;
import org.mouji.common.info.ServiceProviderInfo;
import org.mouji.common.info.ServiceRequest;
import org.mouji.common.info.ServiceSupportInfo;
import org.mouji.common.info.StubEnvInfo;
import org.mouji.common.info.responses.ServiceResponse;
import org.mouji.common.serializer.Serializer;
import org.mouji.common.services.ServiceProvider;
import org.mouji.stub.java.JsonSerializer;
import org.mouji.stub.java.stubs.ClientStub;
import org.mouji.stub.java.stubs.ServerStub;

public class SimpleStubsRunningTest {
	/**
	 * pointer to a server stub
	 */
	private static ServerStub serverStub;

	/**
	 * 
	 */
	private static ServiceProviderInfo spInfo;

	private static final int serverPort = 4040;

	private static ClientStub clientStub;

	private static ServiceInfo<Integer> service;

	@BeforeClass
	public static void initStubs() throws Exception {
		// setting service provider info
		spInfo = new ServiceProviderInfo("127.0.0.1", serverPort, StubEnvInfo.currentEnvInfo());

		// setting service
		service = new ServiceInfo<Integer>("add", 1);

		List<Serializer> list = new ArrayList<Serializer>();
		list.add(new JsonSerializer());

		serverStub = new ServerStub(list, serverPort);

		ServiceProvider serviceProvider = new ServiceProvider() {

			@Override
			public ServiceSupportInfo service(ServiceInfo<?> info) throws ServiceNotSupportedException {
				return new ServiceSupportInfo(info, new SerializationFormat[] { SerializationFormat.defaultFotmat() });
			}

			@Override
			public boolean ping() {
				return true;
			}

			@Override
			public ServiceProviderInfo info() {
				return spInfo;
			}

			@Override
			public ServiceResponse<?> execute(ServiceRequest request) throws ApplicationSpecificErrorException,
					ExecuteInternalError, InvalidArgsException, ServiceNotSupportedException {
				if (request.getService().getId() == 1) {
					if (!(request.getArgs()[0].getContent() instanceof Integer)
							|| !(request.getArgs()[1].getContent() instanceof Integer)) {
						throw new InvalidArgsException("Both must be integers!!");
					}
					Integer num1 = (Integer) request.getArgs()[0].getContent();
					Integer num2 = (Integer) request.getArgs()[1].getContent();
					return new ServiceResponse<>(request.getService(), new SerializedObject<>(num1 + num2),
							request.getRequestId());
				} else {
					throw new ServiceNotSupportedException(request.getService());
				}
			}
		};
		ServiceProvider provider = serviceProvider;
		serverStub.init(provider);

		serverStub.start();

		clientStub = new ClientStub(list);

	}

	@Test
	public void testPing() throws Exception {
		boolean ping = clientStub.ping(spInfo);
		assertTrue(ping);
	}

	@Test
	public void testInfo() throws Exception {
		ServiceProviderInfo provider = clientStub.getInfo(spInfo);
		assertEquals(provider, spInfo);
	}

	@Test
	public void testService() throws Exception {
		ServiceSupportInfo support = clientStub.getServiceSupport(spInfo, service);

		System.out.println(support.getSerializers()[0]);
		System.out.println(SerializationFormat.defaultFotmat());
		System.out.println("--");
		// checking service information
		assertEquals(service, support.getService());
		// // checking number of formats
		assertEquals(support.getSerializers().length, 1);
		// // checking format information
		assertEquals(support.getSerializers()[0], SerializationFormat.defaultFotmat());
	}

	@Test
	public void testExecute() throws Exception {
		ServiceResponse<Integer> response = clientStub.call(service, spInfo, new Integer[] { 4, 5 },
				new JsonSerializer());

		// checking service information
		assertEquals(service, response.getService());
		//
		// checking class of result
		assertEquals(Integer.class, response.getContent().getClass());

		// value of results to be equal
		assertEquals(new Integer(9), response.getContent());
	}

	@AfterClass
	public static void closeAfterTests() {
		try {
			serverStub.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
