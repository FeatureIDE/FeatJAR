import { Action } from '@eclipse-glsp/client';

/**
 * Initiates the saving of the feature model.
 *
 * The action is forwarded to the server, which writes the corresponding signal
 * to its standard output, where it triggers the saving.
 */

export interface SaveAction extends Action {
    kind: typeof SaveAction.KIND;
}

export namespace SaveAction {
    export const KIND = 'save';

    export function create(): SaveAction {
        console.log('Client Save Handler called !');
        return { kind: KIND };
    }
}
