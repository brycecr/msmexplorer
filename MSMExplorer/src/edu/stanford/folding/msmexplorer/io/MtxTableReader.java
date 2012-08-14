/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.folding.msmexplorer.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import prefuse.data.io.AbstractTextTableReader;
import prefuse.data.io.TableReadListener;
import prefuse.data.parser.DataParseException;

/**
 * Reads a table from an MTX file, where the data represents
 * a new edge field
 *
 * @author brycecr
 */
public class MtxTableReader extends AbstractTextTableReader {

	protected final static String DELIM = " ";

	public MtxTableReader() {
		this.setHasHeader(false);
	}
	
	@Override
	protected void read(InputStream is, TableReadListener trl) 
		throws IOException, DataParseException {
		String line;
		
		boolean seenfirst = false;
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while ( (line=br.readLine()) != null ) {

			if (line.charAt(0) == '%') {
				continue;

			//skip first noncoment line b/c its just sum data
			} else if (!seenfirst) {
				seenfirst = true;
				continue;
			}
			
			// split on DELIM character
			String[] cols = line.split(DELIM);

			//take line to be SOURCENODE TARGETNODE VALUEFOREDGE
			//note that we shift down by one b/c mtx should
			//be 1-indexed
			trl.readValue(Integer.parseInt(cols[0]), 
				Integer.parseInt(cols[1]), cols[2]);
		}
	}
}
