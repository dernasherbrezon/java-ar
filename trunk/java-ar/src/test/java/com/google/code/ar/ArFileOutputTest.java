package com.google.code.ar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import junit.framework.TestCase;

public class ArFileOutputTest extends TestCase {

	private final static String FILENAME = "file.deb";

	public void testWriteRead() throws IOException {
		ArFileOutput fileOutput = new ArFileOutput(FILENAME);
		ArEntry[] entries = createValidEntries();
		for (int i = 0; i < entries.length; i++) {
			ArEntry curEntry = entries[i];
			fileOutput.putNextEntry(curEntry);
			fileOutput.write("this is some string. maybe long long long".getBytes(Charset.forName("ASCII")));
			fileOutput.closeEntry();
		}
		fileOutput.close();
		
		ArInputStream aris = new ArInputStream(new FileInputStream(FILENAME));
		ArEntry curEntry = null;
		int index = 0;
		while( (curEntry = aris.getNextEntry()) != null ) {
			assertEquals(entries[index].getFilename(), curEntry.getFilename());
			index++;
		}
		aris.close();
	}

	private static ArEntry[] createValidEntries() {
		ArEntry[] result = new ArEntry[2];
		result[0] = new ArEntry();
		result[0].setFileMode(777);
		result[0].setFileModificationTimestamp(System.currentTimeMillis());
		result[0].setGroupId(1);
		result[0].setOwnerId(1);
		result[0].setFilename("control.tar.gz");
		result[1] = new ArEntry();
		result[1].setFileMode(100);
		result[1].setFileModificationTimestamp(System.currentTimeMillis());
		result[1].setFilename("data.tar.gz");
		result[1].setGroupId(1);
		result[1].setOwnerId(1);
		return result;
	}

	protected void tearDown() throws Exception {
		File f = new File(FILENAME);
		if (f.exists()) {
			if (!f.delete()) {
				throw new Exception("unable to delete file: " + f.getAbsolutePath());
			}
		}
	}

}
