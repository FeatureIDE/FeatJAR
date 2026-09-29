import * as vscode from 'vscode';
import {
	analyzeCoreDead,
	checkSatisfiable,
	countConfigurations,
	featJarDownload,
	featJarPath,
	openGui,
	printModelStats,
	shutdownExtensionShell,
	startExtensionShell,
	exportUVL,
	exportXML,
	exportDIMACS,
	exportTeX
} from './extension-functions';
import { registerSidebar } from './sidebar';

// AI-assisted (isSatisfiable function ): Added a satisfiability check before computing core/dead features
// to prevent the analysis from running on unsatisfiable models.

const FEATJAR_DOWNLOAD_URL = 'https://github.com/skrieter/FeatJAR-ISF-Teamproject-2026/releases/download/feat.jar/feat.jar';


export async function activate(context: vscode.ExtensionContext): Promise<void> {
	await featJarDownload();
	await startExtensionShell(featJarPath());
	registerSidebar(context);
	const output = vscode.window.createOutputChannel('FeatJAR');
	context.subscriptions.push(output);
	
	const checkSatisfiability = vscode.commands.registerCommand(
		'featjar-extension.checkSatisfiability',
		async (uri: vscode.Uri) => {
			const satisfiable = await checkSatisfiable(uri);
			if (satisfiable === undefined) {
				return;
			}

			output.clear();
			output.appendLine(satisfiable ? 'The model is satisfiable.' : 'The model is not satisfiable.');
			output.show();
		}
	);

	const openFeatJarGui = vscode.commands.registerCommand(
		'featjar-extension.openGui',
		(uri: vscode.Uri) => openGui(uri),
	);

	const uvlEditorProvider = vscode.window.registerCustomEditorProvider(
		'featjar-extension.uvlEditor',
		{
			resolveCustomTextEditor(
				document: vscode.TextDocument,
				webviewPanel: vscode.WebviewPanel
			) {
				openGui(document.uri);
			}
		});
	const modelTest = vscode.commands.registerCommand(
        'featjar-extension.modelTest',
		async (uri: vscode.Uri) => {
			const result = await printModelStats(uri);
			if (result === undefined) {
				return;
			}

			console.log(result);
			output.clear();
			output.appendLine(`the stats : ${result.trim()}`);
			output.show();
		},
    );
	const countConfigurationsCommand = vscode.commands.registerCommand(
    	'featjar-extension.countConfigurations',
		async (uri: vscode.Uri) => {
			const result = await countConfigurations(uri);
			if (result === undefined) {
				return;
			}

			console.log(result);
			output.clear();
			output.appendLine(`Number of configurations: ${result.trim()}`);
			output.show();
		},
    );
	const coreDeadFeatures = vscode.commands.registerCommand(
        'featjar-extension.coreDeadFeatures',
        async (uri: vscode.Uri | undefined) => {
            if (!uri) {
                vscode.window.showWarningMessage('Select a UVL file in the FeatJAR sidebar.');
                return;
            }

            const satisfiable = await checkSatisfiable(uri);
            if (satisfiable === undefined) {
                return;
            }

            if (!satisfiable) {
                output.appendLine('Core/Dead analysis not possible: model is not satisfiable.');
                output.show();
                return;
            }

            const result = await analyzeCoreDead(uri);
            if (result === undefined) {
                return;
            }

            output.clear();
            output.appendLine(`Core Features: ${result.core} | Dead Features: ${result.dead}`);
            output.show();
        },
    );
	const exportUVLCommand = vscode.commands.registerCommand(
		"featjar.exportUVL",async (uri: vscode.Uri) => {
			await exportUVL(uri);
		});
	const exportXMLCommand = vscode.commands.registerCommand(
		"featjar.exportXML",async (uri: vscode.Uri) => {
			await exportXML(uri);
		});
	const exportDIMACSCommand = vscode.commands.registerCommand(
		"featjar.exportDIMACS",async (uri: vscode.Uri) => {
			await exportDIMACS(uri);
		}
		
	);
	const exportTeXCommand = vscode.commands.registerCommand(
		"featjar.exportTeX",async (uri: vscode.Uri) => {
			await exportTeX(uri);
		});

context.subscriptions.push(
	checkSatisfiability,
	openFeatJarGui,
	uvlEditorProvider, 
	modelTest, 
	countConfigurationsCommand, 
	coreDeadFeatures, 
	exportUVLCommand,
	exportXMLCommand,
	exportDIMACSCommand,
	exportTeXCommand
);
}

export function deactivate(): void {
	shutdownExtensionShell();
}
