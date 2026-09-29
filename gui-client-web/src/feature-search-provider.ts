import { ICommandPaletteActionProvider, LabeledAction, GModelRoot, Point, SelectAction, CenterAction } from '@eclipse-glsp/client';
import { injectable } from 'inversify';
import { findMatchingElements } from './feature-search-utils';

/**
 * Provides the entries of the command palette that can be searched.
 *
 * Reads names from the child label as the server attaches them as
 * separate labels rather than as a node property.
 */
@injectable()
export class FeatureSearchProvider implements ICommandPaletteActionProvider {
    async getActions(root: Readonly<GModelRoot>, text: string, lastMousePosition?: Point, index?: number): Promise<LabeledAction[]> {
        return findMatchingElements(root, text).map(match => ({
            label: match.label,
            actions: [SelectAction.setSelection([match.id]), CenterAction.create([match.id])],
            icon: 'symbol-property'
        }));
    }
}
