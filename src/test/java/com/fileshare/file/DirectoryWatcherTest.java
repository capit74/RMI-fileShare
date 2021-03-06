package com.fileshare.file;

import com.fileshare.file.util.FileInfo;
import com.fileshare.time.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * @author Kamil Sikora
 *         Data: 13.06.13
 */
public class DirectoryWatcherTest {
    DirectoryWatcher directoryWatcher;
    EventObserver eventObserver;
    File testFile;
    Clock clock;
    private File testDirectory;

    @Before
    public void createDirectoryWatcher() {
        clock = new Clock("aa");
        testDirectory = new File("./test");

        if (testDirectory.exists()) {
            File[] files = testDirectory.listFiles();
            for (File file : files) {
                file.delete();
            }
            testDirectory.delete();
        }
        assertTrue(testDirectory.mkdir());

        directoryWatcher = new DirectoryWatcher("./test", 3, clock);

        eventObserver = new EventObserver();
        directoryWatcher.addObserver(eventObserver);
        directoryWatcher.watchDir();
        //  new Thread(directoryWatcher).start();
    }

    @Test
    public void doesItEvenWorkTest() {
        testFile = DummyFile.generateFile(5096);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (eventObserver.getCaughtEvent() == 0)
            testFile.delete();
        assertTrue(eventObserver.getCaughtEvent() > 0);
    }

    @Test
    public void observerTest() {
        while (eventObserver.getCaughtEvent() == 0) {
            testFile = DummyFile.generateFile(1024);
        }
        assertTrue(eventObserver.getCaughtEvent() == FileInfo.FLAG_CREATED);
    }

    @Test
    public void ignoreTest() {
        Set<String> ignoreList = new HashSet<>();
        ignoreList.add("1MB");
        directoryWatcher.addFilesToIgnore(ignoreList);
        testFile = DummyFile.generateFile(1024);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(directoryWatcher.getFilesToIgnore().size() == 0);
    }

    @After
    public void clean() {
        testFile.delete();
        testDirectory.delete();
    }

    private class EventObserver implements Observer {
        private int caughtEvent;

        public int getCaughtEvent() {
            return caughtEvent;
        }

        @Override
        public void update(Observable o, Object arg) {
            HashMap<String, FileInfo> changes = (HashMap<String, FileInfo>) arg;
            System.out.println("UPDATE + " + changes.size());
            for (FileInfo fileInfo : changes.values()) {
                caughtEvent = fileInfo.getFlag();
            }
        }
    }
}
