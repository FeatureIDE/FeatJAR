import { GEdge, GEdgeView, GNode, RenderingContext } from '@eclipse-glsp/client';
import { injectable } from 'inversify';
import { VNode } from 'snabbdom';
import { Point } from '@eclipse-glsp/client';

const CARDINALITY_RADIUS = 5;

/**
 * Draws edges so that they meet their target node at the point where
 * the mandatory / optional circle marker is located (in the middle of the target node).
 *
 * For rectangular anchor points, the endpoint is placed where the line intersects the node boundaries.
 * This creates a gap next to the marker circle and makes the endpoint appear detached in the case of
 * default edges. The endpoint is therefore moved to the top center of the target node,
 * and the starting point is moved to the outline of an OR or XOR semicircle.
 */

@injectable()
export class FeatureCardinalityEdgeView extends GEdgeView {
    protected override renderLine(edge: Readonly<GEdge>, segments: Point[], context: RenderingContext): VNode {
        return super.renderLine(edge, this.adjustSegments(edge, segments), context);
    }

    protected override renderAdditionals(edge: GEdge, segments: Point[], context: RenderingContext): VNode[] {
        return super.renderAdditionals(edge, this.adjustSegments(edge, segments), context);
    }

    protected adjustSegments(edge: Readonly<GEdge>, segments: Point[]): Point[] {
        return this.connectSourceToGroupBorder(edge, this.connectToCardinalityCircle(edge, segments));
    }

    /**
     * If the edge starts at an OR/XOR group node, move the first route point
     * onto the border of the semicircle.
     */
    protected connectSourceToGroupBorder(edge: Readonly<GEdge>, segments: Point[]): Point[] {
        if (segments.length < 2) {
            return segments;
        }

        const source = edge.source as GNode | undefined;
        if (!source || !source.bounds) {
            return segments;
        }

        const cssClasses = source.cssClasses ?? [];
        const isOrXor = cssClasses.includes('node-or') || cssClasses.includes('node-xor');
        if (!isOrXor) {
            return segments;
        }

        const b = source.bounds;
        const cx = b.x + b.width / 2; // ellipse center x (top-center)
        const cy = b.y; // ellipse center y (flat top edge)
        const rx = b.width / 2;
        const ry = b.height;

        const routed = segments.map(p => ({ ...p }));
        const next = routed[1];

        const dx = next.x - cx;
        const dy = next.y - cy;
        const denom = Math.sqrt((dx * dx) / (rx * rx) + (dy * dy) / (ry * ry));
        if (denom === 0) {
            return routed;
        }

        // Scale the direction vector so it ends exactly on the ellipse border
        const t = 1 / denom;
        routed[0] = { x: cx + dx * t, y: cy + dy * t };

        return routed;
    }

    protected connectToCardinalityCircle(edge: Readonly<GEdge>, segments: Point[]): Point[] {
        if (segments.length < 2) {
            return segments;
        }

        const target = edge.target as GNode | undefined;
        if (!target || !target.bounds) {
            return segments;
        }

        const cssClasses = target.cssClasses ?? [];

        // Do not modify edges that end at a group node (semicircle / invisible point)
        const isGroupTarget = cssClasses.some(c => c.startsWith('node-'));
        if (isGroupTarget) {
            return segments;
        }

        // A circle is only rendered when the edge type matches
        const hasCardinalityCircle = edge.type === 'edge-mandatory' || edge.type === 'edge-optional';

        const bounds = target.bounds;

        // The top center of the target node
        const topCenter = {
            x: bounds.x + bounds.width / 2,
            y: bounds.y
        };

        const routedSegments = segments.map(p => ({ ...p }));

        if (hasCardinalityCircle) {
            // End the edge on the border of the circle
            const previous = routedSegments[routedSegments.length - 2];
            const dx = previous.x - topCenter.x;
            const dy = previous.y - topCenter.y;
            const length = Math.sqrt(dx * dx + dy * dy) || 1;

            routedSegments[routedSegments.length - 1] = {
                x: topCenter.x + (dx / length) * CARDINALITY_RADIUS,
                y: topCenter.y + (dy / length) * CARDINALITY_RADIUS
            };
        } else {
            // If there is no cycle, extend the edge (with XOR and OR groups)
            routedSegments[routedSegments.length - 1] = {
                x: topCenter.x,
                y: topCenter.y // + 0.1
            };
        }

        return routedSegments;
    }
}
