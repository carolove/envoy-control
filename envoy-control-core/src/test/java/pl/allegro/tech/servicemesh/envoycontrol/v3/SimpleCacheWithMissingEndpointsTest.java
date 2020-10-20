package pl.allegro.tech.servicemesh.envoycontrol.v3;

import com.google.common.collect.ImmutableList;
import io.envoyproxy.controlplane.cache.Resources;
import io.envoyproxy.controlplane.cache.Watch;
import io.envoyproxy.controlplane.cache.XdsRequest;
import io.envoyproxy.controlplane.cache.v3.Snapshot;
import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.config.core.v3.Node;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static java.util.Collections.emptyList;

public class SimpleCacheWithMissingEndpointsTest extends pl.allegro.tech.servicemesh.envoycontrol.v3.SimpleCacheTest {

    @Override
    protected boolean shouldSendMissingEndpoints() {
        return true;
    }

    protected static final Snapshot SNAPSHOT_WITH_MISSING_RESOURCES = Snapshot.create(
            emptyList(),
            ImmutableList.of(
                    ClusterLoadAssignment.newBuilder().setClusterName("none").build(),
                    ClusterLoadAssignment.newBuilder().setClusterName(CLUSTER_NAME).build()
            ),
            emptyList(),
            emptyList(),
            emptyList(),
            VERSION2
    );

    @Ignore
    @Override
    public void invalidNamesListShouldReturnWatcherWithNoResponseInAdsMode() {
    }

    @Test
    public void missingNamesListShouldReturnWatcherWithResponseInAdsMode() {
        pl.allegro.tech.servicemesh.envoycontrol.v3.SimpleCache<String> cache = new pl.allegro.tech.servicemesh.envoycontrol.v3.SimpleCache<>(new SingleNodeGroup(), shouldSendMissingEndpoints());

        cache.setSnapshot(SingleNodeGroup.GROUP, MULTIPLE_RESOURCES_SNAPSHOT2);

        ResponseTracker responseTracker = new ResponseTracker();

        Watch watch = cache.createWatch(
                true,
                XdsRequest.create(DiscoveryRequest.newBuilder()
                        .setNode(Node.getDefaultInstance())
                        .setTypeUrl(Resources.V3.ENDPOINT_TYPE_URL)
                        .addResourceNames("none")
                        .addResourceNames(CLUSTER_NAME)
                        .build()),
                Collections.emptySet(),
                responseTracker,
                false);

        assertThatWatchReceivesSnapshot(new WatchAndTracker(watch, responseTracker), SNAPSHOT_WITH_MISSING_RESOURCES);
    }
}