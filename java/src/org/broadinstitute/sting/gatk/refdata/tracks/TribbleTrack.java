/*
 * Copyright (c) 2010.  The Broad Institute
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.broadinstitute.sting.gatk.refdata.tracks;

import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.util.CloseableIterator;
import org.broad.tribble.FeatureReader;
import org.broadinstitute.sting.gatk.refdata.utils.FeatureToGATKFeatureIterator;
import org.broadinstitute.sting.gatk.refdata.utils.GATKFeature;
import org.broadinstitute.sting.utils.GenomeLoc;
import org.broadinstitute.sting.utils.StingException;

import java.io.File;
import java.io.IOException;


/**
 * 
 * @author aaron 
 * 
 * Class TribbleTrack
 *
 * A feature reader track, implementing the RMDTrack for tracks that are generated out of Tribble
 */
public class TribbleTrack extends RMDTrack implements QueryableTrack {
    // our feature reader - allows queries
    private FeatureReader reader;

    // our sequence dictionary, which can be null
    private final SAMSequenceDictionary dictionary;

    /**
     * Create a track
     *
     * @param type the type of track, used for track lookup
     * @param recordType the type of record we produce
     * @param name the name of this specific track
     * @param file the associated file, for reference or recreating the reader
     * @param reader the feature reader to use as the underlying data source
     * @param dict the sam sequence dictionary
     */
    public TribbleTrack(Class type, Class recordType, String name, File file, FeatureReader reader, SAMSequenceDictionary dict) {
        super(type, recordType, name, file);
        this.reader = reader;
        this.dictionary = dict;
    }

    /**
     * @return how to get an iterator of the underlying data.  This is all a track has to support,
     *         but other more advanced tracks support the query interface
     */
    @Override
    public CloseableIterator<GATKFeature> getIterator() {
        try {
            return new FeatureToGATKFeatureIterator(reader.iterator(),this.getName());
        } catch (IOException e) {
            throw new StingException("Unable to read from file",e);
        }
    }

    /**
     * do we support the query interface?
     *
     * @return true
     */
    @Override
    public boolean supportsQuery() {
        return true;
    }

    @Override
    public CloseableIterator<GATKFeature> query(GenomeLoc interval) throws IOException {
        return new FeatureToGATKFeatureIterator(reader.query(interval.getContig(),(int)interval.getStart(),(int)interval.getStop()),this.getName());
    }

    @Override
    public CloseableIterator<GATKFeature> query(GenomeLoc interval, boolean contained) throws IOException {
        return new FeatureToGATKFeatureIterator(reader.query(interval.getContig(),(int)interval.getStart(),(int)interval.getStop(), contained),this.getName());
    }

    @Override
    public CloseableIterator<GATKFeature> query(String contig, int start, int stop) throws IOException {
        return new FeatureToGATKFeatureIterator(reader.query(contig,start,stop),this.getName());
    }

    @Override
    public CloseableIterator<GATKFeature> query(String contig, int start, int stop, boolean contained) throws IOException {
        return new FeatureToGATKFeatureIterator(reader.query(contig,start,stop, contained),this.getName());
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new StingException("Unable to close reader " + reader.toString(),e);
        }
        reader = null;
    }

    public FeatureReader getReader() {
        return reader;
    }

    /**
     * get the sequence dictionary from the track, if available
     * @return a SAMSequenceDictionary if available, null if unavailable
     */
    public SAMSequenceDictionary getSequenceDictionary() {
        return dictionary;
    }

    /**
     * ask for the header, supplying the expected type.  Overridden in track types
     * @param clazz the class of the expected type
     * @param <HeaderType> the expected type
     * @return a object of type HeaderType
     * @throws ClassCastException if the class provided doesn't match our header type
     */
    public <HeaderType> HeaderType getHeader(Class<HeaderType> clazz) throws ClassCastException {
        return (HeaderType) (reader).getHeader(clazz);
    }
}