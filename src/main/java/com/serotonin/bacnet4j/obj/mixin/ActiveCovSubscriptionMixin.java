package com.serotonin.bacnet4j.obj.mixin;

import java.util.List;
import java.util.Map;

import com.serotonin.bacnet4j.obj.AbstractMixin;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.constructed.CovSubscription;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.Recipient;
import com.serotonin.bacnet4j.type.constructed.RecipientProcess;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class ActiveCovSubscriptionMixin extends AbstractMixin {
    public ActiveCovSubscriptionMixin(final BACnetObject bo) {
        super(bo);
    }

    @Override
    protected void beforeReadProperty(final PropertyIdentifier pid) {
        if (pid.equals(PropertyIdentifier.activeCovSubscriptions)) {
            // Update the time remaining amounts by rewriting the subscriptions list for each request.
            final long now = getLocalDevice().getClock().millis();

            final Map<ObjectIdentifier, List<CovContext>> ctxs = getLocalDevice().getCovContexts();
            final SequenceOf<CovSubscription> subscriptions = new SequenceOf<>();
            for (final Map.Entry<ObjectIdentifier, List<CovContext>> e : ctxs.entrySet()) {
                synchronized (e.getValue()) {
                    for (final CovContext ctx : e.getValue()) {
                        if (!ctx.hasExpired(now)) {
                            final RecipientProcess rp = new RecipientProcess(new Recipient(ctx.getAddress()),
                                    ctx.getSubscriberProcessIdentifier());
                            final ObjectPropertyReference opr = new ObjectPropertyReference(e.getKey(),
                                    ctx.getExposedMonitoredProperty(), null);
                            final CovSubscription cs = new CovSubscription(rp, opr,
                                    new Boolean(ctx.isIssueConfirmedNotifications()),
                                    new UnsignedInteger(ctx.getSecondsRemaining(now)), ctx.getCovIncrement());
                            subscriptions.add(cs);
                        }
                    }
                }
            }

            writePropertyInternal(PropertyIdentifier.activeCovSubscriptions, subscriptions);
        }
    }
}
