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
 * Reads a table from an MTX file, where the data
 *
 * @author brycecr
 */
public class MtxTableReader extends AbstractTextTableReader {

	protected final static String DELIM = " ";

	@Override
	protected void read(InputStream is, TableReadListener trl) 
		throws IOException, DataParseException {
		String line;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while ( (line=br.readLine()) != null ) {

			if (line.charAt(0) == '%') {
				continue;
			}
			
			// split on DELIM character
			String[] cols = line.split(DELIM);

			//take line to be SOURCENODE TARGETNODE VALUEFOREDGE
			//note that we shift down by one b/c mtx should
			//be 1-indexed
			trl.readValue(Integer.parseInt(cols[0])-1, 
				Integer.parseInt(cols[1])-1, cols[2]);
		}
	}
}
