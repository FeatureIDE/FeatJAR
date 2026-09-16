import { Action } from '@eclipse-glsp/client';

/**
 * Actions for changing types and cardinalities from the context menu.
 *
 * All of them carry the values as plain strings and numbers. The server then maps the json to its fields.
 * The fields and KIND constant must have the same name in the server for this to work.
 */

/**
 * Sets a feature to abstract, concrete or none.
 */
export interface SetFeatureImplementationTypeAction extends Action {
    kind: typeof SetFeatureImplementationTypeAction.KIND;
    elementId: string;
    featureImplementationType: string;
}

export namespace SetFeatureImplementationTypeAction {
    export const KIND = 'setFeatureImplementationType';
    export function create(elementId: string, featureImplementationType: string): SetFeatureImplementationTypeAction {
        return { kind: KIND, elementId, featureImplementationType };
    }
}

/**
 * Changes the type of a group by altering the bounds
 * whereupon their symbol gets immediately adjusted.
 */
export interface SetNodeTypeAction extends Action {
    kind: typeof SetNodeTypeAction.KIND;
    elementId: string;
    nodeType: string;
}

export namespace SetNodeTypeAction {
    export const KIND = 'setGroupNodeType';
    export function create(elementId: string, nodeType: string): SetNodeTypeAction {
        return { kind: KIND, elementId, nodeType };
    }
}

/**
 * Sets a custom cardinality on a feature, which are shown as a label on its edge.
 */
export interface SetCardinalityGroupNodeBoundsAction extends Action {
    kind: typeof SetCardinalityGroupNodeBoundsAction.KIND;
    elementId: string;
    lowerBound: number;
    upperBound: number;
}
export namespace SetCardinalityGroupNodeBoundsAction {
    export const KIND = 'setCardinalityGroupNodeBounds';
    export function create(elementId: string, lowerBound: number, upperBound: number): SetCardinalityGroupNodeBoundsAction {
        return { kind: KIND, elementId, lowerBound, upperBound };
    }
}

/**
 * Sets a custom cardinality on a group, which are shown inside its diamond.
 */
export interface SetCardinalityFeatureBoundsAction extends Action {
    kind: typeof SetCardinalityFeatureBoundsAction.KIND;
    elementId: string;
    lowerBound: number;
    upperBound: number;
}

export namespace SetCardinalityFeatureBoundsAction {
    export const KIND = 'setCardinalityFeatureBounds';
    export function create(elementId: string, lowerBound: number, upperBound: number): SetCardinalityFeatureBoundsAction {
        return { kind: KIND, elementId, lowerBound, upperBound };
    }
}
