import { Action } from '@eclipse-glsp/client';

export interface ClientMessageAction extends Action {
    kind: typeof ClientMessageAction.KIND;
    message: string;
}

export namespace ClientMessageAction {
    export const KIND = 'clientMessage';

    export function create(message: string): ClientMessageAction {
        return { kind: KIND, message };
    }
}
