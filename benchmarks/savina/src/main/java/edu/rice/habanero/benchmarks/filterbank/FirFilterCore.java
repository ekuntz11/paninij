/*******************************************************************************
 * This file is part of the Panini project at Iowa State University.
 *
 * @PaniniJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * @PaniniJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with @PaniniJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more details and the latest version of this code please see
 * http://paninij.org
 *
 * Contributors:
 * 	Dr. Hridesh Rajan,
 * 	Dalton Mills,
 * 	David Johnston,
 * 	Trey Erenberger
 *******************************************************************************/
package edu.rice.habanero.benchmarks.filterbank;

import org.paninij.lang.Capsule;
import org.paninij.lang.Imported;

@Capsule class FirFilterCore implements ProcessorCore {

    @Imported Processor next;
    @Imported String sourceId;
    @Imported int peekLength;
    @Imported double[] coefficients;

    double[] data;
    int dataIndex = 0;
    boolean dataFull = false;

    void init() {
        this.data = new double[peekLength];
    }

    @Override
    public void process(double value) {

        data[dataIndex] = value;
        dataIndex++;

        if (dataIndex == peekLength) {
            dataFull = true;
            dataIndex = 0;
        }

        if (dataFull) {
            double sum = 0.0;
            for (int i = 0; i < peekLength; i++) {
                sum += (data[i] * coefficients[peekLength - i - 1]);
            }
            next.process(sum);
        }
    }
}
