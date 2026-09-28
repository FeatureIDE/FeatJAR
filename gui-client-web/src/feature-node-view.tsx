/** @jsx svg */
import { GNode, RectangularNodeView, RenderingContext, svg } from '@eclipse-glsp/client';
import { injectable } from 'inversify';
import { VNode } from 'snabbdom';

interface GNodeWithArgs extends GNode {
    args?: Record<string, string | number | boolean>;
}
/**
 * Renders every node of the feature model.
 *
 * All nodes can be distinguished through their CSS class.
 * Features get a rectangle and group nodes are treated special:
 * OR and XOR groups get a semicircle, AND and cardinality groups get a diamond shape.
 * Sometimes a check has to use the full name, otherwise there would be name collisions.
 */
@injectable()
export class FeatureNodeView extends RectangularNodeView {
    override render(node: Readonly<GNode>, context: RenderingContext): VNode {
        const width = Math.max(node.bounds.width, 1);
        const height = Math.max(node.bounds.height, 1);

        const selected = node.selected;
        const strokeColor = selected ? 'blue' : 'black';
        const strokeWidth = selected ? 2 : 1;

        const isOr = node.cssClasses?.includes('node-or') || false;
        const isXor = node.cssClasses?.includes('node-xor') || false;
        const isAnd = node.cssClasses?.includes('node-and') || false;
        const isCardinality = node.cssClasses?.includes('node-cardinality') || false;

        const isAbstract = node.cssClasses?.includes('abstract') || false;
        const isConcrete = node.cssClasses?.includes('concrete') || false;

        const isMandatory = node.cssClasses?.includes('feature-mandatory') || false;
        const isOptional = node.cssClasses?.includes('feature-optional') || false;
        // const isMultiple = node.cssClasses?.includes('feature-multiple') || false;

        const isConstraint = node.cssClasses?.includes('constraint-node') || false;
        const customColor =
            typeof (node as GNodeWithArgs).args?.color === 'string' ? ((node as GNodeWithArgs).args!.color as string) : undefined;
        const showMandatoryMarker = isMandatory && this.hasIncomingEdgeOfType(node, 'edge-mandatory');
        const showOptionalMarker = isOptional && this.hasIncomingEdgeOfType(node, 'edge-optional');

        const isConstraintBox = node.cssClasses?.includes('constraint-box') || false;
        const isConstraintTitle = node.cssClasses?.includes('constraint-title') || false;

        const isCollapsed = node.cssClasses?.includes('collapsed') || false;
        const collapsedCount = Number((node as GNodeWithArgs).args?.collapsedCount ?? 0);

        /*
         * Constraint box only a container
         */
        if (isConstraintBox) {
            return (
                <g>
                    <rect x={0} y={0} width={width} height={height} fill='none' stroke='black' stroke-width={1} />
                    {context.renderChildren(node)}
                </g>
            );
        }

        /*
         * Constraint box title with plain text, no shape
         */
        if (isConstraint) {
            return (
                <g>
                    <rect
                        x={0}
                        y={0}
                        width={width}
                        height={height}
                        style={{
                            fill: 'transparent',
                            stroke: selected ? 'blue' : 'none',
                            strokeWidth: String(strokeWidth)
                        }}
                        pointerEvents='all'
                    />
                    <line x1={0} y1={height} x2={width} y2={height} stroke='#666' stroke-width={1} />
                    {context.renderChildren(node)}
                </g>
            );
        }

        /*
         * AND / Cardinality groups.
         */
        if (isAnd || isCardinality) {
            const cx = width / 2;
            const cy = height / 2;
            const diamond = `M ${cx} 0 L ${width} ${cy} L ${cx} ${height} L 0 ${cy} Z`;

            /*
             * Returns a diamond node for the AND / Cardinality groups
             */
            return (
                <g>
                    <path d={diamond} fill={isAnd ? 'black' : 'white'} stroke={strokeColor} stroke-width={strokeWidth} />
                    {context.renderChildren(node)}
                </g>
            );
        }

        /*
         * Returns for OR / XOR groups a fancy semicircle.
         * OR = completely filled, XOR = border only.
         */
        if (isOr || isXor) {
            return (
                <g>
                    {this.renderSemicircle(width, height, strokeColor, strokeWidth, isOr)}
                    {context.renderChildren(node)}
                </g>
            );
        }

        /*
         * Normal feature node.
         */
        return (
            <g>
                <rect
                    x={0}
                    y={0}
                    width={width}
                    height={height}
                    style={{
                        fill: customColor ?? (isAbstract ? '#d3d3d3' : isConcrete ? '#add8e6' : 'white'),
                        stroke: isConstraintTitle ? 'black' : strokeColor,
                        strokeWidth: isConstraintTitle ? String(1) : String(strokeWidth)
                    }}
                />

                {showMandatoryMarker && <circle cx={width / 2} cy={0} r={5} fill='black' stroke='black' stroke-width={1} />}

                {showOptionalMarker && <circle cx={width / 2} cy={0} r={5} fill='white' stroke='black' stroke-width={1.5} />}

                {context.renderChildren(node)}
                {isCollapsed && this.renderCollapsedBadge(width, height, collapsedCount)}
            </g>
        );
    }
    protected renderCollapsedBadge(width: number, height: number, count: number): VNode {
        const text = `+${count}`;
        const badgeWidth = 12 + text.length * 7;
        const badgeHeight = 16;
        const x = (width - badgeWidth) / 2;
        const y = height + 6;

        return (
            <g>
                <line x1={width / 2} y1={height} x2={width / 2} y2={y} stroke='black' stroke-width={1} stroke-dasharray='2,2' />
                <rect x={x} y={y} width={badgeWidth} height={badgeHeight} rx={8} ry={8} fill='#555' />
                <text x={width / 2} y={y + badgeHeight / 2} text-anchor='middle' dominant-baseline='central' fill='white' font-size='11px'>
                    {text}
                </text>
            </g>
        );
    }

    protected hasIncomingEdgeOfType(node: Readonly<GNode>, edgeType: string): boolean {
        return this.getIncomingEdges(node).some(edge => edge?.type === edgeType);
    }

    protected getIncomingEdges(node: Readonly<GNode>): ReadonlyArray<any> {
        const directIncomingEdges = (node as any).incomingEdges;

        if (Array.isArray(directIncomingEdges)) {
            return directIncomingEdges;
        }

        const rootReference = (node as any).root;
        const root = typeof rootReference === 'function' ? rootReference() : rootReference;

        if (!root) {
            return [];
        }

        const result: any[] = [];

        const visit = (element: any): void => {
            if (!element) {
                return;
            }

            if (this.isEdgeTargetingNode(element, node.id)) {
                result.push(element);
            }

            const children = element.children;
            if (Array.isArray(children)) {
                children.forEach(child => visit(child));
            }
        };

        visit(root);

        return result;
    }

    /**
     * Checks if the element is an edge, connected, and points to the node.
     */
    protected isEdgeTargetingNode(element: any, nodeId: string): boolean {
        const type = element.type;

        if (typeof type !== 'string' || !type.startsWith('edge')) {
            return false;
        }

        const targetId = element.targetId ?? element.target?.id;

        return targetId === nodeId;
    }

    /**
     * Renders the semi-circle that fills the entire node area. It is the basis for OR and XOR nodes.
     */
    protected renderSemicircle(width: number, height: number, strokeColor: string, strokeWidth: number, filled: boolean): VNode {
        const d = [`M 0 0`, `L ${width} 0`, `A ${width / 2} ${height} 0 0 1 0 0`, `Z`].join(' ');

        return <path d={d} fill={filled ? strokeColor : 'white'} stroke={strokeColor} stroke-width={strokeWidth} />;
    }
}
