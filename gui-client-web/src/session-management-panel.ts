import { AbstractUIExtension, EditorContextService, IActionDispatcher, IDiagramStartup, TYPES } from '@eclipse-glsp/client';
import { injectable, inject } from 'inversify';
// TODO maybe merge all actions into one file / folder ?
import { ExitAction } from './client-exit-action';
import { SaveAction } from './client-save-action';

/**
 * Toolbar that contains the save and exit actions.
 *
 * The actions are forwarded to the server, which writes the corresponding signal
 * to its standard output, where it triggers the corresponding server action.
 */
@injectable()
export class SessionManagementPanel extends AbstractUIExtension implements IDiagramStartup {
    static readonly ID = 'session-management-panel';

    @inject(TYPES.IActionDispatcher)
    protected readonly actionDispatcher: IActionDispatcher;

    @inject(EditorContextService)
    protected readonly editorContext: EditorContextService;

    id(): string {
        return SessionManagementPanel.ID;
    }

    containerClass(): string {
        return SessionManagementPanel.ID;
    }

    protected initializeContents(containerElement: HTMLElement): void {
        containerElement.appendChild(
            this.createButton('btn-save', 'Save', () => {
                this.actionDispatcher.dispatch(SaveAction.create());
            })
        );

        containerElement.appendChild(
            this.createButton('btn-exit', 'Exit', () => {
                this.actionDispatcher.dispatch(ExitAction.create());
            })
        );
    }

    protected createButton(id: string, label: string, onClick: () => void): HTMLElement {
        const button = document.createElement('div');
        button.id = id;
        button.className = 'session-management-panel-button';
        button.textContent = label;
        button.onclick = onClick;
        return button;
    }

    /*
     * Shows the panel once the initial model has been loaded.
     */
    postModelInitialization(): void {
        this.show(this.editorContext.modelRoot);
    }
}
