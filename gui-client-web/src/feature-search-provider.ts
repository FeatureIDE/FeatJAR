import {
    ICommandPaletteActionProvider,
    LabeledAction,
    GModelElement,
    GModelRoot,
    Point,
    SelectAction,
    CenterAction
} from '@eclipse-glsp/client';
import { injectable } from 'inversify';

/**
 * Provides the entries of the command palette that can be searched.
 *
 * Reads names from the child label as the server attaches them as
 * separate labels rather than as a node property.
 */
@injectable()
export class FeatureSearchProvider implements ICommandPaletteActionProvider {
    async getActions(root: Readonly<GModelRoot>, text: string, lastMousePosition?: Point, index?: number): Promise<LabeledAction[]> {
        const actions: LabeledAction[] = [];

        for (const element of root.index.all()) {
            const label = this.getLabel(element);
            const css: string[] = (element as any).cssClasses ?? [];
            if (css.some(c => c.includes('constraint'))) {
                continue;
            }
            if (!label) {
                continue;
            }

            if (text && !label.toLowerCase().includes(text.toLowerCase())) {
                continue;
            }

            actions.push({
                label,
                actions: [SelectAction.create({ selectedElementsIDs: [element.id] }), CenterAction.create([element.id])],
                icon: 'symbol-property'
            });
        }

        return actions;
    }

    /**
     * Reads the label of a node. Feature names are attached as a child label and the type starts with "label"
     */
    protected getLabel(element: GModelElement): string | undefined {
        const children = (element as any).children ?? [];
        const labelChild = children.find((c: any) => typeof c.type === 'string' && c.type.startsWith('label') && c.text);
        return labelChild?.text;
    }
}
