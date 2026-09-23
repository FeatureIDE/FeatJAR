// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as vscode from 'vscode';
import { spawn } from 'child_process';
import * as fs from 'node:fs';
import * as path from 'path';
import * as os from 'os';
import { join } from 'node:path';
import { homedir } from 'node:os';
import { registerSidebar } from './sidebar';

function runFeatJar(jarPath: string, args: string[]): Promise<string> {
    return new Promise((resolve, reject) => {
        const child = spawn('java', ['-jar', jarPath, ...args], {
            windowsHide: true,
            stdio: ['ignore', 'pipe', 'pipe']
        });
        let output = '';
        let errors = '';
        child.stderr.setEncoding('utf8');
        child.stderr.on('data', data => { errors += data; });
        child.stdout.setEncoding('utf8');
        child.stdout.on('data', data => {
            output += data;
        });
        child.on('error', reject);
        child.on('close', code => {
            if (code === 0) {
                resolve(output);
            } else {
                reject(new Error(errors.trim() || `FeatJAR exited with code ${code}`));
            }
        });
    });
}
// AI-assisted (isSatisfiable function ): Added a satisfiability check before computing core/dead features
// to prevent the analysis from running on unsatisfiable models.
async function isSatisfiable(
    jarPath: string,
    uri: vscode.Uri
): Promise<boolean> {

    const result = await runFeatJar(
        jarPath,
        [
            'solutions-sat4j',
            '--input',
            uri.fsPath,
            '--limit',
            '1',
            '--format',
            'SimpleCSV'
        ]
    );

    return result.split('\n').some(line => line.startsWith('0;'));
}
const FEATJAR_DOWNLOAD_URL = 'https://github.com/skrieter/FeatJAR-ISF-Teamproject-2026/releases/download/feat.jar/feat.jar';

export async function featJarDownload(): Promise<void> {

	const featJarDirectory = path.join(os.homedir(), '.featjar-bin');
	const featJarPath = path.join(featJarDirectory, 'feat.jar');
	if (!fs.existsSync(featJarPath)) {
		const choice = await vscode.window.showInformationMessage('FeatJAR is not installed. Would you like to download it?', 'Download', 'Cancel');
		if (choice === 'Download') {
			try {
				await fs.promises.mkdir(featJarDirectory, {recursive: true});

				const response = await fetch(FEATJAR_DOWNLOAD_URL);

				if (!response.ok) {
					throw new Error(`Download failed with status ${response.status}`);
				}

				const data = Buffer.from(await response.arrayBuffer());
				const temporaryPath = `${featJarPath}.download`;

				await fs.promises.writeFile(temporaryPath, data);
				await fs.promises.rename(temporaryPath, featJarPath);

				vscode.window.showInformationMessage('FeatJAR was installed successfully.');
			} catch (error) {
				vscode.window.showErrorMessage(
					`Could not download FeatJAR: ${error}`
				);
			}
		}
	}
}

// This method is called when your extension is activated
// Your extension is activated the very first time the command is executed
function openGui(uri: vscode.Uri) {
	const featjarPath = path.join(os.homedir(),'.featjar-bin','feat.jar');
	const process = spawn('java',['-jar', featjarPath, 'gui', '--input', uri.fsPath]);
	process.stdout.on('data', (data) => {
	const output = data.toString();

	if (output.includes('URL:')) {
		const parts = output.split('URL:');
		const url = parts[1].trim();

		vscode.commands.executeCommand(
			'simpleBrowser.show',
			url
		);
	}
	});
}

export async function activate(context: vscode.ExtensionContext) {

	await featJarDownload();
	registerSidebar(context);
	const output = vscode.window.createOutputChannel('FeatJAR');
	context.subscriptions.push(output);
	// Use the console to output diagnostic information (console.log) and errors (console.error)
	// This line of code will only be executed once when your extension is activated
	console.log('Congratulations, your extension "FeatJar Extension" is now active!');
	// The command has been defined in the package.json file
	// Now provide the implementation of the command with registerCommand
	// The commandId parameter must match the command field in package.json
	const disposable = vscode.commands.registerCommand('featjar-extension.helloWorld', () => {
		// The code you place here will be executed every time your command is executed
		// Display a message box to the user
		vscode.window.showInformationMessage('Hello World from World!');
	});
	const disposable2 = vscode.commands.registerCommand('featjar-extension.hellovscode', () => {
		// The code you place here will be executed every time your command is executed
		// Display a message box to the user
		vscode.window.showWarningMessage('This is a warning message from VSCode!');
	});
	const activitybar = vscode.commands.registerCommand('featjarBar', async () => {

    	const files = await vscode.workspace.findFiles('**/*.uvl');

    	console.log(files);
		vscode.window.showInformationMessage(`Found ${files.length} UVL files`);
	});
	const checkSatisfiability = vscode.commands.registerCommand('featjar-extension.checkSatisfiability', async (uri: vscode.Uri) => {
        const jarPath = join(homedir(), '.featjar-bin', 'feat.jar');

        const result = await runFeatJar(jarPath, ['solutions-sat4j', '--input', uri.fsPath, '--limit', '1', '--format', 'SimpleCSV']);
        // The first configuration in SimpleCSV starts with "0;".
		const satisfiable = result.split('\n').some(line => line.startsWith('0;'));
		output.clear();
		output.appendLine(satisfiable? 'The model is satisfiable.': 'The model is not satisfiable.');
		output.show();
    });		
	const countConfigurations = vscode.commands.registerCommand(
    'featjar-extension.countConfigurations',
    async (uri: vscode.Uri) => {
		const jarPath = join(homedir(), '.featjar-bin', 'feat.jar');
        const result = await runFeatJar(jarPath, ['count-sat4j', '--input', uri.fsPath]);
		console.log(result);
		output.clear();
		output.appendLine(`Number of configurations: ${result.trim()}`);
		output.show();
    });
	const coreDeadFeatures = vscode.commands.registerCommand(
        'featjar-extension.coreDeadFeatures',
        async (uri: vscode.Uri | undefined) => {
			// The implementation here is with Ai assisted 
            if (!uri) {
                vscode.window.showWarningMessage('Select a UVL file in the FeatJAR sidebar.');
                return;
            }            
			const jarPath = join(homedir(), '.featjar-bin', 'feat.jar');

			const satisfiable = await isSatisfiable(jarPath, uri);

			if (!satisfiable) {
    			output.appendLine('Core/Dead analysis not possible: model is not satisfiable.');
    			output.show();
    			return;
			}
            const result = await runFeatJar(jarPath, [
                'core-sat4j', '--input', uri.fsPath, '--output-format', 'LiteralList'
            ]);
            // LiteralList separates signed feature names with commas and assignments with newlines.
            const literals = result.split(/\r?\n/)
                .map(line => line.trim())
                .filter(line => line && !/^\[.*?\] \[(INFO|DEBUG|WARN|ERROR)\]/.test(line))
                .flatMap(line => line.split(','))
                .map(value => value.trim());
            if (literals.length === 0 || literals.some(value => !/^[+-].+/.test(value))) {
                throw new Error('FeatJAR returned no valid core/dead literal list. Check whether the model is satisfiable.');
            }
            const core = literals.filter(value => value.startsWith('+')).length;
            const dead = literals.filter(value => value.startsWith('-')).length;
            output.clear();
			output.appendLine(`Core Features: ${core} | Dead Features: ${dead}`);
			output.show();
        }
    );
	const modelTest = vscode.commands.registerCommand(
        'featjar-extension.modelTest',
        async (uri: vscode.Uri) => {
            const jarPath = join(homedir(), '.featjar-bin', 'feat.jar');
       		const result = await runFeatJar(jarPath, ['print-model-stats', '--input', uri.fsPath]);
			console.log(result);
			output.clear();
			//vscode.window.showInformationMessage(`the stats : ${result}`);
			output.appendLine(`the stats : ${result.trim()}`);
			output.show();
        }
    );
	context.subscriptions.push(
    coreDeadFeatures,
    modelTest,
    countConfigurations,
    disposable,
    disposable2,
    checkSatisfiability
	);
	const disposable3 = vscode.commands.registerCommand('featjar-extension.openGui',(uri: vscode.Uri) => {openGui(uri);});
	// AI-assisted: Register a custom editor for UVL files
	// and open the FeatJAR GUI when a UVL file is opened.
	const uvlEditorProvider = vscode.window.registerCustomEditorProvider('featjar-extension.uvlEditor',
	{
		resolveCustomTextEditor(
			document: vscode.TextDocument,
			webviewPanel: vscode.WebviewPanel
		) {
			openGui(document.uri);
		}
	}
	);

	context.subscriptions.push(disposable);
	context.subscriptions.push(disposable2);
	context.subscriptions.push(disposable3);	
	context.subscriptions.push(uvlEditorProvider);
}

// This method is called when your extension is deactivated
export function deactivate() {}
