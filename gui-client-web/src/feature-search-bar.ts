import {
    AbstractUIExtension,
    CenterAction,
    EditorContextService,
    IActionDispatcher,
    IDiagramStartup,
    SelectAction,
    TYPES
} from '@eclipse-glsp/client';
import { injectable, inject } from 'inversify';
import { findMatchingElements, FeatureMatch } from './feature-search-utils';

/**
 * Persistent search bar at the top of the diagram.
 *
 * Typing filters the matches and shows them as a dropdown list, without
 * selecting anything yet. Pressing Enter (or clicking a suggestion)
 * selects and centers a match; pressing Enter again cycles to the next
 * match, Shift+Enter to the previous one.
 */
@injectable()
export class FeatureSearchBar extends AbstractUIExtension implements IDiagramStartup {
    static readonly ID = 'feature-search-bar';

    @inject(TYPES.IActionDispatcher)
    protected readonly actionDispatcher: IActionDispatcher;

    @inject(EditorContextService)
    protected readonly editorContext: EditorContextService;

    protected matches: FeatureMatch[] = [];
    protected currentIndex = -1;
    protected counterLabel: HTMLElement;
    protected suggestionsList: HTMLUListElement;
    id(): string {
        return FeatureSearchBar.ID;
    }

    containerClass(): string {
        return FeatureSearchBar.ID;
    }

    protected initializeContents(containerElement: HTMLElement): void {
        const input = document.createElement('input');
        input.type = 'text';
        input.id = 'txt-feature-search';
        input.placeholder = 'Search features...';

        input.oninput = () => this.updateMatches(input.value);
        input.onkeydown = event => {
            if (event.key !== 'Enter') {
                return;
            }
            event.preventDefault();
            if (this.currentIndex === -1) {
                this.selectMatch(0);
            } else {
                this.selectMatch(this.currentIndex + (event.shiftKey ? -1 : 1));
            }
        };
        input.onfocus = () => {
            if (input.value) {
                this.renderSuggestions(input.value);
            }
        };
        input.onblur = () => {
            // Delayed so a click on a suggestion still registers before the list closes.
            setTimeout(() => this.suggestionsList.classList.remove('visible'), 150);
        };

        this.counterLabel = document.createElement('span');
        this.counterLabel.className = 'feature-search-bar-counter';

        this.suggestionsList = document.createElement('ul');
        this.suggestionsList.className = 'feature-search-bar-suggestions';

        containerElement.appendChild(input);
        containerElement.appendChild(this.counterLabel);
        containerElement.appendChild(this.suggestionsList);
    }

    /*
     * Shows the search bar once the initial model has been loaded.
     */
    postModelInitialization(): void {
        this.show(this.editorContext.modelRoot);
    }

    /**
     * Recomputes the matches for the current search text, without
     * selecting anything yet — selection only happens on Enter or a click.
     */
    protected updateMatches(text: string): void {
        this.matches = findMatchingElements(this.editorContext.modelRoot, text);
        this.currentIndex = -1;
        this.updateCounter();
        this.renderSuggestions(text);
    }

    protected selectMatch(index: number): void {
        this.suggestionsList.classList.remove('visible');

        if (this.matches.length === 0) {
            this.currentIndex = -1;
            this.updateCounter();
            return;
        }

        this.currentIndex = ((index % this.matches.length) + this.matches.length) % this.matches.length;
        const id = this.matches[this.currentIndex].id;

        this.actionDispatcher.dispatchAll([SelectAction.setSelection([id]), CenterAction.create([id])]);

        this.updateCounter();
    }

    protected updateCounter(): void {
        if (this.matches.length === 0) {
            this.counterLabel.textContent = '';
        } else if (this.currentIndex === -1) {
            this.counterLabel.textContent = `${this.matches.length} found`;
        } else {
            this.counterLabel.textContent = `${this.currentIndex + 1} / ${this.matches.length}`;
        }
    }
    /**
     * Rebuilds the dropdown list of matching feature names. Hidden while
     * the search box is empty, so it doesn't dump the whole tree on you.
     */
    protected renderSuggestions(text: string): void {
        this.suggestionsList.replaceChildren();

        if (!text || this.matches.length === 0) {
            this.suggestionsList.classList.remove('visible');
            return;
        }

        this.matches.forEach((match, index) => {
            const item = document.createElement('li');
            item.textContent = match.label;
            item.onmousedown = event => {
                // mousedown (not click) fires before the input's blur handler closes the list.
                event.preventDefault();
                this.selectMatch(index);
            };
            this.suggestionsList.appendChild(item);
        });

        this.suggestionsList.classList.add('visible');
    }
}
