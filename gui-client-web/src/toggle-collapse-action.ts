import { Action } from '@eclipse-glsp/client';

//Collapses or expands the subtree of a feature.
//If no elementId is given, the server uses the currently selected feature.

export interface ToggleCollapseAction extends Action {
    kind: typeof ToggleCollapseAction.KIND;
    elementId?: string;
}

export namespace ToggleCollapseAction {
    export const KIND = 'toggleCollapse';

    export function create(elementId?: string): ToggleCollapseAction {
        return { kind: KIND, elementId };
    }
}
