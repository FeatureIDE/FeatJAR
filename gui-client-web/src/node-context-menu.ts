import { IActionDispatcher, Action } from '@eclipse-glsp/client';
import {
    SetFeatureImplementationTypeAction,
    SetNodeTypeAction,
    SetCardinalityGroupNodeBoundsAction,
    SetCardinalityFeatureBoundsAction
} from './set-type-actions';

interface Entry {
    label: string;
    action: Action | (() => Action | undefined);
}

/**
 * Adds a right-click menu for nodes to change feature and group types.
 *
 * @param actionDispatcher sends the chosen action to the server
 */
export function initializeNodeContextMenu(actionDispatcher: IActionDispatcher): void {
    console.log('context menu installed');
    document.addEventListener('contextmenu', (event: MouseEvent) => {
        console.log('contextmenu fired on', event.target);
        const nodeElement = (event.target as Element).closest('[data-svg-metadata-type="node"]');
        if (!nodeElement) {
            return;
        }
        console.log('node found:', nodeElement);
        event.preventDefault();

        const gModelId = nodeElement.id.replace('sprotty_', '');
        const css = nodeElement.getAttribute('class') ?? '';
        console.log('found node:', nodeElement);
        const entries = buildEntries(gModelId, css);
        if (entries.length === 0) {
            return;
        }

        showMenu(event.clientX, event.clientY, entries, actionDispatcher);
    });
}

function buildEntries(id: string, css: string): Entry[] {
    let entries: Entry[] = [];

    if (css.includes('node-')) {
        entries = [
            { label: 'OR', action: SetNodeTypeAction.create(id, 'node-or') },
            { label: 'XOR', action: SetNodeTypeAction.create(id, 'node-xor') },
            { label: 'AND', action: SetNodeTypeAction.create(id, 'node-and') },
            { label: 'Set Bounds', action: () => promptForBounds(id, true) }
        ];

        return entries;
    }

    if (css.includes('feature-')) {
        entries = [
            { label: 'Make Abstract', action: SetFeatureImplementationTypeAction.create(id, 'abstract') },
            { label: 'Make Concrete', action: SetFeatureImplementationTypeAction.create(id, 'concrete') },
            { label: 'Make Mandatory', action: SetCardinalityFeatureBoundsAction.create(id, 1, 1) },
            { label: 'Make Optional', action: SetCardinalityFeatureBoundsAction.create(id, 0, 1) },
            // { label: 'Make Hidden', action: SetFeatureImplementationTypeAction.create(id, 'hidden') }
            { label: 'Set Bounds', action: () => promptForBounds(id, false) }
        ];
    }

    return entries;
}

/**
 * Asks the user for a lower and upper bound. Returns undefined when the user
 * cancels or enters something invalid. An Upper bound of -1 means unbounded.
 */
function promptForBounds(elementId: string, groupFlag: boolean): Action | undefined {
    const lowerInput = window.prompt('Lower bound:', '0');
    if (lowerInput === null) {
        return undefined;
    }
    const upperInput = window.prompt('Upper bound (-1 = unbounded):', '1');
    if (upperInput === null) {
        return undefined;
    }

    const lower = Number.parseInt(lowerInput, 10);
    const upper = Number.parseInt(upperInput, 10);

    if (Number.isNaN(lower) || Number.isNaN(upper)) {
        return undefined;
    }
    if (lower < 0 || (upper !== -1 && upper < lower)) {
        return undefined;
    }

    if (groupFlag) {
        return SetCardinalityGroupNodeBoundsAction.create(elementId, lower, upper);
    }
    return SetCardinalityFeatureBoundsAction.create(elementId, lower, upper);
}

/**
 * Shows the context menu at the given screen position.
 *
 * Entries are either associated with an action that can be executed immediately or with an input field
 * that first prompts the user to enter data. The action itself is not executed until the user clicks on an entry. Any
 * menu that was previously open is closed first, and clicking elsewhere also closes it.
 *
 * @param x horizontal screen position of the click
 * @param y vertical screen position of the click
 * @param entries the menu items to display
 * @param actionDispatcher sends the chosen action to the server
 */
function showMenu(x: number, y: number, entries: Entry[], actionDispatcher: IActionDispatcher): void {
    document.getElementById('fm-context-menu')?.remove();

    const menu = document.createElement('div');
    menu.id = 'fm-context-menu';
    menu.style.position = 'fixed';
    menu.style.left = `${x}px`;
    menu.style.top = `${y}px`;
    menu.style.background = 'white';
    menu.style.border = '1px solid #888';
    menu.style.zIndex = '99999';
    menu.style.padding = '4px 0';
    menu.style.minWidth = '160px';
    menu.style.boxShadow = '0 2px 8px rgba(0,0,0,0.3)';

    entries.forEach(entry => {
        const item = document.createElement('div');
        item.textContent = entry.label;
        item.style.padding = '4px 16px';
        item.style.cursor = 'pointer';
        item.onmouseenter = () => (item.style.background = '#e8e8e8');
        item.onmouseleave = () => (item.style.background = 'white');
        item.onclick = () => {
            /* If an entry carries a ready action (e.g. OR, XOR node)
             * or, in case of the bounds prompt, a function, the user gets asked for input
             */
            const action = typeof entry.action === 'function' ? entry.action() : entry.action;
            if (action) {
                actionDispatcher.dispatch(action);
            }
            menu.remove();
        };
        menu.appendChild(item);
    });

    document.body.appendChild(menu);

    setTimeout(() => {
        document.addEventListener(
            'mousedown',
            e => {
                if (!menu.contains(e.target as Node)) {
                    menu.remove();
                }
            },
            { once: true }
        );
    });
}
