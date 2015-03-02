/**
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.unsafe.impl.batchimport;

import java.io.IOException;

import org.neo4j.kernel.impl.store.record.PrimitiveRecord;
import org.neo4j.unsafe.impl.batchimport.input.InputEntity;
import org.neo4j.unsafe.impl.batchimport.input.Receiver;
import org.neo4j.unsafe.impl.batchimport.staging.ExecutorServiceStep;
import org.neo4j.unsafe.impl.batchimport.staging.StageControl;

/**
 * Caches the incoming {@link InputEntity} to disk, for later use.
 */
public class InputEntityCacherStep<INPUT extends InputEntity>
        extends ExecutorServiceStep<Batch<INPUT,? extends PrimitiveRecord>>
{
    private final Receiver<INPUT[],IOException> cacher;

    public InputEntityCacherStep( StageControl control, int workAheadSize, int movingAverageSize,
            Receiver<INPUT[],IOException> cacher )
    {
        super( control, "CACHE", workAheadSize, movingAverageSize, 1 );
        this.cacher = cacher;
    }

    @Override
    protected Object process( long ticket, Batch<INPUT,? extends PrimitiveRecord> batch ) throws IOException
    {
        cacher.receive( batch.input );
        return batch;
    }

    @Override
    protected void done()
    {
        try
        {
            cacher.close();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Couldn't close input cacher", e );
        }
    }
}
