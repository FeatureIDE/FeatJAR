/**
 * a small subclass of GLSP's built-in NodeCreationTool.
 * it immediately sends the create request the moment the palette button is clicked,
 * then re-enables normal selection mode right away so the canvas behaves normally afterward (no need to press Escape).
 **/

import { NodeCreationTool, CreateNodeOperation, EnableDefaultToolsAction } from '@eclipse-glsp/client';
import { injectable } from 'inversify';

@injectable()
export class ImmediateNodeCreationTool extends NodeCreationTool {
    override doEnable(): void {
        this.actionDispatcher.dispatchAll([
            CreateNodeOperation.create(this.triggerAction.elementTypeId, { args: this.triggerAction.args }),
            EnableDefaultToolsAction.create()
        ]);
    }
}
