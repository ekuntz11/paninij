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
package edu.rice.habanero.benchmarks.big;

import java.util.Random;

import org.paninij.lang.Capsule;
import org.paninij.lang.Imported;

@Capsule class NodeCore {
    @Imported
    int id;
    @Imported
    Node[] nodes = new Node[BigConfig.W];
    @Imported
    Sink sink;

    int numPings = 0;
    int numMessages = BigConfig.N;
    int expPinger = -1;
    Random random;

    void init() {
        random = new Random(this.id);
    }

    void done() {
        for (Node n : nodes) n.exit();
        sink.exit();
    }

    void ping(int sender) {
        nodes[sender].pong(this.id);
    }

    void pong(int sender) {
        if (sender != expPinger) {
            System.out.println("ERROR: Expected: " + expPinger + ", but recieved ping from " + sender);
        }

        if (numPings == numMessages) {
            sink.finished();
        } else {
            sendPing();
            numPings++;
        }
    }

    private void sendPing() {
        expPinger = random.nextInt(nodes.length);
        nodes[expPinger].ping(id);
    }
}
