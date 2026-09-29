// a new file we created to add a new feature below the selected feature in the context menu.

import { IActionDispatcher, Action, SelectAction, CreateNodeOperation } from '@eclipse-glsp/client';

/** Element type id (server-side) of a newly created, optional feature. Keep in sync with CardinalityType.java on the server. */
const OPTIONAL_FEATURE_TYPE_ID = 'feature-optional';

/**
 * Selects the given feature, then creates a new optional feature below it.
 *
 * The server only knows what to attach the new feature to via its tracked "current
 * selection", which is updated by an explicit SelectAction. A right-click does not
 * select the element the way a left-click does, so this selects the clicked feature
 * first to make sure the new feature is attached below the right one.
 */
export function addFeatureBelow(elementId: string, actionDispatcher: IActionDispatcher): Action {
    actionDispatcher.dispatch(SelectAction.setSelection([elementId]));
    return CreateNodeOperation.create(OPTIONAL_FEATURE_TYPE_ID);
}
