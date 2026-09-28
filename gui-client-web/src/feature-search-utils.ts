import { GModelElement, GModelRoot } from '@eclipse-glsp/client';

export interface FeatureMatch {
    id: string;
    label: string;
}

/**
 * Finds every feature node whose name contains the given search text
 * (case-insensitive). Only actual features match — group nodes
 * (AND/OR/XOR), their cardinality labels, and constraints are excluded.
 * An empty search text matches every feature.
 */
export function findMatchingElements(root: Readonly<GModelRoot>, text: string): FeatureMatch[] {
    const matches: FeatureMatch[] = [];

    for (const element of root.index.all()) {
        const css: string[] = (element as any).cssClasses ?? [];
        if (!css.some(c => c.includes('feature-'))) {
            continue;
        }
        const label = getElementLabel(element);
        if (!label) {
            continue;
        }
        if (text && !label.toLowerCase().includes(text.toLowerCase())) {
            continue;
        }

        matches.push({ id: element.id, label });
    }

    return matches;
}

/**
 * Reads the label of a node. Feature names are attached as a child label and the type starts with "label"
 */
export function getElementLabel(element: GModelElement): string | undefined {
    const children = (element as any).children ?? [];
    const labelChild = children.find((c: any) => typeof c.type === 'string' && c.type.startsWith('label') && c.text);
    return labelChild?.text;
}
