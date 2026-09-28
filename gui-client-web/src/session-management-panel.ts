import {
    AbstractUIExtension,
    EditorContextService,
    GModelRoot,
    IActionDispatcher,
    IDiagramStartup,
    ISelectionListener,
    SelectionService,
    TYPES
} from '@eclipse-glsp/client';
import { injectable, inject } from 'inversify';
// TODO maybe merge all actions into one file / folder ?
import { ExitAction } from './client-exit-action';
import { SaveAction } from './client-save-action';
import { SetFeatureColorAction, ToggleShowAttributesAction } from './set-type-actions';
/**
 * Toolbar that contains the save, exit, and set-color actions.
 *
 * Save/Exit are forwarded to the server, which writes the corresponding signal
 * to its standard output, where it triggers the corresponding server action.
 *
 * "Set Color" only makes sense while a single feature node is selected,
 * so the button is disabled otherwise.
 */
@injectable()
export class SessionManagementPanel extends AbstractUIExtension implements IDiagramStartup, ISelectionListener {
    static readonly ID = 'session-management-panel';

    @inject(TYPES.IActionDispatcher)
    protected readonly actionDispatcher: IActionDispatcher;

    @inject(EditorContextService)
    protected readonly editorContext: EditorContextService;

    @inject(SelectionService)
    protected readonly selectionService: SelectionService;

    protected selectedFeatureId: string | undefined;
    protected colorButton: HTMLElement;

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

        this.colorButton = this.createButton('btn-set-color', 'Set Color...', () => this.promptAndDispatch());
        this.setColorButtonEnabled(false);
        containerElement.appendChild(this.colorButton);

        containerElement.appendChild(this.createShowAttributesToggle());

        this.selectionService.addListener(this);
    }
    /**
     * Builds the "Show attributes" checkbox
     * and sends its state to the server whenever it's switched.
     */
    protected createShowAttributesToggle(): HTMLElement {
        const wrapper = document.createElement('label');
        wrapper.className = 'session-management-panel-toggle';

        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.id = 'chk-show-attributes';
        checkbox.onchange = () => {
            console.log('Show attributes toggled:', checkbox.checked);
            this.actionDispatcher.dispatch(ToggleShowAttributesAction.create(checkbox.checked));
        };

        wrapper.appendChild(checkbox);
        wrapper.appendChild(document.createTextNode('Show attributes'));
        return wrapper;
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

    /**
     * Enables the "Set Color..." button only while exactly one feature node
     * (not a group node or constraint) is selected.
     */
    selectionChanged(root: Readonly<GModelRoot>, selectedElements: string[]): void {
        if (selectedElements.length !== 1) {
            this.selectedFeatureId = undefined;
            this.setColorButtonEnabled(false);
            return;
        }

        const elementId = selectedElements[0];
        const element = root.index.getById(elementId) as { cssClasses?: string[] } | undefined;
        const isFeatureNode = element?.cssClasses?.some(css => css.includes('feature-')) ?? false;

        if (!isFeatureNode) {
            this.selectedFeatureId = undefined;
            this.setColorButtonEnabled(false);
            return;
        }

        this.selectedFeatureId = elementId;
        this.setColorButtonEnabled(true);
    }

    protected setColorButtonEnabled(enabled: boolean): void {
        this.colorButton?.classList.toggle('disabled', !enabled);
    }

    protected promptAndDispatch(): void {
        if (!this.selectedFeatureId) {
            return;
        }
        const colorInput = window.prompt('Color (name or hex code):', '#2e7d32');
        if (colorInput === null || colorInput.trim() === '') {
            return;
        }
        this.actionDispatcher.dispatch(SetFeatureColorAction.create(this.selectedFeatureId, colorInput.trim()));
    }
}
