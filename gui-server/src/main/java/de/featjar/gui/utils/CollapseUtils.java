// Generated with Claude Opus 5.5 (Anthropic, via claude.ai)
package de.featjar.gui.utils;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.glsp.server.model.GModelState;

/**
 * Stores which features are currently collapsed in the diagram.
 * Collapsing is only a view setting: the IDs are kept in the {@link GModelState}
 * of the current session and are neither part of the feature model nor saved to the file.
 */
public class CollapseUtils {

    public static final String COLLAPSED_FEATURES = "collapsedFeatures";
    public static final String CSS_COLLAPSIBLE = "collapsible";
    public static final String CSS_COLLAPSED = "collapsed";
    public static final String ARG_COLLAPSED_COUNT = "collapsedCount";

    private CollapseUtils() {}

    @SuppressWarnings("unchecked")
    public static Set<String> getCollapsedFeatures(final GModelState modelState) {
        Set<String> collapsed =
                modelState.getProperty(COLLAPSED_FEATURES, Set.class).orElse(null);
        if (collapsed == null) {
            collapsed = new HashSet<>();
            modelState.setProperty(COLLAPSED_FEATURES, collapsed);
        }
        return collapsed;
    }

    public static boolean isCollapsed(final GModelState modelState, final String featureId) {
        return getCollapsedFeatures(modelState).contains(featureId);
    }

    public static void toggle(final GModelState modelState, final String featureId) {
        Set<String> collapsed = getCollapsedFeatures(modelState);
        if (!collapsed.remove(featureId)) {
            collapsed.add(featureId);
        }
    }

    public static void expand(final GModelState modelState, final String featureId) {
        getCollapsedFeatures(modelState).remove(featureId);
    }
}
