package com.serotonin.bacnet4j;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetTimeoutException;
import com.serotonin.bacnet4j.npdu.test.TestNetwork;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.RemoteDeviceFinder.RemoteDeviceFuture;

public class LocalDeviceTest {
    static final Logger LOG = LoggerFactory.getLogger(LocalDeviceTest.class);

    // The clock will control the expiration of devices from the cache, but not the real time delays
    // when doing discoveries.
    LocalDevice d1;
    LocalDevice d2;

    @Before
    public void before() throws Exception {
        d1 = new LocalDevice(1, new DefaultTransport(new TestNetwork(1, 100))).initialize();
        d2 = new LocalDevice(2, new DefaultTransport(new TestNetwork(2, 100))).initialize();
    }

    @After
    public void after() {
        // Shut down
        d1.terminate();
        d2.terminate();
    }

    @Test
    public void deviceCacheSuccess() throws InterruptedException, ExecutionException, BACnetException {
        assertNull(d1.getCachedRemoteDevice(2));

        // Ask for device 2 in two different threads.
        final MutableObject<RemoteDevice> rd21 = new MutableObject<>();
        final MutableObject<RemoteDevice> rd22 = new MutableObject<>();
        final Future<?> future1 = d1.submit(() -> {
            try {
                rd21.setValue(d1.getRemoteDevice(2).get());
            } catch (final BACnetException e) {
                // Shouldn't happen
                e.printStackTrace();
            }
        });
        final Future<?> future2 = d1.submit(() -> {
            try {
                rd22.setValue(d1.getRemoteDevice(2).get());
            } catch (final BACnetException e) {
                // Shouldn't happen
                e.printStackTrace();
            }
        });

        future1.get();
        future2.get();

        assertTrue(rd21.getValue() == rd22.getValue());
        assertNotNull(rd21.getValue().getDeviceProperty(PropertyIdentifier.protocolServicesSupported));
        assertNotNull(rd21.getValue().getDeviceProperty(PropertyIdentifier.objectName));
        assertNotNull(rd21.getValue().getDeviceProperty(PropertyIdentifier.protocolVersion));
        assertNotNull(rd21.getValue().getDeviceProperty(PropertyIdentifier.vendorIdentifier));
        assertNotNull(rd21.getValue().getDeviceProperty(PropertyIdentifier.modelName));

        // Ask for it again. Should be the same instance.
        final RemoteDevice rd23 = d1.getRemoteDevice(2).get();

        // Device is cached, so it will still be the same instance.
        Assert.assertTrue(rd21.getValue() == rd23);
    }

    @Test(expected = BACnetTimeoutException.class)
    public void deviceCacheFailure() throws BACnetException {
        d1.getRemoteDevice(4).get(200);
    }

    @Test(expected = CancellationException.class)
    public void cancelGetRemoteDevice() throws CancellationException, BACnetException {
        final RemoteDeviceFuture future = d1.getRemoteDevice(3);
        future.cancel();
        future.get();
    }

    @Test
    public void undefinedDeviceId() throws Exception {
        final LocalDevice ld = new LocalDevice(ObjectIdentifier.UNINITIALIZED,
                new DefaultTransport(new TestNetwork(3, 10)));
        ld.initialize();

        LOG.info("Local device initialized with device id {}", ld.getConfiguration().getInstanceId());
        assertNotEquals(ObjectIdentifier.UNINITIALIZED, ld.getConfiguration().getInstanceId());
    }
}