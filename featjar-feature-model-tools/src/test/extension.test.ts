import * as assert from 'assert';
import * as path from 'path';
import * as vscode from 'vscode';

import {
    featJarPath,
    startExtensionShell,
    shutdownExtensionShell,
    checkSatisfiable,
    countConfigurations,
    analyzeCoreDead,
    printModelStats
} from '../extension-functions';


const resourcesPath = path.resolve(
    __dirname,
    '../../resources'
);


suite('FeatJAR Extension', () => {

    suiteSetup(async () => {
        await startExtensionShell(featJarPath());
    });

    suiteTeardown(() => {
        shutdownExtensionShell();
    });


    test('SAT model is detected as satisfiable', async () => {

        const uri = vscode.Uri.file(
            path.join(resourcesPath, 'sat-test.uvl')
        );

        const result = await checkSatisfiable(uri);

        assert.strictEqual(result, true);
    });


    test('UNSAT model is detected as unsatisfiable', async () => {

        const uri = vscode.Uri.file(
            path.join(resourcesPath, 'unsat-test.uvl')
        );

        const result = await checkSatisfiable(uri);

        assert.strictEqual(result, false);
    });


    test('configuration count is correct', async () => {

        const uri = vscode.Uri.file(
            path.join(resourcesPath, 'sat-test.uvl')
        );

        const result = await countConfigurations(uri);

        assert.strictEqual(result?.trim(), '2');
    });


    test('core/dead features are detected correctly', async () => {

        const uri = vscode.Uri.file(
            path.join(resourcesPath, 'core-dead-test.uvl')
        );

        const result = await analyzeCoreDead(uri);

        assert.deepStrictEqual(
            result,
            {
                core: 1,
                dead: 1
            }
        );
    });
    test('model statistics can be retrieved', async () => {
    const uri = vscode.Uri.file(
        path.join(resourcesPath, 'sat-test.uvl')
    );

    const result = await printModelStats(uri);

    assert.ok(result);
    assert.ok(result.trim().length > 0);
    });
});