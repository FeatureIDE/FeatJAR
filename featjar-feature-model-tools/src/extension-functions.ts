import * as vscode from 'vscode';
import { ChildProcessWithoutNullStreams } from 'node:child_process';
import * as os from 'node:os';
import * as path from 'node:path';
import { spawn } from 'child_process';
import * as fs from 'node:fs';
import { join } from 'node:path';
import { homedir } from 'node:os';

const READY_REQUEST = 'READY';
const ERROR_PREFIX = 'ERROR:';

const FEATJAR_DOWNLOAD_URL = 'https://github.com/skrieter/FeatJAR-ISF-Teamproject-2026/releases/download/feat.jar/feat.jar';

let extensionShell: ChildProcessWithoutNullStreams | undefined;
let shellOutputBuffer = '';
let resolveShellReady: (() => void) | undefined;
const pendingCommands: Array<(output: string) => void> = [];

export function featJarPath(): string {
	return path.join(os.homedir(), '.featjar-bin', 'feat.jar');
}

export async function checkSatisfiable(uri: vscode.Uri): Promise<boolean | undefined> {
    const result = await executeInExtensionShell(['solutions-sat4j', '--input', uri.fsPath, '--limit', '1', '--format', 'SimpleCSV',]);
    if (isErrorResult(result)) {
        return;
    }

	return result.split('\n').some(line => line.startsWith('0;'));
}
export async function printModelStats(uri: vscode.Uri): Promise<string | undefined> {
    const result = await executeInExtensionShell(['print-model-stats', '--input', uri.fsPath,]);

    if (isErrorResult(result)) {
        return;
    }

    return result;
}
export async function countConfigurations(uri: vscode.Uri): Promise<string | undefined> {
    const result = await executeInExtensionShell(['count-sat4j', '--input', uri.fsPath,]);

    if (isErrorResult(result)) {
        return;
    }

    return result;
}

export async function analyzeCoreDead(uri: vscode.Uri): Promise<{ core: number; dead: number } | undefined> {
    // The implementation here is with AI assistance
    const result = await executeInExtensionShell(['core-sat4j', '--input', uri.fsPath, '--output-format', 'LiteralList',]);

    if (isErrorResult(result)) {
        return;
    }

    // LiteralList separates signed feature names with commas
    // and assignments with newlines.
    const literals = result
		.split(/\r?\n/).map(line => line.trim())
		.filter(line => line && !/^\[.*?\] \[(INFO|DEBUG|WARN|ERROR)\]/.test(line))
        .flatMap(line => line.split(','))
        .map(value => value.trim());

    if (literals.length === 0 || literals.some(value => !/^[+-].+/.test(value))) {
        throw new Error(
            'FeatJAR returned no valid core/dead literal list. '
        );
    }

    const core = literals.filter(value => value.startsWith('+')).length;

    const dead = literals.filter(value => value.startsWith('-')).length;

    return { core, dead };
}

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

export function startExtensionShell(jarPath: string): Promise<void> {
	extensionShell = spawn(
		'java',
		['-cp', jarPath, 'de.featjar.base.shell.ExtensionShell'],
		{ windowsHide: true, stdio: 'pipe' },
	);
	extensionShell.stdout.setEncoding('utf8');
	extensionShell.stdout.on('data', (data: string) => readShellOutput(data));

	return new Promise(resolve => {
		resolveShellReady = resolve;
		
		extensionShell?.on('error', () => {
			resolveShellReady = undefined;
			void vscode.window.showErrorMessage('Could not start FeatJAR. Check that Java is installed and FeatJAR is available.');
			resolve();
		});
		extensionShell?.on('close', () => {
			if (resolveShellReady) {
				resolveShellReady = undefined;
				void vscode.window.showErrorMessage('FeatJAR exited before it was ready.');
				resolve();
			}
		});
	});
}

function readShellOutput(data: string): void {
	
	shellOutputBuffer += data;

	let lineBreakIndex: number;
	while ((lineBreakIndex = shellOutputBuffer.indexOf('\n')) >= 0) {
		const line = shellOutputBuffer.slice(0, lineBreakIndex).replace(/\r$/, '');
		shellOutputBuffer = shellOutputBuffer.slice(lineBreakIndex + 1);

		if (line === READY_REQUEST) {
			if (resolveShellReady !== undefined) {
				const resolve = resolveShellReady;
				resolveShellReady = undefined;
				resolve();
			}
			continue;
		}

		if (line.startsWith('RESULT\t')) {
			const fields = line.split('\t', 2);
			const resolveCommand = pendingCommands.shift();
			resolveCommand?.(Buffer.from(fields[1], 'base64url').toString('utf8'));
			
		}
	}
}

function executeInExtensionShell(args: string[]): Promise<string> {
	return new Promise(resolve => {
		pendingCommands.push(resolve);
		extensionShell?.stdin.write(`RUN\t${args.join('\t')}\n`);
	});
}

function isErrorResult(output: string): boolean {
	if (!output.startsWith(ERROR_PREFIX)) {
		return false;
	}

	void vscode.window.showErrorMessage(output);
	return true;
}

export function openGui(uri: vscode.Uri) {
	const featjarPath = path.join(os.homedir(),'.featjar-bin','feat.jar');
	const process = spawn('java',['-jar', featjarPath, 'gui', '--input', uri.fsPath]);
	process.stdout.on('data', (data) => {
	const output = data.toString();

	if (output.includes('URL:')) {
		const parts = output.split('URL:');
		const url = parts[1].trim();

		vscode.commands.executeCommand('simpleBrowser.show',url);
	}
	});
}

export function exportUVL(uri: vscode.Uri) {
	const fileName = path.basename(
    		uri.fsPath,
    		path.extname(uri.fsPath)
			);

		const outputPath = path.join(
    	path.dirname(uri.fsPath),
    	'export',
    	`${fileName}.uvl`
);
			const result = executeInExtensionShell([
            'convert-model',
            '--input',
            uri.fsPath,
			'--output',
			outputPath,
            '--output-format',
            'UVL'
        ]);
}

export function exportXML(uri: vscode.Uri) {
	const fileName = path.basename(
    		uri.fsPath,
    		path.extname(uri.fsPath)
			);

		const outputPath = path.join(
    	path.dirname(uri.fsPath),
    	'export',
    	`${fileName}.xml`
);
			const result = executeInExtensionShell([
            'convert-model',
            '--input',
            uri.fsPath,
			'--output',
			outputPath,
            '--output-format',
            'XML'
        ]);
}

export function exportDIMACS(uri: vscode.Uri) {
	const fileName = path.basename(
    		uri.fsPath,
    		path.extname(uri.fsPath)
			);

		const outputPath = path.join(
    	path.dirname(uri.fsPath),
    	'export',
    	`${fileName}.dimacs`
);
			const result = executeInExtensionShell([
            'convert-model',
            '--input',
            uri.fsPath,
			'--output',
			outputPath,
            '--output-format',
            'DIMACS'
        ]);
}
export function exportTeX(uri: vscode.Uri) {
	const fileName = path.basename(

    	uri.fsPath,
    	path.extname(uri.fsPath)
		);	

		const outputPath = path.join(
    	path.dirname(uri.fsPath),
    	'export',
    	`${fileName}.tex`
);
			const result = executeInExtensionShell([
            'convert-model',
            '--input',
            uri.fsPath,
			'--output',
			outputPath,
            '--output-format',
            'LaTeX'
        ]);
}

export function shutdownExtensionShell(): void {
	extensionShell?.stdin.write('SHUTDOWN\n');
}
