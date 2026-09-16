import { Action } from '@eclipse-glsp/client';

/**
 * Initiates the shut down of the server.
 *
 * The action is forwarded to the server, which writes the corresponding
 * signal to its standard output, where it triggers the saving of the model
 * and afterwards the termination of the server.
 */
export interface ExitAction extends Action {
    kind: typeof ExitAction.KIND;
}

export namespace ExitAction {
    export const KIND = 'exit';

    export function create(): ExitAction {
        console.log('Client Exit Handler called !');
        return { kind: KIND };
    }
}
