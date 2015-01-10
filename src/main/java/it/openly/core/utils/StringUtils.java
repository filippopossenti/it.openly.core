package it.openly.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class StringUtils implements IStringUtils {
	@Override
	public String removeEmptyLines(String instr) {
		BufferedReader rdr = new BufferedReader(new StringReader(instr));
		try {
			StringWriter wtr = new StringWriter();
			String line = null;
			while ((line = rdr.readLine()) != null) {
				String l = line.trim();
				if (!"".equals(l))
					wtr.write(line + "\r\n");
			}
			return wtr.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
