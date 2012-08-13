/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.folding.msmexplorer.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import prefuse.data.Table;
import prefuse.data.io.AbstractTextTableReader;
import prefuse.data.io.TableReadListener;
import prefuse.data.parser.DataParseException;

/**
 *
 * @author gestalt
 */
public class MtxTableReader extends AbstractTextTableReader {

	protected Table m_table;
	protected final static String DELIM = " ";

	private void init (Class<?> type, int size) {
		m_table = new Table(size, size);
		setHasHeader(false);
	}

	@Override
	protected void read(InputStream is, TableReadListener trl) 
		throws IOException, DataParseException {
		String line;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while ( (line=br.readLine()) != null ) {
			
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
