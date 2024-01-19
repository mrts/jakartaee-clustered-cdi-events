package org.clustercdievents.cdievents;

public final class ClusteredEventChecker {

    public static boolean isClustered(Object event) {
        return event.getClass().isAnnotationPresent(Clustered.class);
    }

}
