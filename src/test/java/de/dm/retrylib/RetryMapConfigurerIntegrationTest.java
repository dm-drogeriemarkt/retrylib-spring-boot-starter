package de.dm.retrylib;

import net.openhft.chronicle.map.ChronicleMap;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RetryMapConfigurerIntegrationTest {

    private static final int ENTRY_POSITION = 4728;
    private static final int HEADER_POSITION = 534;

    private RetryMapConfigurer retryMapConfigurer;
    private RetrylibProperties retrylibProperties;
    private PersistenceProperties persistenceProperties;

    @Before
    public void setUp() {
        persistenceProperties = new PersistenceProperties();
        retrylibProperties = new RetrylibProperties();
        retryMapConfigurer = new RetryMapConfigurer(retrylibProperties);
    }

    @Test
    public void retryMapIsWritableAndReadable() throws IOException {
        String fileName = "mapForUnitTest.tmp";

        persistenceProperties.setAverageValueSize(201d);
        persistenceProperties.setMaxEntries(1L);
        persistenceProperties.setFileName(fileName);
        retrylibProperties.setPersistence(persistenceProperties);

        ChronicleMap<String, RetryEntity> retryMap = retryMapConfigurer.configureChronicleMap();

        File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        file.deleteOnExit();

        assertThat(retryMap.isOpen(), is(true));

        retryMap.put("Test", new RetryEntity("key", "retryType", "payload"));
        assertThat(retryMap.size(), is(1));

        RetryEntity retryEntity = retryMap.get("Test");
        assertThat(retryEntity.getKey(), is("key"));
        assertThat(retryEntity.getRetryType(), is("retryType"));
        assertThat(retryEntity.getPayload(), is("payload"));

        retryMap.remove("Test");
        assertThat(retryMap.size(), is(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void retryMapThrowsIllegalArgumentExceptionOnMisconfiguration() throws IOException {
        String fileName = "mapMisconfiguredForUnitTest.tmp";

        persistenceProperties.setAverageValueSize(1d);
        persistenceProperties.setMaxEntries(1L);
        persistenceProperties.setFileName(fileName);
        retrylibProperties.setPersistence(persistenceProperties);

        ChronicleMap<String, RetryEntity> retryMap = retryMapConfigurer.configureChronicleMap();

        assertThat(retryMap.isOpen(), is(true));

        File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        file.deleteOnExit();

        retryMap.put("Test", new RetryEntity("key", "retryType", "payload"));
    }

    @Test(expected = IllegalStateException.class)
    public void retryMapThrowsExceptionOnTooManyEntries() throws IOException {
        String fileName = "mapTooManyEntriesForUnitTest.tmp";

        persistenceProperties.setAverageValueSize(201d);
        persistenceProperties.setMaxEntries(1L);
        persistenceProperties.setFileName(fileName);
        retrylibProperties.setPersistence(persistenceProperties);

        ChronicleMap<String, RetryEntity> retryMap = retryMapConfigurer.configureChronicleMap();

        assertThat(retryMap.isOpen(), is(true));

        File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        file.deleteOnExit();

        retryMap.put("Test", new RetryEntity("key", "retryType", "payload"));
        retryMap.put("Test2", new RetryEntity("key", "retryType", "payload"));
        retryMap.put("Test3", new RetryEntity("key", "retryType", "payload"));
    }

    @Test
    public void retryMapIsRecoverableOnHeaderCorruption() throws IOException {
        String fileName = "mapCorruptHeaderForUnitTest.tmp";

        persistenceProperties.setAverageValueSize(201d);
        persistenceProperties.setMaxEntries(1L);
        persistenceProperties.setFileName(fileName);
        retrylibProperties.setPersistence(persistenceProperties);

        File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        file.deleteOnExit();

        ChronicleMap<String, RetryEntity> retryMap = retryMapConfigurer.configureChronicleMap();
        assertThat(retryMap.isOpen(), is(true));

        retryMap.put("Test", new RetryEntity("key", "retryType", "payload"));
        assertThat(retryMap.size(), is(1));

        retryMap.close();

        writeToPositionInFile(file, "XXXXXXXXXXXXX", HEADER_POSITION);

        ChronicleMap<String, RetryEntity> retryMapAfterCorruptionAndRecovery = retryMapConfigurer.configureChronicleMap();

        assertThat(retryMapAfterCorruptionAndRecovery.size(), is(1));

        RetryEntity retryEntity = retryMapAfterCorruptionAndRecovery.get("Test");
        assertThat(retryEntity.getKey(), is("key"));
        assertThat(retryEntity.getRetryType(), is("retryType"));
        assertThat(retryEntity.getPayload(), is("payload"));

        retryMapAfterCorruptionAndRecovery.remove("Test");

        retryMapAfterCorruptionAndRecovery.put("Test2", new RetryEntity("key", "retryType", "payload"));
        assertThat(retryMapAfterCorruptionAndRecovery.size(), is(1));
    }

    @Test
    public void retryMapIsRecoverableOnEntriesCorruption() throws IOException {
        String fileName = "mapCorruptEntryForUnitTest.tmp";

        persistenceProperties.setAverageValueSize(201d);
        persistenceProperties.setMaxEntries(1L);
        persistenceProperties.setFileName(fileName);
        retrylibProperties.setPersistence(persistenceProperties);

        File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        file.deleteOnExit();

        ChronicleMap<String, RetryEntity> retryMap = retryMapConfigurer.configureChronicleMap();
        assertThat(retryMap.isOpen(), is(true));

        retryMap.put("Test", new RetryEntity("key", "retryType", "payload"));
        assertThat(retryMap.size(), is(1));

        retryMap.close();

        writeToPositionInFile(file, "XXXXXXXXXXXXX", ENTRY_POSITION);

        ChronicleMap<String, RetryEntity> retryMapAfterCorruptionAndRecovery = retryMapConfigurer.configureChronicleMap();

        assertThat(retryMapAfterCorruptionAndRecovery.size(), is(0));

        retryMapAfterCorruptionAndRecovery.put("Test2", new RetryEntity("key", "retryType", "payload"));
        assertThat(retryMapAfterCorruptionAndRecovery.size(), is(1));

        RetryEntity retryEntity = retryMapAfterCorruptionAndRecovery.get("Test2");
        assertThat(retryEntity.getKey(), is("key"));
        assertThat(retryEntity.getRetryType(), is("retryType"));
        assertThat(retryEntity.getPayload(), is("payload"));
    }


    private void writeToPositionInFile(File filename, String data, long position) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filename, "rw")) {
            file.seek(position);
            file.writeBytes(data);
        }
    }
}