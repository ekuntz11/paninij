package org.paninij.proc.check.capsule.events;

import org.paninij.lang.Capsule;
import org.paninij.lang.PaniniEvent;
import org.paninij.lang.Local;
import org.paninij.lang.Broadcast;

@Capsule
class EventWithLocalCore
{
    @Broadcast @Local PaniniEvent<String> event;
}
